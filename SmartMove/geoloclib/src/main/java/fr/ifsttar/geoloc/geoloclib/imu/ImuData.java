package fr.ifsttar.geoloc.geoloclib.imu;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

public class ImuData {
    private SensorManager mSensorManager;           //Sensormanager
    private Vector<ImuMeasurement> imuObservations; //vector of the object
    private ImuMeasurement currentMeasurement;      //object including 6 values of accelerometer&gyro output
    private String fileNameImu;                     //file name of IMU data
    private StringBuilder fileContentsImu;          //string builder
    private File parentDir;                         //main directory of the file created by this application
    private File surveyDir;                         //sub directory difference by date
    private File fileImu;                           //IMU file name
    private double time;                              //system time
    static final private String IMU_HEADER = "# Header Description:\n" +
            "# Time(s),Acc-x-axis(m/s2),Acc-y-axis(m/s2),Acc-z-axis(m/s2)," +
            "Gyro-x-axis(rad/s), Gyro-y-axis(rad/s),Gyro-z-axis(rad/s)," +
            "Mag-x-axis(uT), Mag-y-axis(uT),Mag-z-axis(uT) \n";
    //----------------------------------------------------------------------------------------------

    /**
     * construct function to create the file to retrieve IMU measurements
     */
    public ImuData()
    {

    }
    /**
     * function to create the file to retrieve IMU measurements
     * @param context
     */
    public void createFile(Context context)
    {
        // get system sensor service
        // create directory
        // create new file
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateFormat.setTimeZone(TimeZone.getTimeZone("gmt"));
        Date date = Calendar.getInstance().getTime();

        fileContentsImu = new StringBuilder();
        fileContentsImu.append(IMU_HEADER);
        imuObservations = new Vector<>();
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        parentDir = new File(Environment.getExternalStorageDirectory(), "IFSTTAR_GNSS_IMU_logger");
        //if the directory doesn't exist, make directory
        if(!parentDir.exists())
        {
            parentDir.mkdirs();
        }

        surveyDir = new File(Environment.getExternalStorageDirectory() + "/" + parentDir.getName(),
                dateFormat.format(date));
        //if the directory doesn't exist, make directory
        if(!surveyDir.exists()){
            surveyDir.mkdirs();
        }

        dateFormat = new SimpleDateFormat("dd-MM-yyyy-ssSSS");
        fileNameImu = dateFormat.format(date) + "_IMU.txt";
        fileImu = new File(surveyDir, fileNameImu);
        //if file doesn't exist, create new one
        if(!fileImu.exists()){
            try {
                fileImu.createNewFile();
            }catch (IOException e){ }
        }
    }
    //----------------------------------------------------------------------------------------------

    /**
     * get callbacks from sensors
     */
    public void getImuCallback() {

        //accelerometer
        Sensor sensora = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED);
        mSensorManager.registerListener(listenerAcc, sensora, SensorManager.SENSOR_DELAY_FASTEST);

        //gyroscope
        Sensor sensorg = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        mSensorManager.registerListener(listenerGyro, sensorg, SensorManager.SENSOR_DELAY_FASTEST);

        //magnitude
        Sensor sensorm = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
        mSensorManager.registerListener(listenerMag, sensorm, SensorManager.SENSOR_DELAY_FASTEST);

        //gravity
        Sensor sensorr = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mSensorManager.registerListener(listenerGra, sensorr, SensorManager.SENSOR_DELAY_FASTEST);

    }

    //----------------------------------------------------------------------------------------------
    /**
     * set the vector and return the vector back
     */
    public Vector<ImuMeasurement> setImuObservation(){
        return imuObservations;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * override the Mag Listener to get the SensorEvent
     */
    private SensorEventListener listenerMag = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent Event) {

            // if the object is null, create one
            if(currentMeasurement == null)
            {
                currentMeasurement = new ImuMeasurement();
            }

            currentMeasurement.setMag(Event);

            Log.i("SENSOR", "Acc activated");

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private SensorEventListener listenerGra = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent Event) {

            // if the object is null, create one
            if(currentMeasurement == null)
            {
                currentMeasurement = new ImuMeasurement();
            }

            currentMeasurement.setGra(Event);


            Log.i("SENSOR", "Acc activated");

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    //----------------------------------------------------------------------------------------------
    /**
     * override the Accelerometer Listener to get the SensorEvent
     */
    private SensorEventListener listenerAcc = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent Event) {

            // if the object is null, create one
            if(currentMeasurement == null)
            {
                currentMeasurement = new ImuMeasurement();
            }

            currentMeasurement.setAcc(Event);

            Log.i("SENSOR", "Acc activated");

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    //----------------------------------------------------------------------------------------------
    /**
     * override the Gyroscope Listener to get the SensorEvent
     */
    private SensorEventListener listenerGyro = new SensorEventListener() {
        @Override
        // when the value of sensor output changes, parse the value of sensor
        public void onSensorChanged(SensorEvent Event)
        {
            // if the object is null, create one
            if(currentMeasurement == null)
            {
                currentMeasurement = new ImuMeasurement();
            }

            //parse the event to the object, return 3 element of acc
            currentMeasurement.setGyro(Event);

            Log.i("SENSOR", "Gyro activated");

            //if the acc and gyro both activated
            if(currentMeasurement.isAccSet && currentMeasurement.isGyroSet && currentMeasurement.isMagSet){

                //intialize the roll,pitch,yaw
                imuInitialize();
                //add the object into the vector
                imuObservations.add(currentMeasurement);

                //set the isAccSet and isGyroSet to false
                currentMeasurement.resetImuData();

                //set the object to null to receive new measurements
                currentMeasurement = null;

                //when the vector stored 5 IMUmeasurement
                if(imuObservations.size()==5)
                {
                    //log into file
                    logImuData();

                    //remove the elements of vector and set it as empty
                    imuObservations.removeAllElements();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    //----------------------------------------------------------------------------------------------
    /**
     * log the IMU measurement into file
     */
    private void logImuData(){
        FileOutputStream outputStream;
        OutputStreamWriter outputStreamWriter;

        if(imuObservations == null)
        {
            return;
        }

        //SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        //dateFormat.setTimeZone(TimeZone.getTimeZone("gmt"));
        //directory to save log files

        //we add all the measurements to the contents string
        //fileContentsTime

        for(ImuMeasurement meas: imuObservations) {
            if (Math.abs(meas.timeAcc - meas.timeGyro) < 10 * 1e6 && Math.abs(meas.timeAcc - meas.timeMag) < 10 * 1e6)
            {
                time = meas.timeAcc;
            }
            else {
                Log.i("FILE", "WARNING TIME DIFFERENT");
            }

            fileContentsImu.append(time)
                    .append(",")
                    .append(meas.accX)
                    .append(",")
                    .append(meas.accY)
                    .append(",")
                    .append(meas.accZ)
                    .append(",")
                    .append(meas.gyroX)
                    .append(",")
                    .append(meas.gyroY)
                    .append(",")
                    .append(meas.gyroZ)
                    .append(",")
                    .append(meas.magX)
                    .append(",")
                    .append(meas.magY)
                    .append(",")
                    .append(meas.magZ)
                    .append("\n");

            Log.i("FILE", " " +fileContentsImu+" ");

            try {
                //we append new contents to file
                outputStream = new FileOutputStream(fileImu, true);
                outputStreamWriter = new OutputStreamWriter(outputStream);
                outputStreamWriter.append(fileContentsImu);

                outputStreamWriter.close();
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //clearing contents to next loop
            fileContentsImu = new StringBuilder();
        }
    }

    //----------------------------------------------------------------------------------------------
    /**
     * integrating IMU data
     */
    public void imuInitialize(){
        if(currentMeasurement == null)
        {
            return;
        }

        //initializing
        double Yaw;
        double Pitch;
        double roll;
        Yaw = Math.atan(-currentMeasurement.gyroY/currentMeasurement.gyroX);
        Pitch = Math.asin(currentMeasurement.accX/currentMeasurement.Gravity);
        roll = Math.asin(currentMeasurement.accY/currentMeasurement.Gravity/Math.cos(Pitch));
    }

    //----------------------------------------------------------------------------------------------
    /**
     * unregister the listener, stop the thread
     */
    public void onDestroy() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(listenerAcc);
            mSensorManager.unregisterListener(listenerGyro);
        }
    }
}
