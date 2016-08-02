package com.lemus.oscar.mobilerobot;

/**
 * Created by olemu on 28/07/2016.
 */
public class Coordinate {
    private String longitude;
    private String latitude;
    private String nameC;
    private String description;

    public Coordinate(String nameC, String description, String latitude, String longitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.nameC = nameC;
        this.description = description;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getNameC() {
        return nameC;
    }

    public void setNameC(String name) {
        this.nameC = nameC;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
