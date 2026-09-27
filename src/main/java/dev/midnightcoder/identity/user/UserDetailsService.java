package dev.midnightcoder.identity.user;

import dev.midnightcoder.identity.IUserDetailsService;
import dev.midnightcoder.identity.IUserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
@RequiredArgsConstructor
public class UserDetailsService implements IUserDetailsService {
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository repository;
    private final IUserRoleService userRoleService;

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
        var cachedUser = repository.save(user);
        userRoleService.addRoleForUser(cachedUser.getUuid(), "USER");

        return cachedUser;
    }


}
