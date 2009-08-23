package com.markupartist.iglaset.provider;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.util.Log;

public class CommentsStore {
    private static String TAG = "CommentsStore";

    public ArrayList<Comment> getComments(int drinkId) {
        final ArrayList<Comment> comments = new ArrayList<Comment>();
        try {
            URL endpoint = new URL("http://api.iglaset.se/api/comments/xml/" + drinkId);
            CommentsParser commentsParser = new CommentsParser();
            commentsParser.parseComments(endpoint.openStream(), comments);
        } catch (MalformedURLException e) {
            Log.d(TAG, "Malformed URL: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Failed to read data: " + e.getMessage());
        }

        return comments;
    }
}
