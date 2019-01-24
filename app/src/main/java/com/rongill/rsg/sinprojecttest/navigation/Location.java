package com.rongill.rsg.sinprojecttest.navigation;

import com.rongill.rsg.sinprojecttest.basic_objects.MyCalendar;

import java.io.Serializable;
import java.util.Objects;

public class Location implements Serializable {


    private String name, category, beaconName, structure, floor;
    private MyCalendar dateModified;
    private Point coordinates;

    public Location(){}

    public Location(String name, String category, String beacon, String structure, String floor, Point coordinates){
        this.name = name;
        this.category = category;
        this.beaconName = beacon;
        this.structure = structure;
        this.floor = floor;
        this.coordinates = new Point(coordinates);
    }

    public Location(Location other){
        this.name = other.getName();
        this.category = other.getCategory();
        this.beaconName = other.getBeaconName();
        this.structure = other.getStructure();
        this.floor = other.getFloor();
        this.coordinates = new Point(other.getCoordinates());
        this.dateModified = other.getDateModified();
    }

    public MyCalendar getDateModified() {
        return dateModified;
    }
    public void setDateModified(MyCalendar dateModified) {
        this.dateModified = dateModified;
    }
    public String getStructure(){return structure;}
    public void setStructure(String structure) {
        this.structure = structure;
    }
    public String getFloor() {
        return floor;
    }
    public void setFloor(String floor) {
        this.floor = floor;
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
    public String getBeaconName() {
        return this.beaconName;
    }
    public void setBeaconName(String beacon) {
        this.beaconName = beacon;
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
                Objects.equals(getBeaconName(), location.getBeaconName()) &&
                Objects.equals(getCoordinates(), location.getCoordinates());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getName(), getCategory(), getBeaconName(), getCoordinates());
    }
}
