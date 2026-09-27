package dev.midnightcoder.website.auth;

import dev.midnightcoder.website.registrar.internal.Registrar;
import dev.midnightcoder.website.registrar.RegistrationRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-24
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final Registrar registrar;

    public AuthController(Registrar registrar) {
        this.registrar = registrar;
    }

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

}
