package dev.midnightcoder.identity.profile;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-26
 */
public record RegisterUserProfileRequest(
    String firstName,
    String lastName,
    String contactNumber,
    String email,
    String password
) {
    @Override
    public String toString() {
        return "RegisterUserProfileRequest[redacted]";
    }
}