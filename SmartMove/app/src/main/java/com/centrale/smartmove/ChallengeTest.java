package com.centrale.smartmove;

import static org.junit.Assert.*;

import org.junit.Test;

public class ChallengeTest {
    GoalType type1 = GoalType.EXEMPLE;
    ChallengeGoal goal1 = new ChallengeGoal(type1,23.0);
    Challenge challenge1 = new Challenge("challenge1","toto",50.0,goal1,null);

    @Test
    public void getProgressionString() {
       // challenge1.getProgressionString();
        //assertArrayEquals("11.5 parcourue sur 23",challenge1.getProgressionString());

    }

    @Test
    public void getProgressionDouble() {
        assertEquals(challenge1.getProgressionDouble(),50.0,0.01);
    }

    @Test
    public void isCompleted() {
        assertFalse(challenge1.isCompleted());
    }
}