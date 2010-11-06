package com.markupartist.iglaset;

import com.markupartist.iglaset.util.ErrorReporter;
import com.markupartist.iglaset.util.ImageLoader;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class IglasetApplication extends Application {
    @Override
    public void onCreate() {
        // Remove any orphan barcodes that were scanned in previous runs.
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	SharedPreferences.Editor editor = preferences.edit();
    	editor.remove("orphan_barcode");
    	editor.commit();
    	
        super.onCreate();

        final ErrorReporter reporter = ErrorReporter.getInstance();
        reporter.init(getApplicationContext());
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        if (true == ImageLoader.hasInstance()) {
            ImageLoader.getInstance().clearCache();
        }
    }
}
