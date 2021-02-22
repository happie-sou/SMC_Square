package com.example.smc_square;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class EventsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    FirebaseAuth mFirebaseAuth=FirebaseAuth.getInstance();
    Student user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);


        user = getIntent().getExtras().getParcelable("user");

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        setTitle(R.string.events);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent = null;

            switch (item.getItemId())
            {
                case R.id.nav_home:
                    intent = new Intent(EventsActivity.this, HomeActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                    break;
                case R.id.nav_search:
                    intent = new Intent(EventsActivity.this, SearchActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                    break;
                case R.id.nav_account:
                    intent = new Intent(EventsActivity.this, AccountActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                    break;
            }
            return true;
        }
    };
/*
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFirebaseAuth.signOut();
    }*/
}

