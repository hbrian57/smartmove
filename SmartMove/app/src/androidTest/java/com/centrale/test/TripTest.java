package com.centrale.test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import com.centrale.smartmove.models.TimestampedPosition;
import com.centrale.smartmove.models.TransportType;
import com.centrale.smartmove.models.Trip;
import com.centrale.smartmove.models.TripSegment;

import org.junit.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;

public class TripTest {

    /**
     * Test de la méthode GetTRipCO2Footprint pour un cas classique
     * Résultat attendu: (TransportType.BIKE.getCO2param() * bikeSegment.calculateTotalDistance())
     *                 + (TransportType.CAR.getCO2param() * carSegment.calculateTotalDistance()) + (TransportType.WALKING.getCO2param() * footSegment.calculateTotalDistance()) ;
     *
     */
    // Test classique avec trois voyages: OK
    @Test
    public void testGetTripCO2FootprintClassicCase() throws Exception {

        //Le voyage en vélo
        LinkedList<TimestampedPosition> listOfPosition= new LinkedList<>();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        LinkedList<TimestampedPosition> bikePositions = new LinkedList<>();
        bikePositions.add(new TimestampedPosition(0, 0, timestamp));
        bikePositions.add(new TimestampedPosition(0.1, 0, timestamp));
        TripSegment bikeSegment = new TripSegment(TransportType.BIKE, bikePositions);

        //Le voyage en voiture
        LinkedList<TimestampedPosition> carPositions = new LinkedList<>();
        carPositions.add(new TimestampedPosition(0, 0, timestamp));
        carPositions.add(new TimestampedPosition(1, 0, timestamp));
        TripSegment carSegment = new TripSegment(TransportType.CAR, carPositions);

        //Le voyage à pied
        LinkedList<TimestampedPosition> footPositions = new LinkedList<>();
        footPositions.add(new TimestampedPosition(0, 0, timestamp));
        footPositions.add(new TimestampedPosition(0.05, 0, timestamp));
        TripSegment footSegment = new TripSegment(TransportType.WALKING, footPositions);

        // Ajout des trois segments
        ArrayList<TripSegment> tripSegments = new ArrayList<>();
        tripSegments.add(bikeSegment);
        tripSegments.add(carSegment);
        tripSegments.add(footSegment);

        Trip trip = new Trip(tripSegments);

        // On a déjà testé la fonction calculate donc je peux l'utiliser là.
        double expectedCO2Footprint = (TransportType.BIKE.getCO2param() * bikeSegment.calculateTotalDistance())
                + (TransportType.CAR.getCO2param() * carSegment.calculateTotalDistance()) + (TransportType.WALKING.getCO2param() * footSegment.calculateTotalDistance()) ;
        double actualCO2Footprint = trip.getTripCO2Footprint();

        assertEquals(expectedCO2Footprint, actualCO2Footprint, 5);
    }


    /**
     * Test de la méthode GetTRipCO2Footprint pour un cas ou on est statique
     * Résultat attendu:0
     */
    // Test distance à zéro avec trois voyages: OK
    @Test
    public void testGetTripCO2FootprintDistanceNul() throws Exception {

        //Le voyage en vélo
        LinkedList<TimestampedPosition> listOfPosition= new LinkedList<>();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        LinkedList<TimestampedPosition> bikePositions = new LinkedList<>();
        bikePositions.add(new TimestampedPosition(0, 0, timestamp));
        bikePositions.add(new TimestampedPosition(0, 0, timestamp));
        TripSegment bikeSegment = new TripSegment(TransportType.BIKE, bikePositions);

        //Le voyage en voiture
        LinkedList<TimestampedPosition> carPositions = new LinkedList<>();
        carPositions.add(new TimestampedPosition(0, 0, timestamp));
        carPositions.add(new TimestampedPosition(0, 0, timestamp));
        TripSegment carSegment = new TripSegment(TransportType.CAR, carPositions);

        //Le voyage à pied
        LinkedList<TimestampedPosition> footPositions = new LinkedList<>();
        footPositions.add(new TimestampedPosition(0, 0, timestamp));
        footPositions.add(new TimestampedPosition(0, 0, timestamp));
        TripSegment footSegment = new TripSegment(TransportType.WALKING, footPositions);

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

    /**
     * Test de la méthode GetTRipCO2Footprint pour une exception lat inférieure à 90°
     * Résultat attendu: Impossible que la latitude soit inf à -90°
     */

    @Test
    public void testGetTripCO2FootprintExceptionNegative() throws Exception {

        //Un seul voyage
        LinkedList<TimestampedPosition> listOfPosition= new LinkedList<>();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        LinkedList<TimestampedPosition> bikePositions = new LinkedList<>();
        bikePositions.add(new TimestampedPosition(0, 0, timestamp));
        bikePositions.add(new TimestampedPosition(-100, 0, timestamp));
        TripSegment bikeSegment = new TripSegment(TransportType.BIKE, bikePositions);

        // Ajout des segments
        ArrayList<TripSegment> tripSegments = new ArrayList<>();
        tripSegments.add(bikeSegment);

        Trip trip = new Trip(tripSegments);

        double expectedCO2Footprint = 0;

        try {
            double actualCO2Footprint = trip.getTripCO2Footprint();
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Impossible que la latitude soit supérieure à 90°"));
        }
    }

    /**
     * Test de la méthode GetTRipCO2Footprint pour une exception lat inférieure à 90°
     * Résultat attendu: Impossible que la latitude soit supérieure à 90°
     */
    @Test
    public void testGetTripCO2FootprintExceptionDegreeTooHigh() throws Exception {

        LinkedList<TimestampedPosition> listOfPosition= new LinkedList<>();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        //Un seul voyage
        LinkedList<TimestampedPosition> bikePositions = new LinkedList<>();
        bikePositions.add(new TimestampedPosition(0, 0, timestamp));
        bikePositions.add(new TimestampedPosition(100, 0, timestamp));
        TripSegment bikeSegment = new TripSegment(TransportType.BIKE, bikePositions);

        // Ajout des segments
        ArrayList<TripSegment> tripSegments = new ArrayList<>();
        tripSegments.add(bikeSegment);

        Trip trip = new Trip(tripSegments);

        double expectedCO2Footprint = 0;
        try {
            double actualCO2Footprint = trip.getTripCO2Footprint();
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Impossible que la latitude soit supérieure à 90°"));
        }
    }
}

