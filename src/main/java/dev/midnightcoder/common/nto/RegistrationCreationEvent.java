package dev.midnightcoder.common.nto;

import dev.midnightcoder.common.dto.UserProfileDto;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-24
 */
public record RegistrationCreationEvent(
    UserProfileDto profileDto,
    String ipAddress
) {}
