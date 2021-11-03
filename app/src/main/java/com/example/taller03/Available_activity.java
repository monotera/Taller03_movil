package com.example.taller03;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.Toast;

import com.example.taller03.model.DatabasePaths;
import com.example.taller03.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.model.LatLng;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;


public class Available_activity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private ArrayList<UserItem> userList;
    private RecyclerView recycleList;
    private Button mapBtn;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    Uri avatar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available);

        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase database
        database = FirebaseDatabase.getInstance();

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        userList = new ArrayList<>();
        recycleList = findViewById(R.id.userList_available);
        recycleList.setLayoutManager(new LinearLayoutManager(this));
        fill();

        mapBtn = findViewById(R.id.button_user_location);




    }


    public void fill(){
        FirebaseUser user = mAuth.getCurrentUser();
        myRef = database.getReference(DatabasePaths.USER);

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                User newUser = snapshot.getValue(User.class);
                if(newUser.isAvailable()){
                    Log.i("TAG",snapshot.getKey()+".jpg");
                    storageRef.child(snapshot.getKey()+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            avatar = uri;
                            userList.add(new UserItem(newUser.getName(), avatar,snapshot.getKey()));
                            UserListAdapter userAdapter = new UserListAdapter(userList);
                            recycleList.setAdapter(userAdapter);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("ERROR",e.toString());
                        }
                    });

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