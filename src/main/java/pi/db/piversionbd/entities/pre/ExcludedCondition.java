package pi.db.piversionbd.entities.pre;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "EXCLUDED_CONDITIONS")
@Data
public class ExcludedCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "condition_name", nullable = false)
    private String conditionName;

    @Column(name = "severity_level")
    private Integer severityLevel;

    @Column(name = "auto_reject")
    private Boolean autoReject;
}

