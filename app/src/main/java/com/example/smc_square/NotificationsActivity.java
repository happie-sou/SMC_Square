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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class NotificationsActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationListener {

    private static final String TAG = "NotificationsActivity";
    Student user;
    private Toolbar toolbar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();;
    RecyclerView allNotifications;
    NotificationAdapter notificationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        user = getIntent().getExtras().getParcelable("user");

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Notifications");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent;
                intent = new Intent(NotificationsActivity.this, HomeActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);*/
                finish();
            }
        });
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        CollectionReference requestsRef = db.collection("Users/" + user.getDepno() + "/Notifications");
/*

        db.collection("Users/" + user.getDepno() + "/Notifications")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
*/

        Query query = requestsRef;
        View noRequestsMsg = findViewById(R.id.msg);
        FirestoreRecyclerOptions<sNotification> options = new FirestoreRecyclerOptions.Builder<sNotification>()
                .setQuery(query, sNotification.class)
                .build();

        notificationAdapter = new NotificationAdapter(options, noRequestsMsg);

        allNotifications = findViewById(R.id.allNotifications);
        allNotifications.setLayoutManager(new LinearLayoutManager(this));
        allNotifications.setAdapter(notificationAdapter);

        notificationAdapter.setOnNotificationListener(this);
        notificationAdapter.startListening();
    }

  /*  @Override
    public void onBackPressed()
    {
        *//*Intent intent;
        intent = new Intent(NotificationsActivity.this, HomeActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);*//*
        finish();
    }
*/

    @Override
    public void onNotificationClick(int position, DocumentSnapshot documentSnapshot) {
        Log.d(TAG, "onNotificationClick: " + documentSnapshot.getData());

        db.collection("Users/" + user.getDepno() + "/Notifications")
                .document(documentSnapshot.getId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Notification deleted");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Notification deletion failed with " + e);
                    }
                });

        String splashId = documentSnapshot.getString("splashId");
        Intent intent = new Intent(NotificationsActivity.this, OneSplashActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("splashId",splashId);
        startActivity(intent);
    }
}