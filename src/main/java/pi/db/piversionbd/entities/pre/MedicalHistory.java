package pi.db.piversionbd.entities.pre;

import jakarta.persistence.*;
import lombok.Data;
import pi.db.piversionbd.entities.groups.Member;

@Entity
@Table(name = "MEDICAL_HISTORY")
@Data
public class MedicalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pre_registration_id")
    private PreRegistration preRegistration;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Lob
    @Column(name = "excluded_condition_details")
    private String excludedConditionDetails;
}

