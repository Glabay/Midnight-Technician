package dev.midnightcoder.identity.profile.internal;

import dev.midnightcoder.identity.IUserDetailsService;
import dev.midnightcoder.identity.profile.IUserProfileService;
import dev.midnightcoder.common.dto.UserProfileDto;
import dev.midnightcoder.identity.profile.RegisterUserProfileRequest;
import org.springframework.stereotype.Service;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-24
 */
@Service
public class UserProfileService implements IUserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final IUserDetailsService userDetailsService;

    UserProfileService(
        UserProfileRepository userProfileRepository,
        IUserDetailsService userDetailsService
    ) {
        this.userProfileRepository = userProfileRepository;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public UserProfileDto registerUserProfile(RegisterUserProfileRequest request) {
        var user = userDetailsService.createUser(request.email(), request.password());
        var profile = new UserProfile();
            profile.setUserId(user.getUuid());
            profile.setEmail(request.email());
            profile.setFirstName(request.firstName());
            profile.setLastName(request.lastName());
            profile.setContactNumber(request.contactNumber());

        var cachedProfile = userProfileRepository.save(profile);
        return mapToDto(cachedProfile);
    }

    private UserProfileDto mapToDto(UserProfile profile) {
        return new UserProfileDto(
            profile.getUserId(),
            profile.getEmail(),
            profile.getFirstName(),
            profile.getLastName(),
            profile.getContactNumber(),
            profile.getCreatedAt(),
            profile.getUpdatedAt()
        );
    }
}
