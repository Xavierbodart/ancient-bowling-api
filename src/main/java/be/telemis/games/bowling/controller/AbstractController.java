package be.telemis.games.bowling.controller;


import be.telemis.games.bowling.model.base.ResultObject;

public abstract class AbstractController {

    protected String appName;

    public AbstractController(final String appName) {
        this.appName = appName;
    }

    public <C> ResultObject<C> mapToResultObject(C channelObject) {
        return new ResultObject<>(appName, true, channelObject);
    }

}