package be.telemis.games.bowling.utils;

import be.telemis.games.bowling.factory.FrameEntityFactory;
import be.telemis.games.bowling.model.frame.ThrowEntity;
import be.telemis.games.bowling.model.game.GameEntity;
import be.telemis.games.bowling.model.game.GameStatus;
import be.telemis.games.bowling.model.player.PlayerEntity;
import be.telemis.games.bowling.model.playingsession.PlayingSessionEntity;
import be.telemis.games.bowling.model.playingsession.PlayingSessionStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

public class MockFactory {

    public static PlayerEntity createMockedPlayer(Integer id, String name) {
        PlayerEntity player = new PlayerEntity();
        player.setId(id);
        player.setName(name);
        return player;
    }

    public static PlayingSessionEntity createMockedSession(Integer id, PlayerEntity player, GameEntity game) {
        PlayingSessionEntity session = new PlayingSessionEntity();
        session.setId(id);
        session.setPlayer(player);
        session.setGame(game);
        session.setStatus(PlayingSessionStatus.INITIALIZED);
        session.setCreationDate(Date.from(Instant.now()));
        return session;
    }


    public static ThrowEntity createMockedThrow(int numberOfPinsKnocked) {
        ThrowEntity mockedThrow = new ThrowEntity();
        mockedThrow.setPinsKnocked(numberOfPinsKnocked);
        return mockedThrow;
    }

    public static GameEntity createMockedGame(Integer id, String name) {
        GameEntity game = new GameEntity();
        game.setId(id);
        game.setName(name);
        game.setStatus(GameStatus.INITIALIZED);
        return game;
    }
}
