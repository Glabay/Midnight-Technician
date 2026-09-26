package dev.midnightcoder.customer.events;

import dev.midnightcoder.common.nto.RegistrationCreationEvent;
import dev.midnightcoder.customer.ICustomerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-24
 */
@Component
@RequiredArgsConstructor
public class RegistrationListener {
    private final Logger log = LoggerFactory.getLogger(RegistrationListener.class);

    private final ICustomerService customerService;

    @ApplicationModuleListener
    public void on(RegistrationCreationEvent event) {
        log.info("Registration created: {}", event);
        customerService.createCustomer(event.profileDto());
    }

}
