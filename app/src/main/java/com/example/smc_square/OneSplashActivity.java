package com.example.smc_square;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amrdeveloper.reactbutton.ReactButton;
import com.amrdeveloper.reactbutton.Reaction;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class OneSplashActivity extends AppCompatActivity {

    private static final String TAG = "OneSplash";
    Student user;
    private Toolbar toolbar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String splashId;
    ImageView proPic,splashPic;
    TextView username, description;
    TextView reactionCount, commentCount;
    ReactButton reactButton;
    Button comment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_splash);

        user = getIntent().getExtras().getParcelable("user");
        splashId = getIntent().getExtras().getString("splashId");

        proPic = findViewById(R.id.profile_image);
        splashPic = findViewById(R.id.splashPic);
        username = findViewById(R.id.username);
        description = findViewById(R.id.description);
        reactionCount = findViewById(R.id.reactionCount);
        commentCount = findViewById(R.id.commentCount);
        reactButton = findViewById(R.id.postReaction);
        comment = findViewById(R.id.comment);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        setTitle("Splashes");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent;
                intent = new Intent(OneSplashActivity.this, NotificationsActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);*/
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        String path = "Users/"+user.getDepno()+"/Splashes/"+splashId;

        final DocumentReference docRef = db.document(path);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable final DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    Splash model = new Splash();
                    model.depNo = snapshot.getString("depNo");
                    model.proPic = snapshot.getString("proPic");
                    model.description = snapshot.getString("description");
                    model.reactionCount = snapshot.getLong("reactionCount").intValue();
                    model.commentCount = snapshot.getLong("commentCount").intValue();
                    model.splashPic = snapshot.getString("splashPic");
                    model.username = snapshot.getString("username");

                    username.setText(model.getUsername());
                    Picasso.get().load(model.getProPic()).into(proPic);

                    if(model.getSplashPic() != null && model.getSplashPic().trim().length() != 0){
                        splashPic.setVisibility(View.VISIBLE);
                        Picasso.get().load(model.getSplashPic()).into(splashPic);
                    }
                    else {
                        splashPic.setVisibility(View.GONE);
                    }

                    if(!model.getDescription().trim().equals("")){
                        description.setVisibility(View.VISIBLE);
                        description.setText(model.getDescription());
                    }
                    else{
                        description.setVisibility(View.GONE);
                    }

                    if(model.getReactionCount() == 0) {
                        reactionCount.setText("Be the first one to react");
                        reactButton.setCurrentReaction(reactButton.getDefaultReaction());
                    }
                    else if(model.getReactionCount() == 1) {
                        reactionCount.setText(model.getReactionCount() + " reaction");
                    }
                    else{
                        reactionCount.setText(model.getReactionCount() + " reactions");
                    }

                    int commentsCount = model.getCommentCount();

                    if(commentsCount == 1){
                        commentCount.setText(" . " + commentsCount + " comment");
                    }
                    else if(commentsCount > 1){
                        commentCount.setText(" . " + commentsCount + " comments");
                    }

                    reactionCount.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(OneSplashActivity.this, ReactionsViewActivity.class);
                            intent.putExtra("user", user);
                            intent.putExtra("postId",snapshot.getId());
                            intent.putExtra("pName",snapshot.getString("depNo"));
                            startActivity(intent);
                        }
                    });

                    reactButton.setReactClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onReactionClick(snapshot, reactButton.getDefaultReaction(),  reactButton.getCurrentReaction());

                        }
                    });

                    reactButton.setReactDismissListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            onReactionLongClick(snapshot, reactButton.getDefaultReaction(),  reactButton.getCurrentReaction());
                            return true;
                        }
                    });

                    comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onCommentClick(snapshot);
                        }
                    });

                    commentCount.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onCommentClick(snapshot);
                        }
                    });

                } else {
                    Log.d(TAG, "Current data: null");
                    proPic.setVisibility(View.GONE);
                    splashPic.setVisibility(View.GONE);
                    username.setText("Splash does not exist");
                    description.setVisibility(View.GONE);
                    reactionCount.setVisibility(View.GONE);
                    reactButton.setVisibility(View.GONE);
                    comment.setVisibility(View.GONE);
                }
            }
        });

        CollectionReference reactRef = db.collection(path + "/Reactions");
        reactRef.document(user.getDepno()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Map<String, Object> reactionMap = new HashMap<>();
                                reactionMap.putAll((Map<? extends String, ?>) document.get("reaction"));
                                Reaction reaction = new Reaction(reactionMap.get("reactText").toString(), reactionMap.get("reactType").toString(),reactionMap.get("reactTextColor").toString(), ((Long) reactionMap.get("reactIconId")).intValue());

                                reactButton.setCurrentReaction(reaction);

                                Log.d("onBindViewHolder", "onComplete: Reaction - " + reaction.getReactText() + "|" + reaction.getReactType() + "|" + reaction.getReactIconId() + "|" + reaction.getReactTextColor());
                            } else {
                                Log.d("onBindViewHolder", "Current user hasn't reacted to the post yet");
                            }
                        } else {
                            Log.d("onBindViewHolder", "get failed with ", task.getException());
                        }
                    }
                });
    }

    public void onReactionClick(DocumentSnapshot documentSnapshot, Reaction defaultReaction, Reaction currentReaction) {
        if(currentReaction.getReactType().equals(defaultReaction.getReactType())){
            Log.d(TAG, "onReactionClick: No reaction");

            CollectionReference reactionRef = db.collection(documentSnapshot.getReference().getPath() + "/Reactions");
            reactionRef.document(user.getDepno())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: Reaction deleted successfully");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: Reaction deletion failed");
                        }
                    });

            db.document(documentSnapshot.getReference().getPath())
                    .update("reactionCount", FieldValue.increment(-1))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: Reaction Count decremented");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: Reaction count decrement failed");
                        }
                    });
        }
        else {
            Map<String, Object> data = new HashMap<>();
            data.put("reaction", currentReaction);
            data.put("name", user.getName());
            data.put("proPic", user.getDp());

            CollectionReference reactionRef = db.collection(documentSnapshot.getReference().getPath() + "/Reactions");

            reactionRef.document(user.getDepno()).set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: Reaction saved successfully");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: Error saving the reaction");
                        }
                    });

            db.document(documentSnapshot.getReference().getPath())
                    .update("reactionCount", FieldValue.increment(1))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: Reaction Count incremented");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: Reaction count increment failed");
                        }
                    });

        }
    }

    public void onReactionLongClick( final DocumentSnapshot documentSnapshot, Reaction defaultReaction, Reaction currentReaction) {
        if(currentReaction.getReactType().equals(defaultReaction.getReactType())){
            Log.d(TAG, "onReactionLongClick: No Reaction");
        }
        else {
            final Map<String, Object> data = new HashMap<>();
            data.put("reaction", currentReaction);
            data.put("name", user.getName());
            data.put("proPic", user.getDp());

            final CollectionReference reactionRef = db.collection(documentSnapshot.getReference().getPath() + "/Reactions");

            reactionRef.document(user.getDepno())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d(TAG, "Reaction is changed by the user");
                                } else {
                                    Log.d(TAG, "First reaction by user");
                                    db.document(documentSnapshot.getReference().getPath())
                                            .update("reactionCount", FieldValue.increment(1))
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "onSuccess: Reaction Count incremented");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "onFailure: Reaction count increment failed");
                                                }
                                            });
                                }
                                reactionRef.document(user.getDepno()).set(data)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "onSuccess: Reaction saved successfully");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "onFailure: Error saving the reaction");
                                            }
                                        });
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
        }
    }

    public void onCommentClick( DocumentSnapshot snapshot) {
        Intent intent = new Intent(OneSplashActivity.this, CommentsActivity.class);
        intent.putExtra("activity","OA");
        intent.putExtra("splashId",splashId);
        intent.putExtra("user", user);
        intent.putExtra("postId",snapshot.getId());
        intent.putExtra("pName",snapshot.getString("depNo"));
        startActivity(intent);
    }

/*    @Override
    public void onBackPressed()
    {
        *//*Intent intent;
        intent = new Intent(OneSplashActivity.this, NotificationsActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);*//*
        finish();
    }*/
}