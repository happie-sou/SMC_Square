package com.example.smc_square;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amrdeveloper.reactbutton.ReactButton;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ReactionsAdapter extends FirestoreRecyclerAdapter<Reaction,ReactionsAdapter.ReactionsHolder> {

    TextView emptyMessage;

    public ReactionsAdapter(@NonNull FirestoreRecyclerOptions<Reaction> options, TextView emptyMessage) {
        super(options);
        this.emptyMessage = emptyMessage;
    }

    @Override
    protected void onBindViewHolder(@NonNull ReactionsHolder holder, int position, @NonNull Reaction model) {
        Picasso.get().load(model.getProPic()).into(holder.proPic);
        holder.name.setText(model.getName());
        Map<String, Object> reactionMap = new HashMap<>(model.getReaction());
        com.amrdeveloper.reactbutton.Reaction reaction = new com.amrdeveloper.reactbutton.Reaction(reactionMap.get("reactText").toString(), reactionMap.get("reactType").toString(),reactionMap.get("reactTextColor").toString(), ((Long) reactionMap.get("reactIconId")).intValue());
        holder.reaction.setCurrentReaction(reaction);
        holder.reaction.setEnabled(false);
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        if(getItemCount() == 0){
            emptyMessage.setVisibility(View.VISIBLE);
        }
        else {
            emptyMessage.setVisibility(View.INVISIBLE);
        }
    }

    @NonNull
    @Override
    public ReactionsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.reactions_view_template,parent,false);
        return new ReactionsHolder(view);
    }

    public static class ReactionsHolder extends RecyclerView.ViewHolder {

        ImageView proPic;
        TextView name;
        ReactButton reaction;
        public ReactionsHolder(@NonNull View itemView) {
            super(itemView);
            proPic = itemView.findViewById(R.id.proPic);
            name= itemView.findViewById(R.id.name);
            reaction = itemView.findViewById(R.id.postReaction);
        }
    }
}
