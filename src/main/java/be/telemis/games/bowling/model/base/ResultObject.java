package be.telemis.games.bowling.model.base;

import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Data
public class ResultObject<T> {
    private String appName;
    private Boolean success;
    private int count = 0;
    private T result;
    private List<ErrorMessage> errorMessages;

    protected ResultObject() {
    }

    protected ResultObject(String appName, Boolean success) {
        this.appName = appName;
        this.success = success;
    }

    public ResultObject(String appName, Boolean success, T result) {
        this(appName, success);
        this.count = switch (result) {
            case Collection<?> collection -> collection.size();
            case Map<?, ?> map -> map.size();
            case null -> 0;
            default -> 1;
        };
        this.result = result;
    }

    public ResultObject(String appName, Boolean success, T result, List<ErrorMessage> errorMessages) {
        this(appName, success, result);
        this.errorMessages = errorMessages;
    }

}