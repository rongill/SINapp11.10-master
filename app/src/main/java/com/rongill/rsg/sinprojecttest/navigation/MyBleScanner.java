package com.rongill.rsg.sinprojecttest.navigation;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyBleScanner {

    private BluetoothLeScanner scanner;
    private ScanSettings scanSettings;
    private ArrayList<MyBeacon> scannedDeviceList = new ArrayList<>();
    private MyBeacon closestBeacon;

    public MyBleScanner(){}

    //TODO use the scanCallback here for all scanning UC's
    public MyBleScanner(BluetoothManager bluetoothManager){
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        //Create the scan settings
        ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
        //Set scan latency mode. Lower latency, faster device detection/more battery and resources consumption
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        //Wrap settings together and save on a settings var
        scanSettings = scanSettingsBuilder.build();
        //Get the BLE scanner from the BT adapter
        this.scanner = bluetoothAdapter.getBluetoothLeScanner();

        closestBeacon = new MyBeacon();
    }

    public ArrayList<MyBeacon> getScannedDeviceList() {
        return scannedDeviceList;
    }

    public void setScannedDeviceList(ArrayList<MyBeacon> scannedDeviceList) {
        this.scannedDeviceList = scannedDeviceList;
    }

    public BluetoothLeScanner getScanner() {
        return scanner;
    }

    public void setScanner(BluetoothLeScanner scanner) {
        this.scanner = scanner;
    }

    public void initLeScan(boolean state){
        if(state){
            scanner.startScan(null, this.scanSettings, this.scanCallback);
        } else{
            scanner.stopScan(this.scanCallback);
        }
    }


    public MyBeacon getClosestBeacon(){

        return this.closestBeacon;
    }

    public void setClosestBeacon(MyBeacon closestBeacon) {
        this.closestBeacon = closestBeacon;
    }

    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if(result.getDevice().getName() != null && result.getDevice().getName().contains("SIN")){
                MyBeacon scannedBeacon = new MyBeacon();
                scannedBeacon.setMACaddress(result.getDevice().getAddress());
                scannedBeacon.setName(result.getDevice().getName());
                scannedBeacon.setRssi(result.getRssi());

                getBeaconDataFromServer(scannedBeacon);


            }
        }
    };

    private void getBeaconDataFromServer(final MyBeacon scannedBeacon){
        DatabaseReference beaconRef = FirebaseDatabase.getInstance().getReference()
                .child("beacons");
        Query beaconByNameQuery = beaconRef.orderByChild("name").equalTo(scannedBeacon.getName());
        beaconByNameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean contains = false;
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    scannedBeacon.setStructure(ds.getValue(MyBeacon.class).getStructure());
                    scannedBeacon.setFloor(ds.getValue(MyBeacon.class).getFloor());
                    Point p = new Point();
                    p.setX(Integer.parseInt(ds.child("x").getValue().toString()));
                    p.setY(Integer.parseInt(ds.child("y").getValue().toString()));
                    scannedBeacon.setCoordinates(p);
                }

                //if scanned device already in the list, update it.
                for (int i = 0; i < getScannedDeviceList().size(); i++) {
                    if (getScannedDeviceList().get(i).getMACaddress().contains(scannedBeacon.getMACaddress())) {
                        contains = true;
                        getScannedDeviceList().get(i).setRssi(scannedBeacon.getRssi());
                    }
                }

                //add to the list all the devices scanned that has "SIN" string in the name.
                if (!contains) {
                    getScannedDeviceList().add(scannedBeacon);
                }

                setClosestBeacon();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //put the closest beacon in the head of the list
    private void setClosestBeacon() {
        for (MyBeacon tempBeacon: getScannedDeviceList()){
            if(closestBeacon.getRssi() < tempBeacon.getRssi()){
                closestBeacon = tempBeacon;
            }
        }
    }
}
