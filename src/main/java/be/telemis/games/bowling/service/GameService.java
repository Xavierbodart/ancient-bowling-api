package be.telemis.games.bowling.service;

import be.telemis.games.bowling.constants.GameConstants;
import be.telemis.games.bowling.dao.GameRepository;
import be.telemis.games.bowling.dao.PlayingSessionRepository;
import be.telemis.games.bowling.model.base.PagingCriteria;
import be.telemis.games.bowling.model.game.GameEntity;
import be.telemis.games.bowling.model.game.GameSearchCriteria;
import be.telemis.games.bowling.model.game.GameStatus;
import be.telemis.games.bowling.model.playingsession.PlayingSessionEntity;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class GameService {
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    private final PlayingSessionService playingSessionService;
    private final GameRepository gameRepository;
    private final PlayingSessionRepository playingSessionRepository;

    public GameEntity getGame(Integer gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found"));
    }

    public GameEntity createGame(GameEntity game) {
        game.setId(null);
        game.setWinningPlayingSession(null);
        game.setActivePlayingSession(null);
        game.setStatus(GameStatus.INITIALIZED);
        return gameRepository.save(game);
    }

    public GameEntity startGame(GameEntity game) {
        final Integer gameId = game.getId();
        if(!GameStatus.INITIALIZED.equals(game.getStatus())){
            throw new IllegalStateException("Game has already been started");
        }

        List<PlayingSessionEntity> playingSessions = playingSessionRepository.findByGameId(gameId);
        if(CollectionUtils.isEmpty(playingSessions)){
            throw new IllegalStateException("No playing sessions found for game: " + gameId);
        }
        playingSessions = playingSessionService.startPlayingSessions(playingSessions);
        PlayingSessionEntity activePlayingSession =
                playingSessionService.selectFirstActivePlayingSession(gameId, playingSessions);

        game.setStatus(GameStatus.IN_PROGRESS);
        game.setActivePlayingSession(activePlayingSession);
        return gameRepository.save(game);
    }

    public List<GameEntity> searchGames(GameSearchCriteria searchCriteria) {
        final PagingCriteria pagingCriteria = searchCriteria.getPagingCriteria();
        final String sortField = searchCriteria.getSortField() != null ? searchCriteria.getSortField().getField() :
                GameConstants.GAME_SORT_FIELD_DEFAULT;
        final Sort.Direction sortDirection = searchCriteria.getSortDirection() != null ?
                searchCriteria.getSortDirection() : Sort.Direction.ASC;
        final Pageable pageable = PageRequest.of(pagingCriteria.getPage(), pagingCriteria.getPageSize(),
                Sort.by(sortDirection, sortField));
        return gameRepository.findByCriteria(pageable, searchCriteria.getName(), searchCriteria.getStatus() != null ?
                searchCriteria.getStatus().toString() : null, searchCriteria.getPlayerName());
    }

}
