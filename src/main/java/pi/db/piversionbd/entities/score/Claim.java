package pi.db.piversionbd.entities.score;

import jakarta.persistence.*;
import lombok.Data;
import pi.db.piversionbd.entities.admin.AdminReviewQueueItem;
import pi.db.piversionbd.entities.groups.Group;
import pi.db.piversionbd.entities.groups.Member;
import pi.db.piversionbd.entities.pre.DocumentUpload;

import java.util.List;

@Entity
@Table(name = "CLAIMS")
@Data
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "claim_number", nullable = false)
    private String claimNumber;

    @Column(name = "amount_requested")
    private Float amountRequested;

    @Column(name = "amount_approved")
    private Float amountApproved;

    @Column(name = "scoring_result")
    private Integer scoringResult;

    private String status;

    @OneToOne(mappedBy = "claim")
    private ClaimScoring claimScoring;

    @OneToMany(mappedBy = "claim")
    private List<DocumentUpload> documentUploads;

    @OneToMany(mappedBy = "relatedClaim")
    private List<AdherenceTracking> adherenceTrackingEvents;

    @OneToMany(mappedBy = "claim")
    private List<AdminReviewQueueItem> adminReviewQueueItems;
}

