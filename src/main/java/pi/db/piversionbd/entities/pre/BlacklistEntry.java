package pi.db.piversionbd.entities.pre;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "BLACKLIST")
@Data
public class BlacklistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cin_number", nullable = false)
    private String cinNumber;

    private String reason;

    @Column(name = "is_fraud")
    private Boolean isFraud;

    @Lob
    @Column(name = "fraud_details")
    private String fraudDetails;

    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;
}

