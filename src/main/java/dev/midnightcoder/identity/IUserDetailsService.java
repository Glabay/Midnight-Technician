package dev.midnightcoder.identity;

import dev.midnightcoder.identity.user.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-23
 */
public interface IUserDetailsService {

    boolean userExists(String username);

    Optional<User> getUser(String username);

    User createUser(String email, String password);
}
