package com.centrale.test;

import static org.junit.Assert.assertEquals;

import com.centrale.smartmove.TimestampedPosition;
import com.centrale.smartmove.TransportType;
import com.centrale.smartmove.TripSegment;

import org.junit.Test;

import java.util.LinkedList;

public class TripSegmentTest {


    @Test
    public void testCalculateTotalDistance0() throws Exception {
        LinkedList<TimestampedPosition> listOfPosition= new LinkedList<>();
        listOfPosition.add(new TimestampedPosition(0, 0, 0));
        listOfPosition.add(new TimestampedPosition(0, 0, 0));
        listOfPosition.add(new TimestampedPosition(0, 0, 0));
        TripSegment segment = new TripSegment(TransportType.valueOf("CAR"), listOfPosition);

        double expectedDistance = 0;
        double actualDistance = segment.calculateTotalDistance();

        assertEquals(expectedDistance, actualDistance, 5);
    }

    @Test
    public void testCalculateTotalDistance1() throws Exception {
        LinkedList<TimestampedPosition> listOfPosition= new LinkedList<>();
        listOfPosition.add(new TimestampedPosition(0, 0, 0));
        listOfPosition.add(new TimestampedPosition(1, 0, 0));
        listOfPosition.add(new TimestampedPosition(2, 0, 0));
        TripSegment segment = new TripSegment(TransportType.valueOf("BIKE"), listOfPosition);
        double expectedDistance = 222390;
        double actualDistance = segment.calculateTotalDistance();
        assertEquals(expectedDistance, actualDistance, 5);
    }

    @Test
    public void testCalculateTotalDistance2() throws Exception {
        LinkedList<TimestampedPosition> listOfPosition= new LinkedList<>();
        listOfPosition.add(new TimestampedPosition(0, 0, 0));
        listOfPosition.add(new TimestampedPosition(0, 2, 0));
        listOfPosition.add(new TimestampedPosition(0, 4, 0));
        TripSegment segment = new TripSegment(TransportType.valueOf("FOOT"), listOfPosition);
        double expectedDistance = 444780;
        double actualDistance = segment.calculateTotalDistance();
        assertEquals(expectedDistance, actualDistance, 5);
    }

    @Test
    //Test de la liste vide qui retourne 0
    public void testCalculateTotalDistance3() throws Exception {
        LinkedList<TimestampedPosition> listOfPosition= new LinkedList<>();
        TripSegment segment = new TripSegment(TransportType.valueOf("CAR"), listOfPosition);
        double expectedDistance = 0;
        double actualDistance = segment.calculateTotalDistance();
        assertEquals(expectedDistance, actualDistance, 5);
    }

    @Test
    //Test degré negatif: OK
    public void testCalculateTotalDistanceNegativeDegree() throws Exception {
        LinkedList<TimestampedPosition> listOfPosition= new LinkedList<>();
        listOfPosition.add(new TimestampedPosition(0, 0, 0));
        listOfPosition.add(new TimestampedPosition(-1, 2, 0));
        listOfPosition.add(new TimestampedPosition(0, 4, 0));
        TripSegment segment = new TripSegment(TransportType.valueOf("FOOT"), listOfPosition);
        double expectedDistance = 444780;
        double actualDistance = segment.calculateTotalDistance();
        assertEquals(expectedDistance, actualDistance, 5);
    }

    @Test
    //Test degré sup à 90°: OK
    public void testCalculateTotalDistanceDegreeTooHigh() throws Exception {
        LinkedList<TimestampedPosition> listOfPosition= new LinkedList<>();
        listOfPosition.add(new TimestampedPosition(0, 0, 0));
        listOfPosition.add(new TimestampedPosition(100, 2, 0));
        listOfPosition.add(new TimestampedPosition(0, 4, 0));
        TripSegment segment = new TripSegment(TransportType.valueOf("FOOT"), listOfPosition);
        double expectedDistance = 444780;
        double actualDistance = segment.calculateTotalDistance();
        assertEquals(expectedDistance, actualDistance, 5);
    }



    //TEST POUR LA SECONDE METHODE
    @Test
    //Test pour la voiture: OK
    public void calculateCO2footprintCar() throws Exception {
        LinkedList<TimestampedPosition> positionList = new LinkedList<>();
        positionList.add(new TimestampedPosition(0, 0, 0));
        positionList.add(new TimestampedPosition(0.03, 0, 0));
        positionList.add(new TimestampedPosition(0.06, 0, 0));
        TripSegment tripSegment = new TripSegment(TransportType.CAR, positionList);
        double expectedCO2footprint = 33360;
        double actualCO2footprint = tripSegment.calculateCO2footprint();
        assertEquals(expectedCO2footprint, actualCO2footprint, 10);
    }

    @Test
    //Test pour le Vélo: OK
    public void calculateCO2footprintBike() throws Exception {
        LinkedList<TimestampedPosition> positionList = new LinkedList<>();
        positionList.add(new TimestampedPosition(0, 0, 0));
        positionList.add(new TimestampedPosition(0.03, 0, 0));
        positionList.add(new TimestampedPosition(0.06, 0, 0));
        TripSegment tripSegment = new TripSegment(TransportType.BIKE, positionList);
        double expectedCO2footprint = 3336;
        double actualCO2footprint = tripSegment.calculateCO2footprint();
        assertEquals(expectedCO2footprint, actualCO2footprint, 10);
    }

    @Test
    //Test pour à PIED: OK
    public void calculateCO2footprintFoot() throws Exception {
        LinkedList<TimestampedPosition> positionList = new LinkedList<>();
        positionList.add(new TimestampedPosition(0, 0, 0));
        positionList.add(new TimestampedPosition(0.03, 0, 0));
        positionList.add(new TimestampedPosition(0.06, 0, 0));
        TripSegment tripSegment = new TripSegment(TransportType.FOOT, positionList);
        double expectedCO2footprint = 667.2;
        double actualCO2footprint = tripSegment.calculateCO2footprint();
        assertEquals(expectedCO2footprint, actualCO2footprint, 10);
    }

    @Test
    //Test pour l'exception degré négatif: OK
    public void calculateCO2footprintNegativeDegree() throws Exception {
        LinkedList<TimestampedPosition> positionList = new LinkedList<>();
        positionList.add(new TimestampedPosition(0, 0, 0));
        positionList.add(new TimestampedPosition(-0.03, 0, 0));
        positionList.add(new TimestampedPosition(0.06, 0, 0));
        TripSegment tripSegment = new TripSegment(TransportType.FOOT, positionList);
        double expectedCO2footprint = 667.2;
        double actualCO2footprint = tripSegment.calculateCO2footprint();
        assertEquals(expectedCO2footprint, actualCO2footprint, 10);
    }

    @Test
    //Test pour l'exception degré supérieur à 90: OK
    public void calculateCO2footprintDegreeTooHigh() throws Exception {
        LinkedList<TimestampedPosition> positionList = new LinkedList<>();
        positionList.add(new TimestampedPosition(0, 0, 0));
        positionList.add(new TimestampedPosition(100, 0, 0));
        positionList.add(new TimestampedPosition(0.06, 0, 0));
        TripSegment tripSegment = new TripSegment(TransportType.FOOT, positionList);
        double expectedCO2footprint = 667.2;
        double actualCO2footprint = tripSegment.calculateCO2footprint();
        assertEquals(expectedCO2footprint, actualCO2footprint, 10);
    }
}
