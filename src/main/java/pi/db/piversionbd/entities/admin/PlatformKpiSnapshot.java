package pi.db.piversionbd.entities.admin;

import jakarta.persistence.*;
import lombok.Data;
import pi.db.piversionbd.entities.groups.Group;

import java.time.LocalDateTime;

@Entity
@Table(name = "PLATFORM_KPI_SNAPSHOTS")
@Data
public class PlatformKpiSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "metric_type")
    private String metricType;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "period_start")
    private LocalDateTime periodStart;

    @Column(name = "period_end")
    private LocalDateTime periodEnd;

    @Lob
    private String metrics;
}

