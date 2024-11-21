package be.telemis.games.bowling.service;

import be.telemis.games.bowling.dao.PlayingSessionRepository;
import be.telemis.games.bowling.factory.FrameEntityFactory;
import be.telemis.games.bowling.model.game.GameEntity;
import be.telemis.games.bowling.model.game.GameStatus;
import be.telemis.games.bowling.model.player.PlayerEntity;
import be.telemis.games.bowling.model.playingsession.PlayingSessionEntity;
import be.telemis.games.bowling.model.playingsession.PlayingSessionStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class PlayingSessionService {
    private static final Logger logger = LoggerFactory.getLogger(PlayingSessionService.class);

    private final PlayerService playerService;
    private final PlayingSessionRepository playingSessionRepository;
    private final FrameEntityFactory frameEntityFactory;

    public PlayingSessionEntity createPlayingSession(PlayingSessionEntity playingSession, GameEntity game) {
        if (!GameStatus.INITIALIZED.equals(game.getStatus())) {
            throw new IllegalStateException("Game was already started");
        } PlayerEntity inputPlayer = playingSession.getPlayer(); if (inputPlayer == null) {
            throw new IllegalStateException("Player not found");
        } List<PlayingSessionEntity> existingSessions = getPlayingSessionsByGameId(game.getId());
        if (!CollectionUtils.isEmpty(existingSessions) &&
                existingSessions.stream().map(PlayingSessionEntity::getPlayer)
                        .map(PlayerEntity::getId).anyMatch(playerId -> playerId.equals(inputPlayer.getId()))) {
            throw new IllegalStateException("Player cannot be added twice to the same game");
        }
        playingSession.setId(null); playingSession.setGame(game); playingSession.setScore(0);
        playingSession.setStatus(PlayingSessionStatus.INITIALIZED);
        playingSession.addFrames(frameEntityFactory.initFrameList(playingSession));
        playingSession.setPlayer(playerService.getPlayer(inputPlayer.getId()));
        return playingSessionRepository.save(playingSession);
    }

    public PlayingSessionEntity getPlayingSessionsById(GameEntity game, Integer sessionId) {
        final PlayingSessionEntity session =
                playingSessionRepository.findById(sessionId).orElseThrow(() -> new EntityNotFoundException(
                "Playing session not found"));
        if (!Objects.equals(game.getId(), session.getGame().getId())) {
            throw new IllegalArgumentException("Session does not belong to the game");
        }
        return session;
    }

    public List<PlayingSessionEntity> getPlayingSessionsByGameId(Integer gameId) {
        return playingSessionRepository.findByGameId(gameId);
    }

    public PlayingSessionEntity selectFirstActivePlayingSession(Integer gameId,
                                                                List<PlayingSessionEntity> playingSessions) {
        PlayingSessionEntity playingSessionEntity =
                playingSessions.stream().min(Comparator.comparing(PlayingSessionEntity::getCreationDate))
                        .orElseThrow(() -> new IllegalStateException("There is no active playing session for game " +
                                "with id: " + gameId));
        playingSessionEntity.setStatus(PlayingSessionStatus.ACTIVE);
        return playingSessionRepository.save(playingSessionEntity);
    }

    public List<PlayingSessionEntity> startPlayingSessions(List<PlayingSessionEntity> playingSessions) {
        playingSessions.forEach(session -> session.setStatus(PlayingSessionStatus.IN_PROGRESS));
        return playingSessionRepository.saveAll(playingSessions);
    }

}
