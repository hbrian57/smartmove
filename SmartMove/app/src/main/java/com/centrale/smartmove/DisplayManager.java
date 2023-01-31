package com.centrale.smartmove;


import android.widget.TextView;




public class DisplayManager {
    //Create a display manager that has a state which corresponds to the current window opened
    // the state is an integer that corresponds to the window opened, 0 for the main window, 1 for a challenge detail, 2 for the details of the details of the carbon footprint
    private int state = 0;
    // The display manager creates itself when the application starts
    //create the constructor that sets the state to 0
    public DisplayManager() {
        this.state = 0;
    }

    //create a method that returns the state of the display manager
    public int getState() {
        return state;
    }
    //create a method that modifies the state of the display manager
    public void setState(int state) {
        this.state = state;
    }

    //create a "updateCO2Footprint" method that updates the displayed CO2 footprint
    // the method takes as input the new CO2 footprint as a double
    public void displayCO2Footprint(double newCO2Footprint) {
        //The method should modify the TextView with id "impactTextView" of the impact_tile layout included in activity_dashboard.xml
        //The text that should be displayed is : newCO2Footprint + " kg of CO2eq"
        TextView textViewCO2Footprint = (TextView) findViewById (R.id.impactText);
        textViewCO2Footprint.setText(newCO2Footprint + " tonnes de CO2eq.");
    }
}
