package dev.midnightcoder.website.registrar.internal;

import dev.midnightcoder.common.nto.RegistrationCreationEvent;
import dev.midnightcoder.identity.IUserDetailsService;
import dev.midnightcoder.identity.profile.IUserProfileService;
import dev.midnightcoder.identity.profile.RegisterUserProfileRequest;
import dev.midnightcoder.website.registrar.RegistrationRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
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
public class Registrar {
    private final Logger log = LoggerFactory.getLogger(Registrar.class);
    private final IUserDetailsService userService;
    private final IUserProfileService profileService;
    private final ApplicationEventPublisher eventPublisher;

    public RegistrationStatus registerUser(RegistrationRequest request, String ipAddress) {
        if (userService.userExists(request.email()))
            return RegistrationStatus.ALREADY_EXISTS;

        if (!request.password().isBlank() &&
            !request.password().equals(request.rePassword())
        ) return RegistrationStatus.FAILED;

        log.info("Registering user profile for email: {}", request.email());
        var profile = profileService.registerUserProfile(new RegisterUserProfileRequest(
            request.firstName(),
            request.lastName(),
            request.contactNumber(),
            request.email(),
            request.password()
        ));
        if (profile == null) {
            log.error("Failed to register user profile for email: {}", request.email());
            return RegistrationStatus.FAILED;
        }
        log.info("User profile registered: {}", profile);
        eventPublisher.publishEvent(new RegistrationCreationEvent(profile, ipAddress));
        return RegistrationStatus.CREATED;
    }
}
