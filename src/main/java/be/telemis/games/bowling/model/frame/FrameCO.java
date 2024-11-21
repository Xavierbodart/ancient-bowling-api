package be.telemis.games.bowling.model.frame;

import be.telemis.games.bowling.model.game.AbstractBaseCO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class FrameCO extends AbstractBaseCO {

    private Integer id;
    private FrameStatus status;
    private int frameNumber;
    private int score;
    private int remainingPins;
    private Integer remainingBonusThrows;
    private List<ThrowCO> frameThrows;
}
