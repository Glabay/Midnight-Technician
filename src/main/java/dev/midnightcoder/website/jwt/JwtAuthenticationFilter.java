package dev.midnightcoder.website.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-24
 */
@Component
@NullMarked
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final String ACCESS_COOKIE = "mt_access";
    private final JwtService jwtService;

    JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/auth/");
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        var token = extractCookie(request);
        if (token.isBlank() ||
            SecurityContextHolder.getContext().getAuthentication() == null
        ) {
            log.debug("No token or no authentication");
            filterChain.doFilter(request, response);
            return;
        }
        if (jwtService.isTokenValid(token)) {
            var claims = jwtService.parseClaims(token);
            var principal = claims.getSubject();
            var roles = jwtService.extractRoles(claims);
            var authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
            var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private String extractCookie(HttpServletRequest request) {
        var cookies = request.getCookies();
        if (cookies == null) return "";
        return Arrays.stream(cookies)
            .filter(c -> ACCESS_COOKIE.equals(c.getName()))
            .findFirst()
            .map(Cookie::getValue)
            .orElse("");
    }
}
