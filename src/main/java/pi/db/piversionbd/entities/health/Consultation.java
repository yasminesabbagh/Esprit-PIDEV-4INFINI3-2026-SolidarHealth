package pi.db.piversionbd.entities.health;

import jakarta.persistence.*;
import lombok.Data;
import pi.db.piversionbd.entities.groups.Member;

import java.time.LocalDateTime;

@Entity
@Table(name = "CONSULTATIONS")
@Data
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Lob
    private String diagnosis;

    @Lob
    private String prescription;

    @Column(name = "is_telemedicine")
    private Boolean telemedicine;
}

