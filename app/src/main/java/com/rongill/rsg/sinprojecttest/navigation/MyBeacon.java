package com.rongill.rsg.sinprojecttest.navigation;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rongill.rsg.sinprojecttest.basic_objects.MyCalendar;
import com.rongill.rsg.sinprojecttest.navigation.Point;

import java.io.Serializable;
import java.util.Objects;

public class MyBeacon implements Serializable, Comparable{

    private String name, MACaddress, floor, structure;
    private int rssi;
    private Point coordinates;
    private MyCalendar dateModified;

    public MyBeacon(){}
    public MyBeacon(String name, String MACaddress, int rssi, Point coordinates, String floor) {
        this.name = name;
        this.MACaddress = MACaddress;
        this.rssi = rssi;
        this.coordinates = coordinates;
        this.floor = floor;
        this.dateModified = new MyCalendar();
    }
    public MyBeacon(MyBeacon other){
        this.name = other.getName();
        this.MACaddress = other.getMACaddress();
        this.floor = other.getFloor();
        this.rssi = other.getRssi();
        this.coordinates = new Point(other.getCoordinates());
        this.structure = other.getStructure();
        this.dateModified = other.getDateModified();
    }

    public MyCalendar getDateModified() {
        return dateModified;
    }
    public void setDateModified(MyCalendar dateModified) {
        this.dateModified = dateModified;
    }
    public void setStructure(String structure){
        this.structure = structure;
    }
    public String getStructure(){
        return this.structure;
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

    public void getBeaconDetailsDB(){
        DatabaseReference beaconRef = FirebaseDatabase.getInstance().getReference()
                .child("beacons");
        Query beaconByNameQuery = beaconRef.orderByChild("name").equalTo(this.getName());
        beaconByNameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    structure = ds.getValue(MyBeacon.class).getStructure();
                    floor = ds.getValue(MyBeacon.class).getFloor();
                    Point p = new Point();
                    p.setX(Integer.parseInt(ds.child("x").getValue().toString()));
                    p.setY(Integer.parseInt(ds.child("y").getValue().toString()));
                    setCoordinates(p);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MyBeacon)) return false;
        MyBeacon myBeacon = (MyBeacon) o;
        return getRssi() == myBeacon.getRssi() &&
                Objects.equals(getName(), myBeacon.getName()) &&
                Objects.equals(getMACaddress(), myBeacon.getMACaddress()) &&
                Objects.equals(getFloor(), myBeacon.getFloor()) &&
                Objects.equals(getStructure(), myBeacon.getStructure()) &&
                Objects.equals(getCoordinates(), myBeacon.getCoordinates()) &&
                Objects.equals(getDateModified(), myBeacon.getDateModified());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getMACaddress(), getFloor(), getStructure(), getRssi(), getCoordinates(), getDateModified());
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
