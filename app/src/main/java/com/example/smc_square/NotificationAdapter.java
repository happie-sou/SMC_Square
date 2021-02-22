package com.example.smc_square;

import android.app.Notification;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

public class NotificationAdapter extends FirestoreRecyclerAdapter<sNotification,NotificationAdapter.NotificationHolder> {

    View requestsEmptyMessage;
    private OnNotificationListener listener;

    public NotificationAdapter(@NonNull FirestoreRecyclerOptions<sNotification> options,View requestsEmptyMessage) {
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
    public NotificationAdapter.NotificationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.notification_template,parent,false);
        return new NotificationHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull NotificationHolder holder, int position, @NonNull sNotification model) {
        if(model.getType().equals("reacted")){
            holder.des.setText(model.getName()+" "+ model.getType() +" to your splash");
        }
        else{
            holder.des.setText(model.getName()+" "+ model.getType() +" on your splash");
        }

        Picasso.get().load(model.getProPic()).into(holder.proPic);
    }

    public class NotificationHolder extends RecyclerView.ViewHolder {

        ImageView proPic;
        TextView des;
        RelativeLayout notification;

        public NotificationHolder(@NonNull View itemView) {
            super(itemView);
            proPic = itemView.findViewById(R.id.dp);
            des = itemView.findViewById(R.id.name);
            notification = itemView.findViewById(R.id.notification);

            notification.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onNotificationClick(position, getSnapshots().getSnapshot(position));
                    }
                }
            });
        }
    }

    public interface OnNotificationListener{
        void onNotificationClick(int position, DocumentSnapshot documentSnapshot);
    }

    public void setOnNotificationListener(NotificationAdapter.OnNotificationListener listener) {
        this.listener = listener;
    }
}
