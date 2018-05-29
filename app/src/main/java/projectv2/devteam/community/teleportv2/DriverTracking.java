package projectv2.devteam.community.teleportv2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import projectv2.devteam.community.teleportv2.Common.Common;
import projectv2.devteam.community.teleportv2.Helper.DirectionJSONParser;
import projectv2.devteam.community.teleportv2.Model.DataMessage;
import projectv2.devteam.community.teleportv2.Model.FCMResponse;
import projectv2.devteam.community.teleportv2.Model.Token;
import projectv2.devteam.community.teleportv2.Model.User;
import projectv2.devteam.community.teleportv2.Remote.IFCMService;
import projectv2.devteam.community.teleportv2.Remote.IGoogleAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static projectv2.devteam.community.teleportv2.Common.Common.csUid;
import static projectv2.devteam.community.teleportv2.Common.Common.myId;

public class DriverTracking extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

   //FusedLocationProviderClient fusedLocationProviderClient;
    //LocationCallback locationCallback;

    private GoogleMap mMap;

    String riderLat, riderLng, destlat, destlng, csID, csContact;
    String rideId;

    ImageButton btnCallCS, btnSmsCS, btnwazePU, btnGmapPU, btnwazeDO, btnGmapDO;

    //play services
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST_CODE = 7001;



    ///////////////////////////////////////////////
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    ///////////////////////////////////////////////

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    private Circle riderMarker;
    private Marker driverMarker;

    private Polyline direction;

    String customerId;


    IGoogleAPI mService;
    IFCMService mFCMService;

    GeoFire geoFire;

    Button btnStartTrip,btnCancelRide;

    Location pickupLocation;
    TextView txtcsName,txtpickupInfo,txtdropoffInfo;

    LinearLayout dropoffInfoGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ArrayList points=null;
        PolylineOptions polylineOptions = null;

        dropoffInfoGroup = (LinearLayout)findViewById(R.id.dropoffInfoGroup);

        txtcsName = (TextView)findViewById(R.id.csName);
        txtpickupInfo = (TextView)findViewById(R.id.pickupInfo);
        txtdropoffInfo = (TextView)findViewById(R.id.dropoffInfo);

        btnCallCS = (ImageButton)findViewById(R.id.callCS);
        btnSmsCS = (ImageButton)findViewById(R.id.smsCS);
        btnwazePU = (ImageButton)findViewById(R.id.wazePU);
        btnGmapPU = (ImageButton)findViewById(R.id.gmapPU);
        btnwazeDO = (ImageButton)findViewById(R.id.wazeDO);
        btnGmapDO = (ImageButton)findViewById(R.id.gmapDO);

        
        
        mService=Common.getGoogleAPI();
        mFCMService = Common.getFCMService();
        txtpickupInfo.setText(Common.pickUpLocation);
        txtdropoffInfo.setText(Common.dropOffLocation);

        if (getIntent() !=null)
        {
            csID = getIntent().getStringExtra("csUid");
            riderLat = getIntent().getStringExtra("lat");
            riderLng = getIntent().getStringExtra("lng");
            destlat = getIntent().getStringExtra("destlat");
            destlng = getIntent().getStringExtra("destlng");
            customerId = getIntent().getStringExtra("customerId");

            //Location pickup = new Location();


            if (csID != null) {
                FirebaseDatabase.getInstance()
                        .getReference(Common.user_rider_tbl_)
                        .child(csID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    User csUser = dataSnapshot.getValue(User.class);

                                    txtcsName.setText(csUser.getName());
                                    csContact = csUser.getPhone();
                                }

                        /*txt_phone.setText(csUser.getPhone());
                        txt_motorcycle.setText(csUser.getMotorcycle());
                        txt_plateNum.setText(csUser.getPlateNumber());
                        txt_teamName.setText(csUser.getTeamName());*/
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }
        }

        if (destlng==null)
            dropoffInfoGroup.setVisibility(View.GONE);

        btnCallCS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+csContact));
                startActivity(intent);
            }
        });

        btnSmsCS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSmsToCS();
            }
        });

        btnwazePU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://waze.com/ul?ll="+riderLat+","+riderLng+"&navigate=yes";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setPackage("com.waze");
                startActivity(intent);

            }
        });
        btnGmapPU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.google.com/maps/dir/?api=1&destination=" +riderLat+ "," +riderLng+ "&travelmode=driving";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        });

        btnwazeDO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://waze.com/ul?ll="+destlat+","+destlng+"&navigate=yes";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setPackage("com.waze");
                startActivity(intent);
            }
        });

        btnGmapDO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.google.com/maps/dir/?api=1&destination=" +destlat+ "," +destlng+ "&travelmode=driving";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        });




        /*FirebaseDatabase.getInstance()
                .getReference(Common.user_driver_tbl_)
                .child(csID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            User csUser = dataSnapshot.getValue(User.class);

                            txtcsName.setText(csUser.getName());
                            csContact = csUser.getPhone();
                        }

                        *//*txt_phone.setText(csUser.getPhone());
                        txt_motorcycle.setText(csUser.getMotorcycle());
                        txt_plateNum.setText(csUser.getPlateNumber());
                        txt_teamName.setText(csUser.getTeamName());*//*
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
*/

        /*DatabaseReference assignedCustomer = FirebaseDatabase.getInstance().getReference().child(Common.user_driver_tbl_).child(Common.myId).child("csUid");
        assignedCustomer.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    rideId = dataSnapshot.getValue().toString();
                    {
                        //getCustomerPickUpLocation();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

        setUpLocation();
        //sendOTW(customerId);

        btnCancelRide=(Button)findViewById(R.id.btnCancelRide);
        btnCancelRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAbortDialog();
            }
        });
        
        btnStartTrip = (Button)findViewById(R.id.btnStartTrip);
        btnStartTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (btnStartTrip.getText().equals("I ' M  H E R E"))
                {
                    sendArrivedNotification(customerId);
                    btnStartTrip.setText("S T A R T  T R I P");
                }
                else if (btnStartTrip.getText().equals("S T A R T  T R I P"))
                {
                    //pickupLocation = Common.mLastLocation;
                    sendInTransitNotification(customerId);
                    recordRide();
                    btnStartTrip.setText("D R O P  O F F");
                }else if (btnStartTrip.getText().equals("D R O P  O F F"))
                {
                    sendDropOffNotification(customerId);
                    Intent intent = new Intent(DriverTracking.this,TripDetail.class);
                    startActivity(intent);
                    finish();
                    //calculateCashFee();
                }
            }
        });

        //checkCsUid();
    }

    private void recordRide() {
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl_).child(Common.myId).child("history");
        DatabaseReference csRef = FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl_).child(csID).child("history");
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("History");
        String requestId = historyRef.push().getKey();
        driverRef.child(requestId).setValue(true);
        csRef.child(requestId).setValue(true);

        HashMap map = new HashMap();
        map.put("Driver",Common.myId);
        map.put("Customer",csID);
        historyRef.child(requestId).updateChildren(map);

    }

    private void showAbortDialog() {
        final AlertDialog.Builder cancelDialog = new AlertDialog.Builder(DriverTracking.this);
        cancelDialog.setTitle("Cancel current Booking");
        cancelDialog.setMessage("You are about to cancel your current booking");
        cancelDialog.setCancelable(true);

        cancelDialog.setPositiveButton("Cancel my current booking", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatabaseReference deletecsuid = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl_).child(Common.myId).child("csUid");
                deletecsuid.removeValue();
                csUid="";
                abortRide(mFCMService,getBaseContext());
            }
        });

        cancelDialog.setNegativeButton("Keep my current booking", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        cancelDialog.show();

    }

    private void abortRide(IFCMService mFCMService, Context baseContext) {
        Token token = new Token(customerId);

        /*Notification notification = new Notification("Cancel","Driver cancelled your request");
        Sender sender = new Sender(token.getToken(),notification);*/
        Map<String,String> content = new HashMap<>();
        content.put("title","Abort Ride");
        content.put("message","Driver cancelled your booking");
        DataMessage dataMessage = new DataMessage(token.getToken(),content);

        mFCMService.sendMessage(dataMessage)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if (response.body().success == 1)
                        {
                            Toast.makeText(DriverTracking.this, "Cancelled", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });
    }


    private void getCustomerPickUpLocation(){
            DatabaseReference customerPickupLocation = FirebaseDatabase.getInstance().getReference().child(Common.pickup_request_tbl_).child(rideId).child("l");
            customerPickupLocation.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        List<Object> map = (List<Object>) dataSnapshot.getValue();
                        double locationLat = 0;
                        double locationLng = 0;
                        if (map.get(0) !=null){
                            locationLat = Double.parseDouble(map.get(0).toString());
                        }
                        if (map.get(1) !=null){
                            locationLng = Double.parseDouble(map.get(1).toString());
                        }
                        LatLng csPickUp = new LatLng(locationLat,locationLng);
                       /* if (mCsMarker!=null){
                            mCsMarker.remove();
                        }
                        mCsMarker=mMap.addMarker(new MarkerOptions().position(csPickUp).title("CS PickUp Here"));*/
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }













  /*  private void calculateCashFee() {
        String requestApi = null;
        try{
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+riderLat+","+riderLng+"&destination="+destlat+","+destlng+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.d("TeleportDevCashfee", requestApi);//print url for debug
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                //Extract
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray routes = jsonObject.getJSONArray("routes");

                                JSONObject object = routes.getJSONObject(0);

                                JSONArray legs = object.getJSONArray("legs");

                                JSONObject legsObject = legs.getJSONObject(0);

                                //distance
                                JSONObject distance =legsObject.getJSONObject("distance");
                                String distance_text = distance.getString("text");
                                //only take numbers
                                Double distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+",""));

                                //duration
                                JSONObject timeObject =legsObject.getJSONObject("duration");
                                String time_text = timeObject.getString("text");
                                //only take numbers
                                Double time_value = Double.parseDouble(time_text.replaceAll("[^0-9\\\\.]+",""));


                                //Now we need to write function
                                //New activity
                                Intent intent = new Intent(DriverTracking.this,TripDetail.class);
                                intent.putExtra("start_address",legsObject.getString("start_address"));
                                intent.putExtra("end_address",legsObject.getString("end_address"));
                                intent.putExtra("time",String.valueOf(time_value));
                                intent.putExtra("distance",String.valueOf(distance_value));
                                intent.putExtra("total",Common.formulaPrice(distance_value,time_value));
                                intent.putExtra("location_start",String.format("%f,%f",riderLat,riderLng));
                                intent.putExtra("location_end",String.format("%f,%f",destlat,destlng));

                                startActivity(intent);
                                finish();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        //  place @ charOverride
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(DriverTracking.this,""+t.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });

        }catch(Exception e)
        {
            Log.e("Error", "xxxxxxxxxxxx");
            e.printStackTrace();
        }

    }*/

    private void setUpLocation() {
        /*//////////////////////////////
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
            displayLocation();
        }
        *////////////////////////////////////////////////////////
        if (checkPlayServices())
        {
            buildGoogleApiClient();
            createLocationRequest();
            displayLocation();
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode !=ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICE_RES_REQUEST_CODE).show();
            else {
                Toast.makeText(this, "Device Not Supported", Toast.LENGTH_SHORT).show();
                finish();
            }return false;
        }
        return true;
    }


    /*private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    Common.mLastLocation = location;

                }
                displayLocation();
            }
        };
    }
*/
   /* private void buildLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }*/

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


        riderMarker = mMap.addCircle(new CircleOptions()
                .center(new LatLng(Double.parseDouble(riderLat),Double.parseDouble(riderLng)))
                .radius(100) // 50 => radius is 50m
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(2.0f));

        //Create Geo Fencing
        geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.working_driver_tbl));//working
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(Double.parseDouble(riderLat),Double.parseDouble(riderLng)),0.2f);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
               // sendArrivedNotification(customerId);
                btnStartTrip.setEnabled(true);

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void sendOTW(String customerId) {
        Token token = new Token(customerId);
    /*    Notification notification = new Notification("Arrived","Your Biker is Nearby");
                //String.format("Driver %s has arrived at your location",Common.currentUser.getName()));
        Sender sender = new Sender(token.getToken(),notification);*/

        Map<String,String> content = new HashMap<>();
        content.put("title","OTW");
        content.put("message",customerId);
        DataMessage dataMessage = new DataMessage(token.getToken(),content);

        mFCMService.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success!=1)
                {
                    Toast.makeText(DriverTracking.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

    private void sendArrivedNotification(String customerId) {
        Token token = new Token(customerId);
    /*    Notification notification = new Notification("Arrived","Your Biker is Nearby");
                String.format("Driver %s has arrived at your location",Common.currentUser.getName()));
        Sender sender = new Sender(token.getToken(),notification);*/

        Map<String,String> content = new HashMap<>();
        content.put("title","Arrived");
        content.put("message",String.format("Biker %s has arrived at your location",Common.currentUser.getName()));
        DataMessage dataMessage = new DataMessage(token.getToken(),content);

        mFCMService.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success!=1)
                {
                    Toast.makeText(DriverTracking.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

    private void sendInTransitNotification(String customerId) {
        Token token = new Token(customerId);
       /* Notification notification = new Notification("DropOff",customerId);
        Sender sender = new Sender(token.getToken(),notification);*/

        Map<String,String> content = new HashMap<>();
        content.put("title","PickUp");
        content.put("message",customerId);
        DataMessage dataMessage = new DataMessage(token.getToken(),content);

        mFCMService.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success!=1)
                {
                    Toast.makeText(DriverTracking.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

    private void sendDropOffNotification(String customerId) {
        Token token = new Token(customerId);
       /* Notification notification = new Notification("DropOff",customerId);
        Sender sender = new Sender(token.getToken(),notification);*/

        Map<String,String> content = new HashMap<>();
        content.put("title","DropOff");
        content.put("message",customerId);
        DataMessage dataMessage = new DataMessage(token.getToken(),content);

        mFCMService.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success!=1)
                {
                    Toast.makeText(DriverTracking.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }
   /*  if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
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

                                //Uppdate to FireBase
                                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(lattitude, longitude), new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {
                                        //add Marker
                                        if (mCurrent != null)
                                            mCurrent.remove();
                                        mCurrent = mMap.addMarker(new MarkerOptions()
                                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.pinkmarker))
                                                .position(new LatLng(lattitude, longitude))
                                                .title("my location"));
                                        //move camera to this position
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lattitude, longitude), 15.0f));
                                        //animate marker
                                        // rotateMarker(mCurrent, -360,mMap);
                                    }
                                });
                            }
                        } else {
                            Log.d("Error", "Cannot get Your Location");
                        }
                    }
                });
   *
   * */

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //fix this to call once
        //remove old direction
        /*if (direction !=null)
            direction.remove();*/
        Common.mLastLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
        if (Common.mLastLocation!=null)
        {
                final double latitude =Common.mLastLocation.getLatitude();
                final double longitude =Common.mLastLocation.getLongitude();

                if (driverMarker!=null)
                    driverMarker.remove();
                    driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude))
                                .title("You")
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.pinkmarker)));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),17.0f));
            getDirection();



        }
        else
        {
            Log.d("Error", "Cannot get Your Location");
        }
    }

    private void getDirection() {
       LatLng currentPosition = new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude());

        String requestApi = null;
        try{
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&transit_routing_preference=less_driving&"+
                    "origin="+currentPosition.latitude+","+currentPosition.longitude+"&"+
                    "destination="+riderLat+","+riderLng+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.d("TeleportDevgetdir", requestApi);//print url for debug
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                new ParserTask().execute(response.body().toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        //  place @ charOverride
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(DriverTracking.this,""+t.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });

        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
       /* getDirection();*/
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Common.mLastLocation = location;
        displayLocation();

        //////this can be movedsomewhere where will always execute
        DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference(Common.drivers_available_tbl_);//available
        DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference(Common.working_driver_tbl);

        final GeoFire geoFireAvailable = new GeoFire(refAvailable);
        final GeoFire geoFireWorking = new GeoFire(refWorking);


        DatabaseReference csuid = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl_).child(myId).child("csUid");
        csuid.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    geoFireWorking.setLocation(Common.myId, new GeoLocation(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()));
                    geoFireAvailable.removeLocation(Common.myId);
                }else {
                    geoFireAvailable.setLocation(Common.myId, new GeoLocation(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()));
                    geoFireWorking.removeLocation(Common.myId);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

/*
        switch (Common.csUid){
            case "":
                geoFireWorking.removeLocation(Common.myId);
                geoFireAvailable.setLocation(Common.myId, new GeoLocation(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()));
                break;

            default:
                geoFireAvailable.removeLocation(Common.myId);
                geoFireWorking.setLocation(Common.myId, new GeoLocation(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()));
                break;
        }
        ////////////////////////////////////////////*/
    }

    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>>
    {
        ProgressDialog mDialog = new ProgressDialog(DriverTracking.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Please Wait . . . ");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try
            {
                jObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();
                routes = parser.parse(jObject);//////////////////////////
            } catch (JSONException e)
            {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();

            ArrayList points;
            PolylineOptions polylineOptions = null;

            for (int i=0;i<lists.size();i++)
            {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                List<HashMap<String,String>> path = lists.get(i);

                for (int j=0;j<path.size();j++)
                {
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat,lng);

                    points.add(position);
                }
                polylineOptions.addAll(points);
                polylineOptions.width(10);
                polylineOptions.color(Color.RED);
                polylineOptions.geodesic(true);
            }
                direction = mMap.addPolyline(polylineOptions);

        }
    }

    private void sendSmsToCS() {

        try {
            Uri uri = Uri.parse("smsto:"+csContact);
            // No permisison needed
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO, uri);
            // Set the message to be sent
            smsIntent.putExtra("sms_body", "Good day");
            startActivity(smsIntent);

        } catch (Exception e) {
            Toast.makeText(this, "SMS failed, please try again later!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {

    }




}
