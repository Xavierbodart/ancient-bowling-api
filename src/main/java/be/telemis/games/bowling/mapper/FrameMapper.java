package be.telemis.games.bowling.mapper;


import be.telemis.games.bowling.model.frame.FrameCO;
import be.telemis.games.bowling.model.frame.FrameEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FrameMapper {

    FrameCO mapToCO(FrameEntity frameEntity);

    @InheritInverseConfiguration
    FrameEntity mapFromCO(FrameCO frameCO);
}