package dev.midnightcoder.identity.profile;

import dev.midnightcoder.common.dto.UserProfileDto;
import dev.midnightcoder.website.registrar.RegistrationRequest;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-24
 */
public interface IUserProfileService {
    UserProfileDto registerUserProfile(RegistrationRequest request);
}
