package projectv2.devteam.community.teleportv2.Common;

import android.location.Location;

import projectv2.devteam.community.teleportv2.Model.User;
import projectv2.devteam.community.teleportv2.Remote.FCMClient;
import projectv2.devteam.community.teleportv2.Remote.IFCMService;
import projectv2.devteam.community.teleportv2.Remote.IGoogleAPI;
import projectv2.devteam.community.teleportv2.Remote.RetrofitClient;

/**
 * Created by Santos on 4/28/2018.
 */

public class Common {

    public static String finalFare;
    public static String pickUpLocation;
    public static String dropOffLocation;
    public static String totalDistance;
    public static String totalTime;
    public static String baseFare = "Php. 29.00";
    public static String destlat;
    public static String destlng;




    public static final String drivers_available_tbl_= "DriversAvailable";
    public static final String user_driver_tbl_= "DriversInformation";
    public static final String working_driver_tbl= "DriversWorking";
    public static final String online_driver_tbl= "DriversOnline";
    public static final String user_rider_tbl_= "RidersInformation";
    public static final String pickup_request_tbl_= "PickupRequest";

    public static final String token_tbl= "Tokens";

    public static String driverId;
    public static String myId;
    public static String csUid;


    public static final int PICK_IMAGE_REQUEST = 9999;

    public static User currentUser;

    public static Location mLastLocation = null;

    public static final String baseURL = "https://maps.googleapis.com";
    public static final String fcmURL = "https://fcm.googleapis.com";

    public static final String user_field = "usr";
    public static final String pwd_field = "pwd";



    public static double base_fare = 29;
    private static double time_rate = 0;
    private static double distance_rate = 9;


    public static double formulaPrice(double km, Double min)
    {
        return (base_fare+(time_rate*min)+(distance_rate*km));
    }

    public static IGoogleAPI getGoogleAPI()
    {
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }
    public static IFCMService getFCMService()
    {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }


}
