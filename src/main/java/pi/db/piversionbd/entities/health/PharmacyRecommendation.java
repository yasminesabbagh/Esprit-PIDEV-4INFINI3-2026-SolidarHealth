package pi.db.piversionbd.entities.health;

import jakarta.persistence.*;
import lombok.Data;
import pi.db.piversionbd.entities.groups.Member;

@Entity
@Table(name = "PHARMACY_RECOMMENDATIONS")
@Data
public class PharmacyRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "product_name")
    private String productName;

    private String category;
    private Float price;

    @Column(name = "discount_percentage")
    private Float discountPercentage;

    @Column(name = "eligibility_status")
    private String eligibilityStatus;
}

