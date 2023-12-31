package com.centrale.smartmove.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.centrale.smartmove.models.Challenge;
import com.centrale.smartmove.*;

import java.util.ArrayList;

public class RecyclerChallengeAdapter extends RecyclerView.Adapter<RecyclerChallengeAdapter.RecyclerChallengeVueHolder> {
    Context context;
    ArrayList<Challenge> challenges;

    public RecyclerChallengeAdapter(Context context, ArrayList<Challenge> challenges) {
        //Assign the data to the variables
        this.context = context;
        this.challenges = challenges;
    }


    @NonNull
    @Override
    public RecyclerChallengeAdapter.RecyclerChallengeVueHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Inflate the layout for each row
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.objectif_unique_tile, parent, false);
        return new RecyclerChallengeAdapter.RecyclerChallengeVueHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerChallengeAdapter.RecyclerChallengeVueHolder holder, int position) {
        //Set the data for each row based on the position
        holder.title.setText(challenges.get(position).getTitle());
        holder.description.setText(challenges.get(position).getShort_description());
        holder.progressionBar.setProgress(challenges.get(position).getProgressionDouble().intValue());
        holder.progressionString.setText(challenges.get(position).getProgressionString());
        holder.image.setImageResource(challenges.get(position).getIcon());
        holder.layout.setOnClickListener(v -> {
                Intent intent = new Intent(context, ChallengeDescriptionActivity.class);
                intent.putExtra("challenge", challenges.get(position).getSaveFormat().toString());
                context.startActivity(intent);
            });
    }

    @Override
    public int getItemCount() {
        //Return the number of rows in total
        return challenges.size();
    }

    public static class RecyclerChallengeVueHolder extends RecyclerView.ViewHolder {
        //Declare the views for each row
        //Assign the views to variables
        ImageView image;
        TextView title;
        TextView description;
        ProgressBar progressionBar;
        TextView progressionString;
        LinearLayout layout;

        public RecyclerChallengeVueHolder(@NonNull ViewGroup parent) {
            super(parent);
            layout = parent.findViewById(R.id.challengeLayout);
            image = parent.findViewById(R.id.challengeIcon);
            title = parent.findViewById(R.id.challengeTitle);
            description = parent.findViewById(R.id.challengeDescription);
            progressionBar = parent.findViewById(R.id.challengeProgressBar);
            progressionString = parent.findViewById(R.id.challengeProgressionString);
        }
    }
}
