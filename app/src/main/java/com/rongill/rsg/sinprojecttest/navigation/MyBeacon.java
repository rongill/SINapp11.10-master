package com.rongill.rsg.sinprojecttest.navigation;

import com.rongill.rsg.sinprojecttest.navigation.Point;

import java.io.Serializable;

public class MyBeacon implements Serializable {

    private String name, MACaddress, floor;
    private int rssi;
    private Point coordinates;

    public MyBeacon(){}
    public MyBeacon(String name, String MACaddress, int rssi, Point coordinates, String floor) {
        this.name = name;
        this.MACaddress = MACaddress;
        this.rssi = rssi;
        this.coordinates = coordinates;
        this.floor = floor;
    }
    public MyBeacon(MyBeacon other){
        this.name = other.getName();
        this.MACaddress = other.getMACaddress();
        this.floor = other.getFloor();
        this.rssi = other.getRssi();
        this.coordinates = new Point(other.getCoordinates());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMACaddress() {
        return MACaddress;
    }

    public void setMACaddress(String MACaddress) {
        this.MACaddress = MACaddress;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public Point getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Point coordinates) {
        this.coordinates = coordinates;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }
}
