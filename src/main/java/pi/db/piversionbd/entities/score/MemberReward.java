package pi.db.piversionbd.entities.score;

import jakarta.persistence.*;
import lombok.Data;
import pi.db.piversionbd.entities.groups.Member;

import java.time.LocalDateTime;

@Entity
@Table(name = "MEMBER_REWARDS")
@Data
public class MemberReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "reward_id")
    private RewardCatalogItem reward;

    @Column(name = "points_spent")
    private Integer pointsSpent;

    private String status;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;
}

