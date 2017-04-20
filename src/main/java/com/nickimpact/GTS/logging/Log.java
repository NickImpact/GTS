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
        this.log = "";
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
                "  Name: {{pokemon}}",
                "  Nickname: {{nickname}}",
                "  Level: {{level}}",
                "  Shiny: {{shiny_tf}}",
                "  Ability: {{ability}}",
                "  Nature: {{nature}}",
                "  Gender: {{gender}}",
                "  Growth: {{growth}}",
                "  EV%: {{EV%}}",
                "  IV%: {{IV%}}"
        );
    }

    public static List<String> expiresLog(){
        return Lists.newArrayList(
                "Pokemon:",
                "  Name: {{pokemon}}",
                "  Nickname: {{nickname}}",
                "  Level: {{level}}",
                "  Shiny: {{shiny_tf}}",
                "  Ability: {{ability}}",
                "  Nature: {{nature}}",
                "  Gender: {{gender}}",
                "  Growth: {{growth}}",
                "  EV%: {{EV%}}",
                "  IV%: {{IV%}}"
        );
    }

    public static List<String> removalLog(){
        return Lists.newArrayList(
                "Pokemon:",
                "  Name: {{pokemon}}",
                "  Nickname: {{nickname}}",
                "  Level: {{level}}",
                "  Shiny: {{shiny_tf}}",
                "  Ability: {{ability}}",
                "  Nature: {{nature}}",
                "  Gender: {{gender}}",
                "  Growth: {{growth}}",
                "  EV%: {{EV%}}",
                "  IV%: {{IV%}}"
        );
    }

    public static List<String> purchaseLog(int person){
        if(person == 1)
            return Lists.newArrayList(
                    "Buyer: {{buyer}}",
                    "Money Earned: {{curr_symbol}}{{price}}",
                    "Pokemon Sold: {{pokemon}}"

            );
        else
            return Lists.newArrayList(
                    "Seller: {{seller}}",
                    "Money Spent: {{curr_symbol}}{{price}}",
                    "Pokemon Received: {{pokemon}}"
            );
    }

    public static List<String> auctionLog(int person){
        if(person == 1)
            return Lists.newArrayList(
                "Winner: {{buyer}}",
                "Money Earned: {{curr_symbol}}{{price}}",
                "Pokemon Sold: {{pokemon}}"

            );
        else
            return Lists.newArrayList(
                "Seller: {{seller}}",
                "Money Spent: {{curr_symbol}}{{price}}",
                "Pokemon Received: {{pokemon}}"
            );
    }

    public static List<String> tradeLog(int person){
        if(person == 1)
            return Lists.newArrayList(
                "Partner: {{player}}",
                "Traded: {{pokemon}}",
                "Received: {{poke_looked_for}}"
            );
        else
            return Lists.newArrayList(
                "Partner: {{player}}",
                "Traded: {{poke_looked_for}}",
                "Received: {{pokemon}}"
            );
    }
}
