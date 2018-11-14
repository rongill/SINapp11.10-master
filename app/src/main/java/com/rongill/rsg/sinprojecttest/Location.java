package com.rongill.rsg.sinprojecttest;

import java.io.Serializable;

public class Location implements Serializable {


    private String name, category, beacon;

    public Location(){}

    public Location(String name, String category, String beacon){
        this.name = name;
        this.category = category;
        this.beacon = beacon;
    }

    public Location(Location other){
        this.name = other.getName();
        this.category = other.getCategory();
        this.beacon = other.getBeacon();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBeacon() {
        return beacon;
    }

    public void setBeacon(String beacon) {
        this.beacon = beacon;
    }
}
