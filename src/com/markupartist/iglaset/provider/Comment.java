package com.markupartist.iglaset.provider;

import java.util.Date;

public class Comment {
    private int mDrinkId;
    private int mUserId;
    private CharSequence mCreated;
    private String mComment;
    private String mNickname;
    private int mRating = 0;

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
    public CharSequence getCreated() {
        return mCreated;
    }
    public void setCreated(CharSequence date) {
        //this.mCreated = (Date) date.clone();
        this.mCreated = date;
    }
    /**
     * Get comment date.
     * @return Comment's date. Note that this may be null.
     */
    public String getComment() {
        return mComment;
    }
    public void setComment(String comment) {
        this.mComment = comment;
    }
    public String getNickname() {
        return mNickname;
    }
    public void setNickname(String nickname) {
        this.mNickname = nickname;
    }
    public int getRating() {
        return mRating;
    }
    public void setRating(int rating) {
        this.mRating = rating;
    }

    @Override
    public String toString() {
        return "Comment [mComment=" + mComment + ", mCreated=" + mCreated
                + ", mDrinkId=" + mDrinkId + ", mNickname=" + mNickname
                + ", mUserId=" + mUserId + "]";
    }
}
