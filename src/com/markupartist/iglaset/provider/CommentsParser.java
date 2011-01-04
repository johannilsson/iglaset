package com.markupartist.iglaset.provider;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.text.TextUtils;
import android.text.format.DateFormat;

class CommentsParser extends AbstractParser<Comment> {
    private ArrayList<Comment> mComments = new ArrayList<Comment>();
    private Comment mCurrentComment;
    private static SimpleDateFormat CreatedTime = new SimpleDateFormat("yyyyMMddHHmmss");

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    	mComments.clear();
    }

	@Override
	public void onStartElement(String name, Attributes atts) {
        if (name.equals("comment")) {
            mCurrentComment = new Comment();
            mCurrentComment.setDrinkId(Integer.parseInt(atts.getValue("article_id").trim()));
            mCurrentComment.setUserId(Integer.parseInt(atts.getValue("user_id").trim()));
            mCurrentComment.setNickname(atts.getValue("nickname").trim());

            try {
                Date created = CommentsParser.CreatedTime.parse(atts.getValue("created").trim());
                mCurrentComment.setCreated(DateFormat.format("yyyy-MM-dd", created));
            } catch (ParseException e) {
                mCurrentComment.setCreated(null);
            }

            int rating = 0;
            if (!TextUtils.isEmpty(atts.getValue("rating").trim())) {
                rating = (int) Float.parseFloat(atts.getValue("rating").trim());
            }
            
            mCurrentComment.setRating(rating);
        }
	}

	@Override
	public void onEndElement(String name, String result) {
    	if (name.equals("text") && null != mCurrentComment) {
        	String comment = result.replaceAll("\n", "");
            mCurrentComment.setComment(comment);
    	} else if (name.equals("comment") && null != mCurrentComment) {
            mComments.add(mCurrentComment);
            mCurrentComment = null;
        }
	}

	@Override
	protected ArrayList<Comment> getContent() {
		return mComments;
	}
}
