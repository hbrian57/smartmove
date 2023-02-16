package com.centrale.test;


import static org.junit.Assert.assertEquals;

import static java.sql.Types.NULL;

import com.centrale.smartmove.TimestampedPosition;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class TimestampedPositionTest {

    @Rule
    public ExpectedException thrownException = ExpectedException.none();

    @Test
    public void test0000CalculateDistance() throws Exception {
        //TODO: on fait quoi de la hauteur, tout le temps à zéro ?
        TimestampedPosition position1 = new TimestampedPosition(0, 0, 0);
        TimestampedPosition position2 = new TimestampedPosition(0, 0, 0);

        // Cas : 0 0 0 avec 0 0 0
        double result = position1.calculateDistance(position2);
        assertEquals(0, result, 0.01);
    }

    @Test
    @Ignore
    public void test00100CalculateDistance() throws Exception {
        TimestampedPosition position1 = new TimestampedPosition(0, 0, 0);
        TimestampedPosition position2 = new TimestampedPosition(10, 0, 0);

        // Cas : 0 0 0 avec  0 0 0
        double result = position1.calculateDistance(position2);
        assertEquals(1111951, result, 10);
    }

    @Test
    @Ignore
    public void test00010CalculateDistance() throws Exception {

        TimestampedPosition position1 = new TimestampedPosition(0, 0, 0);
        TimestampedPosition position2 = new TimestampedPosition(0, 20, 0);

        // Cas : 0 0 0 avec 0 20 0
        double result = position1.calculateDistance(position2);
        assertEquals(2223900, result, 10);
    }


    @Test
    public void testhasardCalculateDistance() throws Exception {
        TimestampedPosition position1 = new TimestampedPosition(45, 18, 0);
        TimestampedPosition position2 = new TimestampedPosition(30, 20, 0);

        // Cas au hasard
        double result = position1.calculateDistance(position2);
        assertEquals(1677084, result, 10);
    }

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

    @Test
    public void testException2CalculateDistance() throws Exception {
        TimestampedPosition position1 = new TimestampedPosition(45, 18, 0);
        TimestampedPosition position2 = new TimestampedPosition(-10, 20, 0);

        // Cas au exception <0 degré
        double result = position1.calculateDistance(position2);
        assertEquals(1677084, result, 10);
    }

    @Test
    public void testException3CalculateDistance() throws Exception {
        TimestampedPosition position1 = new TimestampedPosition(45, 18, 0);
        TimestampedPosition position2 = new TimestampedPosition(NULL, 20, 0);

        // Cas au exception coordonnée NULL
        double result = position1.calculateDistance(position2);
        assertEquals(1677084, result, 10);
    }

}