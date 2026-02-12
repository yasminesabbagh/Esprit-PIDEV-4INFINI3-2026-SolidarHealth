package pi.db.piversionbd.entities.admin;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "SYSTEM_ALERTS")
@Data
public class SystemAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_type")
    private String alertType;

    private String severity;
    private String region;
    private String title;

    @Lob
    private String message;

    @Column(name = "is_active")
    private Boolean active;
}

