package dev.midnightcoder.identity;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-23
 */
@Service
@NullMarked
public class MidnightUserDetailsService implements UserDetailsService {
    private final Logger log;
    private final IUserDetailsService userDetailsService;
    private final IUserRoleService roleService;

    public MidnightUserDetailsService(IUserDetailsService userDetailsService, IUserRoleService roleService) {
        this.log = LoggerFactory.getLogger(MidnightUserDetailsService.class);
        this.roleService = roleService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user details for username: {}", username);
        if (!userDetailsService.userExists(username)) {
            log.debug("User with username {} does not exist", username);
            throw new UsernameNotFoundException("User with username " + username + " does not exist");
        }
        var cachedUser = userDetailsService.getUser(username);
        if (cachedUser.isEmpty()) {
            log.debug("User with username {} does not exist in cache", username);
            throw new UsernameNotFoundException("User with username " + username + " does not exist in cache");
        }
        var user = cachedUser.get();
        var roles = roleService.getRolesForUserId(user.getUuid());
        return new MidnightUserDetails(user, roles);
    }
}
