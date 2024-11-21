package be.telemis.games.bowling.model.frame;


import be.telemis.games.bowling.model.game.AbstractBaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "throws")
@Data
@EqualsAndHashCode(callSuper = true)
public class ThrowEntity extends AbstractBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "frame_id", nullable = false)
    private FrameEntity frame;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50)
    private ThrowType type;

    @Column(name = "pins_knocked")
    private int pinsKnocked;

}
