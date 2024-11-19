package be.telemis.games.bowling.model.game;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ThrowCO extends AbstractBaseCO {

    private Integer id;
    private ThrowType type;
    private int pinsKnocked;
}
