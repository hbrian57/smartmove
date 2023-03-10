package fr.ifsttar.geoloc.geoloclib.imu;

import java.util.HashMap;
import java.util.Vector;

import fr.ifsttar.geoloc.geoloclib.satellites.GNSSObservation;

public class FusionFilter {
    private Vector<ImuMeasurement> _ImuMeasurement;
    private HashMap<String, GNSSObservation> _GNSSobservation;

    public void refreshData(Vector<ImuMeasurement> _Vector, HashMap<String, GNSSObservation> _HashMap){
        _ImuMeasurement = _Vector;
        _GNSSobservation = _HashMap;
    }

    public void ImuFusion(){

    }

    public void ImuGnssFusion(){

    }

    public void GetCoordinates(){

    }
}
