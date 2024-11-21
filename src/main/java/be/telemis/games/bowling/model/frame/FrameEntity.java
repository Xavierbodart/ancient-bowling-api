package be.telemis.games.bowling.model.frame;


import be.telemis.games.bowling.model.game.AbstractBaseEntity;
import be.telemis.games.bowling.model.playingsession.PlayingSessionEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "frames")
@Data
@EqualsAndHashCode(callSuper = true)
public class FrameEntity extends AbstractBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private FrameStatus status;

    @Column(name = "frame_number", nullable = false)
    private int frameNumber;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "remaining_pins", nullable = false)
    private int remainingPins;

    @Column(name = "bonus_points", nullable = false)
    private int bonusPoints;

    @Column(name = "remaining_bonus_throws")
    private Integer remainingBonusThrows;

    @ToString.Exclude
    @OneToMany(mappedBy = "frame", cascade = CascadeType.ALL)
    private List<ThrowEntity> frameThrows = new ArrayList<>();

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playing_sessions_id", nullable = false)
    private PlayingSessionEntity playingSession;

    public void addThrow(ThrowEntity frameThrow) {
        this.getFrameThrows().add(frameThrow);
        frameThrow.setFrame(this);
    }

}
