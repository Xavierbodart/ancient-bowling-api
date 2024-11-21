package be.telemis.games.bowling;

import be.telemis.games.bowling.dao.GameRepository;
import be.telemis.games.bowling.dao.PlayerRepository;
import be.telemis.games.bowling.dao.PlayingSessionRepository;
import be.telemis.games.bowling.factory.FrameEntityFactory;
import be.telemis.games.bowling.model.game.GameEntity;
import be.telemis.games.bowling.model.game.GameStatus;
import be.telemis.games.bowling.model.playingsession.PlayingSessionEntity;
import be.telemis.games.bowling.model.playingsession.PlayingSessionStatus;
import be.telemis.games.bowling.service.GameService;
import be.telemis.games.bowling.service.PlayerService;
import be.telemis.games.bowling.service.PlayingSessionService;
import be.telemis.games.bowling.utils.MockFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GameServiceUnitTest {

    private static final int NUMBER_OF_FRAMES = 5;
    private static final int NUMBER_OF_PINS = 15;

    @Mock
    private GameRepository gameRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private PlayingSessionRepository playingSessionRepository;


    private GameEntity mockedGame;
    private PlayingSessionEntity mockedSession;

    private GameService gameService;
    private PlayingSessionService playingSessionService;
    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        FrameEntityFactory frameEntityFactory = new FrameEntityFactory(NUMBER_OF_FRAMES, NUMBER_OF_PINS);
        playerService = new PlayerService(playerRepository);
        playingSessionService = new PlayingSessionService(playerService,
                playingSessionRepository, frameEntityFactory);
        gameService = new GameService(playingSessionService, gameRepository, playingSessionRepository);

        // Create mock entities
        mockedGame = MockFactory.createMockedGame(1, "Test Game");
        mockedSession = MockFactory.createMockedSession(1, MockFactory.createMockedPlayer(1, "Player 1"), mockedGame);
        mockedSession.setFrames(frameEntityFactory.initFrameList(mockedSession));

        mockedGame.setActivePlayingSession(mockedSession);
    }

    @Test
    void testCreateThrow_GameNotFound() {
        when(gameRepository.findById(anyInt())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> gameService.getGame(1));
    }

    @Test
    void testCreateThrow_CreateGame() {
        when(gameRepository.save(any(GameEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        GameEntity game = gameService.createGame(mockedGame);
        assertNotNull(game);
        assertEquals(GameStatus.INITIALIZED, game.getStatus());
    }

    @Test
    void testCreateThrow_StartGame() {
        when(gameRepository.save(any(GameEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(playingSessionRepository.save(any(PlayingSessionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(playingSessionRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(playingSessionRepository.findByGameId(anyInt())).thenReturn(List.of(mockedSession));

        GameEntity game = gameService.startGame(mockedGame);

        assertNotNull(game);
        assertNotNull(game.getActivePlayingSession());
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertEquals(PlayingSessionStatus.ACTIVE, game.getActivePlayingSession().getStatus());
    }

}
