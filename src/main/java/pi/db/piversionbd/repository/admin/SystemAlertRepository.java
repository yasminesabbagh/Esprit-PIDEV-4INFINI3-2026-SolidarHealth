package pi.db.piversionbd.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pi.db.piversionbd.entities.admin.SystemAlert;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemAlertRepository extends JpaRepository<SystemAlert, Long> {

    List<SystemAlert> findByActiveOrderByIdDesc(Boolean active);

    /** For dedupe: at most one active LOW_POOL alert per group. */
    Optional<SystemAlert> findByAlertTypeAndSourceEntityTypeAndSourceEntityIdAndActive(
            String alertType, String sourceEntityType, Long sourceEntityId, Boolean active);
}
