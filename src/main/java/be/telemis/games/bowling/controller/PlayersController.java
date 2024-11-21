package be.telemis.games.bowling.controller;


import be.telemis.games.bowling.mapper.PlayerMapper;
import be.telemis.games.bowling.model.base.ResultObject;
import be.telemis.games.bowling.model.player.PlayerCO;
import be.telemis.games.bowling.model.player.PlayerEntity;
import be.telemis.games.bowling.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/players")
public class PlayersController extends AbstractController {

    private final PlayerService playerService;
    private final PlayerMapper playerMapper;

    @Autowired
    public PlayersController(PlayerService playerService, PlayerMapper playerMapper) {
        super("Players management");
        this.playerService = playerService;
        this.playerMapper = playerMapper;
    }

    @PostMapping
    public ResultObject<PlayerCO> createPlayer(@Valid @RequestBody PlayerCO playerCO) {
        PlayerEntity playerEntity = playerMapper.mapFromCO(playerCO);
        playerEntity = playerService.createPlayer(playerEntity);
        return mapToResultObject(playerMapper.mapToCO(playerEntity));
    }

    @PutMapping("/{playerId}")
    public ResultObject<PlayerCO> updatePlayer(@PathVariable("playerId") Integer playerId,
                                               @Valid @RequestBody PlayerCO playerCO) {
        PlayerEntity playerInputEntity = playerMapper.mapFromCO(playerCO);
        PlayerEntity playerEntity = playerService.updatePlayer(playerId, playerInputEntity);
        return mapToResultObject(playerMapper.mapToCO(playerEntity));
    }

    @GetMapping("/{playerId}")
    public ResultObject<PlayerCO> getPlayer(@PathVariable("playerId") Integer playerId) {
        return mapToResultObject(playerMapper.mapToCO(playerService.getPlayer(playerId)));
    }
}
