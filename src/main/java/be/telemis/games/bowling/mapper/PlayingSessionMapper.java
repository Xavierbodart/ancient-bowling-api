package be.telemis.games.bowling.mapper;


import be.telemis.games.bowling.model.game.PlayingSessionCO;
import be.telemis.games.bowling.model.game.PlayingSessionEntity;
import be.telemis.games.bowling.model.game.PlayingSessionSummaryCO;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlayingSessionMapper {

    PlayingSessionCO mapToCO(PlayingSessionEntity playingSessionEntity);

    @InheritInverseConfiguration
    PlayingSessionEntity mapFromCO(PlayingSessionCO playingSessionCO);

    PlayingSessionSummaryCO mapToSummaryCO(PlayingSessionEntity playingSessionEntity);

}