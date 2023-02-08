package com.centrale.smartmove;

import static androidx.appcompat.graphics.drawable.DrawableContainerCompat.Api21Impl.getResources;

import android.graphics.drawable.Drawable;

public class Equivalent {

    private String sentenceEq;
    private double ratioEq;
    private Drawable imageEq;

    public Equivalent(){
        this.sentenceEq="repas végétariens";
        this.ratioEq=1.961;
        this.imageEq=getResources().getDrawable(R.drawable.repavege);
    }

    public Equivalent(String sentence,double ratio,Drawable image){
        this.sentenceEq=sentence;
        this.ratioEq=ratio;
        this.imageEq=image;
    }

    //Getters and setters---------------------------------------------------------------------------

    public double getRatioEq() {
        return ratioEq;
    }

    public void setRatioEq(double ratioEq) {
        this.ratioEq = ratioEq;
    }

    public Drawable getImageEq() {
        return imageEq;
    }

    public void setImageEq(Drawable imageEq) {
        this.imageEq = imageEq;
    }

    public String getSentenceEq() {
        return sentenceEq;
    }

    public void setSentenceEq(String sentenceEq) {
        this.sentenceEq = sentenceEq;
    }
}
