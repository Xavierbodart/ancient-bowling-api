package be.telemis.games.bowling;

import be.telemis.games.bowling.dao.GameRepository;
import be.telemis.games.bowling.dao.PlayingSessionRepository;
import be.telemis.games.bowling.factory.FrameEntityFactory;
import be.telemis.games.bowling.model.frame.FrameEntity;
import be.telemis.games.bowling.model.frame.ThrowEntity;
import be.telemis.games.bowling.model.frame.ThrowType;
import be.telemis.games.bowling.model.game.GameEntity;
import be.telemis.games.bowling.model.playingsession.PlayingSessionEntity;
import be.telemis.games.bowling.model.playingsession.PlayingSessionStatus;
import be.telemis.games.bowling.service.ThrowService;
import be.telemis.games.bowling.utils.MockFactory;
import be.telemis.games.bowling.utils.TestLoggingUtils;
import be.telemis.games.bowling.utils.TestProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ThrowServiceUnitTest {
    private static final Logger logger = LoggerFactory.getLogger(ThrowServiceUnitTest.class);

    private static final int REGULAR_FIRST_THROW_PIN_KNOCKED = 1;

    @Mock
    private GameRepository gameRepository;
    @Mock
    private PlayingSessionRepository playingSessionRepository;

    private GameEntity mockedGame;
    private PlayingSessionEntity mockedActiveSession;
    private ThrowEntity mockedThrow;
    private ThrowService throwService;

    @BeforeEach
    void setUp() {
        FrameEntityFactory frameEntityFactory = new FrameEntityFactory(TestProperties.NUMBER_OF_FRAMES,
                TestProperties.NUMBER_OF_PINS);

        throwService = new ThrowService(TestProperties.NUMBER_OF_FRAMES, TestProperties.NUMBER_OF_PINS,
                TestProperties.NUMBER_OF_THROWS_PER_FRAME, TestProperties.STRIKE_BONUS_THROWS,
                TestProperties.SPARE_BONUS_THROWS, gameRepository, playingSessionRepository);

        // Create mock entities
        mockedGame = MockFactory.createMockedGame(1, "Test Game");
        mockedThrow = MockFactory.createMockedThrow(REGULAR_FIRST_THROW_PIN_KNOCKED);
        mockedActiveSession = MockFactory.createMockedSession(1, MockFactory.createMockedPlayer(1, "Player 1"), mockedGame);
        mockedActiveSession.setStatus(PlayingSessionStatus.ACTIVE);
        mockedActiveSession.setFrames(frameEntityFactory.initFrameList(mockedActiveSession));
        mockedActiveSession.setGame(mockedGame);
    }

    @Test
    void testCreateFirstRegularThrow_Success() {
        when(playingSessionRepository.findByGameId(anyInt())).thenReturn(List.of(mockedActiveSession));
        when(playingSessionRepository.save(any(PlayingSessionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        PlayingSessionEntity resultSession = throwService.createThrow(mockedGame, mockedActiveSession, mockedThrow);

        assertNotNull(resultSession);
        assertEquals(mockedActiveSession.getId(), resultSession.getId());
        assertEquals(PlayingSessionStatus.ACTIVE, resultSession.getStatus());
        assertEquals(REGULAR_FIRST_THROW_PIN_KNOCKED, resultSession.getScore());

        assertNotNull(resultSession.getFrames());
        final FrameEntity firstFrame = resultSession.getFrames().getFirst();
        assertEquals(REGULAR_FIRST_THROW_PIN_KNOCKED, firstFrame.getScore());

        final ThrowEntity firstThrow = resultSession.getFrames().getFirst().getFrameThrows().getFirst();
        assertFalse(CollectionUtils.isEmpty(resultSession.getFrames().getFirst().getFrameThrows()));
        assertEquals(ThrowType.REGULAR, firstThrow.getType());
        assertEquals(mockedThrow.getPinsKnocked(), firstThrow.getPinsKnocked());
    }

    @Test
    void testFullStrikeSession_Success() {
        final int TOTAL_NUMBER_OF_THROWS = TestProperties.NUMBER_OF_FRAMES + TestProperties.STRIKE_BONUS_THROWS;
        final int EXPECTED_SCORE = 300;
        final List<Integer> throwsPinsKnocked = Collections.nCopies(TOTAL_NUMBER_OF_THROWS,
                TestProperties.NUMBER_OF_PINS);

        logger.info("List of throws  pins knocked: {}", throwsPinsKnocked);

        when(playingSessionRepository.findByGameId(anyInt())).thenReturn(List.of(mockedActiveSession));
        when(playingSessionRepository.save(any(PlayingSessionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PlayingSessionEntity resultSession = playSession(mockedGame, mockedActiveSession, throwsPinsKnocked);

        assertNotNull(resultSession);
        assertEquals(mockedActiveSession.getId(), resultSession.getId());
        assertEquals(PlayingSessionStatus.FINISHED, resultSession.getStatus());
        assertEquals(EXPECTED_SCORE, resultSession.getScore());

        assertNotNull(resultSession.getFrames());
        final FrameEntity firstFrame = resultSession.getFrames().getFirst();
        assertEquals(TestProperties.NUMBER_OF_PINS + TestProperties.STRIKE_BONUS_THROWS * TestProperties.NUMBER_OF_PINS, firstFrame.getScore());
        final FrameEntity lastFrame = resultSession.getFrames().getLast();
        assertEquals(EXPECTED_SCORE, lastFrame.getScore());

        final ThrowEntity firstThrow = resultSession.getFrames().getFirst().getFrameThrows().getFirst();
        assertFalse(CollectionUtils.isEmpty(resultSession.getFrames().getFirst().getFrameThrows()));
        assertEquals(TestProperties.NUMBER_OF_PINS, firstThrow.getPinsKnocked());
        assertEquals(ThrowType.STRIKE, firstThrow.getType());
    }


    @Test
    void testZeroSession_Success() {
        final int EXPECTED_SCORE = 0;
        final Map<Integer, List<Integer>> throwMap = new LinkedHashMap<>();
        throwMap.put(1, List.of(0, 0, 0));
        throwMap.put(2, List.of(0, 0, 0));
        throwMap.put(3, List.of(0, 0, 0));
        throwMap.put(4, List.of(0, 0, 0));
        throwMap.put(5, List.of(0, 0, 0));
        logger.info("List of throws  pins knocked: {}", throwMap);

        when(playingSessionRepository.findByGameId(anyInt())).thenReturn(List.of(mockedActiveSession));
        when(playingSessionRepository.save(any(PlayingSessionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Integer> flatThrows = throwMap.values().stream().flatMap(Collection::stream).toList();
        PlayingSessionEntity resultSession = playSession(mockedGame, mockedActiveSession, flatThrows);

        assertNotNull(resultSession);
        assertEquals(mockedActiveSession.getId(), resultSession.getId());
        assertEquals(PlayingSessionStatus.FINISHED, resultSession.getStatus());
        assertEquals(EXPECTED_SCORE, resultSession.getScore());
    }


    @Test
    void testMixed1Session_Success() {
        final int EXPECTED_SCORE = 55;
        final Map<Integer, List<Integer>> throwMap = new LinkedHashMap<>();
        throwMap.put(1, List.of(8, 1, 1));
        throwMap.put(2, List.of(8, 7));
        throwMap.put(3, List.of(1, 2, 1));
        throwMap.put(4, List.of(15));
        throwMap.put(5, List.of(1, 2, 1));
        logger.info("List of throws  pins knocked: {}", throwMap);

        when(playingSessionRepository.findByGameId(anyInt())).thenReturn(List.of(mockedActiveSession));
        when(playingSessionRepository.save(any(PlayingSessionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Integer> throwList = throwMap.values().stream().flatMap(Collection::stream).toList();
        PlayingSessionEntity resultSession = playSession(mockedGame, mockedActiveSession, throwList);

        assertNotNull(resultSession);
        assertEquals(mockedActiveSession.getId(), resultSession.getId());
        assertEquals(PlayingSessionStatus.FINISHED, resultSession.getStatus());
        assertEquals(EXPECTED_SCORE, resultSession.getScore());
    }

    @Test
    void testMixed2Session_Success() {
        final int EXPECTED_SCORE = 101;
        final Map<Integer, List<Integer>> throwMap = new LinkedHashMap<>();
        throwMap.put(1, List.of(15));
        throwMap.put(2, List.of(8, 1, 2));
        throwMap.put(3, List.of(1, 2, 12));
        throwMap.put(4, List.of(6, 4, 1));
        throwMap.put(5, List.of(15, 8, 2, 3));

        logger.info("List of throws pins knocked: {}", throwMap);

        when(playingSessionRepository.findByGameId(anyInt())).thenReturn(List.of(mockedActiveSession));
        when(playingSessionRepository.save(any(PlayingSessionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Integer> throwList = throwMap.values().stream().flatMap(Collection::stream).toList();
        PlayingSessionEntity resultSession = playSession(mockedGame, mockedActiveSession, throwList);

        assertNotNull(resultSession);
        assertEquals(mockedActiveSession.getId(), resultSession.getId());
        assertEquals(PlayingSessionStatus.FINISHED, resultSession.getStatus());
        assertEquals(EXPECTED_SCORE, resultSession.getScore());
    }

    @Test
    void testMixed3Session_Success() {
        final int EXPECTED_SCORE = 140;
        final Map<Integer, List<Integer>> throwMap = new LinkedHashMap<>();
        throwMap.put(1, List.of(8, 7));
        throwMap.put(2, List.of(3, 4, 5));
        throwMap.put(3, List.of(8, 7));
        throwMap.put(4, List.of(15));
        throwMap.put(5, List.of(1, 2, 12, 15, 15));
        logger.info("List of throws  pins knocked: {}", throwMap);

        when(playingSessionRepository.findByGameId(anyInt())).thenReturn(List.of(mockedActiveSession));
        when(playingSessionRepository.save(any(PlayingSessionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Integer> throwList = throwMap.values().stream().flatMap(Collection::stream).toList();
        PlayingSessionEntity resultSession = playSession(mockedGame, mockedActiveSession, throwList);

        assertNotNull(resultSession);
        assertEquals(mockedActiveSession.getId(), resultSession.getId());
        assertEquals(PlayingSessionStatus.FINISHED, resultSession.getStatus());
        assertEquals(EXPECTED_SCORE, resultSession.getScore());
    }

    @Test
    void testMixed4Session_Success() {
        final int EXPECTED_SCORE = 183;
        final Map<Integer, List<Integer>> throwMap = new LinkedHashMap<>();
        throwMap.put(1, List.of(5, 5, 5));
        throwMap.put(2, List.of(15));
        throwMap.put(3, List.of(15));
        throwMap.put(4, List.of(15));
        throwMap.put(5, List.of(1, 1, 13, 2, 13));
        logger.info("List of throws  pins knocked: {}", throwMap);

        when(playingSessionRepository.findByGameId(anyInt())).thenReturn(List.of(mockedActiveSession));
        when(playingSessionRepository.save(any(PlayingSessionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Integer> throwList = throwMap.values().stream().flatMap(Collection::stream).toList();
        PlayingSessionEntity resultSession = playSession(mockedGame, mockedActiveSession, throwList);

        assertNotNull(resultSession);
        assertEquals(mockedActiveSession.getId(), resultSession.getId());
        assertEquals(PlayingSessionStatus.FINISHED, resultSession.getStatus());
        assertEquals(EXPECTED_SCORE, resultSession.getScore());
    }

    @Test
    void testMixed4Session_FailureOnLastThrow() {
        final Map<Integer, List<Integer>> throwMap = new LinkedHashMap<>();
        throwMap.put(1, List.of(5, 5, 5));
        throwMap.put(2, List.of(15));
        throwMap.put(3, List.of(15));
        throwMap.put(4, List.of(15));
        throwMap.put(5, List.of(1, 1, 13, 2, 15));
        logger.info("List of throws  pins knocked: {}", throwMap);

        when(playingSessionRepository.findByGameId(anyInt())).thenReturn(List.of(mockedActiveSession));
        when(playingSessionRepository.save(any(PlayingSessionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Integer> throwList = throwMap.values().stream().flatMap(Collection::stream).toList();
        assertThrows(IllegalArgumentException.class, () -> playSession(mockedGame, mockedActiveSession, throwList));
    }

    @Test
    void testCreateThrow_ThrowInSessionWithWrongGame() {
        PlayingSessionEntity session = new PlayingSessionEntity();
        GameEntity wrongGame = new GameEntity();
        wrongGame.setId(99);
        session.setGame(wrongGame);

        assertThrows(IllegalArgumentException.class, () -> throwService.createThrow(wrongGame, mockedActiveSession,
                mockedThrow));
    }

    private PlayingSessionEntity playSession(GameEntity game, PlayingSessionEntity session,
                                             List<Integer> throwsPinsKnocked) {
        PlayingSessionEntity resultSession = null;
        for (Integer pinsKnocked : throwsPinsKnocked) {
            resultSession = throwService.createThrow(game, session, MockFactory.createMockedThrow(pinsKnocked));
        }

        logger.info("Final score: {}", resultSession != null ? resultSession.getScore() : null);
        if (resultSession != null) {
            logger.info(TestLoggingUtils.getSessionReport(resultSession));
        }
        return resultSession;
    }

}
