package com.checkhall;

import android.util.Log;

import com.checkhall.util.DeviceUtil;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by machorom on 2017-09-05.
 */
public class FirebaseIDService extends FirebaseInstanceIdService {
    private static final String TAG = "LCheckhall:FirebaseIDService";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        DeviceUtil.setPushTokenId(this, refreshedToken);

        // TODO: Implement this method to send any registration to your app's servers.
        sendRegistrationToServer(refreshedToken);
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
        Log.d(TAG, "sendRegistrationToServer token: " + token);
        DeviceUtil.setPushTokenId(this, token);
    }
}
