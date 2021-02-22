package com.example.smc_square;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchNewActivity extends AppCompatActivity {

    Toolbar toolbar;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_new);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        setTitle("Search"); //Title for the appbar

        db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("Users");

        Client client = new Client("29J8KT8GTV", "a7a8e2ef16a2e8d08f2a96732362fded");
        final Index index = client.getIndex("Users");

        final EditText search = findViewById(R.id.searchBar);
        final ListView searchList = findViewById(R.id.searchList);

        users.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> list = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                list.add(document.getString("name"));
                            }
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(SearchNewActivity.this,android.R.layout.simple_list_item_1,list);
                            searchList.setAdapter(arrayAdapter);
                        } else {
                            Toast.makeText(SearchNewActivity.this, task.getException().toString(),Toast.LENGTH_LONG).show();
                        }
                    }
                });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Query query = new Query(s.toString())
                        .setAttributesToRetrieve("name")
                        .setHitsPerPage(50);
                index.searchAsync(query, new CompletionHandler() {
                    @Override
                    public void requestCompleted(@Nullable JSONObject jsonObject, @Nullable AlgoliaException e) {
                        try {
                            JSONArray hits = jsonObject.getJSONArray("hits");
                            List<String> list = new ArrayList<>();
                            for(int i=0;i<hits.length();i++)
                            {
                                JSONObject jsonObject1 = hits.getJSONObject(i);
                                String name = jsonObject1.getString("name");
                                list.add(name);
                            }
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(SearchNewActivity.this,android.R.layout.simple_list_item_1,list);
                            searchList.setAdapter(arrayAdapter);
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}
