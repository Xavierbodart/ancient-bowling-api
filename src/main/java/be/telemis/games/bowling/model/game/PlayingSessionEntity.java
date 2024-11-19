package be.telemis.games.bowling.model.game;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "playing_sessions")
@Getter
@Setter
public class PlayingSessionEntity extends AbstractBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private PlayingSessionStatus status;

    @Column(name = "score", nullable = false, length = 50)
    private Integer score;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerEntity player;

    @ToString.Exclude
    @OneToMany(mappedBy = "playingSession", cascade = CascadeType.ALL)
    private List<FrameEntity> frames = new ArrayList<>();

    public void addFrames(List<FrameEntity> frames) {
        if (frames != null) {
            frames.forEach(this::addFrame);
        }
    }

    public void addFrame(FrameEntity frame) {
        this.getFrames().add(frame);
        frame.setPlayingSession(this);
    }
}
