package com.markupartist.iglaset.provider;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.util.TimeUtils;

class CommentsParser extends DefaultHandler {
    private static final String TAG = CommentsParser.class.getSimpleName();
    private ArrayList<Comment> mComments = null;
    private Comment mCurrentComment;
    private StringBuilder mBuilder = null;
    private SimpleDateFormat mCreatedTime = null;

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    	mBuilder = new StringBuilder();
    	mCreatedTime = new SimpleDateFormat("yyyyMMddHHmmss");
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

            try {
                Date created = mCreatedTime.parse(atts.getValue("created").trim());
                mCurrentComment.setCreated(DateFormat.format("yyyy-MM-dd", created));
                //mCurrentComment.setCreated(mCreatedTime.parse(atts.getValue("created").trim()));
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

    public void characters(char ch[], int start, int length) {
    	// Discard the data if we're not inside a comment tag.
    	if(null != mCurrentComment) {
    		mBuilder.append(ch, start, length);
    	}
    }

    public void endElement(String uri, String name, String qName)
                throws SAXException {
    	if (name.equals("text") && null != mCurrentComment) {
        	String comment = mBuilder.toString().replaceAll("\n", "").trim();
            mCurrentComment.setComment(comment);
    	} else if (name.equals("comment") && null != mCurrentComment) {
            mComments.add(mCurrentComment);
            mCurrentComment = null;
        }
        
        mBuilder.setLength(0);
    }
}
