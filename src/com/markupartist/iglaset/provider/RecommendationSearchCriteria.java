package com.markupartist.iglaset.provider;

public class RecommendationSearchCriteria extends SearchCriteria {
    private int mUserId;

    public void setUserId(int userId) {
        this.mUserId = userId;
    }

    public int getUserId() {
        return mUserId;
    }
    
    @Override
    public boolean supportsSorting() {
    	return false;
    }
}
