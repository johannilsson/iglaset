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
    private ArrayList<Drink> mDrinks;
    private String mCurrentText;
    private Drink mCurrentDrink;
    private String mCurrentDescription = "";
    private boolean inDescription = false;
    private Volume mCurrentVolume;
    private String mCurrentTagType;
    private boolean mInName;
    private String mCurrentName = "";

    public ArrayList<Drink> parseDrinks(InputStream in, ArrayList<Drink> drinks) {
        try {
            mDrinks = drinks;
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

        return mDrinks;
    }

    public void startElement(String uri, String name, String qName, Attributes atts) {
        if (name.equals("article")) {
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
        } else if (name.equals("commercial_desc")) {
            inDescription = true;
        } else if (name.equals("name")) {
            mInName = true;
        }
    }

    public void characters(char ch[], int start, int length) {
        mCurrentText = (new String(ch).substring(start, start + length).trim());
        //Log.d(TAG, "currentText: " + mCurrentText);
        if (inDescription /*&& !TextUtils.isEmpty(mCurrentText)*/) {
            mCurrentDescription += mCurrentText.replaceAll("\n", "<br>");
        } else if (mInName) {
            mCurrentName += mCurrentText;
        }
    }

    public void endElement(String uri, String name, String qName)
                throws SAXException {
        if (mCurrentDrink != null) {
            if (name.trim().equals("name") && !TextUtils.isEmpty(mCurrentText)) {
                mCurrentDrink.setName(mCurrentName);
                mInName = false;
                mCurrentName = ""; // Reset the name
            } else if (name.equals("producer") && !TextUtils.isEmpty(mCurrentText)) {
                mCurrentDrink.setProducer(mCurrentText);
            } else if (name.equals("supplier") && !TextUtils.isEmpty(mCurrentText)) {
                mCurrentDrink.setSupplier(mCurrentText);
            } else if (name.equals("origin") && !TextUtils.isEmpty(mCurrentText)) {
                mCurrentDrink.setOrigin(mCurrentText);
            } else if (name.equals("origin_country") && !TextUtils.isEmpty(mCurrentText)) {
                mCurrentDrink.setOriginCountry(mCurrentText);
            } else if (name.equals("alc_percent") && !TextUtils.isEmpty(mCurrentText)) {
                mCurrentDrink.setAlcoholPercent(mCurrentText);
            } else if (name.equals("year") && !TextUtils.isEmpty(mCurrentText)) {
                mCurrentDrink.setYear(Integer.parseInt(mCurrentText));
            } else if (name.equals("volume") && !TextUtils.isEmpty(mCurrentText)) {
                mCurrentVolume.setVolume(Integer.parseInt(mCurrentText));
                mCurrentDrink.addVolume(mCurrentVolume);
            } else if (name.equals("tag") && !TextUtils.isEmpty(mCurrentText)) {
                mCurrentDrink.addTag(mCurrentTagType, mCurrentText);
            } else if (name.equals("commercial_desc")) {
                mCurrentDrink.setDescription(mCurrentDescription);
                inDescription = false;
            } else if (name.equals("avg_rating") && !TextUtils.isEmpty(mCurrentText)) {
                mCurrentDrink.setRating(mCurrentText);
            } else if (name.equals("small") && !TextUtils.isEmpty(mCurrentText)) {
                mCurrentDrink.setImageUrl(mCurrentText);
            } else if (name.equals("user_rating") && !TextUtils.isEmpty(mCurrentText)) {
                mCurrentDrink.setUserRating(Float.parseFloat(mCurrentText));
            }
        }

        if (name.trim().equals("article")) {
            mDrinks.add(mCurrentDrink);
        }

    }
}
