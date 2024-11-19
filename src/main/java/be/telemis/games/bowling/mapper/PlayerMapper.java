package be.telemis.games.bowling.mapper;


import be.telemis.games.bowling.model.game.PlayerCO;
import be.telemis.games.bowling.model.game.PlayerEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

    PlayerCO mapToCO(PlayerEntity playerEntity);

    @InheritInverseConfiguration
    PlayerEntity mapFromCO(PlayerCO playerCO);
}