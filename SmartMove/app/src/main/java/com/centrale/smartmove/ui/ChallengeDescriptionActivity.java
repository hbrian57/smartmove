package com.centrale.smartmove.ui;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.centrale.smartmove.R;
import com.centrale.smartmove.models.Challenge;

import org.json.JSONException;
import org.json.JSONObject;

public class ChallengeDescriptionActivity extends AppCompatActivity {

    TextView challengeTitle;
    TextView challengeDescription;
    TextView challengeLongDescription;
    TextView challengeProgressString;
    ImageView challengeImage;
    ProgressBar progress;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.challenge_description_layout);

        challengeTitle = (TextView) findViewById(R.id.challengeTitle);
        challengeDescription = (TextView) findViewById(R.id.challengeShortDescription);
        challengeLongDescription = (TextView) findViewById(R.id.challengeLongDescription);
        challengeImage = (ImageView) findViewById(R.id.challengeIcon);
        challengeProgressString = (TextView) findViewById(R.id.challengeProgressionString);
        progress = (ProgressBar) findViewById(R.id.challengeProgressBar);

        Challenge challenge = new Challenge();
        String challengeJSON = getIntent().getStringExtra("challenge");
        try {
            JSONObject challengeJson = new JSONObject(challengeJSON);
            challenge.loadFromSave(challengeJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        challengeTitle.setText(challenge.getTitle());
        challengeDescription.setText(challenge.getShort_description());
        challengeLongDescription.setText(challenge.getLong_description());
        challengeImage.setImageResource(challenge.getIcon());
        challengeProgressString.setText(challenge.getProgressionString());
        progress.setProgress(challenge.getProgressionDouble().intValue());
    }

}
