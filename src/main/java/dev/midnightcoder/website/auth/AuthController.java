package dev.midnightcoder.website.auth;

import dev.midnightcoder.website.registrar.internal.Registrar;
import dev.midnightcoder.website.registrar.RegistrationRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                .body("Invalid registration request");
        }
        var registrationStatus = registrar.registerUser(request, httpRequest.getRemoteAddr());

        return ResponseEntity.ok()
            .body(registrationStatus.toString());
    }

}
