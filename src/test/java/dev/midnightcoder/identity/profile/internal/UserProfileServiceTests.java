package dev.midnightcoder.identity.profile.internal;

import dev.midnightcoder.common.dto.UserProfileDto;
import dev.midnightcoder.identity.IUserDetailsService;
import dev.midnightcoder.identity.profile.RegisterUserProfileRequest;
import dev.midnightcoder.identity.user.User;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-26
 */
class UserProfileServiceTests {

    @Test
    void registersCredentialsAndPersistsProfileReturningSavedValues() {
        var repository = mock(UserProfileRepository.class);
        var userDetailsService = mock(IUserDetailsService.class);
        var service = new UserProfileService(repository, userDetailsService);
        var request = new RegisterUserProfileRequest(
            "Ada", "Lovelace", "555-0100", "ada@example.test", "test-only-password");
        var user = mock(User.class);
        var userId = UUID.randomUUID();
        when(user.getUuid()).thenReturn(userId);
        when(userDetailsService.createUser(request.email(), request.password())).thenReturn(user);
        var savedProfile = new UserProfile();
        savedProfile.setUserId(userId);
        savedProfile.setEmail("saved@example.test");
        savedProfile.setFirstName("Saved first");
        savedProfile.setLastName("Saved last");
        savedProfile.setContactNumber("555-0199");
        savedProfile.prePersist();
        savedProfile.preUpdate();
        when(repository.save(any(UserProfile.class))).thenReturn(savedProfile);

        var result = service.registerUserProfile(request);

        verify(userDetailsService).createUser(request.email(), request.password());
        var captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(repository).save(captor.capture());
        var profile = captor.getValue();
        assertEquals(userId, profile.getUserId());
        assertEquals(request.email(), profile.getEmail());
        assertEquals(request.firstName(), profile.getFirstName());
        assertEquals(request.lastName(), profile.getLastName());
        assertEquals(request.contactNumber(), profile.getContactNumber());
        assertEquals(new UserProfileDto(
            userId, savedProfile.getEmail(), savedProfile.getFirstName(), savedProfile.getLastName(),
            savedProfile.getContactNumber(), savedProfile.getCreatedAt(), savedProfile.getUpdatedAt()), result);
    }

    @Test
    void registrationInputDoesNotExposePasswordInToString() {
        var request = new RegisterUserProfileRequest(
            "Ada", "Lovelace", "555-0100", "ada@example.test", "test-only-password");

        assertFalse(request.toString().contains(request.password()));
        assertEquals("RegisterUserProfileRequest[redacted]", request.toString());
    }
}