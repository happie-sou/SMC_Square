package com.example.smc_square;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Student implements Parcelable {
    private static final String TAG = "StudentData";
    public String email;
    public String name;
    public String depno;
    public String phno;
    public String dp;
    public Long bonds;
    public Long splashes;

    public Student(String depno, final SearchAdapter s){
        this.depno=depno;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection("Users").document(this.depno);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Student.this.name = document.getString("name");
                        Student.this.email = document.getString("email");
                        Student.this.phno = document.getString("phone");
                        Student.this.dp = document.getString("profileDP");
                        Student.this.bonds = document.getLong("bonds");
                        Student.this.splashes = document.getLong("splashes");
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                    s.notifyDataSetChanged();
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public Student(String email, String name, String depno, String phno,String dp,Long bonds,Long splashes) {
        this.email = email;
        this.name = name;
        this.depno = depno;
        this.phno = phno;
        this.dp = dp;
        this.bonds=bonds;
        this.splashes=splashes;
    }

    public Long getBonds() {
        return bonds;
    }

    public void setBonds(Long bonds) {
        this.bonds = bonds;
    }

    public Long getSplashes() {
        return splashes;
    }

    public void setSplashes(Long splashes) {
        this.splashes = splashes;
    }

    public String getDp() {
        return dp;
    }

    public void setDp(String dp) {
        this.dp = dp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepno() {
        return depno;
    }

    public void setDepno(String depno) {
        this.depno = depno;
    }

    public String getPhno() {
        return phno;
    }

    public void setPhno(String phno) {
        this.phno = phno;
    }

    public Student() {}

//Parcelling part

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Student createFromParcel(Parcel in) {
            return new Student(in);
        }

        public Student[] newArray(int size) {
            return new Student[size];
        }
    };

    public Student(String email, String name, String password, String depno, String phno) {
        this.email = email;
        this.name = name;
        this.depno = depno;
        this.phno = phno;
    }

    public Student(Parcel in){
        this.email = in.readString();
        this.name = in.readString();
        this.depno = in.readString();
        this.phno = in.readString();
        this.dp =  in.readString();
        this.splashes = in.readLong();
        this.bonds = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.email);
        dest.writeString(this.name);
        dest.writeString(this.depno);
        dest.writeString(this.phno);
        dest.writeString(this.dp);
        dest.writeLong(this.splashes);
        dest.writeLong(this.bonds);

    }
    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", depno='" + depno + '\'' +
                ", phoneno='" + phno + '\'' +
                '}';
    }
}