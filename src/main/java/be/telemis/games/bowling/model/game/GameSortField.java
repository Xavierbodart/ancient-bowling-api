package be.telemis.games.bowling.model.game;

public enum GameSortField {
    NAME("name"),
    CREATION_DATE("creation_date"),
    MODIFICATION_DATE("modification_date"),
    STATUS("status");

    private final String field;

    GameSortField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

}
