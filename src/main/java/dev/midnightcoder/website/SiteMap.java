package dev.midnightcoder.website;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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

    @GetMapping({"/", "/home", "/index"})
    public String index(Model model) {
        model.addAttribute("servicesOffered", List.of());
        return "index";
    }
}
