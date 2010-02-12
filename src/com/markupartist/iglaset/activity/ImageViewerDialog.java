package com.markupartist.iglaset.activity;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.markupartist.iglaset.R;
import com.markupartist.iglaset.provider.Drink;
import com.markupartist.iglaset.util.ImageLoader;

public class ImageViewerDialog {
	
    public static void showImage(Context ctx, String url) {
		final Dialog dialog = new Dialog(ctx);
    	dialog.setCanceledOnTouchOutside(true);
    	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	dialog.setContentView(R.layout.article_image_view);
    	
    	final ImageView imageView = (ImageView) dialog.findViewById(R.id.drink_image);
    	final ProgressBar progressBar = (ProgressBar) dialog.findViewById(R.id.progress_bar);
    	
    	ImageLoader.EventHandler eventHandler = new ImageLoader.EventHandler() {
    		
			@Override
			public void onDownloadStarted() {
				progressBar.setVisibility(ProgressBar.VISIBLE);
				imageView.setVisibility(ImageView.GONE);
			}
			
			@Override
			public void onFinished() {
				progressBar.setVisibility(ProgressBar.GONE);
				imageView.setVisibility(ImageView.VISIBLE);
				
			}
			
			@Override
			public void onDownloadError() {
			}
		};
    	
    	ImageLoader.getInstance().load(imageView, url, true, eventHandler);
    	
    	// Clicking the image closes the dialog
    	imageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
    		
    	});
    	
    	dialog.show();
    }
    
    public static View.OnClickListener createListener(final Context ctx, final Drink drink) {
    	return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ImageViewerDialog.showImage(ctx, drink.getLargestImageUrl());
			}
		};
    }
}
