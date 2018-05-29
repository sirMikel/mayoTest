package projectv2.devteam.community.teleportv2;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import projectv2.devteam.community.teleportv2.Common.Common;
import projectv2.devteam.community.teleportv2.Model.DataMessage;
import projectv2.devteam.community.teleportv2.Model.FCMResponse;
import projectv2.devteam.community.teleportv2.Model.Token;
import projectv2.devteam.community.teleportv2.Remote.IFCMService;
import projectv2.devteam.community.teleportv2.Remote.IGoogleAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PickUpHere extends AppCompatActivity {


    TextView txtTime,txtDistance,txtPickUpAdd;
    Button btnAccept, btnCancel;
    MediaPlayer mediaPlayer;

    IGoogleAPI mService;
    IFCMService mFCMService;

    String customerId,csUid;
    String lat, lng,driverId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_up_here);

        mService= Common.getGoogleAPI();
        mFCMService = Common.getFCMService();

        //Init View
        txtPickUpAdd = (TextView)findViewById(R.id.txtPickUpAdd);
        txtTime = (TextView)findViewById(R.id.txtTime);
        txtDistance = (TextView)findViewById(R.id.txtDistanceToPickUp);

        btnAccept = (Button)findViewById(R.id.btnAccept);
        btnCancel = (Button)findViewById(R.id.btnDecline);

        if (getIntent() !=null)
        {
            lat = getIntent().getStringExtra("lat");
            lng = getIntent().getStringExtra("lng");
            csUid = getIntent().getStringExtra("csUid");
            driverId = getIntent().getStringExtra("driverId");
            customerId = getIntent().getStringExtra("customer");


            //copy directions from welcome
            //getPickDropCash(lat,lng,destlat,destlng);//location to pickup
            getDirectionDetails(lat,lng);


        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(customerId))
                    cancelBooking(customerId);
            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                ///////////////////////////////////////////////////////////////////////////////////
                DatabaseReference teleDatabase = FirebaseDatabase.getInstance().getReference();
                //////////////////////////////////////////////////////////////////////////////////
                if (Common.myId==null){
                    Common.myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                }
                if (Common.myId!=null){
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl_).child(Common.myId);
                    HashMap map = new HashMap();
                    map.put("csUid",csUid);
                    ref.updateChildren(map);
                }


                Common.finalFare= "Custom Booking";
                Common.totalTime="";
                Common.totalDistance="";
                Common.pickUpLocation="Please use WAZE or Google Map for Directions";
                Common.dropOffLocation="";
                //addCsUid();

                //sendmessage to CS titled accept
                sendOTWNotification();

                Intent intent = new Intent(PickUpHere.this,DriverTracking.class);
                //sends CS location to new activity
                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
                intent.putExtra("csUid",csUid);
                intent.putExtra("driverId",driverId);
                intent.putExtra("customerId",customerId);

                startActivity(intent);
                finish();
            }
        });

        mediaPlayer = MediaPlayer.create(this, R.raw.levelup);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        /*if (getIntent() !=null)
        {
            lat = getIntent().getStringExtra("lat");
            lng = getIntent().getStringExtra("lng");
            csUid = getIntent().getStringExtra("csUid");
            driverId = getIntent().getStringExtra("driverId");
            customerId = getIntent().getStringExtra("customer");


            //copy directions from welcome
            //getPickDropCash(lat,lng,destlat,destlng);//location to pickup
            getDirectionDetails(lat,lng);

        }*/
    }

   /* private void addCsUid() {
        DatabaseReference assginedCustomer = FirebaseDatabase.getInstance().getReference().child(Common.drivers_available_tbl_).child(Common.myId);
        assginedCustomer.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Map<String,Object>map = (Map<String,Object>)dataSnapshot.getValue();
                    if(map.get("csUid")!=null)
                    {
                        getCustomerPickUpLocation();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getCustomerPickUpLocation(){
        DatabaseReference customerPickupLocation = FirebaseDatabase.getInstance().getReference().child(Common.pickup_request_tbl_).child(csUid).child("l");
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
                    if (mCsMarker!=null){
                        mCsMarker.remove();
                    }
                    mCsMarker=mMap.addMarker(new MarkerOptions().position(csPickUp).title("CS PickUp Here"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }*/

    private void cancelBooking(String customerId) {
        Token token = new Token(customerId);

        /*Notification notification = new Notification("Cancel","Driver cancelled your request");
        Sender sender = new Sender(token.getToken(),notification);*/
        Map<String,String> content = new HashMap<>();
        content.put("title","Cancel");
        content.put("message","Driver cancelled your request");
        DataMessage dataMessage = new DataMessage(token.getToken(),content);

        mFCMService.sendMessage(dataMessage)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if (response.body().success == 1)
                        {
                            Toast.makeText(PickUpHere.this, "Cancelled", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });
    }

    private void sendOTWNotification() {
        Token token = new Token(customerId);
       /* Notification notification = new Notification("DropOff",customerId);
        Sender sender = new Sender(token.getToken(),notification);*/

        Map<String,String> content = new HashMap<>();
        content.put("title","OTW");
        content.put("message",customerId);
        content.put("driverId",driverId);
        DataMessage dataMessage = new DataMessage(token.getToken(),content);

        mFCMService.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success!=1)
                {
                    Toast.makeText(PickUpHere.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

    private void getDirectionDetails(String lat, String lng) {


        String requestApi = null;
        try{
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+Common.mLastLocation.getLatitude()+","+Common.mLastLocation.getLongitude()+"&"+
                    "destination="+lat+","+lng+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.d("TeleportDev", requestApi);//print url for debug
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray routes = jsonObject.getJSONArray("routes");

                                //after get routes,just get first element of routes
                                JSONObject object =routes.getJSONObject(0);
                                //after get first element, get array with name legs
                                JSONArray legs = object.getJSONArray("legs");
                                //get first element of leg array
                                JSONObject legsObject = legs.getJSONObject(0);
                                //get distance
                                JSONObject distance = legsObject.getJSONObject("distance");
                                txtDistance.setText(distance.getString("text"));
                                //get time
                                JSONObject time = legsObject.getJSONObject("duration");
                                txtTime.setText(time.getString("text"));
                                //get address
                                String addressTop = legsObject.getString("end_address");
                                txtPickUpAdd.setText(addressTop);

                                String addressBlw = legsObject.getString("start_address");
                                //txtAddressTop.setText(addressBlw);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        //  place @ charOverride
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(PickUpHere.this,""+t.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });

        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
