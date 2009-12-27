package com.markupartist.iglaset.util;

import android.app.Activity;
import android.content.Context;
import android.view.MenuItem;
import android.widget.Button;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * Simple wrapper around the GoogleAnalyticsTracker.
 * @author johan
 *
 */
public class Tracker {
    private static final String TRACKER_ID = "UA-6205540-8";
    private static final int DEFAULT_TRACKER_INTERVAL = 20;
    private static final String CATEGORY_CLICKS = "Clicks";
    private static final String ACTION_BUTTON = "Button";
    private static final String ACTION_MENU_ITEM = "MenuItem";
    private static Tracker sInstance;
    private GoogleAnalyticsTracker mTracker;

    private Tracker() {
        mTracker = GoogleAnalyticsTracker.getInstance();
    }

    public static Tracker getInstance() {
        if (sInstance == null) {
            sInstance = new Tracker();
        }
        return sInstance;
    }

    public Tracker start(Context context) {
        mTracker.start(TRACKER_ID, DEFAULT_TRACKER_INTERVAL, context);
        return this;
    }

    public Tracker stop() {
        mTracker.stop();
        return this;
    }

    public Tracker trackEvent(CharSequence category, CharSequence action, 
                              CharSequence label, int value) {
        if (mTracker != null)
            mTracker.trackEvent((String) category, (String) action, (String) label, value);
        return this;
    }

    public Tracker trackEvent(MenuItem item) {
        if (mTracker != null)
            trackEvent(CATEGORY_CLICKS, ACTION_MENU_ITEM, item.getTitle(), 0);
        return this;
    }

    public Tracker trackEvent(Button button) {
        if (mTracker != null)
            trackEvent(CATEGORY_CLICKS, ACTION_BUTTON, button.getText(), 0);
        return this;
    }

    public Tracker trackPageView(String page) {
        if (mTracker != null)
            mTracker.trackPageView(page);
        return this;
    }
}
