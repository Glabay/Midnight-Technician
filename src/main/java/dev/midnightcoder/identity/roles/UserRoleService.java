package dev.midnightcoder.identity.roles;

import dev.midnightcoder.identity.IUserRoleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-23
 */
@Service
public class UserRoleService implements IUserRoleService {
    private final RoleRepository repository;

    public UserRoleService(RoleRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addRoleForUser(UUID uuid, String roleName) {
        var role = new Role();
            role.setOwnerId(uuid);
            role.setRoleName(roleName);
        repository.save(role);
    }

    @Override
    public List<Role> getRolesForUserId(UUID uuid) {
        return repository.findByOwnerId(uuid);
    }
}
