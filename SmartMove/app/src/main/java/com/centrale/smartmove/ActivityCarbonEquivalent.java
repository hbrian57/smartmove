package com.centrale.smartmove;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Random;


public class ActivityCarbonEquivalent extends AppCompatActivity{

    Week currentWeek ;
    ArrayList<Equivalent> listCarbonEquivalent=null;
    String displaySentence;
    Drawable displayImage;
    TextView textView;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carbon_equivalent);
        this.calculateAndDisplayEquivalent();
        this.textView = findViewById(R.id.comparaison_explication_phrase);
        this.imageView = findViewById(R.id.comparaisonImage);
        textView.setText(displaySentence);
        this.imageView.setImageDrawable(displayImage);
    }

    public void getListImage() {
        if (listCarbonEquivalent==null){
            this.listCarbonEquivalent=new ArrayList<>();
            Equivalent e1= new Equivalent("jeans en coton",0.043, ContextCompat.getDrawable(this, R.drawable.bouteille));
            this.listCarbonEquivalent.add(e1);
            Equivalent e2= new Equivalent("repas végétariens",1.961, ContextCompat.getDrawable(this, R.drawable.repavege));
            this.listCarbonEquivalent.add(e2);
            Equivalent e3= new Equivalent("kms en TGV",578.035, ContextCompat.getDrawable(this, R.drawable.tgv));
            this.listCarbonEquivalent.add(e3);
            Equivalent e4= new Equivalent("t-shirt en coton",0.192, ContextCompat.getDrawable(this, R.drawable.tee));
            this.listCarbonEquivalent.add(e4);
            Equivalent e5= new Equivalent("feuilles de papier",218.141, ContextCompat.getDrawable(this, R.drawable.feuille));
            this.listCarbonEquivalent.add(e5);
            Equivalent e6= new Equivalent("bouteilles d'eau produites",2.208, ContextCompat.getDrawable(this, R.drawable.bouteille));
            this.listCarbonEquivalent.add(e6);
            Equivalent e7= new Equivalent("kilos de volaille",1., ContextCompat.getDrawable(this, R.drawable.prodvol));
            this.listCarbonEquivalent.add(e7);
            Equivalent e8= new Equivalent("kilos de pommes de terre",4.7, ContextCompat.getDrawable(this, R.drawable.prodpatate));
            this.listCarbonEquivalent.add(e8);
            Equivalent e9= new Equivalent("kilos de pain",1.2, ContextCompat.getDrawable(this, R.drawable.prodpain));
            this.listCarbonEquivalent.add(e9);
            Equivalent e10= new Equivalent("kWh d'électricité",4.3, ContextCompat.getDrawable(this, R.drawable.consoelec));
            this.listCarbonEquivalent.add(e10);
            Equivalent e11= new Equivalent("gobelets de café",8.8, ContextCompat.getDrawable(this, R.drawable.gobcafe));
            this.listCarbonEquivalent.add(e11);
    }}

    public void calculateAndDisplayEquivalent() {
        this.getListImage();
        double co2EqOfCurrentWeek = 1;
        Random rand = new Random();
        Equivalent exampleEq = listCarbonEquivalent.get(rand.nextInt(listCarbonEquivalent.size()));
        double ratio = exampleEq.getRatioEq()*co2EqOfCurrentWeek;
        this.displaySentence = "Cette semaine, ma conso de CO2 équivaut à  "+ratio+" "+ exampleEq.getSentenceEq()+".";
        this.displayImage=exampleEq.imageEq;

    }




}