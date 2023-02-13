package com.centrale.smartmove;


import static org.junit.Assert.assertEquals;

import static java.sql.Types.NULL;

import org.junit.Test;

public class TimestampedPositionTest {

        @Test
        public void test0000CalculateDistance() throws Exception{
            //TODO: on fait quoi de la hauteur, tout le temps à zéro ?
            TimestampedPosition position1 = new TimestampedPosition(0, 0, 0);
            TimestampedPosition position2 = new TimestampedPosition(0, 0, 0);

            // Cas : 0 0 0 avec 0 0 0
            double result = position1.calculateDistance(position2);
            assertEquals(0,result, 0.01);
        }

    @Test
    public void test00100CalculateDistance() throws Exception{
        TimestampedPosition position1 = new TimestampedPosition(0, 0, 0);
        TimestampedPosition position2 = new TimestampedPosition(10, 0, 0);

        // Cas : 0 0 0 avec  0 0 0
        double result = position1.calculateDistance(position2);
        assertEquals(1111951,result, 10);
        }

    @Test
    public void test00010CalculateDistance() throws Exception{
        TimestampedPosition position1 = new TimestampedPosition(0, 0, 0);
        TimestampedPosition position2 = new TimestampedPosition(0, 20, 0);

        // Cas : 0 0 0 avec 0 20 0
        double result = position1.calculateDistance(position2);
        assertEquals(2223900,result, 10);
    }

    @Test
    public void testhasardCalculateDistance() throws Exception{
        TimestampedPosition position1 = new TimestampedPosition(45, 18, 0);
        TimestampedPosition position2 = new TimestampedPosition(30, 20, 0);

        // Cas au hasard
        double result = position1.calculateDistance(position2);
        assertEquals(1677084,result, 10);
    }

    @Test
    public void testException1CalculateDistance() throws Exception{
        TimestampedPosition position1 = new TimestampedPosition(45, 18, 0);
        TimestampedPosition position2 = new TimestampedPosition(1000, 20, 0);

        // Cas au exception >90 degré
        double result = position1.calculateDistance(position2);
        assertEquals(1677084,result, 10);
    }

    @Test
    public void testException2CalculateDistance() throws Exception{
        TimestampedPosition position1 = new TimestampedPosition(45, 18, 0);
        TimestampedPosition position2 = new TimestampedPosition(-10, 20, 0);

        // Cas au exception <0 degré
        double result = position1.calculateDistance(position2);
        assertEquals(1677084,result, 10);
    }

    @Test
    public void testException3CalculateDistance() throws Exception{
        TimestampedPosition position1 = new TimestampedPosition(45, 18, 0);
        TimestampedPosition position2 = new TimestampedPosition(NULL, 20, 0);

        // Cas au exception coordonnée NULL
        double result = position1.calculateDistance(position2);
        assertEquals(1677084,result, 10);
    }
}