package com.markupartist.iglaset;

import com.markupartist.iglaset.util.ErrorReporter;
import com.markupartist.iglaset.util.ImageLoader;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class IglasetApplication extends Application {
	
	private final static String PREF_ORPHAN_CODE = "orphan_barcode";
	
    @Override
    public void onCreate() {    	
        super.onCreate();
        
        // Remove any orphan barcodes that were scanned in previous runs.
    	clearOrphanBarcode();

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
    
	public void storeOrphanBarcode(String barcode) {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	SharedPreferences.Editor editor = preferences.edit();
    	editor.putString(PREF_ORPHAN_CODE, barcode);
    	editor.commit();
	}
	
	public String getOrphanBarcode() {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	return preferences.getString(PREF_ORPHAN_CODE, null);
	}
	
	public void clearOrphanBarcode() {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	SharedPreferences.Editor editor = preferences.edit();
    	editor.remove(PREF_ORPHAN_CODE);
    	editor.commit();
	}
}
