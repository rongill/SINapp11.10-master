package com.rongill.rsg.sinprojecttest.navigation;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanSettings;
import android.content.Context;

import java.util.ArrayList;

public class MyBleScanner {

    private BluetoothLeScanner scanner;
    private ScanSettings scanSettings;
    private ArrayList<MyBeacon> scannedDeviceList = new ArrayList<>();

    public MyBleScanner(){}

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

    public void initLeScan(ScanCallback scanCallback, boolean state){
        if(state){
            scanner.startScan(null, scanSettings, scanCallback);
        } else{
            scanner.stopScan(scanCallback);
        }
    }

    //find the closest beacon from the deviceList based on RSSI and return it
    public MyBeacon findClosestBeacon(){
        MyBeacon proximityMaxBeacon = scannedDeviceList.get(0);
        for(MyBeacon temp : scannedDeviceList){
            if(temp.getRssi() > proximityMaxBeacon.getRssi()){
                proximityMaxBeacon = temp;
            }
        }
        return proximityMaxBeacon;
    }
}
