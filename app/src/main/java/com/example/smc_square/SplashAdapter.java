package com.example.smc_square;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amrdeveloper.reactbutton.ReactButton;
import com.amrdeveloper.reactbutton.Reaction;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class SplashAdapter extends FirestoreRecyclerAdapter<Splash,SplashAdapter.SplashHolder>{

    private static final String TAG = "SplashAdapter";
    private OnSplashListener listener;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String currentUser;
    View requestsEmptyMessage;

    public SplashAdapter(@NonNull FirestoreRecyclerOptions<Splash> options, String currentUserDepNo, View requestsEmptyMessage) {
        super(options);
        currentUser = currentUserDepNo;
        this.requestsEmptyMessage = requestsEmptyMessage;
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        if(getItemCount() == 0){
            requestsEmptyMessage.setVisibility(View.VISIBLE);
        }
        else{
            requestsEmptyMessage.setVisibility(View.INVISIBLE);
        }
    }

    @NonNull
    @Override
    public SplashHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.splash_template,parent,false);
        return new SplashHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull final SplashHolder holder, int position, @NonNull Splash model) {

        holder.username.setText(model.getUsername());
        Picasso.get().load(model.getProPic()).into(holder.proPic);

        if(model.getSplashPic() != null && model.getSplashPic().trim().length() != 0){
            holder.splashPic.setVisibility(View.VISIBLE);
            Picasso.get().load(model.getSplashPic()).into(holder.splashPic);
        }
        else {
            holder.splashPic.setVisibility(View.GONE);
        }

        if(!model.getDescription().trim().equals("")){
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(model.getDescription());
        }
        else{
            holder.description.setVisibility(View.GONE);
        }

        int reactionCount = model.getReactionCount();

        if(reactionCount == 0) {
            holder.reactionCount.setText("Be the first one to react");
            holder.reactButton.setCurrentReaction(holder.reactButton.getDefaultReaction());
        }
        else if(reactionCount == 1) {
            holder.reactionCount.setText(reactionCount + " reaction");
        }
        else{
            holder.reactionCount.setText(reactionCount + " reactions");
        }

        int commentCount = model.getCommentCount();

        if(commentCount == 1){
            holder.commentCount.setText(" . " + commentCount + " comment");
        }
        else if(commentCount > 1){
            holder.commentCount.setText(" . " + commentCount + " comments");
        }

        CollectionReference reactRef = db.collection(getSnapshots().getSnapshot(position).getReference().getPath() + "/Reactions");
        reactRef.document(currentUser).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Map<String, Object> reactionMap = new HashMap<>((Map<? extends String, ?>) document.get("reaction"));
                                Reaction reaction = new Reaction(reactionMap.get("reactText").toString(), reactionMap.get("reactType").toString(),reactionMap.get("reactTextColor").toString(), ((Long) reactionMap.get("reactIconId")).intValue());

                                holder.reactButton.setCurrentReaction(reaction);

                                Log.d("onBindViewHolder", "onComplete: Reaction" + reaction.getReactText() + "|" + reaction.getReactType() + "|" + reaction.getReactIconId() + "|" + reaction.getReactTextColor());
                            } else {
                                Log.d("onBindViewHolder", "Current user hasn't reacted to the post yet");
                                holder.reactButton.setCurrentReaction(holder.reactButton.getDefaultReaction());
                            }
                        } else {
                            Log.d("onBindViewHolder", "get failed with ", task.getException());
                        }
                    }
                });

        DocumentSnapshot snapshot = getSnapshots().getSnapshot(holder.getAdapterPosition());
        final String docId = snapshot.getId();
        Log.d(TAG, "onBindViewHolder: " + model.getDepNo() + "|" + currentUser);
        if (model.getDepNo().equals(currentUser))
            holder.deletePost.setVisibility(View.VISIBLE);
        else
            holder.deletePost.setVisibility(View.INVISIBLE);

        holder.deletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                db.collection("Users/" + currentUser + "/Splashes/" + docId +"/Reactions")
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    document.getReference().delete();
                                }
                                Log.d(TAG, "onSuccess: All reaction documents deleted successfully");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: Reaction documents deletion failed");
                            }
                        });

                db.collection("Users/" + currentUser + "/Splashes/" + docId +"/Comments")
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    document.getReference().delete();
                                }
                                Log.d(TAG, "onSuccess: All comment documents deleted successfully");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: Comment documents deletion failed");
                            }
                        });

                db.collection("Users/" + currentUser + "/Splashes").document(docId)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                if(documentSnapshot.get("splashStorageReference") != null){
                                    StorageReference splashesRef = FirebaseStorage.getInstance().getReference("Splashes/" + documentSnapshot.get("splashStorageReference"));
                                    splashesRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "onSuccess: Splash image deleted from storage");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            Log.d(TAG, "onFailure: Splash image deletion failed");
                                        }
                                    });
                                }

                                documentSnapshot.getReference().delete();

                                db.document("Users/" + currentUser)
                                        .update("splashes", FieldValue.increment(-1))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "onSuccess: splashes Count decremented for user");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "onFailure: splashes count decrement for user failed");
                                            }
                                        });

                                Log.d(TAG, "onSuccess: Splash deleted successfully");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: Error in retrieving splash document | " + e);
                            }
                        });
            }
        });
    }

    public class SplashHolder extends RecyclerView.ViewHolder{
        ImageView proPic,splashPic;
        TextView username, description;
        TextView reactionCount, commentCount;
        ReactButton reactButton;
        Button comment;
        ImageButton deletePost;

        public SplashHolder(@NonNull View itemView) {
            super(itemView);
            proPic = itemView.findViewById(R.id.profile_image);
            splashPic = itemView.findViewById(R.id.splashPic);
            username = itemView.findViewById(R.id.username);
            description = itemView.findViewById(R.id.description);
            reactionCount = itemView.findViewById(R.id.reactionCount);
            reactButton = itemView.findViewById(R.id.postReaction);
            comment = itemView.findViewById(R.id.comment);
            deletePost = itemView.findViewById(R.id.deletePost);
            commentCount = itemView.findViewById(R.id.commentCount);

           reactButton.setReactClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   int position = getAdapterPosition();
                   if(position != RecyclerView.NO_POSITION && listener != null){
                       listener.onReactionClick(position, getSnapshots().getSnapshot(position), reactButton.getDefaultReaction(),  reactButton.getCurrentReaction());
                   }
               }
           });

           reactButton.setReactDismissListener(new View.OnLongClickListener() {
               @Override
               public boolean onLongClick(View v) {
                   int position = getAdapterPosition();
                   if(position != RecyclerView.NO_POSITION && listener != null){
                       listener.onReactionLongClick(position, getSnapshots().getSnapshot(position), reactButton.getDefaultReaction(),  reactButton.getCurrentReaction());
                   }
                   return true;
               }
           });

           comment.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   int position = getAdapterPosition();
                   if(position != RecyclerView.NO_POSITION && listener != null){
                       listener.onCommentClick(position, getSnapshots().getSnapshot(position));
                   }
               }
           });

            commentCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onCommentClick(position, getSnapshots().getSnapshot(position));
                    }
                }
            });

           reactionCount.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   int position = getAdapterPosition();
                   if(position != RecyclerView.NO_POSITION && listener != null){
                       listener.onReactionCountClick(position, getSnapshots().getSnapshot(position));
                   }
               }
           });

        }
    }

    public interface OnSplashListener{
        void onReactionClick(int position, DocumentSnapshot documentSnapshot, Reaction defaultReaction, Reaction currentReaction);
        void onReactionLongClick(int position, DocumentSnapshot documentSnapshot, Reaction defaultReaction, Reaction currentReaction);
        void onCommentClick(int position, DocumentSnapshot snapshot);
        void onReactionCountClick(int position, DocumentSnapshot snapshot);
    }

    public void setOnSplashListener(SplashAdapter.OnSplashListener listener) {
        this.listener = listener;
    }
}
