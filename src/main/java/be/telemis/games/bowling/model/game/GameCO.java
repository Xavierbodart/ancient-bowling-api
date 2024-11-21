package be.telemis.games.bowling.model.game;

import be.telemis.games.bowling.model.playingsession.PlayingSessionSummaryCO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class GameCO extends AbstractBaseCO {

    private Integer id;
    private String name;
    private GameStatus status;
    private PlayingSessionSummaryCO activePlayingSession;
    private PlayingSessionSummaryCO winningPlayingSession;
}
