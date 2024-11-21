package be.telemis.games.bowling.model.playingsession;

import be.telemis.games.bowling.model.frame.FrameCO;
import be.telemis.games.bowling.model.game.AbstractBaseCO;
import be.telemis.games.bowling.model.player.PlayerCO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlayingSessionCO extends AbstractBaseCO {

    private Integer id;
    private PlayingSessionStatus status;
    private PlayerCO player;
    private int score;
    private List<FrameCO> frames = new ArrayList<>();
}
