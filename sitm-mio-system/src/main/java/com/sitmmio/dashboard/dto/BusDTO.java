package com.sitmmio.dashboard.dto;

public class BusDTO {

    private String id;
    private double lat;
    private double lng;
    private String route;

    public BusDTO() {}

    public BusDTO(String id, double lat, double lng, String route) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.route = route;
    }

    public String getId() {
        return id;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getRoute() {
        return route;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setRoute(String route) {
        this.route = route;
    }
}