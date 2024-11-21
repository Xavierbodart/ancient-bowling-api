package be.telemis.games.bowling.factory;

import be.telemis.games.bowling.model.frame.FrameEntity;
import be.telemis.games.bowling.model.frame.FrameStatus;
import be.telemis.games.bowling.model.playingsession.PlayingSessionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FrameEntityFactory {

    @Value("${ancient.bowling.numberOfFrames}")
    private final int NUMBER_OF_FRAMES;
    @Value("${ancient.bowling.numberOfPins}")
    private final int NUMBER_OF_PINS;

    public List<FrameEntity> initFrameList(PlayingSessionEntity playingSession) {
        final List<FrameEntity> frameEntities = new ArrayList<>();
        for (int i = 1; i <= NUMBER_OF_FRAMES; i++) {
            final FrameEntity frameEntity = new FrameEntity();
            frameEntity.setFrameNumber(i);
            frameEntity.setStatus(FrameStatus.CREATED);
            frameEntity.setRemainingPins(NUMBER_OF_PINS);
            frameEntity.setPlayingSession(playingSession);
            frameEntities.add(frameEntity);
        }
        return frameEntities;
    }
}
