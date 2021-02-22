package com.example.smc_square;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.amrdeveloper.reactbutton.ReactButton;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ReactionsViewActivity extends AppCompatActivity {

    Student user;
    String postId, depNo;
    Toolbar toolbar;
    TextView emptyMessage;
    ReactionsAdapter reactionsAdapter;
    RecyclerView reactionSet;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reactions_view);

        reactionSet = findViewById(R.id.reactionSet);
        emptyMessage = findViewById(R.id.msg);

        user = getIntent().getExtras().getParcelable("user");
        postId = getIntent().getExtras().getString("postId");
        depNo = getIntent().getExtras().getString("pName");

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        setTitle("Reactions");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Query query = db.collection("Users/" + depNo + "/Splashes/"+postId+"/Reactions");

        FirestoreRecyclerOptions<Reaction> options = new FirestoreRecyclerOptions.Builder<Reaction>()
                .setQuery(query, Reaction.class)
                .build();
        reactionsAdapter = new ReactionsAdapter(options, emptyMessage);
        reactionSet.setLayoutManager(new LinearLayoutManager(this));
        reactionSet.setAdapter(reactionsAdapter);
        reactionsAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(reactionsAdapter != null)
            reactionsAdapter.stopListening();
    }
}