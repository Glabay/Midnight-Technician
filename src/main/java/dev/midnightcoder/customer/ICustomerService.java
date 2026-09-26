package dev.midnightcoder.customer;

import dev.midnightcoder.common.dto.UserProfileDto;

import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-24
 */
public interface ICustomerService {
    void createCustomer(UserProfileDto userProfileDto);
    CustomerDto getCustomerForUserId(UUID userId);
    CustomerDto getCustomerForEmail(String email);

    CustomerDto syncCustomer(UUID customerId, CustomerDto updated);
}
