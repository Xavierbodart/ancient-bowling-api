package be.telemis.games.bowling.model.playingsession;

import lombok.Getter;

@Getter
public enum PlayingSessionStatus {
    INITIALIZED,
    IN_PROGRESS,
    ACTIVE,
    FINISHED;
}
