package be.telemis.games.bowling.model.game;


import be.telemis.games.bowling.model.playingsession.PlayingSessionEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "games")
@Data
@EqualsAndHashCode(callSuper = true)
public class GameEntity extends AbstractBaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private GameStatus status;

    @ToString.Exclude
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "winning_playing_session_id")
    private PlayingSessionEntity winningPlayingSession;

    @ToString.Exclude
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "active_playing_session_id")
    private PlayingSessionEntity activePlayingSession;

}
