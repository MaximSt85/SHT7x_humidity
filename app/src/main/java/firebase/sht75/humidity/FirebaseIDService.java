package firebase.sht75.humidity;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Max on 16.08.2017.
 */

public class FirebaseIDService extends FirebaseInstanceIdService {

    private static final String TAG = "debugger";

    private SharedPreferences tokenPreferences;
    private SharedPreferences.Editor editor;

    public static String PREFERENCES_TOKEN = "preferencesToken";
    public static String TOKEN = "token";
    public static String TOKEN_UPDATED = "tokenUpdated";

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        updateToken(refreshedToken);

        /*DatabaseReference database;
        database = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUserAuth;
        currentUserAuth = FirebaseAuth.getInstance().getCurrentUser();
        notificationPreferences = getSharedPreferences(NotificationActivity.PREFERENCES_NOTIFICATIONS, Context.MODE_PRIVATE);
        if(currentUserAuth != null) {
            if (database != null) {
                if (notificationPreferences.getBoolean(NotificationActivity.ON_OFF_NOTIFICATIONS, false)) {
                    database = FirebaseDatabase.getInstance().getReference();
                    database.child("users").child(currentUserAuth.getUid()).child("token").setValue(refreshedToken);
                }
            }
        }*/
    }

    public void updateToken(String token) {
        tokenPreferences = getSharedPreferences(PREFERENCES_TOKEN, Context.MODE_PRIVATE);
        editor = tokenPreferences.edit();
        editor.putBoolean(TOKEN_UPDATED, false);
        editor.putString(TOKEN, token);
        editor.commit();
    }
}
