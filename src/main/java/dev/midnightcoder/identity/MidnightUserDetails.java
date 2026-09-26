package dev.midnightcoder.identity;

import dev.midnightcoder.identity.roles.Role;
import dev.midnightcoder.identity.user.User;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-23
 */
@NullMarked
public class MidnightUserDetails implements UserDetails {
    private final Logger log;
    private final User user;
    private final List<Role> roles;

    MidnightUserDetails(User user, List<Role> roles) {
        this.log = LoggerFactory.getLogger(MidnightUserDetails.class);
        this.user = user;
        this.roles = roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        log.debug("Retrieving authorities for user: {}", user.getUsername());
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
            .toList();
    }

    @Override
    public @Nullable String getPassword() {
        return user.getEncryptedPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }
}
