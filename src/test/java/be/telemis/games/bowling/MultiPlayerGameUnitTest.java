package be.telemis.games.bowling;

import be.telemis.games.bowling.dao.GameRepository;
import be.telemis.games.bowling.dao.PlayerRepository;
import be.telemis.games.bowling.dao.PlayingSessionRepository;
import be.telemis.games.bowling.factory.FrameEntityFactory;
import be.telemis.games.bowling.model.game.GameEntity;
import be.telemis.games.bowling.model.game.GameStatus;
import be.telemis.games.bowling.model.player.PlayerEntity;
import be.telemis.games.bowling.model.playingsession.PlayingSessionEntity;
import be.telemis.games.bowling.model.playingsession.PlayingSessionStatus;
import be.telemis.games.bowling.service.GameService;
import be.telemis.games.bowling.service.PlayerService;
import be.telemis.games.bowling.service.PlayingSessionService;
import be.telemis.games.bowling.service.ThrowService;
import be.telemis.games.bowling.utils.MockFactory;
import be.telemis.games.bowling.utils.TestLoggingUtils;
import be.telemis.games.bowling.utils.TestProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MultiPlayerGameUnitTest {
    private static final Logger logger = LoggerFactory.getLogger(ThrowServiceUnitTest.class);

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
    private ThrowService throwService;

    @BeforeEach
    void setUp() {
        FrameEntityFactory frameEntityFactory = new FrameEntityFactory(TestProperties.NUMBER_OF_FRAMES,
                TestProperties.NUMBER_OF_PINS);
        playerService = new PlayerService(playerRepository);
        playingSessionService = new PlayingSessionService(playerService,
                playingSessionRepository, frameEntityFactory);
        gameService = new GameService(playingSessionService, gameRepository, playingSessionRepository);
        throwService = new ThrowService(TestProperties.NUMBER_OF_FRAMES, TestProperties.NUMBER_OF_PINS,
                TestProperties.NUMBER_OF_THROWS_PER_FRAME, TestProperties.STRIKE_BONUS_THROWS,
                TestProperties.SPARE_BONUS_THROWS, gameRepository,
                playingSessionRepository);
        mockedGame = MockFactory.createMockedGame(1, "Test Game");
        mockedSession = MockFactory.createMockedSession(1, MockFactory.createMockedPlayer(1, "Player 1"), mockedGame);
        mockedSession.setFrames(frameEntityFactory.initFrameList(mockedSession));

        mockedSession.setGame(mockedGame);
    }

    @Test
    void testStartMultiPlayerGame_success() {
        //Create players
        final String NAME_PLAYER_1 = "Player 1";
        final String NAME_PLAYER_2 = "Player 2";
        final String NAME_PLAYER_3 = "Player 3";

        when(playerRepository.save(any(PlayerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PlayerEntity player1 = callCreatePlayer(1, NAME_PLAYER_1);
        PlayerEntity player2 = callCreatePlayer(2, NAME_PLAYER_2);
        PlayerEntity player3 = callCreatePlayer(3, NAME_PLAYER_3);
        when(playerRepository.findById(1)).thenReturn(Optional.of(player1));
        when(playerRepository.findById(2)).thenReturn(Optional.of(player2));
        when(playerRepository.findById(3)).thenReturn(Optional.of(player3));

        //Create Game
        when(gameRepository.save(any(GameEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        GameEntity game = gameService.createGame(mockedGame);

        //Create sessions
        when(playingSessionRepository.save(any(PlayingSessionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PlayingSessionEntity session1 = callCreatePlayingSession(1, player1, game);
        PlayingSessionEntity session2 = callCreatePlayingSession(2, player2, game);
        PlayingSessionEntity session3 = callCreatePlayingSession(3, player3, game);

        //Start game
        when(playingSessionRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<PlayingSessionEntity> sessions = invocation.getArgument(0);
            AtomicInteger idCounter = new AtomicInteger(1);
            sessions.forEach(session -> session.setId(idCounter.getAndIncrement()));
            return sessions;
        });
        when(playingSessionRepository.findByGameId(game.getId())).thenReturn(List.of(session1, session2, session3));

        game = gameService.startGame(game);

        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertEquals(PlayingSessionStatus.ACTIVE, game.getActivePlayingSession().getStatus());
        assertEquals(session1, game.getActivePlayingSession());
        assertEquals(NAME_PLAYER_1, game.getActivePlayingSession().getPlayer().getName());
    }


    @Test
    void testPlayMultiPlayerGame1_success() {
        final String NAME_PLAYER_1 = "Player 1";
        final String NAME_PLAYER_2 = "Player 2";
        final String NAME_PLAYER_3 = "Player 3";
        final Map<Integer, List<Integer>> session1ThrowMap = new LinkedHashMap<>();
        session1ThrowMap.put(1, List.of(8, 1, 1));
        session1ThrowMap.put(2, List.of(8, 7));
        session1ThrowMap.put(3, List.of(1, 2, 1));
        session1ThrowMap.put(4, List.of(15));
        session1ThrowMap.put(5, List.of(1, 2, 1));
        final Map<Integer, List<Integer>> session2ThrowMap = new LinkedHashMap<>();
        session2ThrowMap.put(1, List.of(15));
        session2ThrowMap.put(2, List.of(15));
        session2ThrowMap.put(3, List.of(15));
        session2ThrowMap.put(4, List.of(15));
        session2ThrowMap.put(5, List.of(15, 15, 15, 15));
        final Map<Integer, List<Integer>> session3ThrowMap = new LinkedHashMap<>();
        session3ThrowMap.put(1, List.of(15));
        session3ThrowMap.put(2, List.of(8, 1, 2));
        session3ThrowMap.put(3, List.of(1, 2, 12));
        session3ThrowMap.put(4, List.of(6, 4, 1));
        session3ThrowMap.put(5, List.of(15, 8, 2, 3));
        final String EXPECTED_WINNER_NAME = NAME_PLAYER_2;
        doTestPlayMultiPlayerGame1_success(NAME_PLAYER_1, NAME_PLAYER_2, NAME_PLAYER_3, session1ThrowMap,
                session2ThrowMap, session3ThrowMap, EXPECTED_WINNER_NAME);
    }

    @Test
    void testPlayMultiPlayerGame2_success() {
        final String NAME_PLAYER_1 = "Patrice";
        final String NAME_PLAYER_2 = "Xavier";
        final String NAME_PLAYER_3 = "Maayane";
        final Map<Integer, List<Integer>> session1ThrowMap = new LinkedHashMap<>();
        session1ThrowMap.put(1, List.of(8, 1, 1));
        session1ThrowMap.put(2, List.of(8, 7));
        session1ThrowMap.put(3, List.of(1, 2, 1));
        session1ThrowMap.put(4, List.of(15));
        session1ThrowMap.put(5, List.of(1, 2, 1));
        final Map<Integer, List<Integer>> session2ThrowMap = new LinkedHashMap<>();
        session2ThrowMap.put(1, List.of(1, 2, 3));
        session2ThrowMap.put(2, List.of(1, 2, 3));
        session2ThrowMap.put(3, List.of(1, 2, 3));
        session2ThrowMap.put(4, List.of(1, 2, 3));
        session2ThrowMap.put(5, List.of(1, 2, 3));
        final Map<Integer, List<Integer>> session3ThrowMap = new LinkedHashMap<>();
        session3ThrowMap.put(1, List.of(15));
        session3ThrowMap.put(2, List.of(8, 1, 2));
        session3ThrowMap.put(3, List.of(1, 2, 12));
        session3ThrowMap.put(4, List.of(6, 4, 1));
        session3ThrowMap.put(5, List.of(15, 8, 2, 3));
        final String EXPECTED_WINNER_NAME = NAME_PLAYER_3;
        doTestPlayMultiPlayerGame1_success(NAME_PLAYER_1, NAME_PLAYER_2, NAME_PLAYER_3, session1ThrowMap,
                session2ThrowMap, session3ThrowMap, EXPECTED_WINNER_NAME);
    }

    private PlayingSessionEntity callCreatePlayingSession(int id, PlayerEntity player1, GameEntity game) {
        PlayingSessionEntity session = MockFactory.createMockedSession(1, player1, game);
        session = playingSessionService.createPlayingSession(session, game);
        session.setId(id);
        return session;
    }

    private PlayerEntity callCreatePlayer(int id, String name) {
        PlayerEntity player = MockFactory.createMockedPlayer(id, name);
        player = playerService.createPlayer(player);
        player.setId(id);
        return player;
    }

    private PlayingSessionEntity playThrow(GameEntity game, PlayingSessionEntity session, Integer pinsKnocked) {
        return throwService.createThrow(game, session, MockFactory.createMockedThrow(pinsKnocked));
    }

    void doTestPlayMultiPlayerGame1_success(String NAME_PLAYER_1, String NAME_PLAYER_2, String NAME_PLAYER_3,
                                            Map<Integer, List<Integer>> session1ThrowMap,
                                            Map<Integer, List<Integer>> session2ThrowMap,
                                            Map<Integer, List<Integer>> session3ThrowMap, String EXPECTED_WINNER_NAME) {
        Iterator<Integer> session1flatThrows =
                session1ThrowMap.values().stream().flatMap(Collection::stream).toList().iterator();
        Iterator<Integer> session2flatThrows =
                session2ThrowMap.values().stream().flatMap(Collection::stream).toList().iterator();
        Iterator<Integer> session3flatThrows =
                session3ThrowMap.values().stream().flatMap(Collection::stream).toList().iterator();

        //Create mocked players
        PlayerEntity player1 = MockFactory.createMockedPlayer(1, NAME_PLAYER_1);
        PlayerEntity player2 = MockFactory.createMockedPlayer(2, NAME_PLAYER_2);
        PlayerEntity player3 = MockFactory.createMockedPlayer(3, NAME_PLAYER_3);

        //Create Game
        when(gameRepository.save(any(GameEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        GameEntity game = gameService.createGame(mockedGame);

        //Create mocked sessions
        when(playerRepository.findById(1)).thenReturn(Optional.of(player1));
        when(playerRepository.findById(2)).thenReturn(Optional.of(player2));
        when(playerRepository.findById(3)).thenReturn(Optional.of(player3));
        when(playingSessionRepository.save(any(PlayingSessionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PlayingSessionEntity session1 = callCreatePlayingSession(1, player1, game);
        PlayingSessionEntity session2 = callCreatePlayingSession(2, player2, game);
        PlayingSessionEntity session3 = callCreatePlayingSession(3, player3, game);

        Map<Integer, Iterator<Integer>> sessionThrowsMap = new HashMap<>();
        Map<Integer, PlayingSessionEntity> sessionsMap = new HashMap<>();
        sessionThrowsMap.put(session1.getId(), session1flatThrows);
        sessionThrowsMap.put(session2.getId(), session2flatThrows);
        sessionThrowsMap.put(session3.getId(), session3flatThrows);
        sessionsMap.put(1, session1);
        sessionsMap.put(2, session1);
        sessionsMap.put(3, session1);

        //Start game
        when(playingSessionRepository.save(any(PlayingSessionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(playingSessionRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<PlayingSessionEntity> sessions = invocation.getArgument(0);
            AtomicInteger idCounter = new AtomicInteger(1);
            sessions.forEach(session -> session.setId(idCounter.getAndIncrement()));
            return sessions;
        });
        when(playingSessionRepository.findByGameId(game.getId())).thenReturn(List.of(session1, session2, session3));
        game = gameService.startGame(game);

        when(gameRepository.findById(any())).thenReturn(Optional.of(game));
        try {
            while (!GameStatus.FINISHED.equals(game.getStatus())) {
                PlayingSessionEntity activePlayingSession = game.getActivePlayingSession();
                Iterator<Integer> pinsKnockedIterator = sessionThrowsMap.get(activePlayingSession.getId());
                if (!pinsKnockedIterator.hasNext()) {
                    Assertions.fail(String.format("No frame throw available for session %d ",
                            activePlayingSession.getId()));
                } else {
                    Integer pinsKnocked = pinsKnockedIterator.next();
                    PlayingSessionEntity updatedSession = playThrow(game, activePlayingSession, pinsKnocked);
                    sessionsMap.put(updatedSession.getId(), updatedSession);
                    game = gameService.getGame(game.getId());
                }
            }
        } catch (StackOverflowError error) {
            Assertions.fail("The game never finished", error);
        }

        logger.info("The winner is: {}", game.getWinningPlayingSession().getPlayer().getName());
        sessionsMap.values().forEach(session -> logger.info(TestLoggingUtils.getSessionReport(session)));

        assertEquals(EXPECTED_WINNER_NAME, game.getWinningPlayingSession().getPlayer().getName());
    }

}
