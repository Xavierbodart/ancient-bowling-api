package be.telemis.games.bowling.dao;

import be.telemis.games.bowling.model.game.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<GameEntity, Integer> {
}
