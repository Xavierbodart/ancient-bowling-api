package be.telemis.games.bowling.model.game;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlayerCO extends AbstractBaseCO {

    private Integer id;
    private String name;
    private Date birthday;
}
