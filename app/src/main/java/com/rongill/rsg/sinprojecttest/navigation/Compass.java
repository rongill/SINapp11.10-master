package com.rongill.rsg.sinprojecttest.navigation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;

public class Compass implements SensorEventListener, Serializable {

    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float azimuth =0f;
    private float currentAzimuth =0f;
    private int oriantationNew;
    public ImageView stopNavBtn;

    public SensorManager mSensorManager;
    public TextView userLocationTv, titleTv;
    public ImageView compassImage;

    public Compass(){
    }
    public Compass(ImageView compassImage,ImageView stopNavBtn, SensorManager activitySensorManager ){
        this.compassImage = compassImage;
        mSensorManager = activitySensorManager;
        this.stopNavBtn = stopNavBtn;


    }

    public TextView getUserLocationTv() {
        return userLocationTv;
    }

    public void setUserLocationTv(TextView userLocationTv) {
        this.userLocationTv = userLocationTv;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final float alpha = 0.97f;

        synchronized (this){
            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                mGravity[0] = alpha*mGravity[0]+(1-alpha)*sensorEvent.values[0];
                mGravity[1] = alpha*mGravity[1]+(1-alpha)*sensorEvent.values[1];
                mGravity[2] = alpha*mGravity[2]+(1-alpha)*sensorEvent.values[2];
            }

            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                mGeomagnetic[0] = alpha*mGeomagnetic[0]+(1-alpha)*sensorEvent.values[0];
                mGeomagnetic[1] = alpha*mGeomagnetic[1]+(1-alpha)*sensorEvent.values[1];
                mGeomagnetic[2] = alpha*mGeomagnetic[2]+(1-alpha)*sensorEvent.values[2];
            }

            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if(success){
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = (float) Math.toDegrees(orientation[0]) + oriantationNew;
                azimuth = (azimuth+360)%360;//-offsetInt;

                Animation anim = new RotateAnimation(-currentAzimuth,-azimuth,Animation.RELATIVE_TO_SELF,
                        0.5f,Animation.RELATIVE_TO_SELF,0.5f);
                currentAzimuth = azimuth;

                anim.setDuration(500);
                anim.setRepeatCount(0);
                anim.setFillAfter(true);

                compassImage.startAnimation(anim);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
