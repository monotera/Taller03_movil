package com.example.taller03;

import com.google.firebase.database.DataSnapshot;

public interface OnGetDataListener {
    void onSucess(DataSnapshot dataSnapshot);
    void onStart();
    void onFailure();
}
