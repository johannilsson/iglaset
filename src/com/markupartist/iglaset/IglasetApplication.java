package com.markupartist.iglaset;

import com.markupartist.iglaset.util.ErrorReporter;

import android.app.Application;

public class IglasetApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        final ErrorReporter reporter = ErrorReporter.getInstance();
        reporter.init(getApplicationContext());
    }
}
