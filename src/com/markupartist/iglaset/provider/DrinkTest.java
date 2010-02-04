package com.markupartist.iglaset.provider;

import junit.framework.TestCase;

public class DrinkTest extends TestCase {
	private Drink mDrink;
	
	protected void setUp() {
		mDrink = new Drink(0);
	}
	
	// Test empty concatenation
	void testGetConcatenatedOriginEmpty() {
		assertEquals("", mDrink.getConcatenatedOrigin());
	}
	
	// Test country only
	void testGetConcatenatedOriginCountry() {
		mDrink.setOriginCountry("USA");
		assertEquals("USA", mDrink.getConcatenatedOrigin());
	}
		
	// Test both origin and country
	void testGetConcatenatedOriginFull() {
		mDrink.setOrigin("Oregon");
		mDrink.setOriginCountry("USA");
		assertEquals("Oregon, USA", mDrink.getConcatenatedOrigin());
	}
}
