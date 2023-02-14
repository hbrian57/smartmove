package com.centrale.smartmove;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;

public class TripTest {

    // Test classique avec trois voyages: OK
    @Test
    public void testGetTripCO2FootprintClassicCase() throws Exception {

        //Le voyage en vélo
        LinkedList<TimestampedPosition> bikePositions = new LinkedList<>();
        bikePositions.add(new TimestampedPosition(0, 0, 0));
        bikePositions.add(new TimestampedPosition(0.1, 0, 0));
        TripSegment bikeSegment = new TripSegment(TransportType.BIKE, bikePositions);

        //Le voyage en voiture
        LinkedList<TimestampedPosition> carPositions = new LinkedList<>();
        carPositions.add(new TimestampedPosition(0, 0, 0));
        carPositions.add(new TimestampedPosition(1, 0, 0));
        TripSegment carSegment = new TripSegment(TransportType.CAR, carPositions);

        //Le voyage à pied
        LinkedList<TimestampedPosition> footPositions = new LinkedList<>();
        footPositions.add(new TimestampedPosition(0, 0, 0));
        footPositions.add(new TimestampedPosition(0.05, 0, 0));
        TripSegment footSegment = new TripSegment(TransportType.FOOT, footPositions);

        // Ajout des trois segments
        ArrayList<TripSegment> tripSegments = new ArrayList<>();
        tripSegments.add(bikeSegment);
        tripSegments.add(carSegment);
        tripSegments.add(footSegment);

        Trip trip = new Trip(tripSegments);

        // On a déjà testé la fonction calculate donc je peux l'utiliser là.
        double expectedCO2Footprint = (TransportType.BIKE.getCO2param() * bikeSegment.calculateTotalDistance())
                + (TransportType.CAR.getCO2param() * carSegment.calculateTotalDistance()) + (TransportType.FOOT.getCO2param() * footSegment.calculateTotalDistance()) ;
        double actualCO2Footprint = trip.getTripCO2Footprint();

        assertEquals(expectedCO2Footprint, actualCO2Footprint, 5);
    }

    // Test distance à zéro avec trois voyages: OK
    @Test
    public void testGetTripCO2FootprintDistanceNul() throws Exception {

        //Le voyage en vélo
        LinkedList<TimestampedPosition> bikePositions = new LinkedList<>();
        bikePositions.add(new TimestampedPosition(0, 0, 0));
        bikePositions.add(new TimestampedPosition(0, 0, 0));
        TripSegment bikeSegment = new TripSegment(TransportType.BIKE, bikePositions);

        //Le voyage en voiture
        LinkedList<TimestampedPosition> carPositions = new LinkedList<>();
        carPositions.add(new TimestampedPosition(0, 0, 0));
        carPositions.add(new TimestampedPosition(0, 0, 0));
        TripSegment carSegment = new TripSegment(TransportType.CAR, carPositions);

        //Le voyage à pied
        LinkedList<TimestampedPosition> footPositions = new LinkedList<>();
        footPositions.add(new TimestampedPosition(0, 0, 0));
        footPositions.add(new TimestampedPosition(0, 0, 0));
        TripSegment footSegment = new TripSegment(TransportType.FOOT, footPositions);

        // Ajout des trois segments
        ArrayList<TripSegment> tripSegments = new ArrayList<>();
        tripSegments.add(bikeSegment);
        tripSegments.add(carSegment);
        tripSegments.add(footSegment);

        Trip trip = new Trip(tripSegments);
        double expectedCO2Footprint = 0;
        double actualCO2Footprint = trip.getTripCO2Footprint();

        assertEquals(expectedCO2Footprint, actualCO2Footprint, 5);
    }

    // Test exception degré négatif: OK
    @Test
    public void testGetTripCO2FootprintExceptionNegative() throws Exception {

        //Un seul voyage
        LinkedList<TimestampedPosition> bikePositions = new LinkedList<>();
        bikePositions.add(new TimestampedPosition(0, 0, 0));
        bikePositions.add(new TimestampedPosition(-10, 0, 0));
        TripSegment bikeSegment = new TripSegment(TransportType.BIKE, bikePositions);

        // Ajout des segments
        ArrayList<TripSegment> tripSegments = new ArrayList<>();
        tripSegments.add(bikeSegment);

        Trip trip = new Trip(tripSegments);

        double expectedCO2Footprint = 0;
        double actualCO2Footprint = trip.getTripCO2Footprint();

        assertEquals(expectedCO2Footprint, actualCO2Footprint, 5);
    }

    // Test exception degré négatif: OK
    @Test
    public void testGetTripCO2FootprintExceptionDegreeTooHigh() throws Exception {

        //Un seul voyage
        LinkedList<TimestampedPosition> bikePositions = new LinkedList<>();
        bikePositions.add(new TimestampedPosition(0, 0, 0));
        bikePositions.add(new TimestampedPosition(100, 0, 0));
        TripSegment bikeSegment = new TripSegment(TransportType.BIKE, bikePositions);

        // Ajout des segments
        ArrayList<TripSegment> tripSegments = new ArrayList<>();
        tripSegments.add(bikeSegment);

        Trip trip = new Trip(tripSegments);

        double expectedCO2Footprint = 0;
        double actualCO2Footprint = trip.getTripCO2Footprint();

        assertEquals(expectedCO2Footprint, actualCO2Footprint, 5);
    }
}

