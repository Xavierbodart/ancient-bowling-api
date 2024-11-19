package be.telemis.games.bowling.model.game;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlayingSessionSummaryCO extends AbstractBaseCO {

    private Integer id;
    private String status;
    private PlayerCO player;
}
