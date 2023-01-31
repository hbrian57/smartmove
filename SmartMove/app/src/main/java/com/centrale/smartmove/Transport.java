package com.centrale.smartmove;

public class Transport {

    /**
     * String corresponding to the name of the transport
     */
    String nameTransport;
    /**
     * Integer corresponding to the consumption of CO2 of the transport
     */
    float consCO2;

    /**
     * Constructor of a mean of transport with two parameters: its name and its consumption of CO2
     * @param nametr name of the mean of transport
     * @param consocarb integer corresponding to the consumption of CO2 of the transport
     */
    public Transport (String nametr, float consocarb){
    this.nameTransport= nametr;
    this.consCO2= consocarb;
}

    public void setNameTransport(String nameTransport) {
        this.nameTransport = nameTransport;
    }

    public void setConsCO2(int consCO2) {
        this.consCO2 = consCO2;
    }

    public float getConsCO2() {
        return consCO2;
    }

    public String getNameTransport() {
        return nameTransport;
    }
}

