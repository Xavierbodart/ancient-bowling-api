package be.telemis.games.bowling.dao;

import be.telemis.games.bowling.model.game.FrameEntity;
import be.telemis.games.bowling.model.game.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FrameRepository extends JpaRepository<FrameEntity, Integer> {
}
