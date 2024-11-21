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
public class StandardBowlingUnitTest {
    private static final Logger logger = LoggerFactory.getLogger(StandardBowlingUnitTest.class);

    public static final int NUMBER_OF_FRAMES = 10;
    public static final int NUMBER_OF_PINS = 10;
    public static final int NUMBER_OF_THROWS_PER_FRAME = 2;
    public static final int STRIKE_BONUS_THROWS = 2;
    public static final int SPARE_BONUS_THROWS = 1;

    @Mock
    private GameRepository gameRepository;
    @Mock
    private PlayingSessionRepository playingSessionRepository;

    private GameEntity mockedGame;
    private PlayingSessionEntity mockedActiveSession;
    private ThrowService throwService;

    @BeforeEach
    void setUp() {
        FrameEntityFactory frameEntityFactory = new FrameEntityFactory(NUMBER_OF_FRAMES,
                NUMBER_OF_PINS);

        throwService = new ThrowService(NUMBER_OF_FRAMES, NUMBER_OF_PINS,
                NUMBER_OF_THROWS_PER_FRAME, STRIKE_BONUS_THROWS,
                SPARE_BONUS_THROWS, gameRepository, playingSessionRepository);

        // Create mock entities
        mockedGame = MockFactory.createMockedGame(1, "Test Game");
        mockedActiveSession = MockFactory.createMockedSession(1, MockFactory.createMockedPlayer(1, "Player 1"), mockedGame);
        mockedActiveSession.setStatus(PlayingSessionStatus.ACTIVE);
        mockedActiveSession.setFrames(frameEntityFactory.initFrameList(mockedActiveSession));
        mockedActiveSession.setGame(mockedGame);
    }


    @Test
    void testFullStrikeStandardSession_Success() {
        final int TOTAL_NUMBER_OF_THROWS = NUMBER_OF_FRAMES + STRIKE_BONUS_THROWS;
        final int EXPECTED_SCORE = 300;
        final List<Integer> throwsPinsKnocked = Collections.nCopies(TOTAL_NUMBER_OF_THROWS,
                NUMBER_OF_PINS);

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
        assertEquals(NUMBER_OF_PINS + STRIKE_BONUS_THROWS * NUMBER_OF_PINS, firstFrame.getScore());
        final FrameEntity lastFrame = resultSession.getFrames().getLast();
        assertEquals(EXPECTED_SCORE, lastFrame.getScore());

        final ThrowEntity firstThrow = resultSession.getFrames().getFirst().getFrameThrows().getFirst();
        assertFalse(CollectionUtils.isEmpty(resultSession.getFrames().getFirst().getFrameThrows()));
        assertEquals(NUMBER_OF_PINS, firstThrow.getPinsKnocked());
        assertEquals(ThrowType.STRIKE, firstThrow.getType());
    }


    @Test
    void testZeroStandardSession_Success() {
        final int EXPECTED_SCORE = 0;
        final Map<Integer, List<Integer>> throwMap = new LinkedHashMap<>();
        throwMap.put(1, List.of(0, 0));
        throwMap.put(2, List.of(0, 0));
        throwMap.put(3, List.of(0, 0));
        throwMap.put(4, List.of(0, 0));
        throwMap.put(5, List.of(0, 0));
        throwMap.put(6, List.of(0, 0));
        throwMap.put(7, List.of(0, 0));
        throwMap.put(8, List.of(0, 0));
        throwMap.put(9, List.of(0, 0));
        throwMap.put(10, List.of(0, 0));
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
    void testMixedStandardSession_Success() {
        final int EXPECTED_SCORE = 71;
        final Map<Integer, List<Integer>> throwMap = new LinkedHashMap<>();
        throwMap.put(1, List.of(8, 1));
        throwMap.put(2, List.of(8, 2));
        throwMap.put(3, List.of(1, 2));
        throwMap.put(4, List.of(10));
        throwMap.put(5, List.of(1, 2));
        throwMap.put(6, List.of(1, 2));
        throwMap.put(7, List.of(1, 2));
        throwMap.put(8, List.of(1, 2));
        throwMap.put(9, List.of(1, 2));
        throwMap.put(10, List.of(1, 9, 10));
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
