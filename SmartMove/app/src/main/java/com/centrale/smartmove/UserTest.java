package com.centrale.smartmove;

import static org.junit.Assert.*;

import org.junit.Test;

public class UserTest {

    @Test
    public void updateChallenges() {
        User u1 = new User();
        try {
            u1.updateChallenges();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}