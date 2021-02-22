package com.example.smc_square;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class FriendAccountActivity extends AppCompatActivity {

    private static final String TAG = "FriendAccountActivity";
    Student user,friend;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView username, phone, email, depno, bonds, splashes;
    Button sendRequest;
    ImageView profile;
    String preAct, homeAct;
    CardView splashesView, bondsView;
    private Toolbar toolbar;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_account);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.profile); //Title for the appbar

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
/*
                Intent intent = null;

                if(preAct.equals("SA") || homeAct.equals("SA"))
                    intent = new Intent(FriendAccountActivity.this, SearchActivity.class);
                else if(preAct.equals("BA") || homeAct.equals("BA"))
                    intent = new Intent(FriendAccountActivity.this, BondsViewActivity.class);
                else
                    intent = new Intent(FriendAccountActivity.this, FriendRequestActivity.class);

                intent.putExtra("user", user);
                startActivity(intent);*/
                finish();
            }
        });

        bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        user = getIntent().getExtras().getParcelable("user");
        friend =getIntent().getExtras().getParcelable("friend");
        preAct = getIntent().getExtras().getString("preAct");
        homeAct = getIntent().getExtras().getString("goto");

        final DocumentReference docRef = db.collection("Users").document(friend.getDepno());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    String name;
                    String email;
                    String phno;
                    String dp;
                    Long bonds;
                    Long splashes;
                    String depNo = snapshot.getId();
                    name = snapshot.getString("name");
                    email = snapshot.getString("email");
                    phno = snapshot.getString("phone");
                    dp = snapshot.getString("profileDP");
                    bonds = snapshot.getLong("bonds");
                    splashes = snapshot.getLong("splashes");
                    friend = new Student(email,name,depNo,phno,dp,bonds,splashes);
                    display();
                    Log.d(TAG, "onEvent: Student Data: " + snapshot.getData());
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    private void display() {
        username = findViewById(R.id.uname);
        phone = findViewById(R.id.phone);
        email = findViewById(R.id.emailId);
        depno = findViewById(R.id.deptno);
        bonds = findViewById(R.id.numberOfBonds);
        splashes = findViewById(R.id.numberOfSplashes);
        profile = findViewById(R.id.profile);
        sendRequest = findViewById(R.id.sendRequest);
        splashesView = findViewById(R.id.SplashesView);
        bondsView = findViewById(R.id.BondsView);

        loadData();

        splashesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Splashes View");
                Intent intent = new Intent(FriendAccountActivity.this, FriendSplashesViewActivity.class);

                if(preAct.equals("SA")){
                    intent.putExtra("goto", "SA");
                }
                else if(preAct.equals("BA")){
                    intent.putExtra("goto", "BA");
                }

                intent.putExtra("user", user);
                intent.putExtra("friend", friend);
                startActivity(intent);
            }
        });

        bondsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Bonds View");
                Intent intent = new Intent(FriendAccountActivity.this, FriendBondsViewActivity.class);

                if(preAct.equals("SA")){
                    intent.putExtra("goto", "SA");
                }
                else if(preAct.equals("BA")){
                    intent.putExtra("goto", "BA");
                }

                intent.putExtra("user", user);
                intent.putExtra("friend", friend);
                startActivity(intent);
            }
        });
    }

    private void loadData() {

        username.setText(friend.getName());
        phone.setText(friend.getPhno());
        email.setText(friend.getEmail());
        depno.setText(friend.getDepno());
        bonds.setText(friend.getBonds().toString());
        splashes.setText(friend.getSplashes().toString());

        Picasso.get().load(friend.getDp()).into(profile);

        if(user.getDepno().equals(friend.getDepno())){
            sendRequest.setVisibility(View.GONE);
        }
        else {
            db.collection("Users/"+ user.getDepno()+ "/BondRequestSent")
                    .whereEqualTo("depNo", friend.getDepno())
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "BondRequestSent Listen failed.", e);
                                return;
                            }
                            if(value.isEmpty()){
                                Log.d(TAG, "Bond request not sent");

                                db.collection("Users/"+ user.getDepno() + "/BondRequestReceived")
                                        .whereEqualTo("depNo", friend.getDepno())
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot value,
                                                                @Nullable FirebaseFirestoreException e) {
                                                if (e != null) {
                                                    Log.w(TAG, "BondRequestReceived Listen failed.", e);
                                                    return;
                                                }

                                                if(value.isEmpty()){
                                                    Log.d(TAG, "Bond request not received");

                                                    db.collection("Users/"+ user.getDepno() + "/Bonds")
                                                            .whereEqualTo("depNo", friend.getDepno())
                                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onEvent(@Nullable QuerySnapshot value,
                                                                                    @Nullable FirebaseFirestoreException e) {
                                                                    if (e != null) {
                                                                        Log.w(TAG, "Bonds listen failed.", e);
                                                                        return;
                                                                    }
                                                                    if(value.isEmpty()){
                                                                        Log.d(TAG, "Not found in bonds list");

                                                                        sendRequest.setText("Send Bond Request");
                                                                        sendRequest.setOnClickListener(new View.OnClickListener() {
                                                                            public void onClick(View v) {
                                                                                updateBondRequest();
                                                                            }
                                                                        });
                                                                    }
                                                                    else{
                                                                        for (QueryDocumentSnapshot doc : value) {
                                                                            Log.d(TAG, "Bonds: " + doc.getData());

                                                                            sendRequest.setText("Remove bond");
                                                                            sendRequest.setOnClickListener(new View.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(View v) {
                                                                                    removeBond();
                                                                                }
                                                                            });
                                                                        }
                                                                    }
                                                                }
                                                            });
                                                }
                                                else {
                                                    for (QueryDocumentSnapshot doc : value) {
                                                        Log.d(TAG, "Request received => " + doc.getData());
                                                        sendRequest.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                navigateToRequestsActivity();
                                                            }
                                                        });
                                                        sendRequest.setText("Bond Request Received");
                                                    }
                                                }
                                            }
                                        });
                            }
                            else{
                                for (QueryDocumentSnapshot doc : value) {
                                    Log.d(TAG, "Document Exists " + doc.getData());

                                    sendRequest.setText("Cancel Bond Request");
                                    sendRequest.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            cancelBondRequest();
                                        }
                                    });
                                }
                            }
                        }
                    });
        }
    }

    private void cancelBondRequest() {
        db.collection("Users/"+ user.getDepno()+ "/BondRequestSent")
                .whereEqualTo("depNo", friend.getDepno())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                            document.getReference().delete();
                        }
                        Log.d(TAG, "onSuccess: Bond Request removed for user ");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Bond Request removal failed for user with " + e);
                    }
                });

        db.collection("Users/"+ friend.getDepno()+ "/BondRequestReceived")
                .whereEqualTo("depNo", user.getDepno())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                            document.getReference().delete();
                        }
                        Log.d(TAG, "onSuccess: Bond Request removed for friend ");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Bond Request removal failed for friend with " + e);
                    }
                });
    }

    private void removeBond() {
        db.collection("Users/"+ user.getDepno() + "/Bonds")
                .whereEqualTo("depNo", friend.getDepno())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                            document.getReference().delete();
                        }
                        Log.d(TAG, "onSuccess: Bond removed for user ");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Bond removal failed for user with " + e);
                    }
                });

        db.collection("Users/"+ friend.getDepno() + "/Bonds")
                .whereEqualTo("depNo", user.getDepno())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                            document.getReference().delete();
                        }
                        Log.d(TAG, "onSuccess: Bond removed for friend ");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Bond removal failed for friend with " + e);
                    }
                });

        db.document("Users/" + user.getDepno())
                .update("bonds", FieldValue.increment(-1))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Bond Count decremented for user");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Bond count decrement for user failed");
                    }
                });

        db.document("Users/" + friend.getDepno())
                .update("bonds", FieldValue.increment(-1))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Bond Count decremented for friend");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Bond count decrement for friend failed");
                    }
                });
    }

    private void navigateToRequestsActivity() {
        Intent intent = new Intent(this, FriendRequestActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }

    private void updateBondRequest(){

        Map<String, Object> data = new HashMap<>();
        data.put("depNo", friend.getDepno());
        data.put("name" , friend.getName());
        db.collection("Users/"+ user.getDepno()+ "/BondRequestSent")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("BondRequestSent", "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("BondRequestSent", "Error updating document", e);
                    }
                });

        data.clear();
        data.put("depNo", user.getDepno());
        data.put("name", user.getName());
        data.put("proPic", user.getDp());
        db.collection("Users/"+ friend.getDepno()+ "/BondRequestReceived")
                .document()
                .set(data, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("BondRequestReceived", "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("BondRequestReceived", "Error updating document", e);
                    }
                });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent = null;

            switch (item.getItemId())
            {
                case R.id.nav_home:
                    intent = new Intent(FriendAccountActivity.this, HomeActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                    break;
                case R.id.nav_search:
                    break;
                case R.id.nav_account:
                    intent = new Intent(FriendAccountActivity.this, AccountActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                    break;
            }
            return true;
        }
    };

  /*  public void onBackPressed()
    {
        *//*Intent intent = null;

        if(preAct.equals("SA") || homeAct.equals("SA"))
            intent = new Intent(this, SearchActivity.class);
        else if(preAct.equals("BA") || homeAct.equals("BA"))
            intent = new Intent(this, BondsViewActivity.class);
        else
            intent = new Intent(this, FriendRequestActivity.class);

        intent.putExtra("user", user);
        startActivity(intent);*//*
        finish();
    }*/

    @Override
    protected void onResume() {
        super.onResume();

        if(preAct.equals("SA")){
            bottomNavigationView.setVisibility(View.VISIBLE);
            //Highlighting the selected option in bottom navigation view
            Menu menu = bottomNavigationView.getMenu();
            MenuItem menuItem =  menu.getItem(1);
            menuItem.setChecked(true);
        }
    }
}
