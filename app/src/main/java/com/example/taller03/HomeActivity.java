package com.example.taller03;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.taller03.model.DatabasePaths;
import com.example.taller03.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        FirebaseUser user = mAuth.getCurrentUser();
        myRef = database.getReference(DatabasePaths.USER);

        int itemClicked = item.getItemId();
        if(itemClicked == R.id.menuLogOut){
            mAuth.signOut();
            disconect(user, item);
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        if(itemClicked == R.id.available){
            isAvailable(user, item);
        }
        if(itemClicked == R.id.availableList){
            Intent intent = new Intent(HomeActivity.this, Available_activity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void disconect(FirebaseUser user, MenuItem item){
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                User newUser = snapshot.getValue(User.class);

                if(user.getUid().equals(snapshot.getKey())){
                    User p = new User();
                    p.setName(newUser.getName());
                    p.setLastname(newUser.getLastname());
                    p.setNumID(newUser.getNumID());
                    p.setAvailable(false);
                    myRef=database.getReference(DatabasePaths.USER + user.getUid());
                    myRef.setValue(p);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void isAvailable(FirebaseUser user, MenuItem item){
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                User newUser = snapshot.getValue(User.class);

                if(user.getUid().equals(snapshot.getKey())){
                    User p = new User();
                    p.setName(newUser.getName());
                    p.setLastname(newUser.getLastname());
                    p.setNumID(newUser.getNumID());

                    if(newUser.isAvailable()){
                        p.setAvailable(false);
                        item.setTitle("Connect");
                    } else {
                        p.setAvailable(true);
                        item.setTitle("Disconnect");
                    }
                    myRef=database.getReference(DatabasePaths.USER + user.getUid());
                    myRef.setValue(p);
                    if(p.isAvailable()){
                        Toast.makeText(HomeActivity.this, "Change to: "+"connected",
                                Toast.LENGTH_SHORT).show();
                    }
                    if(p.isAvailable()){
                        Toast.makeText(HomeActivity.this, "Change to: "+"disconnected",
                                Toast.LENGTH_SHORT).show();
                    }

                }


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}