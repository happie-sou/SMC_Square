package com.example.smc_square;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

public class BondsViewAdapter extends FirestoreRecyclerAdapter<Friend, BondsViewAdapter.BondsViewHolder> {

    private OnFriendListener listener;
    View requestsEmptyMessage;

    public BondsViewAdapter(@NonNull FirestoreRecyclerOptions<Friend> options, View requestsEmptyMessage) {
        super(options);
        this.requestsEmptyMessage = requestsEmptyMessage;
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        if(getItemCount() == 0){
            requestsEmptyMessage.setVisibility(View.VISIBLE);
        }
        else {
            requestsEmptyMessage.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull BondsViewHolder holder, int position, @NonNull Friend model) {
        holder.name.setText(model.getName());
        Picasso.get().load(model.getProPic()).into(holder.proPic);
    }

    @NonNull
    @Override
    public BondsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.bond_view_resource,parent,false);
        return new BondsViewHolder(view);
    }

    public class BondsViewHolder extends RecyclerView.ViewHolder {

        ImageView proPic;
        TextView name;

        public BondsViewHolder(@NonNull View itemView) {
            super(itemView);
            proPic = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onFriendClick(position, getSnapshots().getSnapshot(position));
                    }
                }
            });
        }
    }

    public interface OnFriendListener{
        void onFriendClick(int position, DocumentSnapshot documentSnapshot);
    }

    public void setOnFriendListener(OnFriendListener listener) {
        this.listener = listener;
    }
}
