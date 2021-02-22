package com.example.smc_square;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private Toolbar toolbar;
    Student user;
    FirebaseFirestore db;
    ListView searchList;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        user = getIntent().getExtras().getParcelable("user");

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        setTitle(R.string.search); //Title for the appbar

        bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        final ArrayList<Student> students = new ArrayList<>();
        final SearchAdapter searchAdapter = new SearchAdapter(this,R.layout.search_resource,students,user);

        db = FirebaseFirestore.getInstance();

        Client client = new Client("29J8KT8GTV", "4dfc83ad5148f78b8f1fe8d152509928");
        final Index index = client.getIndex("Users");

        final EditText search = findViewById(R.id.searchBar);
        searchList = findViewById(R.id.searchList);

        search.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty()){
                    Query query = new Query(s.toString())
                            .setAttributesToRetrieve("name")
                            .setHitsPerPage(50);
                    index.searchAsync(query, new CompletionHandler() {
                        @Override
                        public void requestCompleted(@Nullable JSONObject jsonObject, @Nullable AlgoliaException e) {
                            try {
                                final JSONArray hits = jsonObject.getJSONArray("hits");
                                students.clear();
                                searchAdapter.clear();
                                for(int i=0;i<hits.length();i++)
                                {
                                    JSONObject jsonObject1 = hits.getJSONObject(i);
                                    Log.d("Json", "requestCompleted: "+hits.toString());
                                    String name = jsonObject1.getString("name");
                                    String objID = jsonObject1.getString("objectID");
                                    students.add(new Student(objID,searchAdapter));
                                }
                                searchAdapter.notifyDataSetChanged();
                                searchList.setAdapter(searchAdapter);
                            } catch (JSONException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                }
                else //display an empty list when there is no input. This prevents the display of previous searched name
                {
                    students.clear();
                    searchAdapter.clear();
                    searchAdapter.notifyDataSetChanged();
                    searchList.setAdapter(searchAdapter);
                    Toast.makeText(getApplicationContext(),"Empty text", Toast.LENGTH_LONG).show();
                }
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
                    intent = new Intent(SearchActivity.this, HomeActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                    break;
                case R.id.nav_search:
                    break;
                case R.id.nav_account:
                    intent = new Intent(SearchActivity.this, AccountActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                    break;
            }
            return true;
        }
    };
/*
    public void onBackPressed()
    {
        Intent intent = new Intent(SearchActivity.this, HomeActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);

        finish();
    }
*/
    @Override
    protected void onResume() {
        super.onResume();
        //Highlighting the selected option in bottom navigation view
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);
    }
}