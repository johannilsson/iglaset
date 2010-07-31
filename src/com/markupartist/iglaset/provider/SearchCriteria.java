package com.markupartist.iglaset.provider;

import java.util.ArrayList;

import android.text.TextUtils;

/**
 * Search criteria
 */
public class SearchCriteria {
    private String mQuery;
    private int mCategory;
    private int mPage = 1;
    private String mToken;
    private String mBarcode;
    private ArrayList<Integer> mTags;

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

    public String getToken() {
        return mToken;
    }

    public SearchCriteria setToken(String token) {
        this.mToken = token;
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
            + "]";
    }
}
