package be.telemis.games.bowling.dao;

import be.telemis.games.bowling.model.player.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerEntity, Integer> {

    Optional<PlayerEntity> findByName(String name);
}
