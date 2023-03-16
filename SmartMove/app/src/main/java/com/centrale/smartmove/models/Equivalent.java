package com.centrale.smartmove.models;


import com.centrale.smartmove.R;

public class Equivalent {

    //Attributes------------------------------------------------------------------------------------
    /**
     * String associated with the carbon equivalent
     */
    private String sentenceEq;

    /**
     * double associated with the carbon equivalent
     */
    private double ratioEq;

    /**
     * int that identifies the image
     */
    private int imageID;

    //Constructors----------------------------------------------------------------------------------
    /**
     * Constructor with no parameter
     */
    public Equivalent(){
        this.sentenceEq="repas végétariens";
        this.ratioEq= 1.961;
        this.imageID=  R.drawable.repavege;
    }

    /**
     * Constructor with a String, a double, an integer
     * @param sentence, a String
     * @param ratio, a double
     * @param imageResourceID, an integer
     */
    public Equivalent(String sentence,double ratio, int imageResourceID){
        this.sentenceEq=sentence;
        this.ratioEq=ratio;
        this.imageID=imageResourceID;
    }


//Getters and setters---------------------------------------------------------------------------

    /**
     * Getter
     * @return double ratioEq
     */
    public double getRatioEq() {
        return ratioEq;
    }

    /**
     * Getter
     * @return String sentenceEq
     */
    public String getSentenceEq() {
        return sentenceEq;
    }

    /**
     * Getter
     * @return int imageID
     */
    public int getImageID() {
        return imageID;
    }

    //----------------------------------------------------------------------------------------------
}
