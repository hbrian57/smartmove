package com.centrale.smartmove;

public enum TransportType {
    BIKE("Vélo",0.5f), CAR("Voiture",5f), FOOT("À Pied", 0.1f);

    /** String corresponding to the name of the transport */
    String nameTransport;

    /** Float corresponding to the consumption of CO2 of the transport */
    float CO2param;

    /** Constructor of a mean of transport with two parameters: its name and its consumption of CO2
     * @param name name of the mean of transport
     * @param CO2parameter integer corresponding to the consumption of CO2 of the transport */

    private TransportType (String name, float CO2parameter){
    this.nameTransport= name;
    this.CO2param= CO2parameter;
}

    public float getCO2param() {
        return CO2param;
    }

    public String getNameTransport() {
        return nameTransport;
    }

}

