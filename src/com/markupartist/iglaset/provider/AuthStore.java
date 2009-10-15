package com.markupartist.iglaset.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

import com.markupartist.iglaset.util.HttpManager;

public class AuthStore {
    private static final String TAG = "AuthStore";
    private static final String AUTH_BASE_URI = "http://api.iglaset.se/api/authenticate/";
    private static AuthStore sInstance;
    //private ExpiringToken mExpiringToken;

    private AuthStore() {
        
    }

    public static AuthStore getInstance() {
        if (sInstance == null) {
            sInstance = new AuthStore();
        }
        return sInstance;
    }

    public ExpiringToken authenticateUser(Context context) throws AuthenticationException {
        SharedPreferences sharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(context);

        ExpiringToken token = null;
        if (sharedPreferences.contains("preference_username") 
                && sharedPreferences.contains("preference_password")) {
            String username = sharedPreferences.getString("preference_username", "");
            String password = sharedPreferences.getString("preference_password", "");

            try {
                token = authenticateUser(username, password);
            } catch (AuthenticationException e) {
                removeToken(context);
                throw e;
            }

            storeToken(token, context);
        }

        return token;
    }

    private ExpiringToken authenticateUser(String username, String password) 
            throws AuthenticationException {
        Log.d(TAG, "authenticate user...");
        final HttpPost post = new HttpPost(AUTH_BASE_URI);
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("username", username));
        nameValuePairs.add(new BasicNameValuePair("password", password)); 
        HttpEntity entity = null;

        ExpiringToken expiringToken = null;

        try {
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            final HttpResponse response = HttpManager.execute(post);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                entity = response.getEntity();
                String token = parseResponse(entity.getContent());
                if (token.equals("")) {
                    Log.d(TAG, "Failed to authenticate user " + username);
                    throw new AuthenticationException("Failed to authenticate user " 
                            + username);
                }

                expiringToken = createToken(token);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }            

        return expiringToken;
    }

    public String getStoredToken(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(context);

        return sharedPreferences.getString("preference_token", null);
    }

    private boolean removeToken(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
        .getDefaultSharedPreferences(context);

        Editor editor = sharedPreferences.edit();
        editor.putString("preference_token", null);
    
        return editor.commit();
    }

    public boolean storeToken(ExpiringToken token, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(context);

        Editor editor = sharedPreferences.edit();
        editor.putString("preference_token", token.token);

        return editor.commit();
    }

    public boolean isTokenValid(ExpiringToken token) {
        Time currentTime = new Time();
        currentTime.setToNow();
        return currentTime.before(token.expiring);
    }

    private ExpiringToken createToken(String token) {
        ExpiringToken expiringToken = new ExpiringToken();

        expiringToken.expiring = new Time();
        expiringToken.expiring.set(System.currentTimeMillis() + 3600000);
        expiringToken.token = token;

        return expiringToken;
    }

    private String parseResponse(InputStream inputStream) {
        String token = "";
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(inputStream, "UTF-8");

            int eventType = xpp.getEventType();
            boolean inToken = false;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("token")) {
                        inToken = true;
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (xpp.getName().equals("token")) {
                        inToken = false;
                    }
                } else if (eventType == XmlPullParser.TEXT) {
                    if (inToken) {
                        token = xpp.getText();
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return token;
    }

    public static class ExpiringToken {
        public String token;
        public Time expiring;        
    }
}
