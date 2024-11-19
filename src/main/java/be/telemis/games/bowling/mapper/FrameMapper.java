package be.telemis.games.bowling.mapper;


import be.telemis.games.bowling.model.game.FrameCO;
import be.telemis.games.bowling.model.game.FrameEntity;
import be.telemis.games.bowling.model.game.PlayingSessionCO;
import be.telemis.games.bowling.model.game.PlayingSessionEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FrameMapper {

    FrameCO mapToCO(FrameEntity frameEntity);

    @InheritInverseConfiguration
    FrameEntity mapFromCO(FrameCO frameCO);
}