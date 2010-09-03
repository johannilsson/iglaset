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

import com.markupartist.iglaset.provider.Drink.Volume;

class DrinksParser extends DefaultHandler {
    private static final String TAG = "DrinksParser";
    private ArrayList<Drink> mDrinks = null;
    private Drink mCurrentDrink;
    private Volume mCurrentVolume = null;
    private String mCurrentTagType;
    private StringBuilder mTextBuffer = null;
    private int mArticleCount;
    public static int COUNT_UNDEFINED = -1;

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        mArticleCount = COUNT_UNDEFINED;
        mTextBuffer = new StringBuilder();
    }
    
    public int getArticleCount() {
    	return mArticleCount;
    }
    
    public ArrayList<Drink> parseDrinks(InputStream in, ArrayList<Drink> drinks) {
        try {
            mDrinks = drinks;
            InputSource inputSource = new InputSource(in);
            inputSource.setEncoding("UTF-8");

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

        return mDrinks;
    }

    public void startElement(String uri, String name, String qName, Attributes atts) {
    	if (name.equals("articles")) {
    		String articleCount = atts.getValue("total_articles");
    		if(!TextUtils.isEmpty(articleCount)) {
    			mArticleCount = Integer.parseInt(articleCount);
    		}   		
    	} else if (name.equals("article")) {
            mCurrentDrink = new Drink(Integer.parseInt(atts.getValue("id").trim()));
        } else if (name.equals("supplier") && !TextUtils.isEmpty(atts.getValue("url").trim())) {
            mCurrentDrink.setSupplierUrl(atts.getValue("url").trim());
        } else if (name.equals("volume")) {
            mCurrentVolume = new Volume();
            mCurrentVolume.setArticleId(Integer.parseInt(atts.getValue("sb_article_id").trim()));
            mCurrentVolume.setPriceSek(atts.getValue("price").trim());
            mCurrentVolume.setRetired(Integer.parseInt(atts.getValue("retired").trim()));
        } else if (name.equals("tag")) {
            mCurrentTagType = atts.getValue("type").trim();
        }
    }

    public void characters(char ch[], int start, int length) {
    	mTextBuffer.append(ch, start, length);
    }

    public void endElement(String uri, String name, String qName)
                throws SAXException {
    	
    	Log.d(TAG, name);
    	final String result = mTextBuffer.toString().replace("\n", "").trim();
    	
        if (mCurrentDrink != null) {
            if (name.trim().equals("name")) {
                mCurrentDrink.setName(result);
            } else if (name.equals("producer")) {
                mCurrentDrink.setProducer(result);
            } else if (name.equals("supplier")) {
                mCurrentDrink.setSupplier(result);
            } else if (name.equals("origin")) {
                mCurrentDrink.setOrigin(result);
            } else if (name.equals("origin_country")) {
                mCurrentDrink.setOriginCountry(result);
            } else if (name.equals("alc_percent")) {
                mCurrentDrink.setAlcoholPercent(result);
            } else if (name.equals("year") && !TextUtils.isEmpty(result)) {
                mCurrentDrink.setYear(Integer.parseInt(result));
            } else if (name.equals("volume")) {
                mCurrentVolume.setVolume(Integer.parseInt(result));
                mCurrentDrink.addVolume(mCurrentVolume);
            } else if (name.equals("tag")) {
                mCurrentDrink.addTag(mCurrentTagType, result);
            } else if (name.equals("commercial_desc")) {
            	// Use newline as <br>. That's why the distilled "result" variable
            	// cannot be used.
                mCurrentDrink.setDescription(mTextBuffer.toString().trim().replaceAll("\n", "<br/>"));
            } else if (name.equals("avg_rating")) {
                mCurrentDrink.setRating(result);
            } else if (name.equals("comments")) {
            	mCurrentDrink.setCommentCount(Integer.parseInt(result));
            } else if (name.equals("small")) {
                if (!TextUtils.isEmpty(mTextBuffer)) {
                    mCurrentDrink.setImageUrl(Drink.ImageSize.SMALL, result);
                }
            } else if (name.equals("medium")) {
                if (!TextUtils.isEmpty(mTextBuffer)) {
                    mCurrentDrink.setImageUrl(Drink.ImageSize.MEDIUM, result);
                }
            } else if (name.equals("large")) {
                if (!TextUtils.isEmpty(mTextBuffer)) {
                    mCurrentDrink.setImageUrl(Drink.ImageSize.LARGE, result);
                }
            } else if (name.equals("user_rating") && result.length() > 0) {
            	mCurrentDrink.setUserRating(Float.parseFloat(result));
            } else if (name.equals("comments")) {
            	mCurrentDrink.setCommentCount(Integer.parseInt(result));
            } else if (name.equals("ratings")) {
            	mCurrentDrink.setRatingCount(Integer.parseInt(result));
            }
        }

        if (name.trim().equals("article")) {
            mDrinks.add(mCurrentDrink);
            mCurrentDrink = null;
        }
        
        mTextBuffer.setLength(0);
    }
}
