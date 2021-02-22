package com.example.smc_square;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

class SearchAdapter extends ArrayAdapter<Student> {

    private Context ct;
    private int mresource;
    private Student user;

    public SearchAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Student> objects,Student user) {
        super(context, resource, objects);
        this.ct = context;
        mresource=resource;
        this.user=user;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Student s = (Student) getItem(position);
        LayoutInflater inflater = LayoutInflater.from(ct);
        convertView = inflater.inflate(mresource,parent,false); // converting xml to java

        RelativeLayout namedp = convertView.findViewById(R.id.namedp);
        TextView search_name = convertView.findViewById(R.id.search_name);
        ImageView search_dp = convertView.findViewById(R.id.search_dp);

        search_name.setText(s.getName());
        Picasso.get().load(s.getDp()).into(search_dp);

        namedp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ct,FriendAccountActivity.class);
                intent.putExtra("user", user);
                intent.putExtra("friend", s);
                intent.putExtra("preAct", "SA");
                ct.startActivity(intent);
            }
        });

        return convertView;
    }
}
