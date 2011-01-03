package com.markupartist.iglaset;

import com.google.android.imageloader.ImageLoader;
import com.markupartist.iglaset.util.ErrorReporter;
import com.markupartist.iglaset.util.StringUtils;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class IglasetApplication extends Application {
	
	private final static String PREF_ORPHAN_CODE = "orphan_barcode";
	private final static String PREF_SORT_MODE = "sort_mode";
	private ImageLoader imageLoader;
	
    @Override
    public void onCreate() {    	
        super.onCreate();
 
        final ErrorReporter reporter = ErrorReporter.getInstance();
        reporter.init(getApplicationContext());
        imageLoader = new ImageLoader();
    }

    private SharedPreferences getPreferences() {
    	return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }
    
    private SharedPreferences.Editor getPreferencesEditor() {
    	return getPreferences().edit();
    }
    
	public void storeOrphanBarcode(String barcode) {
    	getPreferencesEditor().putString(PREF_ORPHAN_CODE, barcode).commit();
	}
	
	public String getOrphanBarcode() {
		return getPreferences().getString(PREF_ORPHAN_CODE, null);
	}
	
	public void clearOrphanBarcode() {
		getPreferencesEditor().remove(PREF_ORPHAN_CODE).commit();
	}
	
	public void storeSearchSortMode(Class<? extends Object> namespace, int mode) {
		getPreferencesEditor().putInt(PREF_SORT_MODE + namespace.getSimpleName(), mode).commit();
	}
	
	public int getSearchSortMode(Class<? extends Object> namespace, int defaultValue) {
		return getPreferences().getInt(PREF_SORT_MODE + namespace.getSimpleName(), defaultValue);
	}
	
	@Override
	public Object getSystemService(String name) {
		if(name.equals(ImageLoader.IMAGE_LOADER_SERVICE)) {
			return imageLoader;
		}
		
		return super.getSystemService(name);
	}
}
