package com.markupartist.iglaset.provider;

import com.markupartist.iglaset.util.HasKey;

import android.os.Parcel;
import android.os.Parcelable;

public class Tag implements Parcelable, HasKey<String> {
	
	public static final int UNDEFINED_ID = -1;

    public static final Creator<Tag> CREATOR = new Creator<Tag>() {
        public Tag createFromParcel(Parcel in) {
            return new Tag(in);
        }

        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };
	
	private int id;
	private String type;
	private String name;
	
	public Tag() {
		id = UNDEFINED_ID;
	}
	
    public Tag(Parcel in) {
        id = in.readInt();
        type = in.readString();
        name = in.readString();
    }
    
	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeInt(id);
		parcel.writeString(type);
		parcel.writeString(name);
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}	
	
	public String toString() {
		return this.name;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public String getKey() {
		return this.type;
	}
}
