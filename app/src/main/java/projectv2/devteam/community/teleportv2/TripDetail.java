package projectv2.devteam.community.teleportv2;

import android.content.Intent;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import projectv2.devteam.community.teleportv2.Common.Common;

import static java.lang.Double.parseDouble;
import static projectv2.devteam.community.teleportv2.Common.Common.csUid;
import static projectv2.devteam.community.teleportv2.Common.Common.mLastLocation;

public class TripDetail extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private TextView txtDate, txtFee, txtBaseFare, txtTime, txtDistance, txtEstimatedPayout, txtFrom, txtTo;
    Button btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //Init View
        txtDate = (TextView)findViewById(R.id.txtDate);
        txtFee = (TextView)findViewById(R.id.txtFee);
        txtBaseFare = (TextView)findViewById(R.id.txtBaseFare);
        txtTime = (TextView)findViewById(R.id.txtTime);
        txtDistance = (TextView)findViewById(R.id.txtDistance);
        txtEstimatedPayout = (TextView)findViewById(R.id.txtEstimatedPayout);
        txtFrom = (TextView)findViewById(R.id.txtFrom);
        txtTo = (TextView)findViewById(R.id.txtTo);
        btnDone = (Button)findViewById(R.id.btndone);


       /* //converting/getting Location from a LatLng values
        Location location = new Location("drop off point");
        location.setLatitude(Common.mLastLocation.getLatitude());
        location.setLongitude(Common.mLastLocation.getLongitude());
        txtTo.setText((CharSequence) location);
*/
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //setcsuid-null and others
                DatabaseReference deletecsuid = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl_).child(Common.myId).child("csUid");
                deletecsuid.removeValue();
                csUid="";
                ////////////////////
                Intent intent = new Intent(TripDetail.this,DriverHome.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();


            }
        });



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        settingInformation();

    }

    private void settingInformation() {
        if (getIntent()!=null)
        {
            //set text
            Calendar calendar = Calendar.getInstance();
            String date = String.format("%s",convertToDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)));
            txtDate.setText(date);

            txtFee.setText(Common.finalFare);

            txtBaseFare.setText(Common.baseFare);
            txtTime.setText(Common.totalTime);
            txtDistance.setText(Common.totalDistance);
            txtFrom.setText(Common.pickUpLocation);
            txtTo.setText(Common.dropOffLocation);

            /*txtFee.setText(String.format("Php. %.2f",getIntent().getDoubleExtra("total",0.0)));
            txtEstimatedPayout.setText(String.format("Php. %.2f",getIntent().getDoubleExtra("total",0.0)));
            txtBaseFare.setText(String.format("Php. %.2f",Common.base_fare));
            txtTime.setText(String.format("%s min",getIntent().getStringExtra("time")));
            txtDistance.setText(String.format("%s km",getIntent().getStringExtra("distance")));
            txtFrom.setText(getIntent().getStringExtra("start_address"));
            txtTo.setText(getIntent().getStringExtra("end_address"));*/


            //LatLng dropOff = new LatLng(Double.parseDouble(Common.destlat),Double.parseDouble(Common.destlng));
            //addmarker


            /*String[] location_end = getIntent().getStringExtra("location_end").split(",");
            LatLng dropOff = new LatLng(Double.parseDouble(location_end[0]),Double.parseDouble(location_end[1]));*/

            /*mMap.addMarker(new MarkerOptions().position(dropOff)
                .title("Drop off")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dropOff,14.0f));*/

            LatLng dropofflocation = new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude());

            mMap.addMarker(new MarkerOptions()
                    .position(dropofflocation)
                    .title("Drop off")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dropofflocation,17.0f));
        }
    }

    private String convertToDayOfWeek(int day) {
        switch (day)
        {
            case Calendar.SUNDAY:
                return "Sunday";
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";
            default:
                    return "UNKNOWN";
        }
    }

    @Override
    public void onBackPressed() {

    }
}
