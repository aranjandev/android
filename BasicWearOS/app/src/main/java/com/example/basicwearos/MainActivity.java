package com.example.basicwearos;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Arrays;
import java.util.Vector;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import com.google.common.collect.EvictingQueue;

public class MainActivity extends WearableActivity implements SensorEventListener{

    private TextView mTextView;
    private static final String TAG = "MyActivity";
    private String mMesgAcc = "Acc: ";
    private String mMesgGyr = "Gyr:";
    private String mMesgGra = "Gra:";
    private String mMesgHR = "HR:";
    private static final int Q_SZ = 500;
    private EvictingQueue<Float> mAccXQ, mAccYQ, mAccZQ;
    private EvictingQueue<Float> mGyrXQ, mGyrYQ, mGyrZQ;
    private EvictingQueue<Float> mGvtXQ, mGvtYQ, mGvtZQ;
    DataSampleProcessor mDProc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);

        // initialize class data
        initDataQueues();
        mDProc = new DataSampleProcessor();

        // Enables Always-on
        setAmbientEnabled();
        Log.d(TAG, "My message: on create");
        getAccelerometerData();
        getGyroscopeData();
        getGravityData();
        getHRData();
    }

    private void initDataQueues(){
        mAccXQ = EvictingQueue.create(Q_SZ);
        mAccYQ = EvictingQueue.create(Q_SZ);
        mAccZQ = EvictingQueue.create(Q_SZ);
        mGyrXQ = EvictingQueue.create(Q_SZ);
        mGyrYQ = EvictingQueue.create(Q_SZ);
        mGyrZQ = EvictingQueue.create(Q_SZ);
        mGvtXQ = EvictingQueue.create(Q_SZ);
        mGvtYQ = EvictingQueue.create(Q_SZ);
        mGvtZQ = EvictingQueue.create(Q_SZ);
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

    @SuppressLint("DefaultLocale")
    private String handleAccData(float values[]){
        mAccXQ.add(values[0]);
        mAccYQ.add(values[1]);
        mAccZQ.add(values[2]);

        Vector<Float> stats = mDProc.calcMean(mAccXQ);
        String mMesgAcc;
        mMesgAcc = String.format("Acc: %.2f, %.2f, %.2f, %.2f",
                                        values[0],
                                        values[1],
                                        values[2],
                                        stats.elementAt(0));
        return mMesgAcc;
    }

    private String handleGyrData(float values[]){
        String mMesgGyr = "Gyr: " + String.format("%.1f", values[0]) + "," + String.format("%.1f", values[1]) + "," + String.format("%.1f", values[2]);
        mGyrXQ.add(values[0]);
        mGyrYQ.add(values[1]);
        mGyrZQ.add(values[2]);
        return mMesgGyr;
    }

    private String handleGvtData(float values[]){
        String mMesgGra = "Gra: " + String.format("%.1f", values[0]) + "," + String.format("%.1f", values[1]) + "," + String.format("%.1f", values[2]);
        mGvtXQ.add(values[0]);
        mGvtYQ.add(values[1]);
        mGvtZQ.add(values[2]);
        return mMesgGra;
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mMesgAcc = handleAccData(event.values);
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            mMesgGyr = handleGyrData(event.values);
        }
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            mMesgGra = handleGvtData(event.values);
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