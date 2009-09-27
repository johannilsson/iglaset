package com.markupartist.iglaset.provider;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

public class Drink implements Parcelable {
    private static String THUMB_RESIZE_BASE_URL = "http://api.iglaset.se/resizely/crop/50x50/?url=";
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
    private String mRating = "0";
    private String mImageUrl;
    private ArrayList<Volume> mVolumes;
    private HashMap<String, ArrayList<String>> mTags;

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
        mRating = in.readString();
        mImageUrl = in.readString();

        mVolumes = new ArrayList<Volume>();
        in.readTypedList(mVolumes, Volume.CREATOR);

        mTags = new HashMap<String, ArrayList<String>>();
        in.readMap(mTags, ClassLoader.getSystemClassLoader());
    }

    public void addVolume(Volume volume) {
        if (mVolumes == null)
            mVolumes = new ArrayList<Volume>();
        mVolumes.add(volume);
    }

    public HashMap<String, ArrayList<String>> getTags() {
        return mTags;
    }
    
    public void addTag(String type, String name) {
        if (mTags == null)
            mTags = new HashMap<String, ArrayList<String>>();

        ArrayList<String> tagList;
        if (mTags.containsKey(type)) {
            tagList = mTags.get(type);
            tagList.add(name);
        } else {
            tagList = new ArrayList<String>();
            tagList.add(name);
            mTags.put(type, tagList);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
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
        dest.writeString(mRating);
        dest.writeString(mImageUrl);
        dest.writeTypedList(mVolumes);
        dest.writeMap(mTags);
    }

    public static final Creator<Drink> CREATOR = new Creator<Drink>() {
        public Drink createFromParcel(Parcel in) {
            return new Drink(in);
        }

        public Drink[] newArray(int size) {
            return new Drink[size];
        }
    };

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public String getImageUrl() {
        return THUMB_RESIZE_BASE_URL + mImageUrl;
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

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public String getRating() {
        return mRating;
    }

    public void setRating(String rating) {
        this.mRating = rating;
    }

    @Override
    public String toString() {
        return mName;
    }

    public static class Volume implements Parcelable {
        private String mPriceSek;
        private int mArticleId;
        private int mVolume;

        public Volume() {
        }

        private Volume(Parcel in) {
            mPriceSek = in.readString();
            mArticleId = in.readInt();
            mVolume = in.readInt();
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

        @Override
        public String toString() {
            return "Volume [mArticleId=" + mArticleId + ", mPriceSek="
                    + mPriceSek + ", mVolume=" + mVolume + "]";
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mPriceSek);
            dest.writeInt(mArticleId);
            dest.writeInt(mVolume);
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

