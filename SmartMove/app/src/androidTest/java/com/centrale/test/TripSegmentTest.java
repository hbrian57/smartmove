package com.centrale.test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.centrale.smartmove.models.TimestampedPosition;
import com.centrale.smartmove.models.TransportType;
import com.centrale.smartmove.models.TripSegment;

import org.junit.Test;

import java.util.Date;
import java.util.LinkedList;

public class TripSegmentTest {


    /**Test de la fonction de calcul de distance entre trois positions similaires : lat= 0, long=0
     * Résultat attendu: 0
     */
    @Test
    public void testCalculateTotalDistance0() throws Exception {
        // chemin du RC à St mihiel 824 metres
        Date date = new Date(0);
        int seconde = 10;
        LinkedList<TimestampedPosition> listOfPosition= new LinkedList<>();
        listOfPosition.add(new TimestampedPosition(47.218959, -1.547731, 0));
        listOfPosition.add(new TimestampedPosition(47.218733, -1.548331, 0));
        listOfPosition.add(new TimestampedPosition(47.218686, -1.548455, 0));
        listOfPosition.add(new TimestampedPosition(47.218652, -1.548546, 0));
        listOfPosition.add(new TimestampedPosition(47.218641, -1.548575, 0));
        listOfPosition.add(new TimestampedPosition(47.218636, -1.548589, 0));
        listOfPosition.add(new TimestampedPosition(47.218624, -1.548621, 0));
        listOfPosition.add(new TimestampedPosition(47.219188, -1.549082, 0));
        listOfPosition.add(new TimestampedPosition(47.219223, -1.549111, 0));
        listOfPosition.add(new TimestampedPosition(47.219407, -1.549271, 0));
        listOfPosition.add(new TimestampedPosition(47.21943, -1.54929, 0));
        listOfPosition.add(new TimestampedPosition(47.219504, -1.549354, 0));
        listOfPosition.add(new TimestampedPosition(47.219697, -1.549515, 0));
        listOfPosition.add(new TimestampedPosition(47.219815, -1.549615, 0));
        listOfPosition.add(new TimestampedPosition(47.21989, -1.549666, 0));
        listOfPosition.add(new TimestampedPosition(47.219903, -1.549675, 0));
        listOfPosition.add(new TimestampedPosition(47.220042, -1.549792, 0));
        listOfPosition.add(new TimestampedPosition(47.220443, -1.550124, 0));
        listOfPosition.add(new TimestampedPosition(47.220934, -1.550526, 0));
        listOfPosition.add(new TimestampedPosition(47.221236, -1.550774, 0));
        listOfPosition.add(new TimestampedPosition(47.221456, -1.550955, 0));
        listOfPosition.add(new TimestampedPosition(47.221557, -1.551039, 0));
        listOfPosition.add(new TimestampedPosition(47.221574, -1.551051, 0));
        listOfPosition.add(new TimestampedPosition(47.221637, -1.551115, 0));
        listOfPosition.add(new TimestampedPosition(47.221674, -1.551218, 0));
        listOfPosition.add(new TimestampedPosition(47.222299, -1.551701, 0));
        listOfPosition.add(new TimestampedPosition(47.222549, -1.551905, 0));
        listOfPosition.add(new TimestampedPosition(47.222803, -1.552146, 0));
        listOfPosition.add(new TimestampedPosition(47.222889, -1.552208, 0));
        listOfPosition.add(new TimestampedPosition(47.222924, -1.552199, 0));
        listOfPosition.add(new TimestampedPosition(47.222962, -1.55221, 0));
        listOfPosition.add(new TimestampedPosition(47.222995, -1.552241, 0));
        listOfPosition.add(new TimestampedPosition(47.223018, -1.552288, 0));
        listOfPosition.add(new TimestampedPosition(47.223027, -1.552355, 0));
        listOfPosition.add(new TimestampedPosition(47.223017, -1.552419, 0));
        listOfPosition.add(new TimestampedPosition(47.222988, -1.552471, 0));
        listOfPosition.add(new TimestampedPosition(47.222972, -1.552487, 0));
        listOfPosition.add(new TimestampedPosition(47.222915, -1.552638, 0));
        listOfPosition.add(new TimestampedPosition(47.222888, -1.552933, 0));
        listOfPosition.add(new TimestampedPosition(47.222856, -1.553195, 0));
        listOfPosition.add(new TimestampedPosition(47.222854, -1.553207, 0));
        listOfPosition.add(new TimestampedPosition(47.222852, -1.553231, 0));
        listOfPosition.add(new TimestampedPosition(47.222904, -1.553243, 0));
        listOfPosition.add(new TimestampedPosition(47.222963, -1.553255, 0));
        listOfPosition.add(new TimestampedPosition(47.223037, -1.553259, 0));
        listOfPosition.add(new TimestampedPosition(47.223063, -1.553265, 0));
        listOfPosition.add(new TimestampedPosition(47.223239, -1.553302, 0));
        listOfPosition.add(new TimestampedPosition(47.22349, -1.553355, 0));
        listOfPosition.add(new TimestampedPosition(47.223627, -1.553385, 0));
        listOfPosition.add(new TimestampedPosition(47.223624, -1.553408, 0));
        listOfPosition.add(new TimestampedPosition(47.22362, -1.553445, 0));
        listOfPosition.add(new TimestampedPosition(47.223614, -1.553488, 0));
        listOfPosition.add(new TimestampedPosition(47.22361, -1.553524, 0));
        listOfPosition.add(new TimestampedPosition(47.223606, -1.553559, 0));
        listOfPosition.add(new TimestampedPosition(47.223617, -1.553578, 0));
        listOfPosition.add(new TimestampedPosition(47.223664, -1.553558, 0));
        TripSegment segment = new TripSegment(TransportType.valueOf("CAR"), listOfPosition);

        double expectedDistance = 800;
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

    @Test
    //Test sur un trajet réel
    public void testRealTripCalculateTotalDistance(){
        LinkedList<TimestampedPosition> listOfPosition=new LinkedList<TimestampedPosition>();
        TripSegment tripSegment=new TripSegment(TransportType.STATIC,listOfPosition);
        listOfPosition.add(new TimestampedPosition(47.218959, -1.547731, 0));
        listOfPosition.add(new TimestampedPosition(47.218733, -1.548331, 0));
        listOfPosition.add(new TimestampedPosition(47.218686, -1.548455, 0));
        listOfPosition.add(new TimestampedPosition(47.218652, -1.548546, 0));
        listOfPosition.add(new TimestampedPosition(47.218641, -1.548575, 0));
        listOfPosition.add(new TimestampedPosition(47.218636, -1.548589, 0));
        listOfPosition.add(new TimestampedPosition(47.218624, -1.548621, 0));
        listOfPosition.add(new TimestampedPosition(47.219188, -1.549082, 0));
        listOfPosition.add(new TimestampedPosition(47.219223, -1.549111, 0));
        listOfPosition.add(new TimestampedPosition(47.219407, -1.549271, 0));
        listOfPosition.add(new TimestampedPosition(47.21943, -1.54929, 0));
        listOfPosition.add(new TimestampedPosition(47.219504, -1.549354, 0));
        listOfPosition.add(new TimestampedPosition(47.219697, -1.549515, 0));
        listOfPosition.add(new TimestampedPosition(47.219815, -1.549615, 0));
        listOfPosition.add(new TimestampedPosition(47.21989, -1.549666, 0));
        listOfPosition.add(new TimestampedPosition(47.219903, -1.549675, 0));
        listOfPosition.add(new TimestampedPosition(47.220042, -1.549792, 0));
        listOfPosition.add(new TimestampedPosition(47.220443, -1.550124, 0));
        listOfPosition.add(new TimestampedPosition(47.220934, -1.550526, 0));
        listOfPosition.add(new TimestampedPosition(47.221236, -1.550774, 0));
        listOfPosition.add(new TimestampedPosition(47.221456, -1.550955, 0));
        listOfPosition.add(new TimestampedPosition(47.221557, -1.551039, 0));
        listOfPosition.add(new TimestampedPosition(47.221574, -1.551051, 0));
        listOfPosition.add(new TimestampedPosition(47.221637, -1.551115, 0));
        listOfPosition.add(new TimestampedPosition(47.221674, -1.551218, 0));
        listOfPosition.add(new TimestampedPosition(47.222299, -1.551701, 0));
        listOfPosition.add(new TimestampedPosition(47.222549, -1.551905, 0));
        listOfPosition.add(new TimestampedPosition(47.222803, -1.552146, 0));
        listOfPosition.add(new TimestampedPosition(47.222889, -1.552208, 0));
        listOfPosition.add(new TimestampedPosition(47.222924, -1.552199, 0));
        listOfPosition.add(new TimestampedPosition(47.222962, -1.55221, 0));
        listOfPosition.add(new TimestampedPosition(47.222995, -1.552241, 0));
        listOfPosition.add(new TimestampedPosition(47.223018, -1.552288, 0));
        listOfPosition.add(new TimestampedPosition(47.223027, -1.552355, 0));
        listOfPosition.add(new TimestampedPosition(47.223017, -1.552419, 0));
        listOfPosition.add(new TimestampedPosition(47.222988, -1.552471, 0));
        listOfPosition.add(new TimestampedPosition(47.222972, -1.552487, 0));
        listOfPosition.add(new TimestampedPosition(47.222915, -1.552638, 0));
        listOfPosition.add(new TimestampedPosition(47.222888, -1.552933, 0));
        listOfPosition.add(new TimestampedPosition(47.222856, -1.553195, 0));
        listOfPosition.add(new TimestampedPosition(47.222854, -1.553207, 0));
        listOfPosition.add(new TimestampedPosition(47.222852, -1.553231, 0));
        listOfPosition.add(new TimestampedPosition(47.222904, -1.553243, 0));
        listOfPosition.add(new TimestampedPosition(47.222963, -1.553255, 0));
        listOfPosition.add(new TimestampedPosition(47.223037, -1.553259, 0));
        listOfPosition.add(new TimestampedPosition(47.223063, -1.553265, 0));
        listOfPosition.add(new TimestampedPosition(47.223239, -1.553302, 0));
        listOfPosition.add(new TimestampedPosition(47.22349, -1.553355, 0));
        listOfPosition.add(new TimestampedPosition(47.223627, -1.553385, 0));
        listOfPosition.add(new TimestampedPosition(47.223624, -1.553408, 0));
        listOfPosition.add(new TimestampedPosition(47.22362, -1.553445, 0));
        listOfPosition.add(new TimestampedPosition(47.223614, -1.553488, 0));
        listOfPosition.add(new TimestampedPosition(47.22361, -1.553524, 0));
        listOfPosition.add(new TimestampedPosition(47.223606, -1.553559, 0));
        listOfPosition.add(new TimestampedPosition(47.223617, -1.553578, 0));
        listOfPosition.add(new TimestampedPosition(47.223664, -1.553558, 0));
        double expectedDistance=800;
        double actualDistance=tripSegment.calculateTotalDistance();

        assertEquals(expectedDistance,actualDistance,50);

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
        TripSegment tripSegment = new TripSegment(TransportType.STATIC);
        try{
            tripSegment.computeTransportType();
        }catch(Exception e){
        assertThat(e.getMessage(), is("Le TripSegment n'a pas été initialisé."));
    }}
    /*
    @Test
    //Test si le TripSegment comporte des points
    public void emptyTripSegmentTransportType(){
        TripSegment tripSegment = new TripSegment(TransportType.STATIC);
        try{
            tripSegment.calculateMeanVelocity();
        }catch(Exception e){
            assertThat(e.getMessage(), is("La liste des TimestampedPositions est vide."));
        }}*/




}


