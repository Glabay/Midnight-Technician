package dev.midnightcoder.website.registrar.internal;

import dev.midnightcoder.common.dto.UserProfileDto;
import dev.midnightcoder.common.nto.RegistrationCreationEvent;
import dev.midnightcoder.identity.IUserDetailsService;
import dev.midnightcoder.identity.profile.IUserProfileService;
import dev.midnightcoder.identity.profile.RegisterUserProfileRequest;
import dev.midnightcoder.website.registrar.RegistrationRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-26
 */
class RegistrarTests {
    private final IUserDetailsService userService = mock(IUserDetailsService.class);
    private final IUserProfileService profileService = mock(IUserProfileService.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final Registrar registrar = new Registrar(userService, profileService, eventPublisher);

    @Test
    void mapsRegistrationInputAndPublishesCreationEvent() {
        var request = registrationRequest("test-only-password");
        var profile = new UserProfileDto(
            UUID.randomUUID(), request.email(), request.firstName(), request.lastName(),
            request.contactNumber(), LocalDateTime.now(), null);
        var ipAddress = "192.0.2.10";
        when(profileService.registerUserProfile(any(RegisterUserProfileRequest.class))).thenReturn(profile);

        var result = registrar.registerUser(request, ipAddress);

        assertEquals(RegistrationStatus.CREATED, result);
        verify(userService).userExists(request.email());
        var inputCaptor = ArgumentCaptor.forClass(RegisterUserProfileRequest.class);
        verify(profileService).registerUserProfile(inputCaptor.capture());
        var input = inputCaptor.getValue();
        assertEquals(request.firstName(), input.firstName());
        assertEquals(request.lastName(), input.lastName());
        assertEquals(request.contactNumber(), input.contactNumber());
        assertEquals(request.email(), input.email());
        assertEquals(request.password(), input.password());
        var eventCaptor = ArgumentCaptor.forClass(RegistrationCreationEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertSame(profile, eventCaptor.getValue().profileDto());
        assertEquals(ipAddress, eventCaptor.getValue().ipAddress());
    }

    @Test
    void duplicateUserSkipsProfileCreationAndEventPublication() {
        var request = registrationRequest("test-only-password");
        when(userService.userExists(request.email())).thenReturn(true);

        var result = registrar.registerUser(request, "192.0.2.10");

        assertEquals(RegistrationStatus.ALREADY_EXISTS, result);
        verifyNoInteractions(profileService, eventPublisher);
    }

    @Test
    void mismatchedPasswordsSkipProfileCreationAndEventPublication() {
        var request = registrationRequest("different-test-password");

        var result = registrar.registerUser(request, "192.0.2.10");

        assertEquals(RegistrationStatus.FAILED, result);
        verifyNoInteractions(profileService, eventPublisher);
    }

    @Test
    void missingProfileSkipsEventPublication() {
        var request = registrationRequest("test-only-password");

        var result = registrar.registerUser(request, "192.0.2.10");

        assertEquals(RegistrationStatus.FAILED, result);
        verify(profileService).registerUserProfile(any(RegisterUserProfileRequest.class));
        verifyNoInteractions(eventPublisher);
    }

    private RegistrationRequest registrationRequest(String confirmation) {
        return new RegistrationRequest(
            "Ada", "Lovelace", "555-0100", "ada@example.test", "test-only-password", confirmation);
    }
}