package projectv2.devteam.community.teleportv2;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.firebase.geofire.GeoFire;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import projectv2.devteam.community.teleportv2.Common.Common;

/**
 * Created by Santos on 5/11/2018.
 */

public class OnAppKilled extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Common.drivers_available_tbl_);
        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference(Common.working_driver_tbl);


        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(Common.myId);

        GeoFire geoFire2 = new GeoFire(ref2);
        geoFire2.removeLocation(Common.myId);

    }
}
