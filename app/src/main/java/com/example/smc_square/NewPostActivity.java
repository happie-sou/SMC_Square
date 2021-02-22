package com.example.smc_square;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class NewPostActivity extends AppCompatActivity{

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "NewPostActivity";

    private Button mButtonChooseImage;
    private Button mButtonUpload;
    private EditText mEditTextDescription;
    private ImageView mSplashImage;
   // private Button mButtonCancel;
    private ProgressBar mSplashProgress;
    private ImageButton mClosePost;
    Student user;
    private Uri mImageUri;

    private StorageReference mStorageRef;
    private StorageTask mUploadTask;

    FirebaseFirestore db = FirebaseFirestore.getInstance();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        user = getIntent().getExtras().getParcelable("user");

        mButtonChooseImage = findViewById(R.id.chooseButton);
        mButtonUpload = findViewById(R.id.splashButton);
        mEditTextDescription = findViewById(R.id.description);
        mSplashImage = findViewById(R.id.splashImage);
        //mButtonCancel = findViewById(R.id.cancelButton);
        mSplashProgress = findViewById(R.id.splashProgress);
        mClosePost = findViewById(R.id.closePost);

        mStorageRef = FirebaseStorage.getInstance().getReference("Splashes");
        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        mButtonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUploadTask != null && mUploadTask.isInProgress()){
                    Toast.makeText(NewPostActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                }
                else{
                    mSplashProgress.setVisibility(View.VISIBLE);
                    //mButtonCancel.setEnabled(false);
                    mClosePost.setVisibility(View.INVISIBLE);
                    mEditTextDescription.setEnabled(false);
                    mButtonChooseImage.setEnabled(false);
                    mButtonChooseImage.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
                    upload();
                }
            }
        });

        mClosePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();

            Picasso.get().load(mImageUri).into(mSplashImage);
            mSplashImage.setVisibility(View.VISIBLE);
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void upload() {
        if(mImageUri != null) {
            final String fileName = System.currentTimeMillis() + "." + getFileExtension(mImageUri);
            final StorageReference fileReference = mStorageRef.child(fileName);

            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            fileReference.getDownloadUrl().addOnCompleteListener( new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                        String splashImageUrl = task.getResult().toString();

                                        NewPost newPost = new NewPost(mEditTextDescription.getText().toString().trim(), user.getDp(), splashImageUrl, user.getName(), user.getDepno(), fileName);
                                        db.collection("Users/"+ user.getDepno() + "/Splashes")
                                                .add(newPost)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        mSplashProgress.setVisibility(View.INVISIBLE);
                                                        Toast.makeText(NewPostActivity.this, "Splash posted successfully", Toast.LENGTH_LONG).show();
                                                        Intent intent = new Intent(NewPostActivity.this, HomeActivity.class);
                                                        intent.putExtra("user", user);
                                                        startActivity(intent);
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d(TAG, "onFailure: "  + e.getMessage());
                                                    }
                                                });

                                        db.document("Users/" + user.getDepno())
                                                .update("splashes", FieldValue.increment(1))
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "onSuccess: splashes Count incremented for user");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d(TAG, "onFailure: splashes count increment for user failed");
                                                    }
                                                });
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(NewPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else if(!mEditTextDescription.getText().toString().trim().equals("")){
            //Posting a splash without image
                NewPost newPost = new NewPost(mEditTextDescription.getText().toString().trim(), user.getDp(), user.getName(),user.getDepno());
                db.collection("Users/" + user.getDepno() + "/Splashes")
                        .add(newPost)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                mSplashProgress.setVisibility(View.INVISIBLE);
                                Toast.makeText(NewPostActivity.this, "Splash posted successfully", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(NewPostActivity.this, HomeActivity.class);
                                intent.putExtra("user", user);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: "  + e.getMessage());
                            }
                        });

            db.document("Users/" + user.getDepno())
                    .update("splashes", FieldValue.increment(1))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: splashes Count incremented for user");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: splashes count increment for user failed");
                        }
                    });
        }
        else
        {
            mSplashProgress.setVisibility(View.INVISIBLE);
            /*mButtonCancel.setEnabled(true);
            mButtonCancel.getBackground().setColorFilter(null);*/
            mClosePost.setVisibility(View.VISIBLE);
            Toast.makeText(NewPostActivity.this, "Cannot post an empty splash", Toast.LENGTH_SHORT).show();
        }
    }

/*    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
        finish();
    }*/
}
