package projectv2.devteam.community.teleportv2.Service;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import projectv2.devteam.community.teleportv2.Common.Common;
import projectv2.devteam.community.teleportv2.CustomerCall;
import projectv2.devteam.community.teleportv2.DriverHome;
import projectv2.devteam.community.teleportv2.PickUpHere;
import projectv2.devteam.community.teleportv2.TripDetail;

import static projectv2.devteam.community.teleportv2.Common.Common.csUid;

/**
 * Created by Santos on 4/30/2018.
 */

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


        ////////////////////
        if (remoteMessage.getData() != null) {
            Map<String, String> data = remoteMessage.getData();
            String title = data.get("title");
            //final String message = data.get("message");

            if (title.equals("PickUpHere")) {
                /*Log.d("Teleport", remoteMessage.getNotification().getBody());*/
                //Map<String, String> data = remoteMessage.getData(); // get data from notification
                String customer = data.get("customer");
                String driverId = data.get("driverId");
                String csUid = data.get("csUid");
                String lat = data.get("lat");
                String lng = data.get("lng");

                //Because we will send  the firebase message containing latlng from rider app
                //converts message to latlng
                //LatLng customer_location = new Gson().fromJson(message, LatLng.class);

                Intent intent = new Intent(getBaseContext(), PickUpHere.class);
                intent.putExtra("driverId",driverId);
                intent.putExtra("csUid",csUid);
                intent.putExtra("lat", lat);
                intent.putExtra("lng", lng);
                intent.putExtra("customer", customer);
                //////////////////////////////

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            } else if (title.equals("CustomerCall")) {
                String customer = data.get("customer");
                String driverId = data.get("driverId");
                String csUid = data.get("csUid");
                String lat = data.get("lat");
                String lng = data.get("lng");
                String destlat = data.get("destlat");
                String destlng = data.get("destlng");
                String fare = data.get("fare");


                //Because we will send  the firebase message containing latlng from rider app
                //converts message to latlng
                //LatLng customer_location = new Gson().fromJson(message, LatLng.class);

                Intent intent = new Intent(getBaseContext(), CustomerCall.class);
                intent.putExtra("csUid",csUid);
                intent.putExtra("fare", fare);
                intent.putExtra("lat", lat);
                intent.putExtra("lng", lng);
                intent.putExtra("driverId",driverId);
                intent.putExtra("destlat", destlat);
                intent.putExtra("destlng", destlng);
                intent.putExtra("customer", customer);
                //////////////////////////////

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }else if (title.equals("DropOff")){
                openTripDetail();
            }else if (title.equals("Abort Ride")){
                final String message = data.get("message");

                DatabaseReference deletecsuid = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl_).child(Common.myId).child("csUid");
                deletecsuid.removeValue();
                csUid="";

                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyFirebaseMessaging.this, message, Toast.LENGTH_LONG).show();
                    }
                },5000);

                Intent intent = new Intent(getBaseContext(), DriverHome.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }


        }
    }       ////////////////////

        /*if (remoteMessage != null) {
            *//*Log.d("Teleport", remoteMessage.getNotification().getBody());*//*
            Map<String, String> data = remoteMessage.getData(); // get data from notification
            String customer = data.get("customer");
            String lat = data.get("lat");
            String lng = data.get("lng");
            String destlat = data.get("");
            String destlng = data.get("");


            //Because we will send  the firebase message containing latlng from rider app
            //converts message to latlng
            //LatLng customer_location = new Gson().fromJson(message, LatLng.class);

            Intent intent = new Intent(getBaseContext(), CustomerCall.class);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);
            intent.putExtra("destlat", lat);
            intent.putExtra("destlng", lng);
            intent.putExtra("customer", customer);
            //////////////////////////////

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }*/


    private void openTripDetail() {
        Intent intent = new Intent(this, TripDetail.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
