package dev.midnightcoder.common.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserProfileDto(
    UUID uuid,
    String email,
    String firstName,
    String lastName,
    String contactNumber,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}