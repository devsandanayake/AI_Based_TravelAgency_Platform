package com.example.traveling;

public class ScamLocation {
    private String scamLocation;
    private Double latitude;
    private Double longitude;
    private float rating;

    // Constructor
    public ScamLocation(String scamLocation, Double latitude, Double longitude, float rating) {
        this.scamLocation = scamLocation;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
    }

    // Getters and Setters for the fields
    public String getLocation() {
        return scamLocation;
    }

    public void setLocation(String scamLocation) {
        this.scamLocation = scamLocation;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
