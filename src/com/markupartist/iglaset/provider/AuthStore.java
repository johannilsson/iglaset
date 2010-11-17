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
import android.text.TextUtils;
import android.util.Log;

import com.markupartist.iglaset.util.HttpManager;

public class AuthStore {
    private static final String TAG = AuthStore.class.getSimpleName();
    private static final String AUTH_BASE_URI_V2 = "http://www.iglaset.se/user_session.xml";
    private static AuthStore sInstance;

    private AuthStore() {
    }

    public static AuthStore getInstance() {
        if (sInstance == null) {
            sInstance = new AuthStore();
        }
        return sInstance;
    }

    public void authenticateUser(Context context)
            throws AuthenticationException, IOException {
        SharedPreferences sharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(context);

        Authentication authResponse = null;
        if (sharedPreferences.contains("preference_username") 
                && sharedPreferences.contains("preference_password")) {
            String username = sharedPreferences.getString("preference_username", "");
            String password = sharedPreferences.getString("preference_password", "");

            try {
                authResponse = authenticateUser(username, password);
            } catch (AuthenticationException e) {
                removeAuthentication(context);
                throw e;
            }

            storeAuthentication(authResponse, context);
        }
    }

    private Authentication authenticateUser(String username, String password) 
            throws AuthenticationException, IOException {
        Log.d(TAG, "authenticate user...");

        Authentication.AuthenticationData v2 = authenticateUserv2(username, password);
        Authentication authResponse = new Authentication(v2);

        Log.d(TAG, "Got response " + authResponse.v2.userId);
        return authResponse;
    }
    
    private Authentication.AuthenticationData authenticateUserv2(String username, String password)
    	throws AuthenticationException, IOException {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("user_session[username]", username));
        nameValuePairs.add(new BasicNameValuePair("user_session[password]", password));

        return doAuthentication(AUTH_BASE_URI_V2, nameValuePairs);
    }
    
    private Authentication.AuthenticationData doAuthentication(final String url, final ArrayList<NameValuePair> data)
    	throws AuthenticationException, IOException {
        final HttpPost post = new HttpPost(url);
        post.setEntity(new UrlEncodedFormEntity(data));
        HttpEntity entity = null;

        final HttpResponse response = HttpManager.execute(post);
        Authentication.AuthenticationData authResponse;
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            entity = response.getEntity();
            authResponse = parseResponse(entity.getContent());
            if (!authResponse.looksValid()) {
                Log.i(TAG, "Failed to authenticate user");
                throw new AuthenticationException("Failed to authenticate user");
            }
        } else {
            Log.w(TAG, "Request failed, http status code was not OK.");
            throw new IOException();
        }
        
        return authResponse;
    }

    public boolean hasAuthentication(Context context) {
        try {
            Authentication authentication = getAuthentication(context);
            if (authentication.looksValid()) {
                return true;
            }
        } catch (AuthenticationException e) {
            ; // No auth or failed to request one.
        }
        return false;
    }
    
    public Authentication getAuthentication(Context context)
            throws AuthenticationException {
        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context);

        if (!sharedPreferences.contains("preference_token_v2")) {
            throw new AuthenticationException("User not authenticated");
        }

        Authentication.AuthenticationData v2 = new Authentication.AuthenticationData(
        		sharedPreferences.getString("preference_token_v2", null),
                sharedPreferences.getInt("preference_user_id_v2", 0));
        Authentication response = new Authentication(v2);

        return response;
    }
    
    private boolean removeAuthentication(Context context) {
        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context);

        Editor editor = sharedPreferences.edit();
        editor.remove("preference_token_v2");
        editor.remove("preference_user_id_v2");
        
        return editor.commit();
    }

    public boolean storeAuthentication(Authentication token,
            Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(context);

        Editor editor = sharedPreferences.edit();
        editor.putString("preference_token_v2", token.v2.token);
        editor.putInt("preference_user_id_v2", token.v2.userId);

        return editor.commit();
    }

    private Authentication.AuthenticationData parseResponse(InputStream inputStream)
            throws IOException {
        Authentication.AuthenticationData response = new Authentication.AuthenticationData();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(inputStream, "UTF-8");

            int eventType = xpp.getEventType();
            boolean inToken = false;
            boolean inUserId = false;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if ("token".equals(xpp.getName())) {
                        inToken = true;
                    } else if ("user_id".equals(xpp.getName())) {
                        inUserId = true;
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if ("token".equals(xpp.getName())) {
                        inToken = false;
                    } else if ("user_id".equals(xpp.getName())) {
                        inUserId = false;
                    }
                } else if (eventType == XmlPullParser.TEXT) {
                    if (inToken) {
                        response.token = xpp.getText();
                    }
                    if (inUserId) {
                        response.userId = Integer.parseInt(xpp.getText());
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            Log.w(TAG, "Failed to parse response " + e.getMessage());
            throw new IOException(e.getMessage());
        }

        return response;
    }
    
    // TODO: Handling of API versions should be dealt with in a cleaner way.
    public static class Authentication {
        public static class AuthenticationData {
        	public String token;
        	public int userId;
        	
        	public AuthenticationData() {
        	}
        	
        	public AuthenticationData(String token, int userId) {
        		this.token = token;
        		this.userId = userId;
        	}
        	
        	public boolean looksValid() {
        		return !TextUtils.isEmpty(token) && userId > 0;
        	}
        }
        
        public AuthenticationData v2;
        
        public Authentication(AuthenticationData v2) {
        	this.v2 = v2;
        }

        public boolean looksValid() {
            return v2.looksValid();
        }
    }
}
