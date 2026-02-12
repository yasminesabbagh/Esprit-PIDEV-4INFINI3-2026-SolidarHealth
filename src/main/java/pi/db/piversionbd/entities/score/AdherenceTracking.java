package pi.db.piversionbd.entities.score;

import jakarta.persistence.*;
import lombok.Data;
import pi.db.piversionbd.entities.groups.Member;

@Entity
@Table(name = "ADHERENCE_TRACKING")
@Data
public class AdherenceTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "score_change")
    private Float scoreChange;

    @ManyToOne
    @JoinColumn(name = "related_claim_id")
    private Claim relatedClaim;

    @Column(name = "current_score")
    private Float currentScore;
}

