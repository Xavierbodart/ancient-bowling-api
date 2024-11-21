package be.telemis.games.bowling.service;

import be.telemis.games.bowling.dao.PlayerRepository;
import be.telemis.games.bowling.model.player.PlayerEntity;
import jakarta.persistence.EntityExistsException;
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

    public PlayerEntity createPlayer(PlayerEntity playerEntity) {
        playerEntity.setId(null);
        if (playerRepository.findByName(playerEntity.getName()).isPresent()) {
            throw new EntityExistsException("This player already exists");
        }
        return playerRepository.save(playerEntity);
    }

    public PlayerEntity updatePlayer(Integer playerId, PlayerEntity playerEntity) {
        if (!playerRepository.existsById(playerId)) {
            throw new EntityExistsException("This player already exists");
        }
        return playerRepository.save(playerEntity);
    }

    public PlayerEntity getPlayer(Integer playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Player not found with id: " + playerId));
    }
}
