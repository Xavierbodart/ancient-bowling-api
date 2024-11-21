package be.telemis.games.bowling.utils;

import be.telemis.games.bowling.model.frame.FrameEntity;
import be.telemis.games.bowling.model.frame.ThrowEntity;
import be.telemis.games.bowling.model.frame.ThrowType;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class ThrowUtils {

    public static ThrowType getThrowType(FrameEntity activeFrame, ThrowEntity frameThrow, int NUMBER_OF_PINS) {
        int currentPinsKnocked = frameThrow.getPinsKnocked();
        if (NUMBER_OF_PINS == currentPinsKnocked) {
            return ThrowType.STRIKE;
        } else {
            final List<ThrowEntity> existingFrameThrows = activeFrame.getFrameThrows();
            int framePinsKnocked = CollectionUtils.isEmpty(existingFrameThrows) ? currentPinsKnocked :
                    existingFrameThrows.stream().mapToInt(ThrowEntity::getPinsKnocked).sum() + currentPinsKnocked;
            if (NUMBER_OF_PINS == framePinsKnocked) {
                return ThrowType.SPARE;
            } else {
                return ThrowType.REGULAR;
            }
        }
    }


    public static int getPinsKnocked(FrameEntity frame) {
        return frame.getFrameThrows().stream().mapToInt(ThrowEntity::getPinsKnocked).sum();
    }

}
