package pi.db.piversionbd.entities.groups;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "GROUP_POOLS")
@Data
public class GroupPool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "group_id", unique = true)
    private Group group;

    @Column(name = "pool_balance")
    private Float poolBalance;

    @Column(name = "total_contributions")
    private Float totalContributions;

    @Column(name = "total_paid_out")
    private Float totalPaidOut;
}

