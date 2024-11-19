package be.telemis.games.bowling.service;

import be.telemis.games.bowling.dao.GameRepository;
import be.telemis.games.bowling.dao.PlayingSessionRepository;
import be.telemis.games.bowling.mapper.PlayingSessionMapper;
import be.telemis.games.bowling.mapper.ThrowMapper;
import be.telemis.games.bowling.model.game.*;
import jakarta.persistence.EntityNotFoundException;
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
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ThrowService {
    private static final Logger logger = LoggerFactory.getLogger(ThrowService.class);

    @Value("${ancient.bowling.numberOfFrames}")
    private int NUMBER_OF_FRAMES;
    @Value("${ancient.bowling.numberOfPins}")
    private int NUMBER_OF_PINS;
    @Value("${ancient.bowling.numberOfThrowsPerFrame}")
    private int NUMBER_OF_THROWS_PER_FRAME;
    @Value("${ancient.bowling.strikeBonusThrows}")
    private int STRIKE_BONUS_THROWS;
    @Value("${ancient.bowling.spareBonusThrows}")
    private int SPARE_BONUS_THROWS;

    private final GameRepository gameRepository;
    private final PlayingSessionRepository playingSessionRepository;
    private final PlayingSessionMapper playingSessionMapper;
    private final ThrowMapper throwMapper;

    public PlayingSessionCO createThrow(Integer gameId, Integer playingSessionId, ThrowCO throwCO) {
        final GameEntity gameEntity = gameRepository.findById(gameId).orElseThrow(() -> new EntityNotFoundException(
                "Game not found"));
        final PlayingSessionEntity playingSessionEntity =
                playingSessionRepository.findById(playingSessionId).orElseThrow(() -> new EntityNotFoundException(
                        "Playing session not found"));
        if (!Objects.equals(playingSessionEntity.getGame().getId(), gameEntity.getId())) {
            throw new IllegalArgumentException("Invalid playing session id");
        }
        final PlayingSessionEntity updatedSession = updateSessionFrames(playingSessionEntity,
                throwMapper.mapFromCO(throwCO));
        updateGameSessions(gameEntity);
        return playingSessionMapper.mapToCO(updatedSession);
    }

    private void updateGameSessions(GameEntity gameEntity) {
        final List<PlayingSessionEntity> sessionEntities = playingSessionRepository.findByGameId(gameEntity.getId());
        if (CollectionUtils.isEmpty(sessionEntities)) {
            throw new IllegalStateException("No playing sessions found for the game");
        }
        final PlayingSessionEntity nextPlayingSession = getNextPlayingSession(gameEntity, sessionEntities);

        gameEntity.setActivePlayingSession(nextPlayingSession);
//        gameEntity.setWinningPlayingSessions(getWinningSessions(sessionEntities));
        if (nextPlayingSession == null) {
            gameEntity.setStatus(GameStatus.FINISHED);
        }
        gameRepository.save(gameEntity);
    }

    private PlayingSessionEntity getNextPlayingSession(GameEntity gameEntity, List<PlayingSessionEntity> sessions) {
        final PlayingSessionEntity activeSession = gameEntity.getActivePlayingSession();
        int currentIndex = sessions.indexOf(activeSession);
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

    private List<PlayingSessionEntity> getWinningSessions(List<PlayingSessionEntity> sessions) {
        int maxScore = sessions.stream().mapToInt(PlayingSessionEntity::getScore).max()
                .orElseThrow(() -> new IllegalStateException("No sessions found"));
        return sessions.stream().filter(session -> session.getScore() == maxScore).toList();
    }

    private PlayingSessionEntity updateSessionFrames(PlayingSessionEntity session, ThrowEntity frameThrow) {
//        final List<FrameEntity> frames = getSortedFrames(session);
        final List<FrameEntity> frames = session.getFrames();
        final FrameEntity activeFrame = getActiveFrame(frames);
        validateThrow(activeFrame, frameThrow);
        updateActiveFrame(activeFrame, frameThrow);
        updateFramesWithScore(session, activeFrame, frameThrow);
        if (isLastFrame(activeFrame) && FrameStatus.CLOSED.equals(activeFrame.getStatus())) {
            session.setStatus(PlayingSessionStatus.FINISHED);
        }
        return playingSessionRepository.save(session);
    }

    private List<FrameEntity> getSortedFrames(PlayingSessionEntity playingSession) {
        return playingSession.getFrames().stream().sorted(Comparator.comparingInt(FrameEntity::getFrameNumber)).toList();
    }

    private FrameEntity getActiveFrame(List<FrameEntity> frames) {
        return frames.stream().filter(frame -> FrameStatus.CREATED.equals(frame.getStatus())).findFirst()
                .orElseThrow(() -> new IllegalStateException("No frame available for a throw in this playing session"));
    }

    private Optional<FrameEntity> getFirstCompletedFrame(List<FrameEntity> frames) {
        return frames.stream().filter(frame -> FrameStatus.COMPLETED.equals(frame.getStatus())).findFirst();
    }

    private void validateThrow(FrameEntity frame, ThrowEntity frameThrow) {
        if (frame.getRemainingPins() < frameThrow.getPinsKnocked()) {
            throw new IllegalArgumentException("Number of pins knocked can't exceed the remaining pins");
        }
    }

    private void updateActiveFrame(FrameEntity frame, ThrowEntity frameThrow) {
        frameThrow.setType(getThrowType(frame, frameThrow));
        frame.addThrow(frameThrow);
        frame.setRemainingPins(frame.getRemainingPins() - frameThrow.getPinsKnocked());
    }

    private void updateFramesWithScore(PlayingSessionEntity session, FrameEntity activeFrame, ThrowEntity frameThrow) {
        final List<FrameEntity> frames = session.getFrames();
        final int activeFrameIndex = frames.indexOf(activeFrame);
        final int startIndex = getFirstCompletedFrame(frames)
                .map(frames::indexOf)
                .orElse(activeFrameIndex);
        for (int i = startIndex; i <= activeFrameIndex; i++) {
            FrameEntity currentFrame = frames.get(i);
            int currentPinsKnocked = currentFrame.getFrameThrows().stream().mapToInt(ThrowEntity::getPinsKnocked).sum();
            int previousScore = getPreviousFrameScore(frames, i);
            if (i != activeFrameIndex) {
                int bonusPoints = currentFrame.getBonusPoints() + frameThrow.getPinsKnocked();
                currentFrame.setScore(previousScore + currentPinsKnocked + bonusPoints);
                currentFrame.setBonusPoints(bonusPoints);
            } else {
                currentFrame.setScore(previousScore + currentPinsKnocked);
            }
            updateFrameStatus(currentFrame, frameThrow);
        };
    }

    private int getPreviousFrameScore(List<FrameEntity> frames, int currentIndex) {
        return currentIndex > 0 ? frames.get(currentIndex - 1).getScore() : 0;
    }

    private void updateFrameStatus(FrameEntity frame, ThrowEntity frameThrow) {
        switch (frame.getStatus()) {
            case CREATED -> handleCreatedFrame(frame, frameThrow);
            case COMPLETED -> handleCompletedFrame(frame);
            default -> throw new IllegalStateException("Unexpected frame status: " + frame.getStatus());
        }
    }

    private void handleCreatedFrame(FrameEntity frame, ThrowEntity frameThrow) {
        switch (frameThrow.getType()) {
            case REGULAR -> closeFrameIfLastThrow(frame);
            case STRIKE -> setupFrameForBonusThrows(frame, STRIKE_BONUS_THROWS);
            case SPARE -> setupFrameForBonusThrows(frame, SPARE_BONUS_THROWS);
        }
    }

    private void closeFrameIfLastThrow(FrameEntity frame) {
        if (isLastFrameThrow(frame)) {
            frame.setStatus(FrameStatus.CLOSED);
        }
    }

    private void setupFrameForBonusThrows(FrameEntity frame, int bonusThrows) {
        //Add bonus only once by frame (ie last frame)
        if (frame.getRemainingBonusThrows() == null) {
            frame.setRemainingBonusThrows(bonusThrows);
        } else {
            int remainingBonusThrows = frame.getRemainingBonusThrows() - 1;
            frame.setRemainingBonusThrows(remainingBonusThrows);
        }
        if (!isLastFrame(frame)) {
            frame.setStatus(FrameStatus.COMPLETED);
        } else {
            if (frame.getRemainingBonusThrows() == 0) {
                frame.setStatus(FrameStatus.CLOSED);
            } else {
                frame.setRemainingPins(NUMBER_OF_PINS); // Reset for bonus throws
            }
        }
    }

    private void handleCompletedFrame(FrameEntity frame) {
        int remainingBonusThrows = frame.getRemainingBonusThrows() - 1;
        if (remainingBonusThrows <= 0) {
            frame.setStatus(FrameStatus.CLOSED);
        } else {
            frame.setRemainingBonusThrows(remainingBonusThrows);
        }
    }

    private boolean isLastFrame(FrameEntity frame) {
        return frame.getFrameNumber() == NUMBER_OF_FRAMES;
    }

    private ThrowType getThrowType(FrameEntity activeFrame, ThrowEntity frameThrow) {
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

    private boolean isLastFrameThrow(FrameEntity frame) {
        boolean isLastFrame = frame.getFrameNumber() == NUMBER_OF_FRAMES;
        if (isLastFrame) {
            return (frame.getRemainingBonusThrows() != null && frame.getRemainingBonusThrows() == 0) ||
                    (frame.getRemainingBonusThrows() == null && frame.getFrameThrows().size() == NUMBER_OF_THROWS_PER_FRAME);
        } else {
            return frame.getFrameThrows().size() == NUMBER_OF_THROWS_PER_FRAME;
        }
    }


    //
//    private PlayingSessionEntity updateSessionFrames(PlayingSessionEntity playingSession, ThrowEntity frameThrow) {
//        final List<FrameEntity> frames = playingSession.getFrames().stream()
//                .sorted(Comparator.comparingInt(FrameEntity::getFrameNumber)).toList();
//        final FrameEntity activeFrame = frames.stream()
//                .filter(frame -> FrameStatus.CREATED.equals(frame.getStatus()))
//                .findFirst()
//                .orElseThrow(() -> new IllegalStateException("No frame available for a throw in this playing
//                session"));
//        int pinsKnocked = frameThrow.getPinsKnocked();
//        int remainingPins = activeFrame.getRemainingPins() - pinsKnocked;
//        if (remainingPins < 0) {
//            throw new IllegalArgumentException("Number of pins knocked can't be superior to remaining ones");
//        }
//        final ThrowType throwType = getThrowType(activeFrame, frameThrow);
//        frameThrow.setType(throwType);
//        activeFrame.addThrow(frameThrow);
//        activeFrame.setRemainingPins(remainingPins);
//
//        boolean isLastFrame = activeFrame.getFrameNumber() == NUMBER_OF_FRAMES;
//        boolean isFirstFrameThrow = activeFrame.getFrameThrows().size() == 1;
//        boolean isLastFrameBonusThrow = isLastFrame && activeFrame.getRemainingBonusThrows() != null;
//
//        for (int i = frames.indexOf(activeFrame); i >= 0; i--) {
//            final FrameEntity currentFrame = frames.get(i);
//            switch (currentFrame.getStatus()) {
//                case CLOSED:
//                    break;
//                case CREATED: {
//                    if (isFirstFrameThrow) {
//                        currentFrame.setScore(playingSession.getScore());
//                    }
//                    if (isLastFrameBonusThrow) {
//                        int remainingBonusThrows = currentFrame.getRemainingBonusThrows() - 1;
//                        if (remainingBonusThrows == 0) {
//                            currentFrame.setStatus(FrameStatus.CLOSED);
//                        }
//                        currentFrame.setRemainingBonusThrows(remainingBonusThrows);
//                    } else {
//                        switch (frameThrow.getType()) {
//                            case REGULAR -> {
//                                if (isLastFrameThrow(currentFrame)) {
//                                    currentFrame.setStatus(FrameStatus.CLOSED);
//                                }
//                            }
//                            case STRIKE -> {
//                                currentFrame.setRemainingBonusThrows(STRIKE_BONUS_THROWS);
//                                if (!isLastFrame) {
//                                    currentFrame.setStatus(FrameStatus.COMPLETED);
//                                } else {
//                                    currentFrame.setRemainingPins(NUMBER_OF_PINS);
//                                }
//                            }
//                            case SPARE -> {
//                                currentFrame.setRemainingBonusThrows(SPARE_BONUS_THROWS);
//                                if (!isLastFrame) {
//                                    currentFrame.setStatus(FrameStatus.COMPLETED);
//                                } else {
//                                    currentFrame.setRemainingPins(NUMBER_OF_PINS);
//                                }
//                            }
//                        }
//                    }
//                }
//                case COMPLETED: {
//                    int remainingBonusThrows = currentFrame.getRemainingBonusThrows() - 1;
//                    if (remainingBonusThrows == 0) {
//                        currentFrame.setStatus(FrameStatus.CLOSED);
//                    }
//                    currentFrame.setRemainingBonusThrows(remainingBonusThrows);
//                }
//                int score = currentFrame.getScore() + pinsKnocked;
//                currentFrame.setScore(score);
//                playingSession.setScore(score);
//            }
//        }
//        if (isLastFrame) {
//            playingSession.setStatus(PlayingSessionStatus.FINISHED);
//        }
//        return playingSession;
//    }

    private int computeFrameScore(FrameEntity activeFrameEntity, int pinsKnocked) {
        //REGULAR
        int frameScore = pinsKnocked;
        if (!CollectionUtils.isEmpty(activeFrameEntity.getFrameThrows())) {
            frameScore += activeFrameEntity.getFrameThrows().stream().sorted().limit(NUMBER_OF_THROWS_PER_FRAME - 1).mapToInt(ThrowEntity::getPinsKnocked).sum();
        }
        return frameScore;
    }

    private void computeFrameScore(FrameEntity activeFrameEntity, ThrowCO throwCO) {

    }

}
