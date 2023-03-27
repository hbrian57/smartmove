package fr.ifsttar.geoloc.geoloclib.imu;

import android.hardware.SensorEvent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ImuMeasurement {
    public float accX;
    public float accY;
    public float accZ;
    public float gyroX;
    public float gyroY;
    public float gyroZ;
    public float magX;
    public float magY;
    public float magZ;
    public float graX;
    public float graY;
    public float graZ;
    public double Gravity;
    public boolean isAccSet = false;
    public boolean isGyroSet = false;
    public boolean isMagSet = false;
    public boolean isGraSet = false;
    public double timeAcc;
    public double timeGyro;
    public double timeMag;

    public void setAcc(SensorEvent event){
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("gmt"));
        Date date = Calendar.getInstance().getTime();
        String strDate = dateFormat.format(date);
        Log.i("TIME", "" + date);
        timeAcc = timeTransfer(strDate);
        accX = event.values[0];
        accY = event.values[1];
        accZ = event.values[2];
        isAccSet = true;
        Log.i("SENSOR", "" + accX + "," + accY + "," + accZ);
    }

    public void setGyro(SensorEvent event){
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("gmt"));
        Date date = Calendar.getInstance().getTime();
        String strDate = dateFormat.format(date);
        timeGyro = timeTransfer(strDate);
        gyroX = event.values[0];
        gyroY = event.values[1];
        gyroZ = event.values[2];
        isGyroSet = true;
        Log.i("SENSOR", "" + gyroX + "," + gyroY + "," + gyroZ);
    }

    public void setMag(SensorEvent event){
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("gmt"));
        Date date = Calendar.getInstance().getTime();
        String strDate = dateFormat.format(date);
        timeMag = timeTransfer(strDate);
        magX = event.values[0];
        magY = event.values[1];
        magZ = event.values[2];
        isMagSet = true;
        Log.i("SENSOR", "" + magX + "," + magY + "," + magZ);
    }

    public void setGra(SensorEvent event){
        graX = event.values[0];
        graY = event.values[1];
        graZ = event.values[2];
        isGraSet = true;
        Gravity = Math.sqrt(Math.pow(graX,2)+ Math.pow(graY,2) + Math.pow(graZ,2));
        Log.i("SENSOR", "" + magX + "," + magY + "," + magZ);
    }

    public void resetImuData(){
        isAccSet = false;
        isGyroSet = false;
        isMagSet = false;
        isGraSet = false;
    }

    private double timeTransfer(String date){
        String D = date.substring(0,1);
        String M = date.substring(2,3);
        String Y = date.substring(4,7);
        String h = date.substring(8,9);
        String m = date.substring(10,11);
        String s = date.substring(12,13);
        String ms = date.substring(14,16);
        double JD = GCtoJD(h,M,D);

        double DOW = Math.floor((JD+1.5)%7);
        double SOW = DOW * 86400 + Double.valueOf(h) * 3600 + Double.valueOf(m) * 60 + Double.valueOf(s) + Double.valueOf(ms)/1000;
        return SOW;
    }

    private double GCtoJD(String Year,String Month,String DAY){
        double JD;
        double a = Double.valueOf(Year);
        JD = 367 *  - Math.floor(7 / 4 * (Double.valueOf(Year) + Math.floor((Double.valueOf(Month) + 9) / 12))) + Math.floor(275 * Double.valueOf(Month) /9) + Double.valueOf(DAY) + 1721013.5;
        return JD;
    }
}
