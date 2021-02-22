package com.example.smc_square;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.TimeUnit;

public class LoginNewActivity extends AppCompatActivity {

    FirebaseFirestore db;
    DocumentReference docRef;
    FirebaseAuth mFirebaseAuth;
    EditText dno, code;
    Button signin,verify;
    String phno, codeSent;
    ProgressBar progressBar;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_new);
        mFirebaseAuth = FirebaseAuth.getInstance();
        code = findViewById(R.id.code);
        dno = findViewById(R.id.depno);
        signin = findViewById(R.id.signin);
        verify = findViewById(R.id.verify);
        progressBar = findViewById(R.id.loginProgressBar);

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!(dno.getText().toString().isEmpty()))
                {
                    progressBar.setVisibility(View.VISIBLE);
                    db = FirebaseFirestore.getInstance();
                    docRef = db.collection("Users").document(dno.getText().toString().toLowerCase());
                    docRef.get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.exists()){
                                        phno = documentSnapshot.getString("phone");
                                        sendVerificationCode();
                                    }
                                    else{
                                        Toast.makeText(LoginNewActivity.this, "Kindly check your department number",Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(LoginNewActivity.this, e.toString(),Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                }
                else
                {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginNewActivity.this, "Kindly enter your department number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!code.getText().toString().isEmpty())
                {
                    progressBar.setVisibility(View.VISIBLE);
                    verifyCodeSent();
                }
                else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginNewActivity.this, "Kindly enter the code sent", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void verifyCodeSent() {
        try {
            String codeEntered = code.getText().toString();
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, codeEntered);
            signInWithPhoneAuthCredential(credential);
        }
        catch (Exception e){
            Toast.makeText(this, "Verification Code is invalid", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final String id =dno.getText().toString().toLowerCase();
                            db = FirebaseFirestore.getInstance();

                            db.collection("Users").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {

                                            Intent loginIntent = new Intent(LoginNewActivity.this, HomeActivity.class);
                                            String name;
                                            String email;
                                            String depno;
                                            String phno;
                                            String dp;
                                            Long bonds;
                                            Long splashes;
                                            depno = id;
                                            name = document.getString("name");
                                            email = document.getString("email");
                                            phno = document.getString("phone");
                                            dp = document.getString("profileDP");
                                            bonds = document.getLong("bonds");
                                            splashes = document.getLong("splashes");
                                            Student user = new Student(email,name,depno,phno,dp,bonds,splashes);

                                            loginIntent.putExtra("user", user);
                                            startActivity(loginIntent);
                                            finish();

                                            Log.d("Login", "DocumentSnapshot data: " + document.getData());
                                        } else {
                                            Log.d("Login", "No such document");
                                        }
                                    } else {
                                        Log.d("Login", "get failed with ", task.getException());
                                    }
                                }
                            });
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(LoginNewActivity.this, "Code entered is invalid", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    }
                });
    }

    private void sendVerificationCode() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phno,        // Phone number to verify
                120,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);

        progressBar.setVisibility(View.GONE);
        dno.setVisibility(View.INVISIBLE);
        verify.setVisibility(View.INVISIBLE);
        code.setVisibility(View.VISIBLE);
        signin.setVisibility(View.VISIBLE);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            signInWithPhoneAuthCredential(phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {

            Toast.makeText(LoginNewActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {

            super.onCodeSent(s, forceResendingToken);
            char p8 = phno.charAt(10);
            char p9 = phno.charAt(11);
            char p10 = phno.charAt(12);
            Toast.makeText(LoginNewActivity.this, "Code is sent to ******" + p8 + p9 + p10, Toast.LENGTH_LONG).show();
            codeSent = s;
        }
    };

    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();

        if(currentUser != null)
        {
            String phno = currentUser.getPhoneNumber();
            dialog = ProgressDialog.show(this, "Logging you in", "Just a moment...", true);
            sendUserToHome(phno);
        }
    }

    private void sendUserToHome(String phoneno) {

        // Getting the department number (document name) of the current user with phone number, and sending it to home
        db = FirebaseFirestore.getInstance();
        db.collection("Users").whereEqualTo("phone",phoneno).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for(QueryDocumentSnapshot documentSnapshot : task.getResult())
                    {
                        Intent loginIntent = new Intent(LoginNewActivity.this, HomeActivity.class);
                        String name;
                        String email;
                        String phno;
                        String dp;
                        Long bonds;
                        Long splashes;
                        name = documentSnapshot.getString("name");
                        email = documentSnapshot.getString("email");
                        phno = documentSnapshot.getString("phone");
                        dp = documentSnapshot.getString("profileDP");
                        bonds = documentSnapshot.getLong("bonds");
                        splashes = documentSnapshot.getLong("splashes");
                        Student user = new Student(email,name,documentSnapshot.getId(),phno,dp,bonds,splashes);

                        loginIntent.putExtra("user", user);
                        startActivity(loginIntent);
                        dialog.dismiss();
                    }
                }
                else
                {
                    Toast.makeText(LoginNewActivity.this, task.getException().toString(),Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

    }

}
