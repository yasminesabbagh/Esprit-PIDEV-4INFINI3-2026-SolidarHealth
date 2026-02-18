package pi.db.piversionbd.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pi.db.piversionbd.entities.groups.Member;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);

    long countByEnabledFalse();
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByLockedAtNotNull();
}

