package com.example.smc_square;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {

    private static final String TAG = "CommentsActivity";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Student user, friend;
    String postId;
    String depNo;
    String preAct;
    String splashId;
    RecyclerView commentSet;
    CommentAdapter commentAdapter;
    private Toolbar toolbar;
    Button commentButton;
    EditText commentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        commentButton = findViewById(R.id.commentButton);
        commentText = findViewById(R.id.commentBox);

        user = getIntent().getExtras().getParcelable("user");
        postId = getIntent().getExtras().getString("postId");
        depNo = getIntent().getExtras().getString("pName");
        preAct = getIntent().getExtras().getString("activity");
        splashId = getIntent().getExtras().getString("splashId"); // Just for OneSplash
        friend = getIntent().getExtras().getParcelable("friend"); // Just for FriendSplashesView

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        setTitle("Comments");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent intent = null;

                if (preAct.equals("OA")) {
                    intent = new Intent(CommentsActivity.this, OneSplashActivity.class);
                    intent.putExtra("splashId",splashId);
                }
                else if(preAct.equals("HA")){
                    intent = new Intent(CommentsActivity.this, HomeActivity.class);
                }
                else if(preAct.equals("SVA")) {
                    intent = new Intent(CommentsActivity.this, SplashesViewActivity.class);
                }
                else{
                    intent = new Intent(CommentsActivity.this, FriendSplashesViewActivity.class);
                    intent.putExtra("friend", friend);
                }

                intent.putExtra("user", user);
                startActivity(intent);*/

                onBackPressed();
            }
        });

            Query query = db.collection("Users/" + depNo + "/Splashes/"+postId+"/Comments");

            FirestoreRecyclerOptions<Comment> options = new FirestoreRecyclerOptions.Builder<Comment>()
                    .setQuery(query, Comment.class)
                    .build();
            String path = "Users/" + depNo + "/Splashes/"+postId+"/Comments";
            commentAdapter = new CommentAdapter(options,user,postId,path,depNo);
            commentSet = findViewById(R.id.commentSet);
            commentSet.setLayoutManager(new LinearLayoutManager(CommentsActivity.this));
            commentSet.setAdapter(commentAdapter);
            commentAdapter.startListening();

        commentButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if ( ! commentText.getText().toString().isEmpty()){
                    Map<String, Object> comment = new HashMap<>();
                    comment.put("comment", commentText.getText().toString());
                    comment.put("depNo", user.getDepno());
                    comment.put("proPic", user.getDp());
                    comment.put("name",user.getName());

                    db.document("Users/" + depNo + "/Splashes/"+postId).collection("Comments")
                            .add(comment).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                                    Log.d(TAG, "onSuccess: Commented");

                                    if(!user.getDepno().equals(depNo)) {
                                        Map<String, Object> notificationData = new HashMap<>();
                                        notificationData.put("depNo", user.getDepno());
                                        notificationData.put("name", user.getName());
                                        notificationData.put("proPic", user.getDp());
                                        notificationData.put("splashId", postId);
                                        notificationData.put("commentId", documentReference.getId());
                                        notificationData.put("type", "commented");

                                        db.collection("Users/" + depNo + "/Notifications")
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
                                    else {
                                        Log.d(TAG, "onClick: Notification not created as the user is commenting to her own post");
                                    }

                                    commentText.setText("");

                                    db.document("Users/" + depNo + "/Splashes/"+postId)
                                            .update("commentCount", FieldValue.increment(1))
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "onSuccess: Comment Count incremented");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "onFailure: Comment count increment failed");
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding document", e);
                                }
                            });
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(commentAdapter!=null)
            commentAdapter.stopListening();
    }

    /* @Override
    public void onBackPressed()
    {
        Intent intent;

        if (preAct.equals("OA")) {
            intent = new Intent(CommentsActivity.this, OneSplashActivity.class);
            intent.putExtra("splashId",splashId);
        }
        else if(preAct.equals("HA")){
            intent = new Intent(CommentsActivity.this, HomeActivity.class);
        }
        else if(preAct.equals("SVA")) {
            intent = new Intent(CommentsActivity.this, SplashesViewActivity.class);
        }
        else{
            intent = new Intent(CommentsActivity.this, FriendSplashesViewActivity.class);
            intent.putExtra("friend", friend);
        }

        intent.putExtra("user", user);
        startActivity(intent);
        finish();
    }*/

}
