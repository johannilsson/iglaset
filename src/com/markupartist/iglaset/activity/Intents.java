package com.markupartist.iglaset.activity;

import android.content.Intent;

import com.markupartist.iglaset.provider.Drink;

public class Intents {

	public static final String EXTRA_DRINK = "com.markupartist.iglaset.extras.DRINK";
	
    /**
     * Key to identify a barcode
     */
    public static final String EXTRA_BARCODE = "com.markupartist.iglaset.article.barcode";
	
	public static final String ACTION_PUBLISH_DRINK = "com.markupartist.igaset.action.PUBLISH_DRINK";
	
	/**
	 * Create a publish drink intent. This signals the listeners that the
	 * supplied drink has had its user rating or comment/rating count altered
	 * and should be updated if it's used in the UI.
	 * 
	 * @param drink Drink that has been updated.
	 * @return Intent with {@code .action()} set to {@code ACTION_PUBLISH_DRINK}
	 * and the updated drink attached as {@code EXTRA_DRINK}. 
	 */
	public static Intent createPublishDrinkIntent(Drink drink) {
		Intent intent = new Intent(ACTION_PUBLISH_DRINK);
		intent.putExtra(EXTRA_DRINK, drink);
		return intent;
	}

}
