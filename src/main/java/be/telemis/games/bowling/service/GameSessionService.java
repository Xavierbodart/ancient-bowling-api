package be.telemis.games.bowling.service;

import be.telemis.games.bowling.dao.FrameRepository;
import be.telemis.games.bowling.dao.GameRepository;
import be.telemis.games.bowling.dao.PlayingSessionRepository;
import be.telemis.games.bowling.mapper.GameMapper;
import be.telemis.games.bowling.mapper.PlayingSessionMapper;
import be.telemis.games.bowling.model.game.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class GameSessionService {
    private static final Logger logger = LoggerFactory.getLogger(GameSessionService.class);

    @Value("${ancient.bowling.numberOfFrames}")
    private int NUMBER_OF_FRAMES;
    @Value("${ancient.bowling.numberOfPins}")
    private int NUMBER_OF_PINS;

    private final GameRepository gameRepository;
    private final PlayingSessionRepository playingSessionRepository;
    private final FrameRepository frameRepository;
    private final GameMapper gameMapper;
    private final PlayingSessionMapper playingSessionMapper;


    public GameCO getGame(Integer gameId) {
        final GameEntity gameEntity = findGameEntityById(gameId);
        return gameMapper.mapToCO(gameEntity);
    }

    public GameCO createGame(GameCO gameCO) {
        final GameEntity gameEntity = gameMapper.mapFromCO(gameCO);
        gameEntity.setStatus(GameStatus.INITIALIZED);
        return gameMapper.mapToCO(gameRepository.save(gameEntity));
    }

    public GameCO startGame(Integer gameId) {
        final GameEntity gameEntity = findGameEntityById(gameId);
        gameEntity.setStatus(GameStatus.IN_PROGRESS);
        gameEntity.setActivePlayingSession(selectActivePlayingSession(gameId));
        return gameMapper.mapToCO(gameRepository.save(gameEntity));
    }

    private PlayingSessionEntity selectActivePlayingSession(Integer gameId) {
        final List<PlayingSessionEntity> playingSessionEntities = playingSessionRepository.findByGameId(gameId);
        PlayingSessionEntity playingSessionEntity =
                playingSessionEntities.stream().min(Comparator.comparing(PlayingSessionEntity::getCreationDate)).orElseThrow(
                        () -> new IllegalStateException("There is no active playing session for game with id: " + gameId)
                );
        playingSessionEntity.setStatus(PlayingSessionStatus.ACTIVE);
        return playingSessionRepository.save(playingSessionEntity);
    }

    public List<PlayingSessionCO> getPlayingSessionsByGameId(Integer gameId) {
        final GameEntity gameEntity = findGameEntityById(gameId);
        return playingSessionRepository.findByGameId(gameEntity.getId()).stream().map(playingSessionMapper::mapToCO).toList();
    }

    public PlayingSessionCO createPlayingSession(Integer gameId, PlayingSessionCO playingSessionCO) {
        final GameEntity gameEntity = findGameEntityById(gameId);
        PlayingSessionEntity playingSessionEntity = playingSessionMapper.mapFromCO(playingSessionCO);
        playingSessionEntity.setGame(gameEntity);
        playingSessionEntity.setScore(0);
        playingSessionEntity.setStatus(PlayingSessionStatus.INITIALIZED);
        playingSessionEntity.addFrames(initFrameList(playingSessionEntity));
        return playingSessionMapper.mapToCO(playingSessionRepository.save(playingSessionEntity));
    }

    private List<FrameEntity> initFrameList(PlayingSessionEntity playingSessionEntity) {
        final List<FrameEntity> frameEntities = new ArrayList<>();
        for (int i = 1; i <= NUMBER_OF_FRAMES; i++) {
            final FrameEntity frameEntity = new FrameEntity();
            frameEntity.setFrameNumber(i);
            frameEntity.setStatus(FrameStatus.CREATED);
            frameEntity.setRemainingPins(NUMBER_OF_PINS);
            frameEntity.setPlayingSession(playingSessionEntity);
            frameEntities.add(frameEntity);
        }
        return frameEntities;
    }

    private GameEntity findGameEntityById(Integer gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found with id: " + gameId));
    }
}
