package com.markupartist.iglaset.provider;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.text.TextUtils;

import com.markupartist.iglaset.provider.Drink.Volume;

class DrinksParser extends AbstractParser<Drink> {
    private ArrayList<Drink> mDrinks = new ArrayList<Drink>();
    private Drink mCurrentDrink;
    private Volume mCurrentVolume = null;
    private Tag mCurrentTag = null;
    private int mArticleCount;
    public static int COUNT_UNDEFINED = -1;

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        mArticleCount = COUNT_UNDEFINED;
    }
    
    public int getArticleCount() {
    	return mArticleCount;
    }

	@Override
	public void onStartElement(String name, Attributes atts) {
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
            String articleId = atts.getValue("sb_article_id").trim();
            if (!TextUtils.isEmpty(articleId)) {
                mCurrentVolume.setArticleId(Integer.parseInt(articleId));
            }
            mCurrentVolume.setPriceSek(atts.getValue("price").trim());
            mCurrentVolume.setRetired(Integer.parseInt(atts.getValue("retired").trim()));
        } else if (name.equals("tag")) {
        	mCurrentTag = new Tag();
        	mCurrentTag.setId(Integer.parseInt(atts.getValue("id")));
        	mCurrentTag.setType(atts.getValue("type"));
        } else if (name.equals("producer")) {
        	String producerId = atts.getValue("id");
        	if(producerId != null) {
        		mCurrentDrink.setProducerId(Integer.parseInt(producerId.trim()));
        	} else {
        		mCurrentDrink.setProducerId(Producer.UNDEFINED_ID);
        	}
        }
	}

	@Override
	public void onEndElement(String name, String result) {
    	final String cleanedResult = result.replace("\n", "");
    	
        if (mCurrentDrink != null) {
            if (name.trim().equals("name")) {
                mCurrentDrink.setName(cleanedResult);
            } else if (name.equals("producer")) {
                mCurrentDrink.setProducer(cleanedResult);
            } else if (name.equals("supplier")) {
                mCurrentDrink.setSupplier(cleanedResult);
            } else if (name.equals("origin")) {
                mCurrentDrink.setOrigin(cleanedResult);
            } else if (name.equals("origin_country")) {
                mCurrentDrink.setOriginCountry(cleanedResult);
            } else if (name.equals("alc_percent")) {
                mCurrentDrink.setAlcoholPercent(cleanedResult);
            } else if (name.equals("year") && !TextUtils.isEmpty(cleanedResult)) {
                mCurrentDrink.setYear(Integer.parseInt(cleanedResult));
            } else if (name.equals("volume")) {
                mCurrentVolume.setVolume(Integer.parseInt(cleanedResult));
                mCurrentDrink.addVolume(mCurrentVolume);
            } else if (name.equals("tag")) {      
            	mCurrentTag.setName(cleanedResult);
                mCurrentDrink.addTag(mCurrentTag);
                mCurrentTag = null;
            } else if (name.equals("commercial_desc")) {
            	// Use newline as <br>. That's why the distilled "result" variable
            	// cannot be used.
                mCurrentDrink.setDescription(result.replaceAll("\n", "<br/>"));
            } else if (name.equals("avg_rating") && cleanedResult.length() > 0) {
                mCurrentDrink.setAverageRating(Float.parseFloat(cleanedResult));
            } else if (name.equals("comments")) {
            	mCurrentDrink.setCommentCount(Integer.parseInt(cleanedResult));
            } else if (name.equals("small") && !TextUtils.isEmpty(cleanedResult)) {
                mCurrentDrink.setImageUrl(Drink.ImageSize.SMALL, result);
            } else if (name.equals("medium") && !TextUtils.isEmpty(cleanedResult)) {
                mCurrentDrink.setImageUrl(Drink.ImageSize.MEDIUM, result);
            } else if (name.equals("large") && !TextUtils.isEmpty(cleanedResult)) {
                mCurrentDrink.setImageUrl(Drink.ImageSize.LARGE, cleanedResult);
            } else if (name.equals("user_rating") && cleanedResult.length() > 0) {
            	mCurrentDrink.setUserRating(Float.parseFloat(cleanedResult));
            } else if (name.equals("comments")) {
            	mCurrentDrink.setCommentCount(Integer.parseInt(cleanedResult));
            } else if (name.equals("ratings")) {
            	mCurrentDrink.setRatingCount(Integer.parseInt(cleanedResult));
            } else if (name.equals("estimated_rating") && cleanedResult.length() > 0) {
            	mCurrentDrink.setEstimatedRating(Float.parseFloat(cleanedResult));
            }
        }

        if (name.trim().equals("article")) {
            mDrinks.add(mCurrentDrink);
            mCurrentDrink = null;
        } 
	}

	@Override
	protected ArrayList<Drink> getContent() {
		return mDrinks;
	}
}
