package com.centrale.smartmove;

public class CalculCO2 {

    public double calculCO2ForTrip(Trip t){
        double consoCO2 = 0;
        int i;
        for (i=0; i<t.getListLilTrips().size(); i++) {
            consoCO2= consoCO2+calculCO2ForLitTrip(t.getListLilTrips().get(i));
        }
        return consoCO2;
    }

    public double calculCO2ForLitTrip(LilTrip t){
        double consoCO2 = 0;
        double multiplifacteurTransport;
        String transportType = t.getTransport().nameTransport;
        switch (transportType) {
            case "Bike":
                multiplifacteurTransport=0.15;
                consoCO2=multiplifacteurTransport*t.getDistanceTotal();
                break;
            case "Foot":
                multiplifacteurTransport=0.1;
                consoCO2=multiplifacteurTransport*t.getDistanceTotal();
                break;
            case "Car":
                multiplifacteurTransport=0.4;
                consoCO2=multiplifacteurTransport*t.getDistanceTotal();
                break;
            default:
                System.out.println("Invalid transport type");
        }
        return consoCO2;
    }
    }

