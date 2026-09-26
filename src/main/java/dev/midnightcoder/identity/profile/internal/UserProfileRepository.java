package dev.midnightcoder.identity.profile.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-23
 */
@Repository
interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    boolean existsByEmailIgnoreCase(String email);
    Optional<UserProfile> findByEmailIgnoreCase(String email);
}
