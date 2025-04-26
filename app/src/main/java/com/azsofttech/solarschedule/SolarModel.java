package com.azsofttech.solarschedule;

public class SolarModel {
    //WE HAVE DATE DIRECTLY FROM DEVICE
    String sunrise;
    String sunset;
    String daylength;
    String solarnoon;

    public String getSunrise() {
        return sunrise;
    }

    public void setSunrise(String sunrise) {
        this.sunrise = sunrise;
    }

    public String getSunset() {
        return sunset;
    }

    public void setSunset(String sunset) {
        this.sunset = sunset;
    }

    public String getDaylength() {
        return daylength;
    }

    public void setDaylength(String daylength) {
        this.daylength = daylength;
    }

    public String getSolarnoon() {
        return solarnoon;
    }

    public void setSolarnoon(String solarnoon) {
        this.solarnoon = solarnoon;
    }
}
