package be.telemis.games.bowling.utils;

import be.telemis.games.bowling.model.frame.FrameEntity;
import be.telemis.games.bowling.model.frame.FrameStatus;

import java.util.List;

public class FrameUtils {

    public static FrameEntity getActiveFrame(List<FrameEntity> frames) {
        return frames.stream().filter(frame -> List.of(FrameStatus.CREATED, FrameStatus.EXTENDED).contains(frame.getStatus())).findFirst()
                .orElseThrow(() -> new IllegalStateException("No frame available for a throw in this playing " +
                        "session"));
    }

    public static boolean isLastFrame(FrameEntity frame, int numberOfFrames) {
        return frame.getFrameNumber() == numberOfFrames;
    }

    public static boolean isLastFrameThrow(FrameEntity frame, int numberOfFrames, int numberOfThrowsPerFrame) {
        if (FrameUtils.isLastFrame(frame, numberOfFrames)) {
            return (frame.getRemainingBonusThrows() != null && frame.getRemainingBonusThrows() == 0) ||
                    (frame.getRemainingBonusThrows() == null && frame.getFrameThrows().size() == numberOfThrowsPerFrame);
        } else {
            return frame.getFrameThrows().size() == numberOfThrowsPerFrame;
        }
    }
}
