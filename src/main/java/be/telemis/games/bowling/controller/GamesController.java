package be.telemis.games.bowling.controller;

import be.telemis.games.bowling.mapper.GameMapper;
import be.telemis.games.bowling.mapper.PlayingSessionMapper;
import be.telemis.games.bowling.mapper.ThrowMapper;
import be.telemis.games.bowling.model.base.ResultObject;
import be.telemis.games.bowling.model.frame.ThrowCO;
import be.telemis.games.bowling.model.frame.ThrowEntity;
import be.telemis.games.bowling.model.game.GameCO;
import be.telemis.games.bowling.model.game.GameEntity;
import be.telemis.games.bowling.model.game.GameSearchCriteria;
import be.telemis.games.bowling.model.playingsession.PlayingSessionCO;
import be.telemis.games.bowling.model.playingsession.PlayingSessionEntity;
import be.telemis.games.bowling.service.GameService;
import be.telemis.games.bowling.service.PlayingSessionService;
import be.telemis.games.bowling.service.ThrowService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/games")
public class GamesController extends AbstractController {

    private final GameService gameService;
    private final PlayingSessionService playingSessionService;
    private final ThrowService throwService;

    private final GameMapper gameMapper;
    private final PlayingSessionMapper playingSessionMapper;
    private final ThrowMapper throwMapper;

    @Autowired
    public GamesController(GameService gameService, PlayingSessionService playingSessionService,
                           ThrowService throwService, GameMapper gameMapper,
                           PlayingSessionMapper playingSessionMapper, ThrowMapper throwMapper) {
        super("Games management");
        this.gameService = gameService;
        this.playingSessionService = playingSessionService;
        this.throwService = throwService;
        this.gameMapper = gameMapper;
        this.playingSessionMapper = playingSessionMapper;
        this.throwMapper = throwMapper;
    }

    @PostMapping
    public ResultObject<GameCO> createGame(@Valid @RequestBody GameCO gameCO) {
        final GameEntity gameEntityInput = gameMapper.mapFromCO(gameCO);
        final GameEntity gameEntity = gameService.createGame(gameEntityInput);
        return mapToResultObject(gameMapper.mapToCO(gameEntity));
    }

    @GetMapping
    public ResultObject<List<GameCO>> searchGames(@Valid @ParameterObject @ModelAttribute GameSearchCriteria searchCriteria) {
        List<GameEntity> games = gameService.searchGames(searchCriteria);
        return mapToResultObject(games.stream().map(gameMapper::mapToCO).toList());
    }

    @GetMapping("/{gameId}")
    public ResultObject<GameCO> getGame(@PathVariable("gameId") Integer gameId) {
        final GameEntity gameEntity = gameService.getGame(gameId);
        return mapToResultObject(gameMapper.mapToCO(gameEntity));
    }


    @PostMapping("/{gameId}/start")
    public ResultObject<GameCO> startGame(@PathVariable("gameId") Integer gameId) {
        GameEntity gameEntity = gameService.getGame(gameId);
        gameEntity = gameService.startGame(gameEntity);
        return mapToResultObject(gameMapper.mapToCO(gameEntity));
    }

    @GetMapping("/{gameId}/playing-sessions")
    public ResultObject<List<PlayingSessionCO>> getPlayingSessionsByGameId(@PathVariable("gameId") Integer gameId) {
        final GameEntity gameEntity = gameService.getGame(gameId);
        final List<PlayingSessionCO> playingSessionCOs =
                playingSessionService.getPlayingSessionsByGameId(gameEntity.getId())
                        .stream().map(playingSessionMapper::mapToCO).toList();
        return mapToResultObject(playingSessionCOs);
    }

    @PostMapping("/{gameId}/playing-sessions")
    public ResultObject<PlayingSessionCO> createPlayingSession(@PathVariable("gameId") Integer gameId,
                                                               @Valid @RequestBody PlayingSessionCO playingSessionCO) {
        final GameEntity gameEntity = gameService.getGame(gameId);
        PlayingSessionEntity playingSessionEntity = playingSessionMapper.mapFromCO(playingSessionCO);
        playingSessionEntity = playingSessionService.createPlayingSession(playingSessionEntity, gameEntity);
        return mapToResultObject(playingSessionMapper.mapToCO(playingSessionEntity));
    }

    @GetMapping("/{gameId}/playing-sessions/{playingSessionId}")
    public ResultObject<PlayingSessionCO> getPlayingSessionsById(@PathVariable("gameId") Integer gameId,
                                                                 @PathVariable("playingSessionId") Integer playingSessionId) {
        final GameEntity gameEntity = gameService.getGame(gameId);
        final PlayingSessionEntity playingSessionEntity =
                playingSessionService.getPlayingSessionsById(gameEntity, playingSessionId);
        return mapToResultObject(playingSessionMapper.mapToCO(playingSessionEntity));
    }

    @PostMapping("/{gameId}/playing-sessions/{playingSessionId}/throws")
    public ResultObject<PlayingSessionCO> createThrow(@PathVariable("gameId") Integer gameId,
                                                      @PathVariable("playingSessionId") Integer playingSessionId,
                                                      @Valid @RequestBody ThrowCO throwCO) {
        final GameEntity gameEntity = gameService.getGame(gameId);
        final ThrowEntity throwEntity = throwMapper.mapFromCO(throwCO);
        PlayingSessionEntity playingSessionEntity = playingSessionService.getPlayingSessionsById(gameEntity, playingSessionId);
        playingSessionEntity = throwService.createThrow(gameEntity, playingSessionEntity, throwEntity);
        return mapToResultObject(playingSessionMapper.mapToCO(playingSessionEntity));
    }
}
