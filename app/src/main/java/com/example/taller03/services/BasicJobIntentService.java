package com.example.taller03.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.taller03.MapsActivity;
import com.example.taller03.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.taller03.R;


import com.example.taller03.model.DatabasePaths;

public class BasicJobIntentService extends JobIntentService {
    // Id for the service
    private static final int JOB_ID = 12;

    public static final String CHANNEL_ID = "Taller03";
    private int notificationId;

    // Variables for Firebase DB
    FirebaseDatabase database;
    DatabaseReference myRef;
    ValueEventListener valueEventListener;
    FirebaseUser currentUser;
    FirebaseAuth mAuth;

    // Aux method to queue the task.
    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, BasicJobIntentService.class, JOB_ID, intent);
    }
    // Method that executes in background.
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        notificationId = 1;
        //Initialize Firebase database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        currentUser = mAuth.getCurrentUser();
        createNotificationChannel();
        loadSubscriptionUsers();
    }

    public void loadSubscriptionUsers(){
        myRef = database.getReference(DatabasePaths.USER);
        valueEventListener = myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    User newUser = singleSnapshot.getValue(User.class);

                    if(newUser.isAvailable() && !currentUser.getUid().equals(singleSnapshot.getKey())){
                        // Simple Notification example
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getBaseContext(), CHANNEL_ID);
                        mBuilder.setSmallIcon(R.drawable.g1);
                        mBuilder.setContentTitle("Se conecto un usuario");
                        mBuilder.setContentText("El usuario "+newUser.getName()+" "+newUser.getLastname()+" esta conectado");

                        //Acción asociada a la notificación
                        //CAMBIAAAAAR
                        Intent intent = new Intent(getBaseContext(), MapsActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, 0);
                        mBuilder.setContentIntent(pendingIntent);
                        mBuilder.setAutoCancel(true); //Remueve la notificación cuando se toca

                        //Lanzar la notificacion
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getBaseContext());
                        // notificationId es un entero unico definido para cada notificacion que se lanza
                        notificationManager.notify(Integer.parseInt(newUser.getNumID()), mBuilder.build());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Background", "error en la consulta por subscripcions", databaseError.toException());
            }
        });
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel";
            String description = "channel description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            //IMPORTANCE_MAX MUESTRA LA NOTIFICACIÓN ANIMADA
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
