package pi.db.piversionbd.entities.score;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "REWARDS_CATALOG")
@Data
public class RewardCatalogItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Lob
    private String description;

    @Column(name = "points_required")
    private Integer pointsRequired;
}

