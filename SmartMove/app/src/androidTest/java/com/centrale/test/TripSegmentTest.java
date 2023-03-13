package com.centrale.test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.centrale.smartmove.models.TimestampedPosition;
import com.centrale.smartmove.models.TransportType;
import com.centrale.smartmove.models.TripSegment;

import org.junit.Test;

import java.util.LinkedList;

public class TripSegmentTest {


    /**Test de la fonction de calcul de distance entre trois positions similaires : lat= 0, long=0
     * Résultat attendu: 0
     */
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


    /**Test de la fonction de calcul de distance entre lat= 0, long=0 / lat= 1, long=0/ lat= 2, long=0
     * Résultat attendu: 222390
     */
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

    /**Test de la fonction de calcul de distance entre lat= 0, long=0 / lat= 0, long=2/ lat= 0, long=4
     * Résultat attendu: 222390
     */
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

    /**Test de la fonction de calcul de distance pour une liste vide
     * Résultat attendu: 0
     */
    @Test
    //Test de la liste vide qui retourne 0
    public void testCalculateTotalDistance3() throws Exception {
        LinkedList<TimestampedPosition> listOfPosition= new LinkedList<>();
        TripSegment segment = new TripSegment(TransportType.valueOf("CAR"), listOfPosition);
        double expectedDistance = 0;
        double actualDistance = segment.calculateTotalDistance();
        assertEquals(expectedDistance, actualDistance, 5);
    }

    /**Test de la fonction de calcul de distance pour une exception degré= -1000
     * Résultat attendu: "Impossible que la longitude soit inférieure à -180°"
     */
    @Test
    //Test degré negatif: OK
    public void testCalculateTotalDistanceNegativeDegree() throws Exception {
        LinkedList<TimestampedPosition> listOfPosition= new LinkedList<>();
        listOfPosition.add(new TimestampedPosition(0, 0, 0));
        listOfPosition.add(new TimestampedPosition(-1000, 2, 0));
        listOfPosition.add(new TimestampedPosition(0, 4, 0));
        TripSegment segment = new TripSegment(TransportType.WALKING, listOfPosition);
        double expectedDistance = 444780;
        try {
            double actualDistance = segment.calculateTotalDistance();
        }
        catch (Exception e) {
            assertThat(e.getMessage(), is("Impossible que la longitude soit inférieure à -180°"));
        }
    }

    /**Test de la fonction de calcul de distance pour une exception degré= 1000
     * Résultat attendu: "Impossible que la longitude soit supérieure à 180°"
     */
    @Test
    //Test degré sup à 90°: OK
    public void testCalculateTotalDistanceDegreeTooHigh() throws Exception {
        LinkedList<TimestampedPosition> listOfPosition= new LinkedList<>();
        listOfPosition.add(new TimestampedPosition(0, 0, 0));
        listOfPosition.add(new TimestampedPosition(1000, 2, 0));
        listOfPosition.add(new TimestampedPosition(0, 4, 0));
        TripSegment segment = new TripSegment(TransportType.WALKING, listOfPosition);
        double expectedDistance = 444780;
        try {
            double actualDistance = segment.calculateTotalDistance();
        }
        catch (Exception e) {
            assertThat(e.getMessage(), is("Impossible que la longitude soit superieure à 180°"));
        }
    }



    //TEST POUR LA SECONDE METHODE
    /**Test de la méthode de calcul de l'empreinte carbone pour une voiture
     * Résultat attendu: 33360
     * ATTENTION: LE TEST A ETE REALISE QUAND LES COEFFICIENTS ÉTAIENT ENCORE FAUX, IL EST POSSIBLE QUE LE TEST SOIT FAUX AJD CAR L'EXPECTED VALUE NE CORRESPOND PLUS
     */
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

    /**Test de la méthode de calcul de l'empreinte carbone pour un vélo
     * Résultat attendu: 3336
     * ATTENTION: LE TEST A ETE REALISE QUAND LES COEFFICIENTS ÉTAIENT ENCORE FAUX, IL EST POSSIBLE QUE LE TEST SOIT FAUX AJD CAR L'EXPECTED VALUE NE CORRESPOND PLUS
     */
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

    /**Test de la méthode de calcul de l'empreinte carbone pour un PIETON
     * Résultat attendu: 667.2
     * ATTENTION: LE TEST A ETE REALISE QUAND LES COEFFICIENTS ÉTAIENT ENCORE FAUX, IL EST POSSIBLE QUE LE TEST SOIT FAUX AJD CAR L'EXPECTED VALUE NE CORRESPOND PLUS
     */
    @Test
    //Test pour à PIED: OK
    public void calculateCO2footprintFoot() throws Exception {
        LinkedList<TimestampedPosition> positionList = new LinkedList<>();
        positionList.add(new TimestampedPosition(0, 0, 0));
        positionList.add(new TimestampedPosition(0.03, 0, 0));
        positionList.add(new TimestampedPosition(0.06, 0, 0));
        TripSegment tripSegment = new TripSegment(TransportType.WALKING, positionList);
        double expectedCO2footprint = 667.2;
        double actualCO2footprint = tripSegment.calculateCO2footprint();
        assertEquals(expectedCO2footprint, actualCO2footprint, 10);
    }

    /** CES TESTS SONT MAUVAIS:
    @Test
    //Test pour l'exception degré négatif: OK
    public void calculateCO2footprintNegativeDegree() throws Exception {
        LinkedList<TimestampedPosition> positionList = new LinkedList<>();
        positionList.add(new TimestampedPosition(0, 0, 0));
        positionList.add(new TimestampedPosition(-0.03, 0, 0));
        positionList.add(new TimestampedPosition(0.06, 0, 0));
        TripSegment tripSegment = new TripSegment(TransportType.WALKING, positionList);
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
        TripSegment tripSegment = new TripSegment(TransportType.WALKING, positionList);
        double expectedCO2footprint = 667.2;
        double actualCO2footprint = tripSegment.calculateCO2footprint();
        assertEquals(expectedCO2footprint, actualCO2footprint, 10);

    */

    /** Tests de la fonction computeTransportType
     *
     */
    @Test
    //Test si le TripSegment a été initialisé
    public void nullTripSegmentTransportType(){
        TripSegment tripSegment = null;
        try{
            tripSegment.computeTransportType();
        }catch(Exception e){
        assertThat(e.getMessage(), is("Le TripSegment n'a pas été initialisé."));
    }}

    @Test
    //Test si le TripSegment comporte des points
    public void emptyTripSegmentTransportType(){
        TripSegment tripSegment = null;
        try{
            tripSegment.computeTransportType();
        }catch(Exception e){
            assertThat(e.getMessage(), is("Le TripSegment n'a pas été initialisé."));
        }}
}


