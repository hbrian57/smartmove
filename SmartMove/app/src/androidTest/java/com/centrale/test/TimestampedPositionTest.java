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

import java.sql.Timestamp;


public class TimestampedPositionTest {

    @Rule
    public ExpectedException thrownException = ExpectedException.none();

    /**
     * Test de base en latitude 0 longitude 0, sans mouvement.
     * Résultat attendu: 0
     */
    @Test
    public void test0000CalculateDistance() throws Exception {
        TimestampedPosition position1 = new TimestampedPosition(0., 0.);
        TimestampedPosition position2 = new TimestampedPosition(0., 0.);
        // Cas : 0 0 0 avec 0 0 0

        double result = position1.calculateDistance(position2);
        assertEquals(0., result, 0.01);
    }

    /**
     * Test de base entre latitude 10 longitude 0 et latitude 0 longitude 0.
     * Résultat attendu: 1111951
     */
    @Test

    public void test00100CalculateDistance() throws Exception {
        TimestampedPosition position1 = new TimestampedPosition(0, 0);
        TimestampedPosition position2 = new TimestampedPosition(10, 0);

        // Cas : 0 0 0 avec  0 0 0
        double result = position1.calculateDistance(position2);
        assertEquals(1113194., result, 10);
    }

    /**
     * Test de base entre latitude 0 longitude 20 et latitude 0 longitude 0.
     * Résultat attendu: 2223900
     */
    @Test

    public void test00010CalculateDistance() throws Exception {

        TimestampedPosition position1 = new TimestampedPosition(0, 0);
        TimestampedPosition position2 = new TimestampedPosition(0, 20);

        // Cas : 0 0 0 avec 0 20 0
        double result = position1.calculateDistance(position2);
        assertEquals(2226389, result, 10);
    }

    /**
     * Test entre deux points choisis au hasard
     * Résultat attendu: 1677084
     */
    @Test
    public void testhasardCalculateDistance() throws Exception {
        TimestampedPosition position1 = new TimestampedPosition(45, 18);
        TimestampedPosition position2 = new TimestampedPosition(30, 20);

        // Cas au hasard
        double result = position1.calculateDistance(position2);
        assertEquals(1678960, result, 10);
    }
}





