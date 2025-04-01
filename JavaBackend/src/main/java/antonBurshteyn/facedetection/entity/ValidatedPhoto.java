package antonBurshteyn.facedetection.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "faceParameters")
@ToString(exclude = "faceParameters")
public class ValidatedPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] data;

    @Column(nullable = false)
    private LocalDateTime validatedAt;

    @OneToOne(mappedBy = "validatedPhoto", cascade = CascadeType.ALL, orphanRemoval = true)
    private FaceParameters faceParameters;

    public ValidatedPhoto(String name) {
        this.name = name;
    }
}
