package be.telemis.games.bowling.controller;


import be.telemis.games.bowling.model.base.ResultObject;
import be.telemis.games.bowling.model.game.PlayerCO;
import be.telemis.games.bowling.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/players")
public class PlayersController extends AbstractController {

    private final PlayerService playerService;

    @Autowired
    public PlayersController(PlayerService playerService) {
        super("Players management");
        this.playerService = playerService;
    }

    @PostMapping
    public ResultObject<PlayerCO> createPlayer(@Valid @RequestBody PlayerCO PlayerCO) {
        return mapToResultObject(playerService.createPlayer(PlayerCO));
    }

    @GetMapping("/{PlayerId}")
    public ResultObject<PlayerCO> getPlayer(@PathVariable("PlayerId") Integer PlayerId) {
        return mapToResultObject(playerService.getPlayer(PlayerId));
    }
}
