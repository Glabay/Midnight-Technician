package dev.midnightcoder.website.auth;

import dev.midnightcoder.identity.MidnightUserDetailsService;
import dev.midnightcoder.website.jwt.JwtService;
import dev.midnightcoder.website.security.CookieHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-24
 */
@Service
class AuthService {
    private final CookieHelper cookieHelper;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final MidnightUserDetailsService userDetailsService;

    AuthService(
        CookieHelper cookieHelper,
        JwtService jwtService,
        BCryptPasswordEncoder passwordEncoder,
        LoginAttemptService loginAttemptService,
        @Qualifier("midnightUserDetailsService")
        MidnightUserDetailsService userDetailsService
    ) {
        this.cookieHelper = cookieHelper;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.userDetailsService = userDetailsService;
    }

    public ResponseEntity<Void> handleLoginAttempt(
        HttpServletRequest request,
        HttpServletResponse response,
        String email,
        String password
    ) {
        var ip = request.getRemoteAddr();
        if (loginAttemptService.isLocked(email, ip)) {
            return ResponseEntity
                .status(HttpStatus.LOCKED)
                .build(); // Locked
        }
        var user = userDetailsService.loadUserByUsername(email);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            loginAttemptService.onFailure(email, ip);
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }
        loginAttemptService.onSuccess(email, ip);
        var roles = user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
        var accessToken = jwtService.generateAccessToken(
            email,
            roles.isEmpty()
                ? List.of()
                : roles
        );
        var ttl = Duration.ofMinutes(jwtService.getAccessTtlMinutes());
        cookieHelper.setAccessCookie(response, accessToken, ttl);

        if (isFormLike(request)) {
            return ResponseEntity.status(HttpStatus.SEE_OTHER) // See Other
                .header("Location", destinationForRoles(roles))
                .build();
        }
        return ResponseEntity.noContent().build();
    }

    private boolean isFormLike(HttpServletRequest request) {
        var contentType = request.getContentType();
        var accept = request.getHeader("Accept");
        var xrw = request.getHeader("X-Requested-With");
        var formContent = contentType != null && contentType.startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        var htmlAccept = accept != null && accept.contains("text/html");
        var isAjax = "XMLHttpRequest".equalsIgnoreCase(xrw);
        return formContent || (htmlAccept && !isAjax);
    }

    private String destinationForRoles(List<String> roles) {
        if (roles == null) return "/dashboard";
        var isTech = roles.stream().anyMatch(r -> "ROLE_TECHNICIAN".equals(r) || "TECHNICIAN".equals(r));
        return isTech ? "/dashboard/ticketing" : "/dashboard";
    }
}
