package be.telemis.games.bowling.model.game;

import lombok.Getter;

@Getter
public enum GameStatus {
    INITIALIZED,
    IN_PROGRESS,
    FINISHED;
}
