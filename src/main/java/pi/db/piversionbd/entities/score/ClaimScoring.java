package pi.db.piversionbd.entities.score;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "CLAIM_SCORING")
@Data
public class ClaimScoring {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "claim_id", unique = true)
    private Claim claim;

    @Column(name = "reliability_score")
    private Float reliabilityScore;

    @Column(name = "document_score")
    private Float documentScore;

    @Column(name = "medical_score")
    private Float medicalScore;

    @Column(name = "compliance_score")
    private Float complianceScore;

    @Column(name = "total_score")
    private Float totalScore;

    @Lob
    @Column(name = "fraud_indicators")
    private String fraudIndicators;
}

