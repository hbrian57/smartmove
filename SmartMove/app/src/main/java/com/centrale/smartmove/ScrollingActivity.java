package com.centrale.smartmove;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.centrale.smartmove.databinding.ActivityScrollingBinding;

public class ScrollingActivity extends AppCompatActivity {

    private ActivityScrollingBinding binding;
    private static final String LOG_TAG = ScrollingActivity.class.getSimpleName();
    Button btValidate;
    CheckBox cbAccept;
    LinearLayout texteCGU;
    Boolean gcuAccepted=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScrollingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        cbAccept=(CheckBox) findViewById(R.id.cbaccept);
        btValidate=(Button) findViewById(R.id.btvalidate);
        texteCGU=(LinearLayout) findViewById(R.id.layoutCGU);
        String[] titres = getResources().getStringArray(R.array.titresCGU);
        String[] textes = getResources().getStringArray(R.array.contentCGU);
        btValidate.setAlpha((float) 0.2);
        //on crée 2 textview par paire de titre/texte
        //on les ajoute au layout texteCGU
        //on applique le style "CGUtitre" au titre et "CGUtexte" au texte
        for (int i=0;i<titres.length;i++){
            TextView tvTitre = new TextView(this);
            tvTitre.setText(titres[i]);
            tvTitre.setTextAppearance(R.style.CGUtitre);
            LinearLayout newLayout = new LinearLayout(this);
            TextView tvTexte = new TextView(this);
            tvTexte.setText(textes[i]);
            tvTexte.setTextAppearance(R.style.CGUtexte);
            newLayout.addView(tvTitre);
            newLayout.addView(tvTexte);
            newLayout.setPadding(15,0,15,50);
            newLayout.setOrientation(LinearLayout.VERTICAL);
            texteCGU.addView(newLayout);
        }
        //avant ci-dessous, lire cguaccepted dans les properties.
        btValidate.setEnabled(gcuAccepted);

    }



    public void onButtonClicked(View view) {
        Log.d(LOG_TAG, "Validé");
        SharedPreferences sharedPref = getSharedPreferences("SMARTMOVE",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("acceptedGCU", 1);
        editor.apply();
        Intent intent = new Intent(this,DashboardActivity.class);
        startActivity(intent);

    }



    public void onCheckBoxClicked(View view) {
        btValidate.setEnabled(((CheckBox)view).isChecked());
        if (((CheckBox)view).isChecked()){
            btValidate.setAlpha(1);
        } else {
            btValidate.setAlpha((float)0.2);
        }
    }
}
