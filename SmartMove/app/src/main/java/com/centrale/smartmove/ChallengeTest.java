package com.centrale.smartmove;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;

public class ChallengeTest {


    @Test
    public void getProgressionString() {
       // challenge1.getProgressionString();
        //assertArrayEquals("11.5 parcourue sur 23",challenge1.getProgressionString());

    }

    @Test
    public void getProgressionDouble() {
        Date date1=  new Date(System.currentTimeMillis());
        GoalType type1 = GoalType.EXEMPLE;
        ChallengeGoal goal1 = new ChallengeGoal(type1,23.0);
        Challenge challenge1 = new Challenge("challenge1","toto",50.0,goal1,date1);
        System.out.println(challenge1.getProgressionDouble());
    }

    @Test @Ignore
    public void isCompleted() {
       // assertFalse(challenge1.isCompleted());
    }
}