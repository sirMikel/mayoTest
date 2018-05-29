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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import projectv2.devteam.community.teleportv2.Common.Common;
import projectv2.devteam.community.teleportv2.Model.DataMessage;
import projectv2.devteam.community.teleportv2.Model.FCMResponse;
import projectv2.devteam.community.teleportv2.Model.Token;
import projectv2.devteam.community.teleportv2.Model.User;
import projectv2.devteam.community.teleportv2.Remote.IFCMService;
import projectv2.devteam.community.teleportv2.Remote.IGoogleAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerCall extends AppCompatActivity {

    TextView txtTime,txtDistance,txtPickUpAdd,txtPrice,txtDropOffAdd,txtTravelDistance;
    Button btnAccept, btnCancel;
    MediaPlayer mediaPlayer;

    IGoogleAPI mService;
    IFCMService mFCMService;

    String customerId,driverId;
    String lat, lng, destlat, destlng,fare,csUid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_call);

        mService=Common.getGoogleAPI();
        mFCMService = Common.getFCMService();


        //Init View
        txtTime = (TextView)findViewById(R.id.txtTime);
        txtDistance = (TextView)findViewById(R.id.txtDistanceToPickUp);
        txtPickUpAdd = (TextView)findViewById(R.id.txtPickUpAdd);
        txtPrice = (TextView)findViewById(R.id.txtPrice);
        txtDropOffAdd = (TextView)findViewById(R.id.txtDropOffAdd);
        txtTravelDistance = (TextView)findViewById(R.id.txtTravelDistance);

        btnAccept = (Button)findViewById(R.id.btnAccept);
        btnCancel = (Button)findViewById(R.id.btnDecline);

        if (getIntent() !=null)
        {
            fare = getIntent().getStringExtra("fare");
            lat = getIntent().getStringExtra("lat");
            lng = getIntent().getStringExtra("lng");
            csUid = getIntent().getStringExtra("csUid");
            destlat = getIntent().getStringExtra("destlat");
            destlng = getIntent().getStringExtra("destlng");
            driverId = getIntent().getStringExtra("driverId");
            customerId = getIntent().getStringExtra("customer");

            //location to pickup
            getDirectionDetails();

            //copy directions from welcome
            //pickuplocation//dropoff//fare
            getPickDropCash();
        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(customerId))
                    cancelBooking(customerId);
                mediaPlayer.release();
            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sendmessage to CS titled accept


                ///////////FIREBASE///////////
                DatabaseReference teleDatabase = FirebaseDatabase.getInstance().getReference();
                if (Common.myId==null){
                    Common.myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                }
                if (Common.myId!=null){
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl_).child(Common.myId);//available
                    HashMap map = new HashMap();
                    map.put("csUid",csUid);
                    ref.updateChildren(map);
                }


                ///////////////////////////////
               /* DatabaseReference getinfo = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl_).child(Common.myId);
                getinfo.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                    //    Map<object> driverUser = (Map<>) dataSnapshot.getValue();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
*/

                sendOTWNotification();
                Intent intent = new Intent(CustomerCall.this,DriverTracking.class);
                intent.putExtra("csUid", csUid);
                intent.putExtra("customerId",customerId);
                intent.putExtra("lat", lat);
                intent.putExtra("lng", lng);
                intent.putExtra("driverId",driverId);
                intent.putExtra("destlat",destlat);
                intent.putExtra("destlng",destlng);

                mediaPlayer.release();
                //sends CS location to new activity
                startActivity(intent);
                finish();
            }
        });

        mediaPlayer = MediaPlayer.create(this, R.raw.levelup);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();


        Common.destlat=destlat;
        Common.destlng=destlng;
        Common.finalFare=fare;
        txtPrice.setText(fare);

    /*
        //location to pickup
        getDirectionDetails();

        //copy directions from welcome
        //pickuplocation//dropoff//fare
        getPickDropCash();*/

    }

    private void sendOTWNotification() {
        //dId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                    Toast.makeText(CustomerCall.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

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
                            Toast.makeText(CustomerCall.this, "Cancelled", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });
    }


    private void getPickDropCash() {

        String requestApi = null;
        try{
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+lat+","+lng+"&destination="+destlat+","+destlng+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.d("TeleportDevPDcash", requestApi);//print url for debug
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
                                String distance_text = distance.getString("text");

                                //only take numbers
                                Double distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+",""));
                                //txtDistance.setText(distance.getString("text"));
                                Common.totalDistance=distance_text;
                                txtTravelDistance.setText(distance_text);


                                //get time
                                JSONObject timeObject = legsObject.getJSONObject("duration");
                                String time_text = timeObject.getString("text");
                                //only take numbers
                                Double time_value = Double.parseDouble(time_text.replaceAll("[^0-9\\\\.]+",""));
                               // txtTime.setText(time.getString("text"));
                                Common.totalTime=time_text;


                                //getting price
                                //fare=Common.formulaPrice(distance_value,time_value);
                                //txtPrice.setText(String.format("Php.%.2f",fare));

                                //get address
                                String pickup = legsObject.getString("start_address");
                                txtPickUpAdd.setText(pickup);
                                Common.pickUpLocation=pickup;


                                String dropoff = legsObject.getString("end_address");
                                txtDropOffAdd.setText(dropoff);
                                Common.dropOffLocation=dropoff;




                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        //  place @ charOverride
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(CustomerCall.this,""+t.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });

        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void getDirectionDetails() {
        String requestApi = null;
        try{
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+Common.mLastLocation.getLatitude()+","+Common.mLastLocation.getLongitude()+"&"+
                    "destination="+lat+","+lng+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.d("TeleportDevDire", requestApi);//print url for debug
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
                                //txtAddress.setText(addressTop);

                                String addressBlw = legsObject.getString("start_address");
                                //txtAddressTop.setText(addressBlw);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        //  place @ charOverride
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(CustomerCall.this,""+t.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });

        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
       /* if (mediaPlayer.isPlaying())
            mediaPlayer.release();*/
        super.onStop();
    }

    @Override
    protected void onPause() {
       /* if (mediaPlayer.isPlaying())
            mediaPlayer.pause();*/
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
       /* if (mediaPlayer != null && !mediaPlayer.isPlaying())
            mediaPlayer.start();*/

    }

    @Override
    public void onBackPressed() {

    }
}
