package dev.midnightcoder.website;

import dev.midnightcoder.website.jwt.JwtService;
import dev.midnightcoder.website.registrar.RegistrationRequest;
import dev.midnightcoder.website.security.CookieHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-24
 */
@Controller
@RequiredArgsConstructor
public class SiteMap {
    private final CookieHelper cookieHelper;
    private final JwtService jwtService;

    @GetMapping({"/", "/home", "/index"})
    public String index(Model model) {
        model.addAttribute("servicesOffered", List.of());
        return "index";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("newUser", new RegistrationRequest(
            "", "", "", "", "", ""
        ));
        return "register";
    }

    @GetMapping("/login")
    public String login(
        @RequestParam(
            value = "error",
            required = false
        )
        String error,
        Model model,
        HttpServletRequest request
    ) {
        var atCookie = cookieHelper.getCookie(request, CookieHelper.ACCESS_COOKIE);
        if (atCookie.isPresent() && jwtService.isTokenValid(atCookie.get())) {
            var claims = jwtService.parseClaims(atCookie.get());
            var roles = jwtService.extractRoles(claims);
            String dest = destinationForRoles(roles);
            return "redirect:" + dest;
        }
        if (error != null)
            model.addAttribute("error", "Invalid email or password");
        return "login";
    }

    private String destinationForRoles(List<String> roles) {
        if (roles == null) return "/dashboard";
        var isTech = roles.stream().anyMatch(r -> "ROLE_TECHNICIAN".equals(r) || "TECHNICIAN".equals(r));
        return isTech ? "/dashboard/ticketing" : "/dashboard";
    }
}
