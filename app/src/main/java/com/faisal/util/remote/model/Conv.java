package com.faisal.util.remote.model;

/**
 * Create by Sk. Faisal
 * purpose: seen and timestamp display model
 * email:faisal.hossain.pk@gmail.com
 * github:https://github.com/resilientbd
 * Date: 03/07/2020
 */

public class Conv {
    public boolean seen;
    public long timeStamp;

    public Conv() {

    }

    public Conv(boolean seen, long timeStamp) {
        this.seen = seen;
        this.timeStamp = timeStamp;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
