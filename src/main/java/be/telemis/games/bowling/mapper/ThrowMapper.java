package be.telemis.games.bowling.mapper;


import be.telemis.games.bowling.model.game.ThrowCO;
import be.telemis.games.bowling.model.game.ThrowEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ThrowMapper {

    ThrowCO mapToCO(ThrowEntity throwEntity);

    @InheritInverseConfiguration
    ThrowEntity mapFromCO(ThrowCO throwCO);
}