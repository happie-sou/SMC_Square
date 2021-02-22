package com.example.smc_square;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

public class AccountActivity extends AppCompatActivity {

    private static final String TAG = "AccountActivity";
    TextView username, phone, email, depno, bonds, splashes;
    CardView splashesView, bondsView;
    private Toolbar toolbar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Student user;
    ImageView profile;
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        setTitle(R.string.myAccount);

        bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        user = getIntent().getExtras().getParcelable("user");

        final DocumentReference docRef = db.collection("Users").document(user.getDepno());
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
                    user = new Student(email,name,depNo,phno,dp,bonds,splashes);
                    display();
                    Log.d(TAG, "onEvent: Student Data: " + snapshot.getData());
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);
    }

    private void display(){

        username = findViewById(R.id.uname);
        phone = findViewById(R.id.phone);
        email = findViewById(R.id.emailId);
        depno = findViewById(R.id.deptno);
        profile = findViewById(R.id.profile);
        bonds = findViewById(R.id.numberOfBonds);
        splashes = findViewById(R.id.numberOfSplashes);
        splashesView = findViewById(R.id.SplashesView);
        bondsView = findViewById(R.id.BondsView);
        
        loadData();
        
        splashesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Splashes View");
                Intent intent = new Intent(AccountActivity.this, SplashesViewActivity.class);
                intent.putExtra("user", user);
                intent.putExtra("preAct", "AA");
                startActivity(intent);
            }
        });
        
        bondsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Bonds View");
                Intent intent = new Intent(AccountActivity.this, BondsViewActivity.class);
                intent.putExtra("user", user);
                intent.putExtra("preAct", "AA");
                startActivity(intent);
            }
        });
    }

    private void loadData() {
        username.setText(user.getName());
        phone.setText(user.getPhno());
        email.setText(user.getEmail());
        depno.setText(user.getDepno());
        bonds.setText(user.getBonds().toString());
        splashes.setText(user.getSplashes().toString());
        Picasso.get().load(user.getDp()).into(profile);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent = null;

            switch (item.getItemId())
            {
                case R.id.nav_home:
                    intent = new Intent(AccountActivity.this, HomeActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                    break;
                case R.id.nav_search:
                    intent = new Intent(AccountActivity.this, SearchActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                    break;
                case R.id.nav_account:
                    break;
            }
            return true;
        }
    };

  /*  public void onBackPressed()
    {
        *//*Intent intent = new Intent(AccountActivity.this, HomeActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
        finish();*//*

        finish();
    }*/
}
