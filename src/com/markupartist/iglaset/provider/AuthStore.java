package com.markupartist.iglaset.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.markupartist.iglaset.util.HttpManager;

import android.util.Log;

public class AuthStore {
    private static final String TAG = "AuthStore";
    private static final String AUTH_BASE_URI = "http://api.iglaset.se/api/authenticate/";

    public String authenticateUser(String username, String password) {
        String token = null;
        final HttpPost post = new HttpPost(AUTH_BASE_URI + username + "/" + password);
        HttpEntity entity = null;

        try {
            final HttpResponse response = HttpManager.execute(post);
            //if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            Log.d(TAG, "status: " + response.getStatusLine().getStatusCode());
                entity = response.getEntity();
                Log.d(TAG, "about to parse response");
                token = parseResponse(entity.getContent());
                if (token.equals("")) {
                    token = null; // Replace with exception...
                }
            //}
            Log.d(TAG, "parse done");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Log.d(TAG, "token: " + token);
        return token;
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
}
