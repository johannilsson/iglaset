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
import android.util.Log;

class CommentsParser extends DefaultHandler {
    private static final String TAG = "CommentsParser";
    private ArrayList<Comment> mComments;
    private String mCurrentText;
    private Comment mCurrentComment;
    private boolean mInComment = false;
    private String mCurrentCommentString = "";

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
            mCurrentComment.setCreated(atts.getValue("created").trim());

            mInComment = true;
            // TODO: Set Time later on...
            //Time created = new Time();
            //created.parse(s);
        }
    }

    public void characters(char ch[], int start, int length) {
        mCurrentText = (new String(ch).substring(start, start + length).trim());

        if (mInComment) {
            mCurrentCommentString += mCurrentText.replaceAll("\n", "");;
        }
    }

    public void endElement(String uri, String name, String qName)
                throws SAXException {
        if (mCurrentComment != null) {
            if (name.trim().equals("comment") && !TextUtils.isEmpty(mCurrentCommentString)) {
                mCurrentComment.setComment(mCurrentCommentString);
                mCurrentCommentString = "";
            }
        }

        if (name.trim().equals("comment")) {
            mComments.add(mCurrentComment);
        }
    }
}
