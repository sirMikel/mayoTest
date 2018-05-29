package projectv2.devteam.community.teleportv2;

import android.*;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.SphericalUtil;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import projectv2.devteam.community.teleportv2.Common.Common;
import projectv2.devteam.community.teleportv2.Model.Token;
import projectv2.devteam.community.teleportv2.Remote.IGoogleAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {


    private static final String PREF_NAME = "SwitchState" ;
    private GoogleMap mMap;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;

    //play services
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST_CODE = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;


    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference drivers;
    GeoFire geoFire;

    Marker mPickupMarker, mCurrent;
    Circle riderCircleMarker;

    Switch location_switch;
    SupportMapFragment mapFragment;

    //car animation
    private List<LatLng> polyLineList;
    private Marker pickUpLocationMarker, carMarker;
    private float v;
    private double lat, lng;
    private Handler handler;
    private LatLng startPosition, endPosition, currentPosition;
    private int index, next;
    //private Button btnGo;
    private PlaceAutocompleteFragment places;
    AutocompleteFilter typeFilter;
    private String destination;
    private PolylineOptions polyLineOptions, blackPolylineOptions;
    private Polyline blackPolyline, greyPolyLine;

    private IGoogleAPI mService;

    //online presence system
    DatabaseReference /*onlineRef*/ availableRef, currentUserRef;

    //Firebase Storage
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        startService(new Intent(DriverHome.this,OnAppKilled.class));
        //initFirebaseStorage
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navigationHeaderView = navigationView.getHeaderView(0);
        TextView txtName = (TextView) navigationHeaderView.findViewById(R.id.txtDriverName);
        TextView txtRefCode = (TextView) navigationHeaderView.findViewById(R.id.txtRefCode);//change to team name
        TextView txtTeamName = (TextView) navigationHeaderView.findViewById(R.id.txtTeamName);//change to team name
        CircleImageView imageAvatar = (CircleImageView) navigationHeaderView.findViewById(R.id.image_avatar);

        //access from common
        txtName.setText(Common.currentUser.getName());
        txtRefCode.setText(Common.currentUser.getRefCode());
        txtTeamName.setText(Common.currentUser.getTeamName());

        //but with avatar check for null
        if (Common.currentUser.getAvatarUrl() != null
                && !TextUtils.isEmpty(Common.currentUser.getAvatarUrl())) {
            Picasso.with(this)
                    .load(Common.currentUser.getAvatarUrl())
                    .into(imageAvatar);
        }

        //paste here
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //presence system
        //onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        //availableRef = FirebaseDatabase.getInstance().getReference().child(Common.drivers_available_tbl_);

        /*currentUserRef = FirebaseDatabase.getInstance().getReference()
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());*/

       /* availableRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //remove value from driver tbl when disconnected
                currentUserRef.onDisconnect().removeValue();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/


        /*SharedPreferences online = getSharedPreferences(PREF_NAME,0);
        boolean silent = online.getBoolean("switchkey",false);
        location_switch

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean silent = settings.getBoolean("switchkey", false);
        location_switch.setChecked(silent);*/




        //Init View
        location_switch = (Switch) findViewById(R.id.location_switch);

        SharedPreferences sharedPref  = getSharedPreferences("switchState", Context.MODE_PRIVATE);
        boolean silent = sharedPref.getBoolean("switchkey", false );

        location_switch.setChecked(silent);

        location_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    ///FirebaseDatabase.getInstance().goOnline();//set connected when switch to on

                    if (ActivityCompat.checkSelfPermission(DriverHome.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DriverHome.this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    buildLocationRequest();
                    buildLocationCallback();
                    fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
                    displayLocation();
                    Snackbar.make(mapFragment.getView(), "You are now online", Snackbar.LENGTH_SHORT)
                            .show();

                }else{
                    DatabaseReference ref =FirebaseDatabase.getInstance().getReference(Common.drivers_available_tbl_);

                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.removeLocation(Common.myId);

                    //FirebaseDatabase.getInstance().goOffline();//set disconnect
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    if (mCurrent!=null){
                        mCurrent.remove();
                    }

                    mMap.clear();

                    /*/*//*//**//*working switch log out//////////from simcoder//////////////////////////////
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Common.driver_tbl_);

                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.removeLocation(userId);*/
                    ///////////////////////////////////////////////////////////////*//*//*

                    Snackbar.make(mapFragment.getView(), "You are now offline", Snackbar.LENGTH_SHORT)
                            .show();
                }
                SharedPreferences sharedPref = getSharedPreferences("switchState", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("switchkey", isChecked);
                editor.apply();

            }
        });


      /* location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {
                if (isOnline) {
                    ///FirebaseDatabase.getInstance().goOnline();//set connected when switch to on

                    if (ActivityCompat.checkSelfPermission(DriverHome.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DriverHome.this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    buildLocationRequest();
                    buildLocationCallback();
                    fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
                    displayLocation();
                    Snackbar.make(mapFragment.getView(), "You are now online", Snackbar.LENGTH_SHORT)
                            .show();
                } else {
                    DatabaseReference ref =FirebaseDatabase.getInstance().getReference(Common.drivers_available_tbl_);

                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.removeLocation(Common.myId);

                    //FirebaseDatabase.getInstance().goOffline();//set disconnect
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    mCurrent.remove();
                    mMap.clear();

                    *//**//*//**//*working switch log out//////////from simcoder//////////////////////////////
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Common.driver_tbl_);

                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.removeLocation(userId);
                    //////////////////////////////////////////////////////////////*//*//**//*//**//*

                    Snackbar.make(mapFragment.getView(), "You are now offline", Snackbar.LENGTH_SHORT)
                            .show();
                }

                SharedPreferences sharedPref = getSharedPreferences("switchState", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("switchkey", isOnline);
                editor.apply();

            }
        });*/

        polyLineList = new ArrayList<>();

        //places API
        typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();

        places = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (location_switch.isChecked()) {
                    destination = place.getAddress().toString();
                    destination = destination.replace(" ", "+");

                    getDirection();
                } else {
                    Toast.makeText(DriverHome.this, "Please Change your status to ONLINE", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(DriverHome.this, "" + status.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        //GeoFire
        drivers = FirebaseDatabase.getInstance().getReference(Common.drivers_available_tbl_);//avaiable
        geoFire = new GeoFire(drivers);

        setUpLocation();
        mService = Common.getGoogleAPI();
        updateFirebaseToken();
        Common.myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    }

    private void updateFirebaseToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tbl);

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(token);
    }


    private void getDirection() {
        currentPosition = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());

        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + currentPosition.latitude + "," + currentPosition.longitude + "&" +
                    "destination=" + destination + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);
            Log.d("TeleportDev", requestApi);//print url for debug
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject route = jsonArray.getJSONObject(i);
                                    JSONObject poly = route.getJSONObject("overview_polyline");
                                    String polyline = poly.getString("points");
                                    polyLineList = decodePoly(polyline);
                                }
                                //Adusting Bounds
                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                for (LatLng latLng : polyLineList)
                                    builder.include(latLng);
                                LatLngBounds bounds = builder.build();
                                CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
                                mMap.animateCamera(mCameraUpdate);

                                polyLineOptions = new PolylineOptions();
                                polyLineOptions.color(Color.GRAY);
                                polyLineOptions.width(8);
                                polyLineOptions.startCap(new SquareCap());
                                polyLineOptions.endCap(new SquareCap());
                                polyLineOptions.jointType(JointType.ROUND);
                                polyLineOptions.addAll(polyLineList);
                                greyPolyLine = mMap.addPolyline(polyLineOptions);

                                blackPolylineOptions = new PolylineOptions();
                                blackPolylineOptions.color(Color.BLACK);
                                blackPolylineOptions.width(8);
                                blackPolylineOptions.startCap(new SquareCap());
                                blackPolylineOptions.endCap(new SquareCap());
                                blackPolylineOptions.jointType(JointType.ROUND);
                                blackPolyline = mMap.addPolyline(blackPolylineOptions);

                                if (mPickupMarker != null)
                                    mPickupMarker.remove();
                                mPickupMarker = mMap.addMarker(new MarkerOptions()
                                        .position(polyLineList.get(polyLineList.size() - 1))
                                        .title("Pickup Location"));

                                /*mCurrent=mMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.colored_rocket))
                                        .position(new LatLng(lattitude,longitude))
                                        .title("my location"));*/


                                ///ANIMATION
                                /*
                                ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0,100);
                                polyLineAnimator.setDuration(2000);
                                polyLineAnimator.setInterpolator(new LinearInterpolator());
                                polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        List<LatLng> points = greyPolyLine.getPoints();
                                        int percentValue = (int)valueAnimator.getAnimatedValue();

                                    }
                                });*/


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        //  place @ charOverride
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(DriverHome.this, "" + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    buildLocationCallback();
                    buildLocationRequest();
                    if (location_switch.isChecked())
                        displayLocation();
                }
        }
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //request Runtime Permission
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        } else {
            buildLocationRequest();
            buildLocationCallback();
            if (location_switch.isChecked()){
                displayLocation();
            }

        }

    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    Common.mLastLocation = location;

                }
                if (location_switch.isChecked()){
                    displayLocation();
                }
            }
        };
    }

    private void buildLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Common.mLastLocation = location;

                        if (Common.mLastLocation != null) {
                            if (location_switch.isChecked()) {
                                final double lattitude = Common.mLastLocation.getLatitude();
                                final double longitude = Common.mLastLocation.getLongitude();

                                LatLng center = new LatLng(lattitude, longitude);
                                LatLng northSide = SphericalUtil.computeOffset(center, 10000, 0);
                                LatLng southSide = SphericalUtil.computeOffset(center, 10000, 0);

                                LatLngBounds bounds = LatLngBounds.builder()
                                        .include(northSide)
                                        .include(southSide)
                                        .build();
                                places.setBoundsBias(bounds);
                                places.setFilter(typeFilter);

                                LatLng myLocation = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());
                                if (mCurrent!=null)
                                    mCurrent.remove();
                                mCurrent = mMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.pinkmarker))
                                        .position(myLocation)
                                        .title("My Location"));
                                //move camera to this position
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15.0f));
                                //animate marker
                                // rotateMarker(mCurrent, -360,mMap);

                                //Update to FireBase
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Common.drivers_available_tbl_);
                                GeoFire geoFire = new GeoFire(ref);
                                geoFire.setLocation(Common.myId,new GeoLocation(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()));

                                if (riderCircleMarker!=null)
                                    riderCircleMarker.remove();
                                riderCircleMarker = mMap.addCircle(new CircleOptions()
                                        .center(myLocation)
                                        .radius(1900) // => radius is m
                                        .strokeColor(Color.BLUE)
                                        .fillColor(0x080000FF)
                                        .strokeWidth(0.8f));



                            }
                        } else {
                            Log.d("Error", "Cannot get Your Location");
                        }
                    }
                });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.driver_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_trip_history) {
            // Handle the camera action
        } else if (id == R.id.nav_way_bill) {

        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_update_info) {
            showDialogUpdateInfo();

        } else if (id == R.id.nav_change_pwd) {
            showDialogChangePwd();

        } else if (id == R.id.nav_sign_out) {
            signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showDialogUpdateInfo() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(DriverHome.this);
        alertDialog.setTitle("Update Information");
        alertDialog.setMessage("Carefully fill all information");

        LayoutInflater inflater = this.getLayoutInflater();
        View layout_pwd = inflater.inflate(R.layout.layout_update_information, null);

        final MaterialEditText edtName = (MaterialEditText) layout_pwd.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = (MaterialEditText) layout_pwd.findViewById(R.id.edtPhone);
        final ImageView image_upload = (ImageView) layout_pwd.findViewById(R.id.image_upload);
        image_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        alertDialog.setView(layout_pwd);
        //set button
        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                final android.app.AlertDialog waitingDialog = new SpotsDialog(DriverHome.this);
                waitingDialog.show();

                String name = edtName.getText().toString();
                String phone = edtPhone.getText().toString();
                Map<String, Object> updateInfo = new HashMap<>();
                if (!TextUtils.isEmpty(name))
                    updateInfo.put("name", name);
                if (!TextUtils.isEmpty(phone))
                    updateInfo.put("phone", phone);

                DatabaseReference driverInformations = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl_);
                driverInformations.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .updateChildren(updateInfo)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                    Toast.makeText(DriverHome.this, "Information Updated!!", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(DriverHome.this, "Information Update Failed!!", Toast.LENGTH_SHORT).show();
                                waitingDialog.dismiss();
                            }
                        });

            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            Uri saveUri = data.getData();
            if (saveUri != null) {
                final ProgressDialog mDialog = new ProgressDialog(this);
                mDialog.setMessage("Uploading . . .");
                mDialog.show();

                String imageName = UUID.randomUUID().toString();//Random name image upload
                final StorageReference imageFolder = storageReference.child("images/" + imageName);
                imageFolder.putFile(saveUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                mDialog.dismiss();

                                imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        //update this url to avatar
                                        //1st needs to add avatar property on user model
                                        Map<String, Object> avatarUpdate = new HashMap<>();
                                        avatarUpdate.put("avatarUrl", uri.toString());

                                        DatabaseReference driverInformations = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl_);
                                        driverInformations.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .updateChildren(avatarUpdate)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                            Toast.makeText(DriverHome.this, "Image successfully uploaded!!", Toast.LENGTH_SHORT).show();
                                                        else
                                                            Toast.makeText(DriverHome.this, "Upload Error!", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                    }
                                });
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                mDialog.setMessage("Uploading " + progress + "%");
                            }
                        });

            }
        }
    }

    private void showDialogChangePwd() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(DriverHome.this);
        alertDialog.setTitle("CHANGE PASSWORD");
        alertDialog.setMessage("Carefully fill all information");

        LayoutInflater inflater = this.getLayoutInflater();
        View layout_pwd = inflater.inflate(R.layout.layout_change_pwd, null);

        final MaterialEditText edtPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtPassword);
        final MaterialEditText edtNewPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtNewPassword);
        final MaterialEditText edtRepeatPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtRepeatPassword);

        alertDialog.setView(layout_pwd);

        //Set button
        alertDialog.setPositiveButton("CHANGE PASSWORD", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int i) {

                final android.app.AlertDialog waitingDialog = new SpotsDialog(DriverHome.this);
                waitingDialog.show();

                if (edtNewPassword.getText().toString().equals(edtRepeatPassword.getText().toString())) {
                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    //get auth credentials
                    //sample with email
                    AuthCredential credential = EmailAuthProvider.getCredential(email, edtPassword.getText().toString());
                    FirebaseAuth.getInstance().getCurrentUser()
                            .reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseAuth.getInstance().getCurrentUser()
                                                .updatePassword(edtRepeatPassword.getText().toString())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            //Update Driver information
                                                            Map<String,Object> password = new HashMap<>();

                                                            password.put("password", edtRepeatPassword.getText().toString());

                                                            DatabaseReference driverInformation = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl_);
                                                            driverInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                    .updateChildren(password)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful())
                                                                                Toast.makeText(DriverHome.this, "Password change successful!!", Toast.LENGTH_SHORT).show();
                                                                            else
                                                                                Toast.makeText(DriverHome.this, "Password change but may not be successful", Toast.LENGTH_SHORT).show();
                                                                            waitingDialog.dismiss();
                                                                        }
                                                                    });
                                                        }
                                                        else
                                                        {
                                                            Toast.makeText(DriverHome.this, "Cannot complete action", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        waitingDialog.dismiss();
                                        Toast.makeText(DriverHome.this, "Password Incorrect", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    waitingDialog.dismiss();
                    Toast.makeText(DriverHome.this, "Password doesn't match ", Toast.LENGTH_SHORT).show();
                }

            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //show dialog
        alertDialog.show();

    }

    private void signOut() {


        /////////////////////////////////////////////
        //reset remember value
        Paper.init(this);
        Paper.book().destroy();

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(DriverHome.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            boolean isSuccess = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.uber_style_map)
            );
            if (!isSuccess)
                Log.e("Error", "Map style failed!!!");

        } catch (Resources.NotFoundException ex) {
            ex.printStackTrace();
        }

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);


        ///////////////////////////////////


        /*LatLng initialView = new LatLng(14.553228, 121.022150);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(initialView, 10.0f));*/


        //Update location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        buildLocationRequest();
        buildLocationCallback();
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
    }
}
