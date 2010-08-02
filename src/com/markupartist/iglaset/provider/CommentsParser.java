package com.markupartist.iglaset.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;

class CommentsParser extends DefaultHandler {
    private static final String TAG = "CommentsParser";
    private ArrayList<Comment> mComments = null;
    private Comment mCurrentComment;
    private StringBuilder mBuilder = null;
    private Time mCreatedTime = null;

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    	mBuilder = new StringBuilder();
    	mCreatedTime = new Time();
    }
    
    public ArrayList<Comment> parseComments(InputStream in, ArrayList<Comment> comments) {
        try {
            mComments = comments;
            InputSource inputSource = new InputSource(in);

            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            xr.setContentHandler(this);
            xr.parse(inputSource);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } catch (SAXException e) {
            Log.e(TAG, e.toString());
        } catch (ParserConfigurationException e) {
            Log.e(TAG, e.toString());
        }

        return mComments;
    }

    public void startElement(String uri, String name, String qName, Attributes atts) {
        if (name.equals("comment")) {
            mCurrentComment = new Comment();
            mCurrentComment.setDrinkId(Integer.parseInt(atts.getValue("article_id").trim()));
            mCurrentComment.setUserId(Integer.parseInt(atts.getValue("user_id").trim()));
            mCurrentComment.setNickname(atts.getValue("nickname").trim());

            // The api returns the created time as RFC 2445.
            mCreatedTime.parse(atts.getValue("created").trim());
            mCurrentComment.setCreated(mCreatedTime);
            
            int rating = 0;
            if (!TextUtils.isEmpty(atts.getValue("user_rating").trim())) {
                rating = Integer.parseInt(atts.getValue("user_rating").trim());
            }
            
            mCurrentComment.setRating(rating);
        }
    }

    public void characters(char ch[], int start, int length) {
    	// Discard the data if we're not inside a comment tag.
    	if(null != mCurrentComment) {
    		mBuilder.append(ch, start, length);
    	}
    }

    public void endElement(String uri, String name, String qName)
                throws SAXException {
        if (name.equals("comment") && null != mCurrentComment) {
        	String comment = mBuilder.toString().replaceAll("\n", "").trim();
            mCurrentComment.setComment(comment);
            mComments.add(mCurrentComment);
            mCurrentComment = null;
        }
        
        mBuilder.setLength(0);
    }
}
