package be.telemis.games.bowling.model.game;

import jakarta.persistence.Column;
import lombok.Data;

import java.util.Date;

@Data
public class AbstractBaseEntity {

    @Column(name = "creation_date")
    private Date creationDate;

    @Column(name = "modification_date")
    private Date modificationDate;
}
