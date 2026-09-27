package dev.midnightcoder.website.auth;

import dev.midnightcoder.website.jwt.JwtService;
import dev.midnightcoder.website.registrar.internal.Registrar;
import dev.midnightcoder.website.registrar.RegistrationRequest;
import dev.midnightcoder.website.security.CookieHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;
import java.util.stream.Collectors;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-24
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final Registrar registrar;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> registerNewUser(
        HttpServletRequest httpRequest,
        @Valid @ModelAttribute("newUser")
        RegistrationRequest request,
        BindingResult bindingResult,
        HttpServletResponse response
    ) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                .body("Invalid registration request");
        }
        var registrationStatus = registrar.registerUser(request, httpRequest.getRemoteAddr());
        try {
            switch (registrationStatus) {
                case CREATED -> response.sendRedirect("/login");
                case ALREADY_EXISTS -> response.sendRedirect("/register?userExists");
                case INVALID_CREDENTIALS -> response.sendRedirect("/register?passMissMatch");
                case FAILED -> response.sendRedirect("/register?error");
            }
        }
        catch (IOException e) {
            return ResponseEntity.badRequest()
                .body(e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping(
        value = "/login",
        consumes = {
            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    public ResponseEntity<Void> performLogin(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestParam("email") String email,
        @RequestParam("password") String password
    ) {
        return authService.handleLoginAttempt(
            request,
            response,
            email,
            password
        );
    }

}
