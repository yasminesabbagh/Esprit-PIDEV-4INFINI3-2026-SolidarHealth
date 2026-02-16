package pi.db.piversionbd.entities.admin;

import jakarta.persistence.*;
import lombok.Data;
import pi.db.piversionbd.entities.groups.Member;

@Entity
@Table(name = "RETENTION_INTERVENTIONS")
@Data
public class RetentionIntervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "prediction_id")
    private MemberChurnForecast prediction;

    @Column(name = "action_type")
    private String actionType;

    @Column(name = "discount_percentage")
    private Float discountPercentage;

    private Boolean executed;

    private String result;
}

