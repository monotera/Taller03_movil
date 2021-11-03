package com.example.taller03;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.taller03.model.DatabasePaths;
import com.example.taller03.model.User;
import com.example.taller03.services.BasicJobIntentService;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.taller03.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String TAG = MainActivity.class.getName();
    private static final String LOCATIONS_FILE = "locations.json";
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private JSONArray json_locations_file;
    ArrayList<LocationJSON> json_locationJSONS = new ArrayList<>();
    private Logger logger = Logger.getLogger("TAG");
    boolean first_location = true;
    LatLng list_user_location;
    public final static double RADIUS_OF_EARTH_KM = 6371;
    String list_user_id;
    HashMap<String,Marker> hasMarkers = new HashMap<>();

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    //Variables de permisos
    private final int LOCATION_PERMISSION_ID = 103;
    public static final int REQUEST_CHECK_SETTINGS = 201;
    String locationPerm = Manifest.permission.ACCESS_FINE_LOCATION;

    //Variables de localizacion
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    Location mCurrentLocation;
    boolean print_intial_distance =  true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            list_user_id = extras.getString("key");
            Log.d("aaaaaa",list_user_id);
        }

        try {
            JSONObject jsonFile = loadLocationsByJSON();
            json_locations_file = jsonFile.getJSONArray("locations");

            for(int i = 0; i < json_locations_file.length(); i++){
                LocationJSON temp = new LocationJSON();
                temp.setName(json_locations_file.getJSONObject(i).getString("name"));
                temp.setLatitude(json_locations_file.getJSONObject(i).getDouble("latitude"));
                temp.setLongitude(json_locations_file.getJSONObject(i).getDouble("longitude"));
                json_locationJSONS.add(temp);

            }
            //Log.i(json_locations.getJSONObject(0).getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mLocationRequest = createLocationRequest();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, "El permiso es necesario para acceder a la localizacion", LOCATION_PERMISSION_ID);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                Log.i("LOCATION", "LOCATION CALLBACK" + location);
                if (location != null) {
                    mCurrentLocation = location;
                    update_my_location();
                    if(list_user_id!=null)
                        get_location_FB();
                    first_location = false;
                    /*logger.info(String.valueOf(location.getLatitude()));
                    logger.info(String.valueOf(location.getLongitude()));
                    logger.info(String.valueOf(location.getAltitude()));*/
                }
            }
        };

        turnOnLocationAndStartUpdates();
        if(extras != null){
          //  Double distance = distance(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude(),list_user_location.latitude,list_user_location.longitude);
            //Toast.makeText(MapsActivity.this,"La distancia es de :" + distance.toString(),Toast.LENGTH_SHORT).show();
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        print_intial_distance = true;
        first_location = true;
        startLocationUpdates();

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Service launch from i.e. onCreate
//        Intent intent = new Intent(MainActivity.this, BasicIntentService.class);
//        startService(intent);
        Intent intent = new Intent(MapsActivity.this, BasicJobIntentService.class);
        intent.putExtra("milliSeconds", 5000);
        BasicJobIntentService.enqueueWork(MapsActivity.this, intent);
        Log.i(TAG, "After the call to the service");
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //Permitir gestos
        mMap.getUiSettings().setAllGesturesEnabled(true);
        //zoom buttons
        mMap.getUiSettings().setZoomControlsEnabled(true);
        paintJson();

    }
    public void paintJson(){

        for (LocationJSON loc: json_locationJSONS) {
            LatLng json_loc = new LatLng(loc.getLatitude(),loc.getLongitude());
            mMap.addMarker(new MarkerOptions().position(json_loc).title(loc.getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }
    }
    public String loadJSONFromAsset(String assetName) {
        String json = null;
        try {
            InputStream is = this.getAssets().open(assetName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public JSONObject loadLocationsByJSON() throws JSONException {
        return new JSONObject(loadJSONFromAsset(LOCATIONS_FILE));
    }


    //Location section

    public void update_my_location() {
        FirebaseUser user = mAuth.getCurrentUser();
        myRef = database.getReference(DatabasePaths.USER);
        update_my_location_FB(user);
        Marker home = hasMarkers.get("Home");
        if(home != null) {
            home.remove();
            hasMarkers.remove("Home");
        }
        LatLng my_location = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        Marker marker = mMap.addMarker(new MarkerOptions().position(my_location).title("My location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
        hasMarkers.put("Home",marker);
        if(first_location){
            mMap.moveCamera(CameraUpdateFactory.newLatLng(my_location));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        }
        paintJson();
    }



    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, null);
        }
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    private void requestPermission(Activity context, String permiso, String justificacion, int idCode) {
        if (ContextCompat.checkSelfPermission(context, permiso) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, permiso)) {
                Toast.makeText(context, justificacion, Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(context, new String[]{permiso}, idCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION_ID: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Ya hay permiso para acceder a la localizacion", Toast.LENGTH_LONG).show();
                    turnOnLocationAndStartUpdates();
                } else {
                    Toast.makeText(this, "No hay Permiso :(", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void turnOnLocationAndStartUpdates() {
        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task =
                client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, locationSettingsResponse -> {
            startLocationUpdates(); //Todas las condiciones para recibir localizaciones
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. No way to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS: {
                if (resultCode == RESULT_OK) {
                    startLocationUpdates();
                } else {
                    Toast.makeText(this, "Sin acceso a localizacion, hardware deshabilitado!", Toast.LENGTH_LONG).show();
                }
            }
        }

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
            Intent intent = new Intent(MapsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        if(itemClicked == R.id.available){
            isAvailable(user, item);
        }
        if(itemClicked == R.id.availableList){
            Intent intent = new Intent(MapsActivity.this, Available_activity.class);
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
                    p.setLng(newUser.getLng());
                    p.setLat(newUser.getLat());
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
                    p.setLng(newUser.getLng());
                    p.setLat(newUser.getLat());

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
                        Toast.makeText(MapsActivity.this, "Change to: "+"connected",
                                Toast.LENGTH_SHORT).show();
                    }
                    if(p.isAvailable()){
                        Toast.makeText(MapsActivity.this, "Change to: "+"disconnected",
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

    //Firebase

    public void get_location_FB(){
        FirebaseUser user = mAuth.getCurrentUser();
        myRef = database.getReference(DatabasePaths.USER);

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                User newUser = snapshot.getValue(User.class);
                if(list_user_id.equals(snapshot.getKey())){
                    Log.i("asd","Ebtro");
                    list_user_location = new LatLng(newUser.getLat(),newUser.getLng());
                    if(print_intial_distance){
                        Double distance = distance(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude(),list_user_location.latitude,list_user_location.longitude);
                        Toast.makeText(MapsActivity.this,"La distancia es de : " + distance.toString() + " km",Toast.LENGTH_SHORT).show();
                        print_intial_distance = false;
                    }

                    Marker validation = hasMarkers.get("list_user");
                    if(validation != null) {
                        validation.remove();
                        hasMarkers.remove("list_user");
                    }

                    Marker marker = mMap.addMarker(new MarkerOptions().position(list_user_location).title(newUser.getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                    hasMarkers.put("list_user",marker);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                User newUser = snapshot.getValue(User.class);
                if(list_user_id.equals(snapshot.getKey())){
                    Log.i("asd","Ebtro");
                    list_user_location = new LatLng(newUser.getLat(),newUser.getLng());
                    Double distance = distance(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude(),list_user_location.latitude,list_user_location.longitude);
                    Toast.makeText(MapsActivity.this,"La distancia es de : " + distance.toString() + " km",Toast.LENGTH_SHORT).show();
                    Marker validation = hasMarkers.get("list_user");
                    if(validation != null) {
                        validation.remove();
                        hasMarkers.remove("list_user");
                    }

                    Marker marker = mMap.addMarker(new MarkerOptions().position(list_user_location).title(newUser.getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                    hasMarkers.put("list_user",marker);
                }
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
    public void update_my_location_FB(FirebaseUser user){
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                User newUser = snapshot.getValue(User.class);

                if(user.getUid().equals(snapshot.getKey())){
                    User p = new User();
                    p.setName(newUser.getName());
                    p.setLastname(newUser.getLastname());
                    p.setNumID(newUser.getNumID());
                    p.setAvailable(newUser.isAvailable());
                    p.setLat((float) mCurrentLocation.getLatitude());
                    p.setLng((float) mCurrentLocation.getLongitude());
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


    public double distance(double lat1, double long1, double lat2, double long2) {
        double latDistance = Math.toRadians(lat1 - lat2);
        double lngDistance = Math.toRadians(long1 - long2);
        double a = Math.sin(latDistance / 2) *
                Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) *
                        Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lngDistance / 2) *
                        Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double result = RADIUS_OF_EARTH_KM * c;
        return Math.round(result * 100.0) / 100.0;
    }

}