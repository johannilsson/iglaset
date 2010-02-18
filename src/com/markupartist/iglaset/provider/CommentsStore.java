package com.markupartist.iglaset.provider;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import android.util.Log;

import com.markupartist.iglaset.util.HttpManager;

public class CommentsStore {
    private static String TAG = "CommentsStore";
    private static String COMMENTS_BASE_URI = "http://api.iglaset.se/api/comments/xml/";

    public ArrayList<Comment> getComments(int drinkId) {
        final ArrayList<Comment> comments = new ArrayList<Comment>();
        final HttpGet get = new HttpGet(COMMENTS_BASE_URI + drinkId);
        HttpEntity entity = null;
        try {
            final HttpResponse response = HttpManager.execute(get);
            entity = response.getEntity();
            CommentsParser commentsParser = new CommentsParser();
            commentsParser.parseComments(entity.getContent(), comments);
        } catch (IOException e) {
            Log.d(TAG, "Failed to read data: " + e.getMessage());
        }
        return comments;
    }
}
