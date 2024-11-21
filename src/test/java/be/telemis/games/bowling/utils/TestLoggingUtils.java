package be.telemis.games.bowling.utils;

import be.telemis.games.bowling.model.frame.FrameEntity;
import be.telemis.games.bowling.model.frame.ThrowEntity;
import be.telemis.games.bowling.model.playingsession.PlayingSessionEntity;

import java.util.List;

public class TestLoggingUtils {

    public static String getSessionReport(PlayingSessionEntity session) {
        final List<FrameEntity> frames = session.getFrames();
        final StringBuilder sessionReport = new StringBuilder();

        sessionReport.append(String.format("%-15s%-30s%-10s%n", "Frame number", "Pins knocked", "Frame score"));

        for (FrameEntity frame : frames) {
            final List<Integer> throwsPinsKnocked =
                    frame.getFrameThrows().stream().map(ThrowEntity::getPinsKnocked).toList();

            sessionReport.append(String.format("%-15d%-30s%-10d%n",
                    frame.getFrameNumber(),
                    throwsPinsKnocked,
                    frame.getScore()));
        }
        return """
                
                GAME SESSION REPORT OF %s:
                %s""".formatted(session.getPlayer().getName(), sessionReport);
    }
}
