package com.markupartist.iglaset.activity;

import android.content.Intent;

import com.markupartist.iglaset.provider.Drink;

public class Intents {

	public final static String EXTRA_DRINK = "com.markupartist.iglaset.extras.DRINK";
	public final static String ACTION_PUBLISH_DRINK = "com.markupartist.igaset.action.PUBLISH_DRINK";
	
	public static Intent createPublishDrinkIntent(Drink drink) {
		Intent intent = new Intent(ACTION_PUBLISH_DRINK);
		intent.putExtra(EXTRA_DRINK, drink);
		return intent;
	}

}
