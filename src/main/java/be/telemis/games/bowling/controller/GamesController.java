package be.telemis.games.bowling.controller;

import be.telemis.games.bowling.model.base.ResultObject;
import be.telemis.games.bowling.model.game.GameCO;
import be.telemis.games.bowling.model.game.PlayingSessionCO;
import be.telemis.games.bowling.model.game.ThrowCO;
import be.telemis.games.bowling.service.GameSessionService;
import be.telemis.games.bowling.service.ThrowService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/games")
public class GamesController extends AbstractController {

    private final GameSessionService gameSessionService;
    private final ThrowService throwService;

    @Autowired
    public GamesController(GameSessionService gameSessionService, ThrowService throwService) {
        super("Games management");
        this.gameSessionService = gameSessionService;
        this.throwService = throwService;
    }

    @PostMapping
    public ResultObject<GameCO> createGame(@Valid @RequestBody GameCO gameCO) {
        return mapToResultObject(gameSessionService.createGame(gameCO));
    }

    @GetMapping("/{gameId}")
    public ResultObject<GameCO> getGame(@PathVariable("gameId") Integer gameId) {
        return mapToResultObject(gameSessionService.getGame(gameId));
    }

    @PostMapping("/{gameId}/start")
    public ResultObject<GameCO> startGame(@PathVariable("gameId") Integer gameId) {
        return mapToResultObject(gameSessionService.startGame(gameId));
    }

    @GetMapping("/{gameId}/playing-sessions")
    public ResultObject<List<PlayingSessionCO>> getPlayingSessionsByGameId(@PathVariable("gameId") Integer gameId) {
        return mapToResultObject(gameSessionService.getPlayingSessionsByGameId(gameId));
    }

    @PostMapping("/{gameId}/playing-sessions")
    public ResultObject<PlayingSessionCO> createPlayingSession(@PathVariable("gameId") Integer gameId,
                                                               @Valid @RequestBody PlayingSessionCO playingSessionCO) {
        return mapToResultObject(gameSessionService.createPlayingSession(gameId, playingSessionCO));
    }

    @PostMapping("/{gameId}/playing-sessions/{playingSessionId}/throws")
    public ResultObject<PlayingSessionCO> createThrow(@PathVariable("gameId") Integer gameId,
                                                      @PathVariable("playingSessionId") Integer playingSessionId,
                                                      @Valid @RequestBody ThrowCO throwCO) {
        return mapToResultObject(throwService.createThrow(gameId, playingSessionId, throwCO));
    }
}
