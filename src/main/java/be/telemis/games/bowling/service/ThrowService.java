package be.telemis.games.bowling.service;

import be.telemis.games.bowling.dao.GameRepository;
import be.telemis.games.bowling.dao.PlayingSessionRepository;
import be.telemis.games.bowling.model.frame.FrameEntity;
import be.telemis.games.bowling.model.frame.FrameStatus;
import be.telemis.games.bowling.model.frame.ThrowEntity;
import be.telemis.games.bowling.model.game.GameEntity;
import be.telemis.games.bowling.model.game.GameStatus;
import be.telemis.games.bowling.model.playingsession.PlayingSessionEntity;
import be.telemis.games.bowling.model.playingsession.PlayingSessionStatus;
import be.telemis.games.bowling.utils.FrameUtils;
import be.telemis.games.bowling.utils.ThrowUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class ThrowService {
    private static final Logger logger = LoggerFactory.getLogger(ThrowService.class);

    @Value("${ancient.bowling.numberOfFrames}")
    private final int NUMBER_OF_FRAMES;
    @Value("${ancient.bowling.numberOfPins}")
    private final int NUMBER_OF_PINS;
    @Value("${ancient.bowling.numberOfThrowsPerFrame}")
    private final int NUMBER_OF_THROWS_PER_FRAME;
    @Value("${ancient.bowling.strikeBonusThrows}")
    private final int STRIKE_BONUS_THROWS;
    @Value("${ancient.bowling.spareBonusThrows}")
    private final int SPARE_BONUS_THROWS;

    private final GameRepository gameRepository;
    private final PlayingSessionRepository playingSessionRepository;

    public PlayingSessionEntity createThrow(GameEntity game, PlayingSessionEntity playingSession,
                                            ThrowEntity frameThrow) {
        if (!Objects.equals(playingSession.getGame().getId(), game.getId())) {
            throw new IllegalArgumentException("Session does not belong to the game");
        }
        if (!PlayingSessionStatus.ACTIVE.equals(playingSession.getStatus())) {
            throw new IllegalArgumentException("It is not this session's turn!");
        }
        final PlayingSessionEntity updatedSession = updateActiveSessionAfterThrow(playingSession, frameThrow);
        updateGameSessions(game, updatedSession);
        return updatedSession;
    }

    private void updateGameSessions(GameEntity game, PlayingSessionEntity currentSession) {
        final List<PlayingSessionEntity> sessions = playingSessionRepository.findByGameId(game.getId());
        if (CollectionUtils.isEmpty(sessions)) {
            throw new IllegalStateException("No playing session found for the game");
        }
        final PlayingSessionEntity nextPlayingSession = getNextPlayingSession(currentSession, sessions);
        if (nextPlayingSession != null) {
            nextPlayingSession.setStatus(PlayingSessionStatus.ACTIVE);
            playingSessionRepository.save(nextPlayingSession);
        } else {
            game.setStatus(GameStatus.FINISHED);
        }

        game.setActivePlayingSession(nextPlayingSession);
        game.setWinningPlayingSession(getWinningSession(sessions));

        gameRepository.save(game);
    }

    private PlayingSessionEntity getNextPlayingSession(PlayingSessionEntity currentSession,
                                                       List<PlayingSessionEntity> sessions) {
        int currentIndex = sessions.indexOf(currentSession);
        if(PlayingSessionStatus.ACTIVE.equals(currentSession.getStatus())){
            return currentSession;
        }
        // Find the next session not finished, looping back to the start if necessary
        for (int i = 1; i <= sessions.size(); i++) {
            int nextIndex = (currentIndex + i) % sessions.size();
            final PlayingSessionEntity nextSession = sessions.get(nextIndex);
            if (!PlayingSessionStatus.FINISHED.equals(nextSession.getStatus())) {
                return nextSession;
            }
        }
        return null;
    }

    private PlayingSessionEntity getWinningSession(List<PlayingSessionEntity> sessions) {
        return sessions.stream()
                .max(Comparator.comparingInt(PlayingSessionEntity::getScore))
                .orElseThrow(() -> new IllegalStateException("No sessions found"));
    }

    private PlayingSessionEntity updateActiveSessionAfterThrow(PlayingSessionEntity session, ThrowEntity frameThrow) {
        final List<FrameEntity> frames = session.getFrames();
        final FrameEntity activeFrame = FrameUtils.getActiveFrame(frames);
        int activeFrameIndex = frames.indexOf(activeFrame);
        validateThrow(activeFrame, frameThrow);
        addThrowToFrame(activeFrame, frameThrow);

        int sessionScore = updateFrameScore(frames, activeFrame, activeFrameIndex, frameThrow);
        session.setScore(sessionScore);

        if (FrameUtils.isLastFrame(activeFrame, NUMBER_OF_FRAMES) && FrameStatus.CLOSED.equals(activeFrame.getStatus())) {
            session.setStatus(PlayingSessionStatus.FINISHED);
        } else if (List.of(FrameStatus.COMPLETED, FrameStatus.CLOSED).contains(activeFrame.getStatus())) {
            session.setStatus(PlayingSessionStatus.IN_PROGRESS);
        }
        return playingSessionRepository.save(session);
    }

    private void validateThrow(FrameEntity frame, ThrowEntity frameThrow) {
        if (frame.getRemainingPins() < frameThrow.getPinsKnocked()) {
            throw new IllegalArgumentException("Number of pins knocked can't exceed the remaining pins");
        }
    }

    private void addThrowToFrame(FrameEntity frame, ThrowEntity frameThrow) {
        frameThrow.setType(ThrowUtils.getThrowType(frame, frameThrow, NUMBER_OF_PINS));
        frame.addThrow(frameThrow);
        frame.setRemainingPins(frame.getRemainingPins() - frameThrow.getPinsKnocked());
    }

    private int updateFrameScore(List<FrameEntity> frames, FrameEntity activeFrame, int currentFrameIndex,
                                 ThrowEntity frameThrow) {
        final FrameEntity currentFrame = frames.get(currentFrameIndex);
        if (FrameStatus.CLOSED.equals(currentFrame.getStatus())) {
            return currentFrame.getScore();
        }
        int pinKnocked = frameThrow.getPinsKnocked();
        int score;
        if (!Objects.equals(currentFrame, activeFrame)) {
            currentFrame.setBonusPoints(currentFrame.getBonusPoints() + pinKnocked);
        }
        updateFrameStatus(currentFrame, frameThrow);

        if (currentFrameIndex > 0) {
            score = updateFrameScore(frames, activeFrame, currentFrameIndex - 1, frameThrow)
                    + ThrowUtils.getPinsKnocked(currentFrame) + currentFrame.getBonusPoints();
        } else {
            score = ThrowUtils.getPinsKnocked(currentFrame) + currentFrame.getBonusPoints();
        }
        currentFrame.setScore(score);
        return score;
    }


    private void updateFrameStatus(FrameEntity frame, ThrowEntity frameThrow) {
        switch (frame.getStatus()) {
            case CREATED -> handleCreatedFrame(frame, frameThrow);
            case COMPLETED -> handleCompletedFrame(frame);
            case EXTENDED -> handleExtendedFrame(frame);
            default -> throw new IllegalStateException("Unexpected frame status: " + frame.getStatus());
        }
    }

    private void handleCreatedFrame(FrameEntity frame, ThrowEntity frameThrow) {
        switch (frameThrow.getType()) {
            case STRIKE -> setupFrameForBonusThrows(frame, STRIKE_BONUS_THROWS);
            case SPARE -> setupFrameForBonusThrows(frame, SPARE_BONUS_THROWS);
            default -> closeFrameIfLastThrow(frame);
        }
    }

    private void closeFrameIfLastThrow(FrameEntity frame) {
        if (FrameUtils.isLastFrameThrow(frame, NUMBER_OF_FRAMES, NUMBER_OF_THROWS_PER_FRAME)) {
            frame.setStatus(FrameStatus.CLOSED);
        }
    }

    private void setupFrameForBonusThrows(FrameEntity frame, int bonusThrows) {
        //Add bonus only once by frame (ie last frame)
        if (frame.getRemainingBonusThrows() == null) {
            frame.setRemainingBonusThrows(bonusThrows);
        } else {
            frame.setRemainingBonusThrows(frame.getRemainingBonusThrows() - 1);
        }
        if (!FrameUtils.isLastFrame(frame, NUMBER_OF_FRAMES)) {
            frame.setStatus(FrameStatus.COMPLETED);
        } else { //last frame
            if (frame.getRemainingBonusThrows() == 0) {
                frame.setStatus(FrameStatus.CLOSED);
            } else {
                if (frame.getRemainingBonusThrows() <= 0) {
                    frame.setStatus(FrameStatus.CLOSED);
                } else {
                    frame.setStatus(FrameStatus.EXTENDED);
                    frame.setRemainingPins(NUMBER_OF_PINS); // Reset for bonus throws
                }
            }
        }
    }

    private void handleCompletedFrame(FrameEntity frame) {
        frame.setRemainingBonusThrows(frame.getRemainingBonusThrows() - 1);
        if (frame.getRemainingBonusThrows() <= 0) {
            frame.setStatus(FrameStatus.CLOSED);
        }
    }

    private void handleExtendedFrame(FrameEntity frame) {
        frame.setRemainingBonusThrows(frame.getRemainingBonusThrows() - 1);
        if (frame.getRemainingBonusThrows() <= 0) {
            frame.setStatus(FrameStatus.CLOSED);
        } else if (frame.getRemainingPins() == 0) {
            frame.setRemainingPins(NUMBER_OF_PINS); // Reset for bonus throws
        }
    }

}
