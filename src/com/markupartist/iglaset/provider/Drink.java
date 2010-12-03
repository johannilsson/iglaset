package com.markupartist.iglaset.provider;

import java.util.ArrayList;
import java.util.TreeMap;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class Drink implements Parcelable {
    private static String THUMB_RESIZE_FORMAT =
    	"http://api.iglaset.se/resizely/crop/%dx%d/?url=%s";
    private int mId;
    private String mName;
    private String mOrigin;
    private String mOriginCountry;
    private String mProducer;
    private String mSupplierUrl;
    private String mSupplier;
    private String mAlcoholPercent;
    private int mYear;
    private String mDescription;
    private float mUserRating;
    private float mEstimatedRating;
    private float mAverageRating;
    private ArrayList<Volume> mVolumes;
    private ArrayList<Tag> mTags;
    private int mCommentCount;
    private int mRatingCount;
    private TreeMap<ImageSize, String> mImages;
    
    public enum ImageSize {
    	SMALL,
    	MEDIUM,
    	LARGE
    }
    
    public Drink(int id) {
        mId = id;
    }

    private Drink(Parcel in) {
        mId = in.readInt();
        mName = in.readString();
        mOrigin = in.readString();
        mOriginCountry = in.readString();
        mProducer = in.readString();
        mSupplierUrl = in.readString();
        mSupplier = in.readString();
        mAlcoholPercent = in.readString();
        mYear = in.readInt();
        mDescription = in.readString();
        mCommentCount = in.readInt();
        mRatingCount = in.readInt();
        
        mImages = new TreeMap<ImageSize, String>();
        in.readMap(mImages, ClassLoader.getSystemClassLoader());

        mVolumes = new ArrayList<Volume>();
        in.readTypedList(mVolumes, Volume.CREATOR);

        mTags = new ArrayList<Tag>();
        in.readTypedList(mTags, Tag.CREATOR);

        mUserRating = in.readFloat();
        mAverageRating = in.readFloat();
        mEstimatedRating = in.readFloat();
    }

    public void addVolume(Volume volume) {
        if (mVolumes == null)
            mVolumes = new ArrayList<Volume>();
        mVolumes.add(volume);
    }

    public void addTag(Tag tag) {
    	if(mTags == null) {
    		mTags = new ArrayList<Tag>();
    	}
    	
    	mTags.add(tag);
    }
    
    public ArrayList<Tag> getTags() {
    	return mTags;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mName);
        dest.writeString(mOrigin);
        dest.writeString(mOriginCountry);
        dest.writeString(mProducer);
        dest.writeString(mSupplierUrl);
        dest.writeString(mSupplier);
        dest.writeString(mAlcoholPercent);
        dest.writeInt(mYear);
        dest.writeString(mDescription);
        dest.writeInt(mCommentCount);
        dest.writeInt(mRatingCount);
        dest.writeMap(mImages);
        dest.writeTypedList(mVolumes);
        dest.writeTypedList(mTags);
        dest.writeFloat(mUserRating);
        dest.writeFloat(mAverageRating);
        dest.writeFloat(mEstimatedRating);
    }

    public static final Creator<Drink> CREATOR = new Creator<Drink>() {
        public Drink createFromParcel(Parcel in) {
            return new Drink(in);
        }

        public Drink[] newArray(int size) {
            return new Drink[size];
        }
    };

    /**
     * Set the URL of a drink image. If the URL is null or empty it is
     * considered invalid and will be discarded from the image list.
     * @param size Size of the image.
     * @param url Image URL.
     */
    public void setImageUrl(ImageSize size, String url) {
    	if(null == mImages)
    		mImages = new TreeMap<ImageSize, String>();
    	
    	if(null != url && url.length() > 0)
    		mImages.put(size, url);
        else
        	mImages.remove(size);
    }
    
    /**
     * Get the thumbnail URL for this article.
     * @param width Thumbnail width in pixels.
     * @param height Thumbnail height in pixels.
     * @return Thumbnail URL or null if not available.
     */
    public String getThumbnailUrl(int width, int height) {
    	if(null != mImages && mImages.size() > 0) {
    		return String.format(THUMB_RESIZE_FORMAT, width, height, getSmallestImageUrl());
    	}
    	
    	return null;
    }

    
    /**
     * Get the image URL for a given image size.
     * @param size Size of image.
     * @return URL to image or null if not available.
     */
    public String getImageUrl(ImageSize size) {
    	if(null != mImages && mImages.containsKey(size))
    		return mImages.get(size);
    	
    	return null;
    }
    
    /**
     * Fetches the largest available image for the drink.
     * @return Image url or null if no images are available.
     */
    public String getLargestImageUrl() {
    	if(null != mImages && mImages.size() > 0)
    		return mImages.get(mImages.lastKey());

    	return null;
    }

    /**
     * Fetches the smallest available image for the drink.
     * @return Image url or null if no images are available.
     */
    public String getSmallestImageUrl() {
    	if(null != mImages && mImages.size() > 0)
    		return mImages.get(mImages.firstKey());
    	
    	return null;
    }

    public int getId() {
        return mId;
    }

    public ArrayList<Volume> getVolumes() {
        return mVolumes;
    }
    
    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getOrigin() {
        return mOrigin;
    }

    public void setOrigin(String origin) {
        this.mOrigin = origin;
    }
    
    public String getOriginCountry() {
        return mOriginCountry;
    }

    public void setOriginCountry(String originCountry) {
        this.mOriginCountry = originCountry;
    }

    /**
     * Get the concatenated origin. This includes both the country and the
     * area (if available).
     * @return String containing concatenated origin.
     */
    public String getConcatenatedOrigin() {
        if(TextUtils.isEmpty(mOrigin) && TextUtils.isEmpty(mOriginCountry))
            return "";
        else if(TextUtils.isEmpty(mOrigin))
            return mOriginCountry;
        else if(TextUtils.isEmpty(mOriginCountry))
            return mOrigin;
        else if(false == mOrigin.equals(mOriginCountry))
        	return mOrigin + ", " + mOriginCountry;
        else
        	return mOriginCountry;
    }
    
    public String getProducer() {
        return mProducer;
    }

    public void setProducer(String producer) {
        this.mProducer = producer;
    }

    public String getSupplierUrl() {
        return mSupplierUrl;
    }

    public void setSupplierUrl(String supplierUrl) {
        this.mSupplierUrl = supplierUrl;
    }

    public String getSupplier() {
        return mSupplier;
    }

    public void setSupplier(String supplier) {
        this.mSupplier = supplier;
    }

    public String getAlcoholPercent() {
        return mAlcoholPercent;
    }

    public void setAlcoholPercent(String alcoholPercent) {
        this.mAlcoholPercent = alcoholPercent;
    }

    public int getYear() {
        return mYear;
    }

    public void setYear(int mYear) {
        this.mYear = mYear;
    }

    public String getDescription() {
        return mDescription;
    }
    
    public Boolean hasDescription() {
    	return TextUtils.isEmpty(mDescription) == false;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public float getAverageRating() {
        return mAverageRating;
    }

    public void setAverageRating(float rating) {
        this.mAverageRating = rating;
    }

    public float getUserRating() {
        return mUserRating;
    }

    public Boolean hasUserRating() {
    	return mUserRating != 0.0;
    }
    
    public void setUserRating(float userRating) {
        this.mUserRating = userRating;
    }
    
    public void setEstimatedRating(float rating) {
    	this.mEstimatedRating = rating;
    }
    
    public float getEstimatedRating() {
    	return this.mEstimatedRating;
    }
    
    public boolean hasEstimatedRating() {
    	return this.mEstimatedRating != 0.0;
    }

    /**
     * Get the number of comments for this drink.
     * @return Number of comments.
     */
    public int getCommentCount() {
    	return mCommentCount;
    }
    
    /**
     * Set the number of comments for this drink.
     * @param count Number of comments.
     */
    public void setCommentCount(int count) {
    	mCommentCount = count;
    }
    
    public int getRatingCount() {
    	return mRatingCount;
    }
    
    public void setRatingCount(int count) {
    	mRatingCount = count;
    }
    
    @Override
    public String toString() {
        return mName;
    }

    public static class Volume implements Parcelable {
        private String mPriceSek;
        private int mArticleId;
        private int mVolume;
        private int mRetired;

        public Volume() {
        }

        private Volume(Parcel in) {
            mPriceSek = in.readString();
            mArticleId = in.readInt();
            mVolume = in.readInt();
            mRetired = in.readInt();
        }

        public String getPriceSek() {
            return mPriceSek;
        }
        public void setPriceSek(String priceSek) {
            this.mPriceSek = priceSek;
        }
        public int getArticleId() {
            return mArticleId;
        }
        public void setArticleId(int articleId) {
            this.mArticleId = articleId;
        }
        public int getVolume() {
            return mVolume;
        }
        public void setVolume(int volume) {
            this.mVolume = volume;
        }
        public int getRetired() {
            return mRetired;
        }
        public void setRetired(int retired) {
            this.mRetired = retired;
        }
        public boolean isRetired() {
            return mRetired == 1;
        }

        @Override
        public String toString() {
            return "Volume [" 
                +"mArticleId=" + mArticleId 
                + ", mPriceSek=" + mPriceSek 
                + ", mRetired=" + mRetired 
                + ", mVolume=" + mVolume 
                + "]";
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mPriceSek);
            dest.writeInt(mArticleId);
            dest.writeInt(mVolume);
            dest.writeInt(mRetired);
        }

        public static final Creator<Volume> CREATOR = new Creator<Volume>() {
            public Volume createFromParcel(Parcel in) {
                return new Volume(in);
            }

            public Volume[] newArray(int size) {
                return new Volume[size];
            }
        };
    }
}

