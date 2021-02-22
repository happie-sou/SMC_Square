package com.example.smc_square;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText email, pwd;
    TextView t1;
    Button signIn;
    ProgressBar progressBar;
    FirebaseAuth mFirebaseAuth=FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        email = findViewById(R.id.email);
        pwd = findViewById(R.id.password);
        signIn = findViewById(R.id.signin);
        t1 = findViewById(R.id.signup);
        progressBar = findViewById(R.id.progressBar);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(mFirebaseAuth.getCurrentUser() != null){
                    Toast.makeText(LoginActivity.this, "You are logged in", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(i);
                }
                else
                {
                    Toast.makeText(LoginActivity.this,"Please login",Toast.LENGTH_SHORT).show();
                }

            }
        };
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String e = email.getText().toString();
                String p = pwd.getText().toString();

                progressBar.setVisibility(View.VISIBLE);

                if (e.isEmpty()) {
                    email.setError("Please enter your email");
                    email.requestFocus();
                    progressBar.setVisibility(View.GONE);
                } else if (p.isEmpty()) {
                    pwd.setError("Please enter your password");
                    pwd.requestFocus();
                    progressBar.setVisibility(View.GONE);
                } else if (!(e.isEmpty() && p.isEmpty())) {
                        mFirebaseAuth.signInWithEmailAndPassword(e,p).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()){
                                    Toast.makeText(LoginActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                                else
                                {
                                    email.setText("");
                                    pwd.setText("");
                                    Intent inToHome = new Intent(LoginActivity.this, HomeActivity.class);
                                    startActivity(inToHome);
                                }
                            }
                        });

                }
                else
                {
                    Toast.makeText(LoginActivity.this,"Error Occurred!",Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
        t1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(LoginActivity.this,MainActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mFirebaseAuth.signOut();
//    }
}
