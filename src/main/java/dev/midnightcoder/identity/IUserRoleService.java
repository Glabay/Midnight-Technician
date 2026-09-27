package dev.midnightcoder.identity;

import dev.midnightcoder.identity.roles.Role;

import java.util.List;
import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-23
 */
public interface IUserRoleService {
    List<Role> getRolesForUserId(UUID uuid);

    void addRoleForUser(UUID uuid, String user);
}
