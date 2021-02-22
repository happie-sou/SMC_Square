package com.example.smc_square;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amrdeveloper.reactbutton.Reaction;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.api.LogDescriptor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements SplashAdapter.OnSplashListener{

    private static final String TAG = "HomeActivity";
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView uname;
    public Student user;
    ImageView profile;
    RecyclerView allSplash;
    SplashAdapter splashAdapter;
    BottomNavigationView bottomNavigationView;
    long backPressedTime;
    Toast backPress;
    final List<String> friends = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        user = getIntent().getExtras().getParcelable("user");

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.layoutHome);
        actionBarDrawerToggle = new ActionBarDrawerToggle(HomeActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.Navigation_View);
        View navView = navigationView.inflateHeaderView(R.layout.slide_nav_header);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                UserMenuSelector(menuItem);
                return false;
            }
        });

        bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        // Display username in navigation drawer
        uname = (TextView) navView.findViewById(R.id.navUserFullName);
        profile = (ImageView) navView.findViewById(R.id.imageView);

    }

    private void loadPosts() {

        Log.d(TAG, "loadPosts: " + friends);

        Query query = db.collectionGroup("Splashes").whereIn("depNo", friends);

        FirestoreRecyclerOptions<Splash> options = new FirestoreRecyclerOptions.Builder<Splash>()
                .setQuery(query, Splash.class)
                .build();
        View noRequestsMsg = findViewById(R.id.msg);

        splashAdapter = new SplashAdapter(options, user.getDepno(), noRequestsMsg);

        allSplash = findViewById(R.id.allUsersPostSet);
        allSplash.setLayoutManager(new LinearLayoutManager(HomeActivity.this));

        splashAdapter.startListening();
        allSplash.setAdapter(splashAdapter);

        splashAdapter.setOnSplashListener(HomeActivity.this);
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
    public void onReactionLongClick(int position, final DocumentSnapshot documentSnapshot, Reaction defaultReaction, final Reaction currentReaction) {
        if(currentReaction.getReactType().equals(defaultReaction.getReactType())){
            Log.d(TAG, "onReactionLongClick: No Reaction");
        }
        else {
            final CollectionReference reactionRef = db.collection(documentSnapshot.getReference().getPath() + "/Reactions");

            reactionRef.document(user.getDepno())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d(TAG, "Reaction is changed by the user: ");
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
                                }

                                final Map<String, Object> data = new HashMap<>();
                                data.put("reaction", currentReaction);
                                data.put("name", user.getName());
                                data.put("proPic", user.getDp());

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
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
        }
    }

    @Override
    public void onCommentClick(int position, DocumentSnapshot snapshot) {
        Intent intent = new Intent(HomeActivity.this, CommentsActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("postId",snapshot.getId());
        intent.putExtra("pName",snapshot.getString("depNo"));
        intent.putExtra("activity","HA");
        startActivity(intent);
    }

    @Override
    public void onReactionCountClick(int position, DocumentSnapshot snapshot) {
        Intent intent = new Intent(HomeActivity.this, ReactionsViewActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("postId",snapshot.getId());
        intent.putExtra("pName",snapshot.getString("depNo"));
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        friends.clear();

        if(currentUser != null)
        {
            uname.setText(user.getName());
            Picasso.get().load(user.getDp()).into(profile);

            friends.add(user.getDepno());

            db.collection("Users/" + user.getDepno() + "/Bonds")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    friends.add(document.get("depNo").toString());
                                }
                                loadPosts();
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
        else
        {
            Log.d(TAG, "onCreate: No current user");
            Intent intent = new Intent(HomeActivity.this, LoginNewActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(splashAdapter != null)
            splashAdapter.stopListening();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent = null;

            switch (item.getItemId())
            {
                case R.id.nav_home:
                    break;
                case R.id.nav_search:
                    intent = new Intent(HomeActivity.this, SearchActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                    break;
                case R.id.nav_account:
                    intent = new Intent(HomeActivity.this, AccountActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                    break;
            }
            return true;
        }

    };

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
     /*   if(item.getItemId()== R.id.message){
            startActivity(new Intent(HomeActivity.this, MessageActivity.class));
        }*/
        if(item.getItemId()== R.id.post){
            Intent intent = new Intent(HomeActivity.this, NewPostActivity.class);
            intent.putExtra("user", user);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_nav_top_right,menu);
        return true;
    }

    private void UserMenuSelector(MenuItem item)
    {
        Intent intent;
        switch(item.getItemId())
        {
            case R.id.nav_notifications:
                intent = new Intent(HomeActivity.this, NotificationsActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
                break;

            case R.id.nav_friendRequests:
                intent = new Intent(HomeActivity.this, FriendRequestActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
                break;

            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();
                Intent inToMain = new Intent(HomeActivity.this, LoginNewActivity.class);
                startActivity(inToMain);
                Toast.makeText(this,"Logged out",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onBackPressed() {

        if(backPressedTime + 2000 > System.currentTimeMillis()){
            backPress.cancel();
            finish();
            super.onBackPressed();
            return;
        }
        else{
            backPress = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT);
            backPress.show();
        }

        backPressedTime = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        Process.killProcess(Process.myPid());
        super.onDestroy();
    }
}