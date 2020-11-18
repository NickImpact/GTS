package net.impactdev.gts.api.util;

public class WaitingInteger {

    private String fallback;
    private Integer result;

    public WaitingInteger(String fallback) {
        this.fallback = fallback;
    }

    public WaitingInteger(int result) {
        this.result = result;
    }

    @Override
    public String toString() {
        if(this.result != null) {
            return this.result.toString();
        }

        return this.fallback;
    }

}
