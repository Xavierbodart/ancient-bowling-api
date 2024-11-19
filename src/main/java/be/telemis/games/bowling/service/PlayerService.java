package be.telemis.games.bowling.service;

import be.telemis.games.bowling.dao.PlayerRepository;
import be.telemis.games.bowling.mapper.PlayerMapper;
import be.telemis.games.bowling.model.game.PlayerCO;
import be.telemis.games.bowling.model.game.PlayerEntity;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class PlayerService {
    private static final Logger logger = LoggerFactory.getLogger(PlayerService.class);

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;

    public PlayerCO createPlayer(PlayerCO playerCO) {
        final PlayerEntity playerEntity = playerMapper.mapFromCO(playerCO);
        return playerMapper.mapToCO(playerRepository.save(playerEntity));
    }

    public PlayerCO getPlayer(Integer playerId) {
        return playerMapper.mapToCO(playerRepository.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Player not found with id: " + playerId)));
    }
}
