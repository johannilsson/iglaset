package com.markupartist.iglaset.provider;

public class Tag {
	
	public static final int UNDEFINED_ID = -1;

	private int id;
	private String type;
	private String name;
	
	public Tag() {
		id = UNDEFINED_ID;
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
}
