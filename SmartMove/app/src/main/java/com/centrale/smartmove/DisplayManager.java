package com.centrale.smartmove;


import android.widget.TextView;


/**
 * @author arochut
 *
 */
public class DisplayManager {

    private DashboardActivity parentDashboard;

    public DisplayManager(DashboardActivity dashboard) {
        this.parentDashboard = dashboard;
    }

    public void displayCO2Footprint(double newCO2Footprint) {
        TextView textViewCO2Footprint = (TextView) parentDashboard.getEditableObjects().get(R.id.impactText);
        textViewCO2Footprint.setText(newCO2Footprint + " tonnes de CO2eq.");
    }

    public void updateDefiDisplay(User user){
    }
}
