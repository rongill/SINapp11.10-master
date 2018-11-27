package com.rongill.rsg.sinprojecttest.navigation;

import com.rongill.rsg.sinprojecttest.navigation.Point;

public class MyBeacon {

    private String name, MACaddress;
    private int rssi;
    private Point coordinates;

    public MyBeacon(){}
    public MyBeacon(String name, String MACaddress, int rssi, Point coordinates) {
        this.name = name;
        this.MACaddress = MACaddress;
        this.rssi = rssi;
        this.coordinates = coordinates;
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
}
