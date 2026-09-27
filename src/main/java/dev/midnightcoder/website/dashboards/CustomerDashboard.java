package dev.midnightcoder.website.dashboards;

import dev.midnightcoder.customer.ICustomerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-27
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class CustomerDashboard {
    private final Logger log = LoggerFactory.getLogger(CustomerDashboard.class);
    private final ICustomerService customerService;

    @GetMapping
    public String getCustomerDashboard(
        HttpServletRequest request,
        Model model
    ) {
        var email = request.getRemoteUser();
        log.info("Customer dashboard requested for email: {}", email);
        var customerDto = customerService.getCustomerForEmail(email);
        log.info("Customer dashboard requested for customer: {}", customerDto);

        model.addAttribute("deviceTypes", List.of());
        model.addAttribute("customerEmail", email);
        model.addAttribute("customer", customerDto);
        model.addAttribute("services", List.of());
        model.addAttribute("openTickets", List.of());
        model.addAttribute("devices", List.of());
        model.addAttribute("devicesInRepair", List.of());
        return "dashboards/customer/dashboard";
    }
}
