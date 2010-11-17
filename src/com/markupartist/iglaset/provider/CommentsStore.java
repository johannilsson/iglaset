package com.markupartist.iglaset.provider;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import android.util.Log;

import com.markupartist.iglaset.util.HttpManager;

public class CommentsStore {
    private static String TAG = CommentsStore.class.getSimpleName();
    private static String COMMENTS_BASE_URI = "http://www.iglaset.se/articles/%d/comments.xml";

    public ArrayList<Comment> getComments(Drink drink) {
        final ArrayList<Comment> comments = new ArrayList<Comment>();
        final HttpGet get = new HttpGet(String.format(COMMENTS_BASE_URI, drink.getId()));
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
