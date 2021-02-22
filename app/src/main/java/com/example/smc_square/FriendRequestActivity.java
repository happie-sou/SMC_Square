package com.example.smc_square;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FriendRequestActivity extends AppCompatActivity implements FriendRequestAdapter.OnFriendRequestListener{

    private static final String TAG = "FriendRequestActivity";
    Student user;
    private Toolbar toolbar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();;
    RecyclerView allRequests;
    FriendRequestAdapter requestAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        user = getIntent().getExtras().getParcelable("user");

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        setTitle(R.string.bondRequests);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(FriendRequestActivity.this, HomeActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);*/
                finish();
            }
        });

        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        CollectionReference requestsRef = db.collection("Users/" + user.getDepno() + "/BondRequestReceived");
        Query query = requestsRef;
        View noRequestsMsg = findViewById(R.id.msg);
        FirestoreRecyclerOptions<Friend> options = new FirestoreRecyclerOptions.Builder<Friend>()
                .setQuery(query, Friend.class)
                .build();

        requestAdapter = new FriendRequestAdapter(options, noRequestsMsg);

        allRequests = findViewById(R.id.allFriendRequests);
        allRequests.setLayoutManager(new LinearLayoutManager(this));
        allRequests.setAdapter(requestAdapter);

        requestAdapter.setOnFriendRequestListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        requestAdapter.stopListening();
    }

    @Override
    public void onFriendRequestClick(int position, DocumentSnapshot documentSnapshot) {
        Log.d(TAG, "onFriendRequestClick: " + documentSnapshot.getData());
        String friendDepNo = documentSnapshot.getString("depNo");

        DocumentReference docRef = db.collection("Users/").document(friendDepNo);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        String name;
                        String email;
                        String phno;
                        String dp;
                        Long bonds;
                        Long splashes;
                        String depNo = document.getId();
                        name = document.getString("name");
                        email = document.getString("email");
                        phno = document.getString("phone");
                        dp = document.getString("profileDP");
                        bonds = document.getLong("bonds");
                        splashes = document.getLong("splashes");
                        Student s = new Student(email,name,depNo,phno,dp,bonds,splashes);
                        Intent intent = new Intent(FriendRequestActivity.this,FriendAccountActivity.class);
                        intent.putExtra("user", user);
                        intent.putExtra("friend", s);
                        intent.putExtra("preAct", "FA");
                        startActivity(intent);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public void onAcceptClick(int position, final DocumentSnapshot documentSnapshot) {
        Log.d(TAG, "onAcceptClick: " + documentSnapshot.getData());

        String friendDepNo = documentSnapshot.getString("depNo");
        final String friendName = documentSnapshot.getString("name");
        String friendProPic = documentSnapshot.getString("proPic");

        //Request is deleted for the current user
        db.collection("Users/"+ user.getDepno() + "/BondRequestReceived")
                .whereEqualTo("depNo", friendDepNo)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "Request received deleted => " + document.getId() + " => " + document.getData());
                                document.getReference().delete();
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        //Added as a friend
        Map<String, Object> data = new HashMap<>();
        data.put("depNo", friendDepNo);
        data.put("name" , friendName);
        data.put("proPic", friendProPic);
        db.collection("Users/"+ user.getDepno() + "/Bonds")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, friendName + " is added as your friend");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });

        data.clear();
        data.put("depNo", user.getDepno());
        data.put("name" , user.getName());
        data.put("proPic", user.getDp());
        db.collection("Users/"+ friendDepNo + "/Bonds")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "You are added as " + friendName + "'s friend");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });

        Toast.makeText(this, "You are now friends with " + friendName, Toast.LENGTH_LONG).show();

        //Request deleted for friend
        db.collection("Users/"+ friendDepNo + "/BondRequestSent")
                .whereEqualTo("depNo", user.getDepno())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "Request sent deleted => " + document.getId() + " => " + document.getData());
                                document.getReference().delete();
                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        db.document("Users/" + user.getDepno())
                .update("bonds", FieldValue.increment(1))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Bond Count incremented for user");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Bond count increment for user failed");
                    }
                });

        db.document("Users/" + friendDepNo)
                .update("bonds", FieldValue.increment(1))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Bond Count incremented for friend");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Bond count increment for friend failed");
                    }
                });
    }

    @Override
    public void onDeclineClick(int position, DocumentSnapshot documentSnapshot) {
        Log.d(TAG, "onDeclineClick: " + documentSnapshot.getData());

        String friendDepNo = documentSnapshot.getString("depNo");

        //Request is deleted for the current user
        db.collection("Users/"+ user.getDepno() + "/BondRequestReceived")
                .whereEqualTo("depNo", friendDepNo)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "Request received deleted => " + document.getId() + " => " + document.getData());
                                document.getReference().delete();
                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        //Request deleted for friend
        db.collection("Users/"+ friendDepNo + "/BondRequestSent")
                .whereEqualTo("depNo", user.getDepno())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "Request sent deleted => " + document.getId() + " => " + document.getData());
                                document.getReference().delete();
                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}