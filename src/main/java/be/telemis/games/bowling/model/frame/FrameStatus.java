package be.telemis.games.bowling.model.frame;

import lombok.Getter;

@Getter
public enum FrameStatus {
    CREATED,
    COMPLETED,
    EXTENDED,
    CLOSED;
}
