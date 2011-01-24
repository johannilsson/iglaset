package com.markupartist.iglaset.provider;

public class RecommendationSearchCriteria extends SearchCriteria {

	private final static int[] SortModes = {
		SearchCriteria.SORT_MODE_RATING,
		SearchCriteria.SORT_MODE_NAME
	};
	
	private int mUserId;

    public void setUserId(int userId) {
        this.mUserId = userId;
    }

    public int getUserId() {
        return mUserId;
    }
 
	@Override
	public int[] getSortModes() {
		return SortModes;
	}

	@Override
	public int getDefaultSortMode() {
		return SearchCriteria.SORT_MODE_RATING;
	}
}
