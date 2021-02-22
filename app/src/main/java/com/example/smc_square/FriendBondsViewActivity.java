package com.example.smc_square;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FriendBondsViewActivity extends AppCompatActivity implements BondsViewAdapter.OnFriendListener{

    private static final String TAG = "FriendBondsViewActivity";
    Student user, friend;
    private Toolbar toolbar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();;
    RecyclerView allBonds;
    String homeAct;
    BondsViewAdapter bondsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_bonds_view);

        user = getIntent().getExtras().getParcelable("user");
        friend = getIntent().getExtras().getParcelable("friend");
        homeAct = getIntent().getExtras().getString("goto");

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        setTitle("Bonds");

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(FriendBondsViewActivity.this, FriendAccountActivity.class);
                intent.putExtra("friend", friend);
                intent.putExtra("preAct", "FBA");
                intent.putExtra("goto", homeAct);
                intent.putExtra("user", user);
                startActivity(intent);*/
                finish();
            }
        });

        setUpRecyclerView();
    }

    private void setUpRecyclerView() {

        Query query = db.collection("Users/" + friend.getDepno() + "/Bonds");
        View noRequestsMsg = findViewById(R.id.msg);
        FirestoreRecyclerOptions<Friend> options = new FirestoreRecyclerOptions.Builder<Friend>()
                .setQuery(query, Friend.class)
                .build();

        bondsAdapter = new BondsViewAdapter(options, noRequestsMsg);

        allBonds = findViewById(R.id.userBonds);
        allBonds.setLayoutManager(new LinearLayoutManager(this));
        allBonds.setAdapter(bondsAdapter);

        bondsAdapter.setOnFriendListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bondsAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        bondsAdapter.stopListening();
    }

    @Override
    public void onFriendClick(int position, DocumentSnapshot documentSnapshot) {
        Log.d(TAG, "onFriendClick: " + documentSnapshot.getData());
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
                        Intent intent = new Intent(FriendBondsViewActivity.this,FriendAccountActivity.class);
                        intent.putExtra("user", user);
                        intent.putExtra("friend", s);
                        intent.putExtra("preAct", "FBA");
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

/*    public void onBackPressed()
    {
        *//*Intent intent = new Intent(FriendBondsViewActivity.this, FriendAccountActivity.class);
        intent.putExtra("friend", friend);
        intent.putExtra("preAct", "FBA");
        intent.putExtra("goto", homeAct);
        intent.putExtra("user", user);
        startActivity(intent);*//*

        finish();
    }*/
}