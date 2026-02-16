package pi.db.piversionbd.entities.admin;

import jakarta.persistence.*;
import lombok.Data;
import pi.db.piversionbd.entities.groups.Member;

import java.util.List;

@Entity
@Table(name = "MEMBER_CHURN_FORECASTS")
@Data
public class MemberChurnForecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "churn_probability")
    private Float churnProbability;

    @Column(name = "risk_level")
    private String riskLevel;

    @Lob
    @Column(name = "risk_factors")
    private String riskFactors;

    @Lob
    private String recommendation;

    @OneToMany(mappedBy = "prediction")
    private List<RetentionIntervention> interventions;
}

