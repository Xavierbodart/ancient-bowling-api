package be.telemis.games.bowling.dao;

import be.telemis.games.bowling.model.playingsession.PlayingSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayingSessionRepository extends JpaRepository<PlayingSessionEntity, Integer> {

    List<PlayingSessionEntity> findByGameId(Integer gameId);
}
