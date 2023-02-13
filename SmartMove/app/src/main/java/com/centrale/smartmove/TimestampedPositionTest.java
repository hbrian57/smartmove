package com.centrale.smartmove;


import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TimestampedPositionTest {

        @Test
        public void testCalculateDistance() throws Exception{
            // TODO: l'erreur de degré supérieur à 90. FAIT !
            //TODO: on fait quoi de la hauteur, tout le temps à zéro ?

            TimestampedPosition position1 = new TimestampedPosition(0, 0, 0);
            TimestampedPosition position2 = new TimestampedPosition(0, 0, 0);
            TimestampedPosition position3 = new TimestampedPosition(10, 0, 0);
            TimestampedPosition position4 = new TimestampedPosition(0, 20, 0);
            TimestampedPosition position5 = new TimestampedPosition(45, 18, 0);
            TimestampedPosition position6 = new TimestampedPosition(30, 20, 0);
            TimestampedPosition position7 = new TimestampedPosition(1000, 20, 0);

            // Cas : 0 0 0 avec 0 0 0
            double result = position1.calculateDistance(position2);
            //Cas 0 0 avec 10 0
            double result0 = position1.calculateDistance(position3);
            //Cas 0 0 avec 0 10
            double result1 = position1.calculateDistance(position4);
            //TODO: a faire car on a pas traité le problème des heights
            double result2 = position1.calculateDistance(position5);
            //Cas normal: deux positions au hasard
            double result3 = position5.calculateDistance(position6);
            //Cas où on lève une exception: latitude ou longitude >90 degré
            double result4 = position7.calculateDistance(position6);




            assertEquals(0,result, 0.01);
            assertEquals(1111951,result0, 10);
            assertEquals(2223900,result1, 10);
            assertEquals(1677084,result3,10 );

        }
}

