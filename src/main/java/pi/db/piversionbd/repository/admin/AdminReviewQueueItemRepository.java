package pi.db.piversionbd.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pi.db.piversionbd.entities.admin.AdminReviewQueueItem;
import pi.db.piversionbd.entities.pre.PreRegistration;

import java.util.List;

@Repository
public interface AdminReviewQueueItemRepository extends JpaRepository<AdminReviewQueueItem, Long> {
    List<AdminReviewQueueItem> findByPreRegistration(PreRegistration preRegistration);

}

