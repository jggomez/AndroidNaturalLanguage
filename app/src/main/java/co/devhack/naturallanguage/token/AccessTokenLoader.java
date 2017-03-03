package co.devhack.naturallanguage.token;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.language.v1beta1.CloudNaturalLanguageAPIScopes;

import java.io.IOException;
import java.io.InputStream;

import co.devhack.naturallanguage.R;

/**
 * Created by jggomez on 02-Mar-17.
 */

public class AccessTokenLoader extends AsyncTaskLoader<String> {

    private String TAG = "AccessTokenLoader";
    private static final String PREFS = "access_token_pref";
    private static final String PREF_ACCESS_TOKEN = "access_token";

    public AccessTokenLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public String loadInBackground() {
        final SharedPreferences prefs =
                getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        String token = prefs.getString(PREF_ACCESS_TOKEN, null);

        if (token != null) {
            final GoogleCredential credential = new GoogleCredential()
                    .setAccessToken(token)
                    .createScoped(CloudNaturalLanguageAPIScopes.all());

            final Long seconds = credential.getExpiresInSeconds();

            if (seconds != null && seconds > 3600) {
                return token;
            }
        }

        try {

            InputStream stream = getContext().getResources().openRawResource(R.raw.mlapicredential);

            GoogleCredential credential = GoogleCredential.fromStream(stream)
                    .createScoped(CloudNaturalLanguageAPIScopes.all());

            credential.refreshToken();
            String accessToken = credential.getAccessToken();
            prefs.edit().putString(PREF_ACCESS_TOKEN, accessToken).apply();
            return accessToken;

        } catch (IOException e) {
            Log.e(TAG, "Error obteniendo el access token " + e.getMessage());
        }

        return null;

    }
}
