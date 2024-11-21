package be.telemis.games.bowling.model.game;

import be.telemis.games.bowling.model.base.PagingCriteria;
import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
public class GameSearchCriteria {

    private PagingCriteria pagingCriteria;
    private GameSortField sortField;
    private Sort.Direction sortDirection;
    private String name;
    private GameStatus status;
    private String playerName;
}
