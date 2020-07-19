package com.king.tovisit_raghav_c0780996_android.helperClass;

import java.io.Serializable;

public class Places implements Serializable
{

    private String name;
    private Boolean isVisited;
    private int id;
    private Double lat, lng;

    public Places(int id, String name, Boolean isVisited, Double lat, Double lng) {
        this.name = name;
        this.isVisited = isVisited;
        this.lat = lat;
        this.lng = lng;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getVisited() {
        return isVisited;
    }

    public void setVisited(Boolean visited) {
        isVisited = visited;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

}
