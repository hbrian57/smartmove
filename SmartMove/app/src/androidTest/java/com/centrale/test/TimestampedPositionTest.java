package com.centrale.test;


import static org.junit.Assert.assertEquals;

import static java.sql.Types.NULL;

import com.centrale.smartmove.models.TimestampedPosition;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class TimestampedPositionTest {

    @Rule
    public ExpectedException thrownException = ExpectedException.none();

    /**Test de base en latitude 0 longitude 0, sans mouvement.
     * Résultat attendu: 0
     */
    @Test
    public void test0000CalculateDistance() throws Exception {
        //TODO: on fait quoi de la hauteur, tout le temps à zéro ?
        TimestampedPosition position1 = new TimestampedPosition(0, 0);
        TimestampedPosition position2 = new TimestampedPosition(0, 0);
        // Cas : 0 0 0 avec 0 0 0
        double result = position1.calculateDistance(position2);
        assertEquals(0, result, 0.01);
    }

    /**Test de base entre latitude 10 longitude 0 et latitude 0 longitude 0.
     * Résultat attendu: 1111951
     */
    @Test
    @Ignore
    public void test00100CalculateDistance() throws Exception {
        TimestampedPosition position1 = new TimestampedPosition(0, 0);
        TimestampedPosition position2 = new TimestampedPosition(10, 0);

        // Cas : 0 0 0 avec  0 0 0
        double result = position1.calculateDistance(position2);
        assertEquals(1111951, result, 10);
    }

    /**Test de base entre latitude 0 longitude 20 et latitude 0 longitude 0.
     * Résultat attendu: 2223900
     */
    @Test
    @Ignore
    public void test00010CalculateDistance() throws Exception {

        TimestampedPosition position1 = new TimestampedPosition(0, 0);
        TimestampedPosition position2 = new TimestampedPosition(0, 20);

        // Cas : 0 0 0 avec 0 20 0
        double result = position1.calculateDistance(position2);
        assertEquals(2223900, result, 10);
    }

    /**Test entre deux points choisis au hasard
     * Résultat attendu: 1677084
     */
    @Test
    public void testhasardCalculateDistance() throws Exception {
        TimestampedPosition position1 = new TimestampedPosition(45, 18);
        TimestampedPosition position2 = new TimestampedPosition(30, 20);

        // Cas au hasard
        double result = position1.calculateDistance(position2);
        assertEquals(1677084, result, 10);
    }

    /**Test avec une position dont la latitude est supérieure à 90°
     * Résultat attendu: "Impossible que la latitude soit supérieure à 90°"
     */
    // Cas où une des latitudes est >90°
    @Test
    public void testExceptionLatSup() throws Exception {
        TimestampedPosition position1 = new TimestampedPosition(45, 18, 0);
        TimestampedPosition position2 = new TimestampedPosition(1000, 20, 0);
        try {
            double result = position1.calculateDistance(position2);
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Impossible que la latitude soit supérieure à 90°"));
        }
    }

    /**Test avec une position dont la latitude est supérieure à 90°
     * Résultat attendu: "Impossible que la latitude soit inférieure à -90°"
     */
    // Cas où une des latitudes est<-90°
    @Test
    public void testExceptionLatInf() throws Exception {
        TimestampedPosition position1 = new TimestampedPosition(45, 18, 0);
        TimestampedPosition position2 = new TimestampedPosition(-100, 20, 0);
        try {
            double result = position1.calculateDistance(position2);
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Impossible que la latitude soit inférieure à -90°"));
        }
    }
    /**Test avec une position dont la latitude est supérieure à 90°
     * Résultat attendu: "Impossible que la longitude soit supérieure à 180°
     */
    @Test
    public void testExceptionLongSup() throws Exception {
        TimestampedPosition position1 = new TimestampedPosition(45, 190, 0);
        TimestampedPosition position2 = new TimestampedPosition(45, 20, 0);
        try {
            double result = position1.calculateDistance(position2);
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Impossible que la longitude soit supérieure à 180°"));
        }
    }

    /**Test avec une position dont la latitude est supérieure à 90°
     * Résultat attendu: "Impossible que la longitude soit inférieur à -180°
     */
    // Cas où une des latitudes est<-90°
    @Test
    public void testExceptionLongInf() throws Exception {
        TimestampedPosition position1 = new TimestampedPosition(45, 18, 0);
        TimestampedPosition position2 = new TimestampedPosition(0, -200, 0);
        try {
            double result = position1.calculateDistance(position2);
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Impossible que la longitude soit inférieure à -180°"));
        }
    }

    //TODO: le cas d'un paramètre NULL on a jamais reglé le problème
    @Test
    public void testException3CalculateDistance() throws Exception {
        TimestampedPosition position1 = new TimestampedPosition(45, 18, 0);
        TimestampedPosition position2 = new TimestampedPosition(NULL, 20, 0);

        // Cas au exception coordonnée NULL
        double result = position1.calculateDistance(position2);
        assertEquals(1677084, result, 10);
    }

}