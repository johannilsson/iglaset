package com.markupartist.iglaset.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.markupartist.iglaset.R;
import com.markupartist.iglaset.util.ImageLoader;

public class ViewItemImageActivity extends Activity {
    private static final String ACTION = "com.markupartist.iglaset.VIEW_ITEM_IMAGE";
	private static final String EXTRA_IMAGE_URL = "com.markupartist.iglaset.imageurl";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.article_image_view);
		
		final Bundle extras = getIntent().getExtras();
		if(null != extras) {
			String url = extras.getString(EXTRA_IMAGE_URL);
			if(null != url) {
				ImageView view = (ImageView) findViewById(R.id.drink_image);
				ImageLoader.getInstance().load(view, url, true);
			}
		}
	}
	
	
    /**
     * @param ctx
     * @param url
     */
    public static void showImage(Context ctx, String url) {
    	final Intent intent = new Intent(ACTION);
    	intent.putExtra(EXTRA_IMAGE_URL, url);
    	ctx.startActivity(intent);
    }
}
