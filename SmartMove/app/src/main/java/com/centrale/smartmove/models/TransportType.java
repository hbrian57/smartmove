package com.centrale.smartmove.models;

import androidx.annotation.StringRes;

import com.centrale.smartmove.R;

public enum TransportType {

    //Instances-------------------------------------------------------------------------------------
    STATIC (R.string.transportTypeStatic,0.0f),
    BIKE(R.string.transportTypeBike,0.5f),
    CAR(R.string.transportTypeCar,5f),
    WALKING(R.string.transportTypeFoot, 0.1f);

    //Attributes------------------------------------------------------------------------------------
    /**
     *Int corresponding to the identification of the name of the TransportType
     */
    private final @StringRes int nameTransport;

    /** Float corresponding to the consumption of CO2 of the transport */
    private final float CO2param;

    //Getters and Setters---------------------------------------------------------------------------
    /**
     * Getter
     * @return float CO2param
     */
    public float getCO2param() {
        return CO2param;
    }


    //Constructors----------------------------------------------------------------------------------
    /** Constructor of a TransportType with two parameters: its name and its consumption of CO2
     * @param name name of the mean of transport
     * @param CO2parameter integer corresponding to the consumption of CO2 of the transport */
    private TransportType (@StringRes Integer name, float CO2parameter){
        this.nameTransport= name;
        this.CO2param= CO2parameter;
    }

    //----------------------------------------------------------------------------------------------
}

