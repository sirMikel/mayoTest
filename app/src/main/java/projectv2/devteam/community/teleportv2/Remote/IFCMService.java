package projectv2.devteam.community.teleportv2.Remote;

import projectv2.devteam.community.teleportv2.Model.DataMessage;
import projectv2.devteam.community.teleportv2.Model.FCMResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Santos on 4/30/2018.
 */

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAP7guPPM:APA91bEslkUxMIzFL9PqQ3X2oTExXAtb_r4kui1Rp2qvTG6-BzPmM6ha05pn9B1_goMdA9LlbNHwoNaUJRt1p0Ux6blnYb25eg9jOGhdRJqVUmptN-ei1FsaHWUptel1vFSWJDe_ffsl"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body DataMessage body);

}
