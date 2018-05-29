package projectv2.devteam.community.teleportv2.Service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import projectv2.devteam.community.teleportv2.Common.Common;
import projectv2.devteam.community.teleportv2.Model.Token;

/**
 * Created by Santos on 4/30/2018.
 */

public class MyFirebaseIdService extends FirebaseInstanceIdService {

    //press ctrl o

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        updateTokenToServer(refreshedToken); // when we have refresh token, we need to update to our realtimedatabase

    }

    private void updateTokenToServer(String refreshedToken) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tbl);

        Token token = new Token(refreshedToken);
        if (FirebaseAuth.getInstance().getCurrentUser() !=null) //if already login, update token
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
            .setValue(token);
    }
}
