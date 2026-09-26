package dev.midnightcoder.customer;

import dev.midnightcoder.customer.internal.Customer;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link Customer}
 */
public record CustomerDto(
    UUID uuid,
    UUID userId,
    String firstName,
    String lastName,
    String email,
    String contactNumber,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}