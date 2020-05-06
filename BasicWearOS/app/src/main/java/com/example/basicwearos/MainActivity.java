package com.example.basicwearos;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.util.Pair;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.HashMap;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import com.google.common.collect.EvictingQueue;

public class MainActivity extends WearableActivity implements SensorEventListener{
    private static final int Q_SZ = 500;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 101;
    private static final int[] ALL_SENSOR_TYPES = {
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_GRAVITY,
            Sensor.TYPE_HEART_RATE
    };
    private static final String[] ALL_SENSOR_NAMES = {
            "Acc",
            "Gyr",
            "Gra",
            "HR"
    };
    private static final int[] ALL_SENSOR_DIM = {
            3,
            3,
            3,
            1
    };
    private static final int[] ALL_SENSOR_DELAYS = {
            SensorManager.SENSOR_DELAY_GAME,
            SensorManager.SENSOR_DELAY_GAME,
            SensorManager.SENSOR_DELAY_GAME,
            SensorManager.SENSOR_DELAY_GAME,
            SensorManager.SENSOR_DELAY_NORMAL
    };
    private static final boolean LOG_DATA_TO_FILE = true;
    private static final String TAG = "MyActivity";
    private int nChannels;
    private TextView mTextView;
    private HashMap sensorMessages;
    private HashMap sensorMessagePrefix;
    // Map to store the starting Q index and number of Qs for a given sensor
    private HashMap sensorDataIndex;
    private Vector<EvictingQueue<Float>> allDataQ;
    DataSampleProcessor dataProcessor;
    FileOutputStream sensorLogStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.text);
        mTextView.setMaxLines(8);
        if(LOG_DATA_TO_FILE) {
            // create log file
            createSensorDataFile();
        }
        // new data sample processor
        nChannels = 0;
        for(int dim : ALL_SENSOR_DIM){
            nChannels += dim;
        }
        dataProcessor = new DataSampleProcessor(nChannels, Q_SZ);
        // initialize all Qs for data
        initAllDataQ();
        // init all sensors to start getting data
        initAllSensors();
        // Enables Always-on
        setAmbientEnabled();
    }

    private void initAllDataQ(){
        allDataQ = new Vector<EvictingQueue<Float>>();
        sensorDataIndex = new HashMap();
        for(int i=0; i<ALL_SENSOR_TYPES.length; i++){
            sensorDataIndex.put(ALL_SENSOR_TYPES[i], new Pair<Integer, Integer>(i, ALL_SENSOR_DIM[i]));
            for(int j=0; j<ALL_SENSOR_DIM[i]; j++) {
                allDataQ.add(EvictingQueue.<Float>create(Q_SZ));
            }
        }
        // initialize sensor message and logging
        sensorMessagePrefix = new HashMap();
        sensorMessages = new HashMap();
        for(int sensorType: ALL_SENSOR_TYPES) {
            sensorMessagePrefix.put(
                    sensorType,
                    String.format("%d,", ((Pair<Integer, Integer>) sensorDataIndex.get(sensorType)).first.intValue()));
            sensorMessages.put(sensorType, sensorMessagePrefix.get(sensorType));
        }
    }

    private void initAllSensors(){
        for(int i=0; i<ALL_SENSOR_TYPES.length; i++) {
            registerSensor(ALL_SENSOR_TYPES[i], ALL_SENSOR_DELAYS[i]);
        }
    }

    private String updateSensorMessage(int sensorType, String message){
        String logString = sensorMessagePrefix.get(sensorType) + message;
        sensorMessages.put(sensorType, logString);
        return logString;
    }

    private void registerSensor(int sensorType, int delay){
        SensorManager sensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));
        Sensor sensor = sensorManager.getDefaultSensor(sensorType);
        if(sensor != null){
            Log.d(TAG, "*********** !" + sensorType);
        }
        sensorManager.registerListener((SensorEventListener) this, sensor, delay);
    }

    private String getCurrentTimeStr() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return df.format(c.getTime());
    }

    private String getMeanString(){
        String meanString = "";
        float[] allMeans;
        try{
            allMeans = dataProcessor.calcAllMean();
            StringBuilder allMeansString = new StringBuilder("M:");
            for (float allMean : allMeans) {
                allMeansString.append(String.format("%.1f,", allMean));
            }
            meanString = String.format("%s", allMeansString.toString());
        }
        catch (Exception e){
            Log.d(TAG, "dataProcessor.calcAllMean exception: " + e.toString());
        }
        return meanString;
    }

    private void createSensorDataFile(){
        Log.d(TAG, "Asking for file create perms");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        else {
            Log.d(TAG, "Creating file (already has perms)");
            try {
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                String fileName = String.format("sensors-%s.csv", getCurrentTimeStr());
                File dataFile = new File(path, fileName);
                boolean notExist = dataFile.createNewFile();
                if (notExist) {
                    Log.d(TAG, "Created a new file in Downloads: " + fileName);
                } else {
                    Log.d(TAG, "File already exists: " + fileName);
                }
                sensorLogStream = new FileOutputStream(dataFile, true);
                // write sensor data header
                String headerString = "#";
                for(int i=0; i<ALL_SENSOR_NAMES.length; i++){
                    headerString += String.format("%d=%s,", i, ALL_SENSOR_NAMES[i]);
                }
                headerString += "\ntimestamp,sensor,d1,d2,d3,d4\n";
                sensorLogStream.write(headerString.getBytes());
            } catch (IOException | SecurityException e) {
                Log.e("Exception", "File creation failed: " + e.toString());
            }
        }
    }

    private void writeSensorDataToFile(String sensorString){
        try {
            String writeString = getCurrentTimeStr() + "," + sensorString + "\n";
            sensorLogStream.write(writeString.getBytes());
        }
        catch(IOException | NullPointerException e)
        {
            Log.e("Exception", "Sensor data write failed: " + e.toString());
        }
    }

    @SuppressLint("DefaultLocale")
    private void handleSensorData(int sensorType, float values[]) {
        Pair<Integer, Integer> indNum = (Pair<Integer, Integer>) sensorDataIndex.get(sensorType);
        int startInd = indNum.first.intValue();
        String dataString = "";
        int channelInd = 0;
        for(int i=0; i<indNum.second.intValue(); i++){
            channelInd = startInd + i;
            allDataQ.elementAt(channelInd).add(values[i]);
            dataString += String.format("%.1f,", values[i]);
            Log.d(TAG, "** channelInd being set = " + channelInd);
            boolean good = dataProcessor.setData(allDataQ.elementAt(channelInd), channelInd);
            if(!good){
                Log.e(TAG, "-- Error setting data dataProcessor.setData()");
            }
        }
        String sensorString = updateSensorMessage(sensorType, dataString);
        if(LOG_DATA_TO_FILE){
            writeSensorDataToFile(sensorString);
        }
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        handleSensorData(event.sensor.getType(), event.values);
        String viewString = "";
        for(int sensorType : ALL_SENSOR_TYPES){
            viewString += sensorMessages.get(sensorType) + "\n";
        }
        viewString += getMeanString();
        mTextView.setText(viewString);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onRequestPermissionsResult (int requestCode,
                                            String[] permissions,
                                            int[] grantResults){
        if(requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            createSensorDataFile();
        }
    }
}