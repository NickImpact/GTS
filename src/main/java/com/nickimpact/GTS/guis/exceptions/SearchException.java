package com.nickimpact.GTS.guis.exceptions;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class SearchException extends Exception{

    private final String message;

    public SearchException(String result){
        this.message = result;
    }

    public Text getResult(){
        return Text.of(TextColors.RED, this.message);
    }
}
