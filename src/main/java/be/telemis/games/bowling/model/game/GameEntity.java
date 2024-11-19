package be.telemis.games.bowling.model.game;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "games")
@Data
@EqualsAndHashCode(callSuper = true)
public class GameEntity extends AbstractBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private GameStatus status;
//
//    @OneToMany(cascade = CascadeType.ALL)
//    @JoinTable(name = "game_winning_sessions",
//            joinColumns = @JoinColumn(name = "game_id"),
//            inverseJoinColumns = @JoinColumn(name = "session_id"))
//    private List<PlayingSessionEntity> winningPlayingSessions = new ArrayList<>();

    @ToString.Exclude
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "active_playing_session_id")
    private PlayingSessionEntity activePlayingSession;
}
