package pi.db.piversionbd.entities.pre;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "RISK_ASSESSMENTS")
@Data
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pre_registration_id")
    private PreRegistration preRegistration;

    @Column(name = "risk_coefficient")
    private Float riskCoefficient;

    @Column(name = "calculated_price")
    private Float calculatedPrice;

    @Lob
    private String exclusions;
}

