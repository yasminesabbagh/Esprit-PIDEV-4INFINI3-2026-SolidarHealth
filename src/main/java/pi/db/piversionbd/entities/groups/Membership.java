package pi.db.piversionbd.entities.groups;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "MEMBERSHIPS")
@Data
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "package_type")
    private String packageType;

    @Column(name = "monthly_amount")
    private Float monthlyAmount;

    @Column(name = "consultations_limit")
    private Integer consultationsLimit;

    @Column(name = "annual_limit")
    private Float annualLimit;

    /** pending, active, suspended, cancelled. New memberships start as pending until first payment. */
    @Column(name = "status", length = 20)
    private String status;

    /** When set, membership is ended (soft delete). Used with status=cancelled. */
    @Column(name = "ended_at")
    private Instant endedAt;

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_SUSPENDED = "suspended";
    public static final String STATUS_CANCELLED = "cancelled";
}

