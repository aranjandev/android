package com.example.basicwearos;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends WearableActivity implements SensorEventListener{

    private TextView mTextView;
    private static final String TAG = "MyActivity";
    private String mMesgAcc = "Acc: ";
    private String mMesgGyr = "Gyr:";
    private String mMesgGra = "Gra:";
    private String mMesgHR = "HR:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);

        // Enables Always-on
        setAmbientEnabled();
        Log.d(TAG, "My message: on create");
        getAccelerometerData();
        getGyroscopeData();
        getGravityData();
        getHRData();
    }

    private void getAccelerometerData() {
        SensorManager mSensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));
        Sensor mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if(mAccelerometerSensor != null){
            Log.d(TAG, "*********** Accelerometer found!");
        }
        mSensorManager.registerListener((SensorEventListener) this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    private void getGyroscopeData() {
        SensorManager mSensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));
        Sensor mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        if(mAccelerometerSensor != null){
            Log.d(TAG, "*********** Gyroscope found!");
        }
        mSensorManager.registerListener((SensorEventListener) this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    private void getGravityData() {
        SensorManager mSensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));
        Sensor mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        if(mGravitySensor != null){
            Log.d(TAG, "*********** Gravity sensor found!");
        }
        mSensorManager.registerListener((SensorEventListener) this, mGravitySensor, SensorManager.SENSOR_DELAY_GAME);
    }

    private void getHRData() {
        SensorManager mSensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));
        Sensor mHRSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        if(mHRSensor != null){
            Log.d(TAG, "*********** HR sensor found!");
        }
        mSensorManager.registerListener((SensorEventListener) this, mHRSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private String currentTimeStr() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        return df.format(c.getTime());
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mMesgAcc = "Acc: " + String.format("%.1f", event.values[0]) + "," + String.format("%.1f", event.values[1]) + "," + String.format("%.1f", event.values[2]);
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            mMesgGyr = "Gyr: " + String.format("%.1f", event.values[0]) + "," + String.format("%.1f", event.values[1]) + "," + String.format("%.1f", event.values[2]);
        }
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            mMesgGra = "Gra: " + String.format("%.1f", event.values[0]) + "," + String.format("%.1f", event.values[1]) + "," + String.format("%.1f", event.values[2]);
        }
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            mMesgHR = "HR: " + String.format("%d", (int)event.values[0]);
        }

        String msg = mMesgAcc + "\n" + mMesgGyr + "\n" + mMesgGra + "\n" + mMesgHR;
        mTextView.setText(msg);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}