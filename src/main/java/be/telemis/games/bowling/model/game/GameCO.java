package be.telemis.games.bowling.model.game;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GameCO extends AbstractBaseCO {

    private Integer id;
    private String name;
    private GameStatus status;
    //TODO: pas en input
    private PlayingSessionSummaryCO activePlayingSession;
    private List<PlayingSessionSummaryCO> winningPlayingSession;
}
