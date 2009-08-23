package com.markupartist.iglaset.provider;

import android.text.format.Time;

public class Comment {
    private int mDrinkId;
    private int mUserId;
    private Time mCreated;
    private String mComment;

    public Comment() {
        
    }

    public int getDrinkId() {
        return mDrinkId;
    }
    public void setDrinkId(int drinkId) {
        this.mDrinkId = drinkId;
    }
    public int getUserId() {
        return mUserId;
    }
    public void setUserId(int userId) {
        this.mUserId = userId;
    }
    public Time getCreated() {
        return mCreated;
    }
    public void setCreated(Time created) {
        this.mCreated = created;
    }
    public String getComment() {
        return mComment;
    }
    public void setComment(String comment) {
        this.mComment = comment;
    }

    @Override
    public String toString() {
        return mComment;
    }
}
