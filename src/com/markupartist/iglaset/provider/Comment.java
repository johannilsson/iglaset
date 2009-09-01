package com.markupartist.iglaset.provider;


public class Comment {
    private int mDrinkId;
    private int mUserId;
    private String mCreated;
    private String mComment;
    private String mNickname;

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
    public String getCreated() {
        return mCreated;
    }
    public void setCreated(String created) {
        this.mCreated = created;
    }
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

    @Override
    public String toString() {
        return "Comment [mComment=" + mComment + ", mCreated=" + mCreated
                + ", mDrinkId=" + mDrinkId + ", mNickname=" + mNickname
                + ", mUserId=" + mUserId + "]";
    }
}
