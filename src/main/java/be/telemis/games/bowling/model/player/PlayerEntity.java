package be.telemis.games.bowling.model.player;


import be.telemis.games.bowling.model.game.AbstractBaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Entity
@Table(name = "players")
@Data
@EqualsAndHashCode(callSuper = true)
public class PlayerEntity extends AbstractBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "birthday")
    private Date birthday;

}
