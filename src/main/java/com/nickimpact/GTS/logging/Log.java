package com.nickimpact.GTS.logging;

import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by nickd on 4/17/2017.
 */
public class Log {

    private int id;
    private Date date;
    private UUID actor;
    private String action;
    private String log;

    public Log(int id, Date date, UUID actor, String action){
        this.id = id;
        this.date = date;
        this.action = action;
        this.actor = actor;
    }

    public int getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public UUID getActor() {
        return actor;
    }

    public String getAction() {
        return action;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public static List<String> additionLog(){
        return Lists.newArrayList(
                "Pokemon:",
                "Name: {{pokemon}}",
                "Nickname: {{nickname}}",
                "Level: {{level}}",
                "Shiny: {{shiny_tf}}",
                "Ability: {{ability}}",
                "Nature: {{nature}}",
                "Gender: {{gender}}",
                "Growth: {{growth}}",
                "EV%: {{EV%}}",
                "IV%: {{IV%}}"
        );
    }
}
