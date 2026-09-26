package dev.midnightcoder.identity.user;

import dev.midnightcoder.identity.IUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-23
 */
@Service
public class UserDetailsService implements IUserDetailsService {
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository repository;

    UserDetailsService(BCryptPasswordEncoder passwordEncoder, UserRepository repository) {
        this.passwordEncoder = passwordEncoder;
        this.repository = repository;
    }

    @Override
    public boolean userExists(String username) {
        return repository.existsByUsername(username);
    }

    @Override
    public Optional<User> getUser(String username) {
        return repository.getByUsername(username);
    }

    @Override
    public User createUser(String email, String password) {
        var user = new User();
            user.setUsername(email);
            user.setEncryptedPassword(passwordEncoder.encode(password));
        return repository.save(user);
    }


}
