package be.telemis.games.bowling.model.game;

import lombok.Getter;

@Getter
public enum PlayingSessionStatus {
    INITIALIZED,
    ACTIVE,
    WAITING,
    FINISHED;
}
