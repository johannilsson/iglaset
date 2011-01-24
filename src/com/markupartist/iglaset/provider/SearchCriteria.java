package com.markupartist.iglaset.provider;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Vector;

import com.markupartist.iglaset.R;

import android.content.Context;
import android.text.TextUtils;

/**
 * Search criteria
 */
public abstract class SearchCriteria {
    private String mQuery;
    private int mCategory;
    private int mPage = 1;
    private String mBarcode;
    private ArrayList<Integer> mTags;
    private AuthStore.Authentication mAuthentication;
    
    public final static int SORT_MODE_NONE = 0;
    public final static int SORT_MODE_NAME = 1;
    public final static int SORT_MODE_PRODUCER = 2;
    public final static int SORT_MODE_RECOMMENDATIONS = 3;
    public final static int SORT_MODE_RATING = 4;
    public final static int SORT_MODE_DATE = 5;
    private int mSortMode = SORT_MODE_NONE;
    
    public SearchCriteria() {
    	mSortMode = getDefaultSortMode();
    }
    
	public abstract int[] getSortModes();
	public abstract int getDefaultSortMode();
    
    public CharSequence[] getSortModeNames(Context context) {
    	Vector<CharSequence> names = new Vector<CharSequence>();

    	int[] modes = getSortModes();    	
    	if(modes != null && modes.length > 0) {
    		for(int i=0; i<modes.length; ++i) {
    			switch(modes[i]) {
    			case SORT_MODE_NAME:
    				names.add(context.getText(R.string.sort_mode_name));
    				break;
    			case SORT_MODE_PRODUCER:
    				names.add(context.getText(R.string.sort_mode_producer));
    				break;
    			case SORT_MODE_RECOMMENDATIONS:
    				names.add(context.getText(R.string.sort_mode_recommendations));
    				break;
    			case SORT_MODE_RATING:
    				names.add(context.getText(R.string.sort_mode_rating));
    				break;
    			case SORT_MODE_DATE:
    				names.add(context.getText(R.string.sort_mode_date));
    				break;
    			case SORT_MODE_NONE:
    				names.add(context.getText(R.string.sort_mode_none));
    			default:
    				break;
    			}
    		}
    	}

    	CharSequence[] result = {};
    	return names.toArray(result);
    }
    
    public int getSortIndexFromMode(int mode) {
    	int[] modes = getSortModes();    	
    	for(int i=0; i<modes.length; ++i) {
    		if(modes[i] == mode)
    			return i;
    	}
    	
    	return 0;
    }
    
    public int getSortModeFromIndex(int index) {
    	int[] modes = getSortModes();
    	if(index < 0 || index > modes.length - 1) {
    		throw new InvalidParameterException("Sort mode can not be <0 or greater than the number of available modes");
    	}
    	
    	return modes[index];
    }
    
    public boolean supportsSorting() {
    	int[] modes = getSortModes();
    	return modes != null && modes.length > 0;
    }
    
    public AuthStore.Authentication getAuthentication() {
		return mAuthentication;
	}

	public void setAuthentication(AuthStore.Authentication authentication) {
		this.mAuthentication = authentication;
	}
	
	public void setSortMode(int mode) {
		mSortMode = mode;
	}
	
	public int getSortMode() {
		return mSortMode;
	}

	public String getQuery() {
        return mQuery;
    }

    public SearchCriteria setQuery(String query) {
        this.mQuery = query;
        return this;
    }

    public int getCategory() {
        return mCategory;
    }

    public SearchCriteria setCategory(int category) {
        this.mCategory = category;
        return this;
    }

    public int getPage() {
        return mPage;
    }

    public SearchCriteria setPage(int page) {
        this.mPage = page;
        return this;
    }

    public boolean hasBarcode() {
        return !TextUtils.isEmpty(mBarcode);
    }

    public SearchCriteria setBarcode(String barcode) {
        mBarcode = barcode;
        return this;
    }

    public String getBarcode() {
        return mBarcode;
    }

    public SearchCriteria setTags(ArrayList<Integer> tags) {
    	mTags = tags;
    	return this;
    }
    
    public ArrayList<Integer> getTags() {
    	return mTags;
    }
    
    @Override
    public String toString() {
        return "SearchCriteria [" 
            + "mCategory=" + mCategory 
            + ", mPage=" + mPage
            + ", mQuery=" + mQuery 
            + ", mBarcode=" + mBarcode
            + ", mTags=" + mTags
            + ", mSortMode=" + mSortMode
            + "]";
    }
}
