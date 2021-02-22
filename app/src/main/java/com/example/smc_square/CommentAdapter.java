package com.example.smc_square;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

public class CommentAdapter extends FirestoreRecyclerAdapter<Comment,CommentAdapter.CommentHolder> {

    private static final String TAG ="Displaying Comment" ;
    private Student user;
    private String splashUser;
    private String path, postId;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public CommentAdapter(@NonNull FirestoreRecyclerOptions<Comment> options, Student user, String postId, String path, String splashUser) {
        super(options);
        this.user = user;
        this.path = path;
        this.splashUser = splashUser;
        this.postId = postId;
    }

    @Override
    protected void onBindViewHolder(@NonNull final CommentAdapter.CommentHolder holder, int position, @NonNull Comment model) {
        holder.comment.setText(model.getComment());
        Picasso.get().load(model.getProPic()).into(holder.proPic);
        holder.name.setText(model.getName());
        DocumentSnapshot snapshot = getSnapshots().getSnapshot(holder.getAdapterPosition());
        final String docId = snapshot.getId();

        if (model.getDepNo().equals(user.getDepno()))
            holder.deleteComment.setVisibility(View.VISIBLE);
        else
            holder.deleteComment.setVisibility(View.INVISIBLE);

        holder.deleteComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection(path).document(docId)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully deleted!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error deleting document", e);
                            }
                        });

                if(!user.getDepno().equals(splashUser)) {
                    db.collection("Users/" + splashUser + "/Notifications")
                            .whereEqualTo("commentId", docId)
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

                db.collection("Users/" + splashUser + "/Splashes").document(postId)
                        .update("commentCount", FieldValue.increment(-1))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess: Comment Count decremented");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: Comment count decrement failed" + e);
                            }
                        });
            }
        });
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.comment_template,parent,false);
        return new CommentHolder(view);
    }

    public class CommentHolder extends RecyclerView.ViewHolder {
        TextView comment;
        ImageView proPic;
        ImageButton deleteComment;
        TextView name;
        public CommentHolder(@NonNull View itemView) {
            super(itemView);
            comment = itemView.findViewById(R.id.commentDes);
            proPic = itemView.findViewById(R.id.propic);
            deleteComment = itemView.findViewById(R.id.deleteComment);
            name= itemView.findViewById(R.id.Name);
        }
    }
}