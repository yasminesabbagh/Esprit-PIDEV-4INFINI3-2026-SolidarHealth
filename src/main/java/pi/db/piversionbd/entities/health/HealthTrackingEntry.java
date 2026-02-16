package pi.db.piversionbd.entities.health;

import jakarta.persistence.*;
import lombok.Data;
import pi.db.piversionbd.entities.groups.Member;

import java.time.LocalDateTime;

@Entity
@Table(name = "HEALTH_TRACKING")
@Data
public class HealthTrackingEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "metric_type")
    private String metricType;

    private Float value;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;
}

