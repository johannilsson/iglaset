package com.markupartist.iglaset;

import com.google.android.imageloader.ImageLoader;
import com.markupartist.iglaset.util.ErrorReporter;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class IglasetApplication extends Application {
	
	private final static String PREF_ORPHAN_CODE = "orphan_barcode";
	private ImageLoader imageLoader;
	
    @Override
    public void onCreate() {    	
        super.onCreate();
 
        final ErrorReporter reporter = ErrorReporter.getInstance();
        reporter.init(getApplicationContext());
        imageLoader = new ImageLoader();
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
	
	@Override
	public Object getSystemService(String name) {
		if(name.equals(ImageLoader.IMAGE_LOADER_SERVICE)) {
			return imageLoader;
		}
		
		return super.getSystemService(name);
	}
}
