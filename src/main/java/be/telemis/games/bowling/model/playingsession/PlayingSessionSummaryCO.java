package be.telemis.games.bowling.model.playingsession;

import be.telemis.games.bowling.model.game.AbstractBaseCO;
import be.telemis.games.bowling.model.player.PlayerCO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlayingSessionSummaryCO extends AbstractBaseCO {

    private Integer id;
    private String status;
    private PlayerCO player;
    private int score;
}
