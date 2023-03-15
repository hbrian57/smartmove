package com.centrale.smartmove.ui;


import static java.lang.Double.parseDouble;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.res.Resources;


import com.centrale.smartmove.models.Equivalent;
import com.centrale.smartmove.R;

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
    /**
     * This method launch the carbon equivalent activity.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carbon_equivalent);
        ImageView navToHere = findViewById(R.id.imageViewNavToCarbonEquivalent);
        navToHere.setVisibility(View.INVISIBLE);
        carbonFootprintDisplayed = getIntent().getDoubleExtra(getString(R.string.carbonFootprintDisplayed), 0);
        calculateAndDisplayEquivalent();
        displayCarbonFootprint();
    }


    /**
     * This method randomly selects a picture that corresponds to a carbon equivalent.
     */
    public void getListImage() {
        Resources res = getResources();
        //open the raw ressource file equivalentscarbone.json
        //Create a json object based on this file
        //parse it and create multiple equivalents objects to update listCarbonEquivalent with its content

        if (listCarbonEquivalent == null) {
            this.listCarbonEquivalent = new ArrayList<>();
            Equivalent e2 = new Equivalent(getString(R.string.repas_vege),parseDouble(getString(R.string.repavege_ratio)), R.drawable.repavege);
            this.listCarbonEquivalent.add(e2);
            Equivalent e3 = new Equivalent(getString(R.string.kms_en_tgv), parseDouble(getString(R.string.tgv_ratio)), R.drawable.tgv);
            this.listCarbonEquivalent.add(e3);
            Equivalent e4 = new Equivalent(getString(R.string.tshirt_en_coton), parseDouble(getString(R.string.tee_ratio)), R.drawable.tee);
            this.listCarbonEquivalent.add(e4);
            Equivalent e5 = new Equivalent(getString(R.string.feuilles_de_papier), parseDouble(getString(R.string.feuille_ratio)), R.drawable.feuille);
            this.listCarbonEquivalent.add(e5);
            Equivalent e6 = new Equivalent(getString(R.string.bouteilles_d_eau), parseDouble(getString(R.string.bouteille_ratio)), R.drawable.bouteille);
            this.listCarbonEquivalent.add(e6);
            Equivalent e7 = new Equivalent(getString(R.string.kilos_de_volaille), parseDouble(getString(R.string.prodvol_ratio)), R.drawable.prodvol);
            this.listCarbonEquivalent.add(e7);
            Equivalent e8 = new Equivalent(getString(R.string.pomme2terre), parseDouble(getString(R.string.prodpatate_ratio)),  R.drawable.prodpatate);
            this.listCarbonEquivalent.add(e8);
            Equivalent e9 = new Equivalent(getString(R.string.kg_2_pain), parseDouble(getString(R.string.prodpain_ratio)), R.drawable.prodpain);
            this.listCarbonEquivalent.add(e9);
            Equivalent e10 = new Equivalent(getString(R.string.kwh_elec), parseDouble(getString(R.string.consoelec_ratio)),  R.drawable.consoelec);
            this.listCarbonEquivalent.add(e10);
            Equivalent e11 = new Equivalent(getString(R.string.gobelets_cafe),parseDouble(getString(R.string.gobcafe_ratio)),R.drawable.gobcafe);
            this.listCarbonEquivalent.add(e11);
        }


    }

    /**
     * this method calculate and display the equivalent in accordance with the picture randomly chosen.
     */
    public void calculateAndDisplayEquivalent() {
        this.getListImage();
        double co2EqOfCurrentWeek = carbonFootprintDisplayed;
        Random rand = new Random();
        Equivalent exampleEq = listCarbonEquivalent.get(rand.nextInt(listCarbonEquivalent.size()));
        double ratio = exampleEq.getRatioEq() * co2EqOfCurrentWeek;
        ratio = Math.round(ratio*100.0)/100.0;
        this.displaySentence = getString(R.string.equivaut_a_semaine) + " " + ratio + " " + exampleEq.getSentenceEq() + ".";
        int imageEqID = exampleEq.getImageID();
        this.displayImage = ContextCompat.getDrawable(this, imageEqID);
        this.textView = findViewById(R.id.comparaison_explication_phrase);
        this.imageView = findViewById(R.id.comparaisonImage);
        textView.setText(displaySentence);
        this.imageView.setImageDrawable(displayImage);
    }

    /**
     * It displays a link where the calculation is explained.
     * @param v : the view.
     */
    public void viewCalculationExplanation(View v) {
        String url = "https://gitlab.univ-eiffel.fr/teamgeoloc/smartloc/-/wikis/What-is-Smartmove";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);

    }

    /**
     * It displays the carbon footprint of the current week.
     */
    public void displayCarbonFootprint(){
        // Double carbonFootprint = user.calculateCurrentWeekCarbonFootprint();
        TextView textView = findViewById(R.id.impactTextDashboard);

        textView.setText(carbonFootprintDisplayed + getString(R.string.tonnes_2_CO2eq));
    }
}




