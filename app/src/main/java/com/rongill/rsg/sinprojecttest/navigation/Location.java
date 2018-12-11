package com.rongill.rsg.sinprojecttest.navigation;

import java.io.Serializable;
import java.util.Objects;

public class Location implements Serializable {


    private String name, category, beacon;

    private Point coordinates;

    public Location(){}

    public Location(String name, String category, String beacon, Point coordinates){
        this.name = name;
        this.category = category;
        this.beacon = beacon;
        this.coordinates = new Point(coordinates);
    }

    public Location(Location other){
        this.name = other.getName();
        this.category = other.getCategory();
        this.beacon = other.getBeacon();
        this.coordinates = new Point(other.getCoordinates());
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
        return this.beacon;
    }

    public void setBeacon(String beacon) {
        this.beacon = beacon;
    }

    public Point getCoordinates() {
        return this.coordinates;
    }

    public void setCoordinates(Point coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        Location location = (Location) o;
        return Objects.equals(getName(), location.getName()) &&
                Objects.equals(getCategory(), location.getCategory()) &&
                Objects.equals(getBeacon(), location.getBeacon()) &&
                Objects.equals(getCoordinates(), location.getCoordinates());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getName(), getCategory(), getBeacon(), getCoordinates());
    }
}
