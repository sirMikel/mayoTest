package projectv2.devteam.community.teleportv2.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by Santos on 4/30/2018.
 */

public interface IGoogleAPI {
    @GET
    Call<String> getPath(@Url String url);
}
