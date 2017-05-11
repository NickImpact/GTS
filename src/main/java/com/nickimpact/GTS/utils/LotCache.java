package com.nickimpact.GTS.utils;

import java.util.Date;

/**
 * Created by nickd on 5/10/2017.
 */
public class LotCache {

    private Lot lot;
    private boolean expired;
    private Date date;

    public LotCache(Lot lot, boolean expired, Date date){
        this.lot = lot;
        this.expired = expired;
        this.date = date;
    }


    public Lot getLot() {
        return lot;
    }

    public boolean isExpired() {
        return expired;
    }

    public Date getDate() {
        return date;
    }
}
