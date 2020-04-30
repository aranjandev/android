package com.example.basicwearos;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.util.Pair;

import java.util.Vector;
import java.util.HashMap;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import com.google.common.collect.EvictingQueue;

public class MainActivity extends WearableActivity implements SensorEventListener{

    private TextView mTextView;
    private static final String TAG = "MyActivity";
    private HashMap sensorMessages;
    private HashMap sensorMessagePrefix;
    private static final int Q_SZ = 500;
    // Map to store the starting Q index and number of Qs for a given sensor
    private HashMap sensorDataIndex;
    private Vector<EvictingQueue<Float>> allDataQ;
//    DataSampleProcessor mDProc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        mDProc = new DataSampleProcessor();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.text);
        initSensorMessages();
        // initialize all Qs for data
        initAllDataQ();
        // init all sensors to start getting data
        initAllSensors();
        // Enables Always-on
        setAmbientEnabled();
    }

    private void initSensorMessages(){
        sensorMessagePrefix = new HashMap();
        sensorMessages = new HashMap();
        sensorMessagePrefix.put(Sensor.TYPE_ACCELEROMETER, "Acc: ");
        sensorMessages.put(Sensor.TYPE_ACCELEROMETER, sensorMessagePrefix.get(Sensor.TYPE_ACCELEROMETER));

        sensorMessagePrefix.put(Sensor.TYPE_GYROSCOPE, "Gyr: ");
        sensorMessages.put(Sensor.TYPE_GYROSCOPE, sensorMessagePrefix.get(Sensor.TYPE_GYROSCOPE));

        sensorMessagePrefix.put(Sensor.TYPE_GRAVITY, "Gra: ");
        sensorMessages.put(Sensor.TYPE_GRAVITY, sensorMessagePrefix.get(Sensor.TYPE_GRAVITY));

        sensorMessagePrefix.put(Sensor.TYPE_HEART_RATE, "HR: ");
        sensorMessages.put(Sensor.TYPE_HEART_RATE, sensorMessagePrefix.get(Sensor.TYPE_HEART_RATE));
    }

    private void initAllDataQ(){
        allDataQ = new Vector<EvictingQueue<Float>>();
        sensorDataIndex = new HashMap();
        sensorDataIndex.put(Sensor.TYPE_ACCELEROMETER, new Pair<Integer, Integer>(0, 3));
        for(int i=0; i<3; i++) {
            allDataQ.add(EvictingQueue.<Float>create(Q_SZ));
        }
        sensorDataIndex.put(Sensor.TYPE_GYROSCOPE, new Pair<Integer, Integer>(3, 3));
        for(int i=0; i<3; i++) {
            allDataQ.add(EvictingQueue.<Float>create(Q_SZ));
        }
        sensorDataIndex.put(Sensor.TYPE_GRAVITY, new Pair<Integer, Integer>(6, 3));
        for(int i=0; i<3; i++) {
            allDataQ.add(EvictingQueue.<Float>create(Q_SZ));
        }
        sensorDataIndex.put(Sensor.TYPE_HEART_RATE, new Pair<Integer, Integer>(9, 1));
        allDataQ.add(EvictingQueue.<Float>create(Q_SZ));
    }

    private void updateSensorMessage(int sensorType, String message){
        sensorMessages.put(sensorType, sensorMessagePrefix.get(sensorType) + message);
    }

    private void registerSensor(int sensorType, int delay){
        SensorManager sensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));
        Sensor sensor = sensorManager.getDefaultSensor(sensorType);
        if(sensor != null){
            Log.d(TAG, "*********** !" + sensorType);
        }
        sensorManager.registerListener((SensorEventListener) this, sensor, delay);
    }

    private void initAllSensors(){
        registerSensor(Sensor.TYPE_ACCELEROMETER, SensorManager.SENSOR_DELAY_GAME);
        registerSensor(Sensor.TYPE_GYROSCOPE, SensorManager.SENSOR_DELAY_GAME);
        registerSensor(Sensor.TYPE_GRAVITY, SensorManager.SENSOR_DELAY_GAME);
        registerSensor(Sensor.TYPE_GRAVITY, SensorManager.SENSOR_DELAY_GAME);
        registerSensor(Sensor.TYPE_HEART_RATE, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private String currentTimeStr() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        return df.format(c.getTime());
    }

    private void handleSensorData(int sensorType, float values[]) {
        Pair<Integer, Integer> indNum = (Pair<Integer, Integer>) sensorDataIndex.get(sensorType);
        int startInd = indNum.first.intValue();
        String dataString = "";
        for(int i=0; i<indNum.second.intValue(); i++){
            allDataQ.elementAt(startInd + i).add(values[i]);
            dataString += String.format("%.2f, ", values[i]);
        }
        updateSensorMessage(sensorType, dataString);
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        handleSensorData(event.sensor.getType(), event.values);
        String viewString = (String) sensorMessages.get(Sensor.TYPE_ACCELEROMETER) + "\n" +
                            (String) sensorMessages.get(Sensor.TYPE_GYROSCOPE) + "\n" +
                            (String) sensorMessages.get(Sensor.TYPE_GRAVITY) + "\n" +
                            (String) sensorMessages.get(Sensor.TYPE_HEART_RATE);
        Log.d(TAG, "screen string = " + viewString);
        mTextView.setText(viewString);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}