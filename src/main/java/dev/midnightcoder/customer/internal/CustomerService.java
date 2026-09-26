package dev.midnightcoder.customer.internal;

import dev.midnightcoder.common.dto.UserProfileDto;
import dev.midnightcoder.common.nto.CustomerWelcomeEvent;
import dev.midnightcoder.customer.CustomerDto;
import dev.midnightcoder.customer.ICustomerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-24
 */
@Service
@RequiredArgsConstructor
public class CustomerService implements ICustomerService {
    private final Logger log = LoggerFactory.getLogger(CustomerService.class);
    private final CustomerRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void createCustomer(UserProfileDto userProfileDto) {
        var customer = new Customer();
            customer.setUserId(userProfileDto.uuid());
            customer.setEmail(userProfileDto.email());
            customer.setFirstName(userProfileDto.firstName());
            customer.setLastName(userProfileDto.lastName());
            customer.setContactNumber(userProfileDto.contactNumber());
        repository.save(customer);
        log.info("Customer created: {}", customer);
        eventPublisher.publishEvent(
            new CustomerWelcomeEvent(
                customer.getEmail(),
                customer.getFirstName()
            )
        );
    }



    @Override
    public CustomerDto getCustomerForUserId(UUID userId) {
        return repository.findByUserId(userId).stream()
            .map(this::mapToDto)
            .findFirst()
            .orElse(null);
    }

    @Override
    public CustomerDto getCustomerForEmail(String email) {
        return repository.findByEmail(email).stream()
            .map(this::mapToDto)
            .findFirst()
            .orElse(null);
    }

    @Override
    public CustomerDto syncCustomer(UUID customerId, CustomerDto updated) {
        var cachedUser = repository.findById(customerId)
            .orElse(null);
        if (cachedUser == null)
            return null;
        cachedUser.setFirstName(updated.firstName());
        cachedUser.setLastName(updated.lastName());
        cachedUser.setEmail(updated.email());
        cachedUser.setContactNumber(updated.contactNumber());
        repository.save(cachedUser);
        return mapToDto(cachedUser);
    }

    private CustomerDto mapToDto(Customer customer) {
        return new CustomerDto(
            customer.getUuid(),
            customer.getUserId(),
            customer.getFirstName(),
            customer.getLastName(),
            customer.getEmail(),
            customer.getContactNumber(),
            customer.getCreatedAt(),
            customer.getUpdatedAt()
        );
    }
}
