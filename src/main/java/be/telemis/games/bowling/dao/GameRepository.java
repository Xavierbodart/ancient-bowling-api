package be.telemis.games.bowling.dao;

import be.telemis.games.bowling.model.game.GameEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<GameEntity, Integer> {

    @Query(value = """
            SELECT g.* FROM games g
            LEFT JOIN playing_sessions s on g.winning_playing_session_id = s.id
            LEFT JOIN players p on p.id = s.player_id
               WHERE (:name IS NULL OR g.name like %:name%)
               AND (:status IS NULL OR g.status = :status)
               AND (:playerName IS NULL OR p.name = :playerName)
            """, nativeQuery = true)
    List<GameEntity> findByCriteria(Pageable pageable, @Param("name") String name, @Param("status") String status,
                                    @Param("playerName") String playerName);
}
