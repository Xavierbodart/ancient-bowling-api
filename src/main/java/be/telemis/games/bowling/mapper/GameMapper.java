package be.telemis.games.bowling.mapper;


import be.telemis.games.bowling.model.game.GameCO;
import be.telemis.games.bowling.model.game.GameEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GameMapper {

    GameCO mapToCO(GameEntity gameEntity);

    @InheritInverseConfiguration
    GameEntity mapFromCO(GameCO gameCO);
}