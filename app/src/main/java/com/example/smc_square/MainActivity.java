package com.example.smc_square;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText email, pwd, cpwd, name, dofno, phno;
    TextView t1;
    Switch aSwitch;
    Button signUp;
    FirebaseFirestore db;
    FirebaseAuth mFirebaseAuth=FirebaseAuth.getInstance();
    FirebaseFirestore firebaseFirestore;
    String userID;
    ProgressBar progressBar;
    boolean dorf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = findViewById(R.id.email);
        name = findViewById(R.id.name);
        pwd = findViewById(R.id.password);
        cpwd = findViewById(R.id.cpassword);
        dofno = findViewById(R.id.dno);
        dorf = false;
        signUp = findViewById(R.id.signup);
        t1 = findViewById(R.id.signin);
        progressBar = findViewById(R.id.progressBar);
        aSwitch = findViewById(R.id.switch2);
        phno = findViewById(R.id.phno);

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(aSwitch.isChecked())
                {
                    dofno.setHint("Enter Faculty ID");
                    dorf = true;
                }
                else
                {
                    dofno.setHint("Enter Department number");
                    dorf = false;
                }
            }
        });

        firebaseFirestore = FirebaseFirestore.getInstance();
        db = firebaseFirestore.getInstance();

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String e = email.getText().toString();
                String p = pwd.getText().toString();
                String c = cpwd.getText().toString();
                final String n = name.getText().toString();
                final String d = dofno.getText().toString();
                final String pno = phno.getText().toString();


                progressBar.setVisibility(View.VISIBLE);

                if (e.isEmpty() || p.isEmpty() || c.isEmpty() || n.isEmpty() || d.isEmpty() || pno.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Kindly fill all details to register", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                } else if (!(e.isEmpty() && p.isEmpty() && n.isEmpty() && d.isEmpty() && pno.isEmpty())) {
                    if (p.equals(c)) {

                        mFirebaseAuth.createUserWithEmailAndPassword(e, p).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.GONE);
                                } else {
                                    userID = mFirebaseAuth.getCurrentUser().getUid();
                                    DocumentReference documentReference = firebaseFirestore.collection("Users").document(userID);
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("name", n);
                                    user.put("email", e);
                                    user.put("phno", pno);
                                    if(dorf)
                                        user.put("facultyno", d);
                                    else
                                        user.put("deptno", d);

                                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(MainActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                        }
                                    });

                                }
                            }
                        });
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Password does not match", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                }
                else
                {
                    Toast.makeText(MainActivity.this,"Error occurred",Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        t1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, LoginNewActivity.class);
                startActivity(i);

            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();

        if(currentUser != null)
        {
            sendUserToHome();
        }
    }

    private void sendUserToHome() {
        Intent loginIntent = new Intent(MainActivity.this, LoginNewActivity.class);
        startActivity(loginIntent);
    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mFirebaseAuth.signOut();
//    }
}
