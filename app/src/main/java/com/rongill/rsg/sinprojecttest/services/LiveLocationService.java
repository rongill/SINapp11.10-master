package com.rongill.rsg.sinprojecttest.services;

import android.app.Service;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.rongill.rsg.sinprojecttest.basic_objects.User;
import com.rongill.rsg.sinprojecttest.navigation.Compass;
import com.rongill.rsg.sinprojecttest.navigation.Location;
import com.rongill.rsg.sinprojecttest.navigation.MyBeacon;
import com.rongill.rsg.sinprojecttest.navigation.MyBleScanner;
import com.rongill.rsg.sinprojecttest.navigation.StaticIndoorNavigation;

public class LiveLocationService extends Service {
    private final String TAG = "DynamicNavService";
    private User currentUser;
    MyBleScanner myBleScanner;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        currentUser = (User)intent.getSerializableExtra("CURRENT_USER");
        myBleScanner = new MyBleScanner((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE));
        myBleScanner.initLeScan(scanCallback, true);

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                myBleScanner.initLeScan(scanCallback, false);
                //TODO make notification message when live location has stopped.
            }
        };

        //will stop the scan after 10min
        handler.postDelayed(runnable, 600000);


        return super.onStartCommand(intent, flags, startId);
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if(result.getDevice().getName() != null && result.getDevice().getName().contains("SIN")){
                MyBeacon scannedBeacon = new MyBeacon();
                scannedBeacon.setMACaddress(result.getDevice().getAddress());
                scannedBeacon.setName(result.getDevice().getName());
                scannedBeacon.setRssi(result.getRssi());

                myBleScanner.setBeaconDataFromServer(scannedBeacon);
                currentUser.setCurrentBeacon(myBleScanner.getNearestBeacon());
            }
        }
    };

    public LiveLocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
      //  throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }
}
