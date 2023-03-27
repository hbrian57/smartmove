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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        if(listCarbonEquivalent.size()!=0) {
            return;
        }
        Resources res = getResources();
        try {
            InputStream locationdesimages = res.openRawResource(R.raw.equivalentscarbone);
            BufferedReader reader = new BufferedReader(new InputStreamReader(locationdesimages));
            StringBuilder jsonStringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonStringBuilder.append(line);
            }
            reader.close();
            locationdesimages.close();

            JSONObject json = new JSONObject(jsonStringBuilder.toString());
            JSONArray equivalents = json.getJSONArray("equivalents");

            for (int i = 0; i < equivalents.length(); i++) {
                JSONObject equivalent = equivalents.getJSONObject(i);
                Equivalent carbonEquivalent = new Equivalent(
                        equivalent.getString("sentence"),
                        equivalent.getDouble("ratio"),
                        equivalent.getInt("imageID")
                );
                listCarbonEquivalent.add(carbonEquivalent);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
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




