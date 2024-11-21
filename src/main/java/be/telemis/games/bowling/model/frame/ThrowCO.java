package be.telemis.games.bowling.model.frame;

import be.telemis.games.bowling.model.game.AbstractBaseCO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class ThrowCO extends AbstractBaseCO {

    private Integer id;
    private ThrowType type;
    private int pinsKnocked;
}
