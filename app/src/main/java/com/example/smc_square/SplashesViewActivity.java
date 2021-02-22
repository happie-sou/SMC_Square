package com.example.smc_square;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amrdeveloper.reactbutton.Reaction;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashesViewActivity extends AppCompatActivity implements SplashAdapter.OnSplashListener{

    private static final String TAG = "SplashesViewActivity";
    private Toolbar toolbar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public Student user, friend;
    RecyclerView allSplash;
    SplashAdapter splashAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashes_view);
        user = getIntent().getExtras().getParcelable("user");

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        setTitle("Splashes");

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(SplashesViewActivity.this, AccountActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);*/
                finish();
            }
        });

    }

    private void loadPosts() {

        Query query = db.collection("Users/" + user.getDepno() + "/Splashes");

        FirestoreRecyclerOptions<Splash> options = new FirestoreRecyclerOptions.Builder<Splash>()
                .setQuery(query, Splash.class)
                .build();

        View noRequestsMsg = findViewById(R.id.msg);

        splashAdapter = new SplashAdapter(options, user.getDepno(), noRequestsMsg);

        allSplash = findViewById(R.id.userSplashes);
        allSplash.setLayoutManager(new LinearLayoutManager(SplashesViewActivity.this));

        splashAdapter.startListening();
        allSplash.setAdapter(splashAdapter);

        splashAdapter.setOnSplashListener(SplashesViewActivity.this);
    }

    @Override
    public void onReactionClick(int position, final DocumentSnapshot documentSnapshot, Reaction defaultReaction, Reaction currentReaction) {
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
                            Log.d(TAG, "onFailure: Reaction deletion failed" + e);
                        }
                    });

            if(!user.getDepno().equals(documentSnapshot.get("depNo"))) {
                db.collection("Users/" + documentSnapshot.get("depNo") + "/Notifications")
                        .whereEqualTo("depNo", user.getDepno()).whereEqualTo("type", "reacted")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                        document.getReference().delete();
                                        Log.d(TAG, "onComplete: Notification deleted successfully");
                                    }
                                } else {
                                    Log.d(TAG, "Error getting notification document: ", task.getException());
                                }
                            }
                        });
            }

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
                            Log.d(TAG, "onFailure: Reaction count decrement failed" + e);
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
                            Log.d(TAG, "onFailure: Error saving the reaction" + e);
                        }
                    });

            if(!user.getDepno().equals(documentSnapshot.get("depNo"))) {
                Map<String, Object> notificationData = new HashMap<>();
                notificationData.put("depNo", user.getDepno());
                notificationData.put("name", user.getName());
                notificationData.put("proPic", user.getDp());
                notificationData.put("splashId", documentSnapshot.getId());
                notificationData.put("type", "reacted");

                db.collection("Users/" + documentSnapshot.get("depNo") + "/Notifications")
                        .add(notificationData)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "onSuccess: Notification added successfully");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: Failed to add notification | " + e);
                            }
                        });
            }
            else
            {
                Log.d(TAG, "onReactionClick: Notification not created as the user is reacting to her own post");
            }

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

    @Override
    public void onReactionLongClick(int position, final DocumentSnapshot documentSnapshot, Reaction defaultReaction, Reaction currentReaction) {
        if(currentReaction.getReactType().equals(defaultReaction.getReactType())){
            Log.d(TAG, "onReactionLongClick: No Reaction");
        }
        else {
            final Map<String, Object> data = new HashMap<>();
            data.put("reaction", currentReaction);
            data.put("name", user.getName());
            data.put("proPic", user.getDp());

            final CollectionReference reactionRef = db.collection(documentSnapshot.getReference().getPath() + "/Reactions");

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

            if(!user.getDepno().equals(documentSnapshot.get("depNo"))){
                Map<String, Object> notificationData = new HashMap<>();
                notificationData.put("depNo", user.getDepno());
                notificationData.put("name", user.getName());
                notificationData.put("proPic", user.getDp());
                notificationData.put("splashId", documentSnapshot.getId());
                notificationData.put("type", "reacted");

                db.collection("Users/" + documentSnapshot.get("depNo") + "/Notifications")
                        .add(notificationData)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "onSuccess: Notification added successfully");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: Failed to add notification | " + e);
                            }
                        });
            }
            else
            {
                Log.d(TAG, "onReactionClick: Notification not created as the user is reacting to her own post");
            }

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
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
        }
    }

    @Override
    public void onCommentClick(int position, DocumentSnapshot snapshot) {
        Intent intent = new Intent(SplashesViewActivity.this, CommentsActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("postId",snapshot.getId());
        intent.putExtra("pName",snapshot.getString("depNo"));
        intent.putExtra("activity","SVA");
        startActivity(intent);
    }

    @Override
    public void onReactionCountClick(int position, DocumentSnapshot snapshot) {
        Intent intent = new Intent(SplashesViewActivity.this, ReactionsViewActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("postId",snapshot.getId());
        intent.putExtra("pName",snapshot.getString("depNo"));
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPosts();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(splashAdapter != null)
            splashAdapter.stopListening();
    }

/*    public void onBackPressed()
    {
        *//*Intent intent = new Intent(SplashesViewActivity.this, AccountActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);*//*
        finish();
    }*/
}