package com.centrale.smartmove;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class activityCarbonEquivalent extends AppCompatActivity {
    Week w ;
    String Eqphrase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carbon_equivalent);
        TextView Texteq = findViewById(R.id.phraseEq);
        Texteq.setText(Eqphrase);
    }

    public Map<Double, Drawable> getListImage() {
        Map<Double, Drawable> list = new HashMap<>();
        list.put(1.961,getResources().getDrawable(R.drawable.repavege));
        list.put(578.035,getResources().getDrawable(R.drawable.tgv));
        list.put(218.341,getResources().getDrawable(R.drawable.feuille));
        list.put(1.,getResources().getDrawable(R.drawable.prodvol));
        list.put(2.208,getResources().getDrawable(R.drawable.bouteille));
        list.put(4.7,getResources().getDrawable(R.drawable.prodpatate));
        list.put(1.2,getResources().getDrawable(R.drawable.prodpain));
        list.put(8.8,getResources().getDrawable(R.drawable.gobcafe));
        list.put(4.3,getResources().getDrawable(R.drawable.consoelec));
        return list;
    }


    public void calculateAndDisplayEquivalent() {
        double co2eqwk =w.getTotalCO2Footprint();
        Map <Double, Drawable> map =getListImage();
        List<Map.Entry<Double,Drawable>> entries = new ArrayList<>(map.entrySet());
        Random rand = new Random();
        Map.Entry<Double, Drawable> randomEntry = entries.get(rand.nextInt(entries.size()));
        double ratio = randomEntry.getKey()*co2eqwk;
        Eqphrase = "Cette semaine, ma conso de CO2 équivaut à : "+ratio;

    }


}