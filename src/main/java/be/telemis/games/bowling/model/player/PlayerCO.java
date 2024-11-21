package be.telemis.games.bowling.model.player;

import be.telemis.games.bowling.model.game.AbstractBaseCO;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlayerCO extends AbstractBaseCO {

    private Integer id;
    private String name;
    @PastOrPresent(message = "Birthday cannot be in the future")
    private Date birthday;
}
