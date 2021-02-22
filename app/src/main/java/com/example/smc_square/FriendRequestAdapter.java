package com.example.smc_square;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

public class FriendRequestAdapter extends FirestoreRecyclerAdapter<Friend,FriendRequestAdapter.FriendRequestHolder> {

    private OnFriendRequestListener listener;
    View requestsEmptyMessage;

    public FriendRequestAdapter(@NonNull FirestoreRecyclerOptions<Friend> options, View requestsEmptyMessage) {
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

    @NonNull
    @Override
    public FriendRequestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.friend_request_resource,parent,false);
        return new FriendRequestHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull FriendRequestHolder holder, int position, @NonNull Friend model) {
        holder.name.setText(model.getName());
        Picasso.get().load(model.getProPic()).into(holder.proPic);
    }

    public class FriendRequestHolder extends RecyclerView.ViewHolder
    {
        ImageView proPic;
        TextView name;
        Button btnAccept;
        Button btnDecline;

        public FriendRequestHolder(@NonNull View itemView) {
            super(itemView);
            proPic = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);
            btnAccept = itemView.findViewById(R.id.accept);
            btnDecline = itemView.findViewById(R.id.decline);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onFriendRequestClick(position, getSnapshots().getSnapshot(position));
                    }
                }
            });

            btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onAcceptClick(position, getSnapshots().getSnapshot(position));
                    }
                }
            });

            btnDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onDeclineClick(position, getSnapshots().getSnapshot(position));
                    }
                }
            });
        }
    }

    public interface OnFriendRequestListener{
        void onFriendRequestClick(int position, DocumentSnapshot documentSnapshot);
        void onAcceptClick(int position, DocumentSnapshot documentSnapshot);
        void onDeclineClick(int position, DocumentSnapshot documentSnapshot);
    }

    public void setOnFriendRequestListener(OnFriendRequestListener listener) {
        this.listener = listener;
    }
}
