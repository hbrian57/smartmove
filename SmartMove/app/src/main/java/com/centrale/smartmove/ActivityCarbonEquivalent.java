package com.centrale.smartmove;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.apache.tools.ant.types.Resource;

import java.util.ArrayList;
import java.util.Random;


public class ActivityCarbonEquivalent extends AppCompatActivity {

    Double carbonFootprintDisplayed;

    ArrayList<Equivalent> listCarbonEquivalent = null;
    String displaySentence;
    Drawable displayImage;
    TextView textView;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carbon_equivalent);
        ImageView navToHere = findViewById(R.id.imageViewNavToCarbonEquivalent);
        navToHere.setVisibility(View.INVISIBLE);
        carbonFootprintDisplayed = getIntent().getDoubleExtra(getString(R.string.carbonFootprintDisplayed), 0);


        calculateAndDisplayEquivalent();
        displayCarbonFootprint();

    }

    public void getListImage() {
        if (listCarbonEquivalent == null) {
            this.listCarbonEquivalent = new ArrayList<>();
            Equivalent e2 = new Equivalent(getString(R.string.repas_vege), 1.961, ContextCompat.getDrawable(this, R.drawable.repavege));
            this.listCarbonEquivalent.add(e2);
            Equivalent e3 = new Equivalent(getString(R.string.kms_en_tgv), 578.035, ContextCompat.getDrawable(this, R.drawable.tgv));
            this.listCarbonEquivalent.add(e3);
            Equivalent e4 = new Equivalent(getString(R.string.tshirt_en_coton), 0.192, ContextCompat.getDrawable(this, R.drawable.tee));
            this.listCarbonEquivalent.add(e4);
            Equivalent e5 = new Equivalent(getString(R.string.feuilles_de_papier), 218.141, ContextCompat.getDrawable(this, R.drawable.feuille));
            this.listCarbonEquivalent.add(e5);
            Equivalent e6 = new Equivalent(getString(R.string.bouteilles_d_eau), 2.208, ContextCompat.getDrawable(this, R.drawable.bouteille));
            this.listCarbonEquivalent.add(e6);
            Equivalent e7 = new Equivalent(getString(R.string.kilos_de_volaille), 1., ContextCompat.getDrawable(this, R.drawable.prodvol));
            this.listCarbonEquivalent.add(e7);
            Equivalent e8 = new Equivalent(getString(R.string.pomme2terre), 4.7, ContextCompat.getDrawable(this, R.drawable.prodpatate));
            this.listCarbonEquivalent.add(e8);
            Equivalent e9 = new Equivalent(getString(R.string.kg_2_pain), 1.2, ContextCompat.getDrawable(this, R.drawable.prodpain));
            this.listCarbonEquivalent.add(e9);
            Equivalent e10 = new Equivalent(getString(R.string.kwh_elec), 4.3, ContextCompat.getDrawable(this, R.drawable.consoelec));
            this.listCarbonEquivalent.add(e10);
            Equivalent e11 = new Equivalent(getString(R.string.gobelets_cafe), 8.8, ContextCompat.getDrawable(this, R.drawable.gobcafe));
            this.listCarbonEquivalent.add(e11);
        }
    }

    public void calculateAndDisplayEquivalent() {
        this.getListImage();
        double co2EqOfCurrentWeek = carbonFootprintDisplayed;
        Random rand = new Random();
        Equivalent exampleEq = listCarbonEquivalent.get(rand.nextInt(listCarbonEquivalent.size()));
        double ratio = exampleEq.getRatioEq() * co2EqOfCurrentWeek;
        ratio = Math.round(ratio*100.0)/100.0;
        this.displaySentence = getString(R.string.equivaut_a_semaine) + " " + ratio + " " + exampleEq.getSentenceEq() + ".";
        this.displayImage = exampleEq.imageEq;
        this.textView = findViewById(R.id.comparaison_explication_phrase);
        this.imageView = findViewById(R.id.comparaisonImage);
        textView.setText(displaySentence);
        this.imageView.setImageDrawable(displayImage);

    }

    public void viewCalculationExplanation(View v) {
        String url = getString(R.string.url_explication_conso_carbone);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
        ;
    }

    public void displayCarbonFootprint(){
        // Double carbonFootprint = user.calculateCurrentWeekCarbonFootprint();
        TextView textView = findViewById(R.id.impactTextDashboard);

        textView.setText(carbonFootprintDisplayed + getString(R.string.tonnes_2_CO2eq));
    }
}




