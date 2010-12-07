package com.markupartist.iglaset.provider;

public class RatingSearchCriteria extends SearchCriteria {
    private int mUserId;

	private final static int[] SortModes = {
		SearchCriteria.SORT_MODE_DATE,
		SearchCriteria.SORT_MODE_RATING,
		SearchCriteria.SORT_MODE_NAME
	};
	
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
		return SORT_MODE_DATE;
	}
}
