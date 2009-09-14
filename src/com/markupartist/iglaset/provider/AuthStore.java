package com.markupartist.iglaset.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

public class AuthStore {
    private static final String TAG = "AuthStore";

    public String authenticateUser(String username, String password) {
        String token = null;

        try {
            URL endpoint = new URL("http://api.iglaset.se/api/authenticate/"
                    + username + "/" + password);
            token = parseResponse(endpoint.openStream());
            if (token.equals("")) {
                token = null; // Replace with exception...
            }
            Log.d(TAG, "token: " + token);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
