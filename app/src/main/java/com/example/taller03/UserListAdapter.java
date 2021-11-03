package com.example.taller03;


import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolderDatos> {

    private ArrayList<UserItem> items;

    public UserListAdapter(ArrayList<UserItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolderDatos onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_item, null, false);
        return new ViewHolderDatos(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderDatos holder, int position) {
        holder.name.setText(items.get(position).getName());
        Picasso.get().load(items.get(position).getImage()).into(holder.avatar);
        holder.id = items.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolderDatos extends RecyclerView.ViewHolder {
        TextView name;
        ImageView avatar;
        Button mapBtn;
        String id;
        public ViewHolderDatos(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.edit_user_name);
            avatar = itemView.findViewById(R.id.imageView_user);
            mapBtn = itemView.findViewById(R.id.button_user_location);
            mapBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("demo",id);
                    Intent intent = new Intent(itemView.getContext(),MapsActivity.class);
                    intent.putExtra("key",id);
                    itemView.getContext().startActivity(intent);
                }
            });
        }

    }

}
