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

public class ImageViewerDialog extends Dialog implements ImageLoader.EventHandler, View.OnClickListener {

	private ImageView imageView;
	private ProgressBar progressBar;
	
	public ImageViewerDialog(Context context, String url) {
		super(context);

    	setCanceledOnTouchOutside(true);
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	setContentView(R.layout.article_image_view);
    	
    	imageView = (ImageView) findViewById(R.id.drink_image);
    	imageView.setOnClickListener(this);
    	progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    	
    	ImageLoader.getInstance().load(imageView, url, true, this);
	}
    
    public static View.OnClickListener createListener(final Context ctx, final Drink drink) {
    	return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				@SuppressWarnings("unused")
				ImageViewerDialog dialog = new ImageViewerDialog(ctx, drink.getLargestImageUrl());
			}
		};
    }

	@Override
	public void onDownloadError() {
		// TODO Auto-generated method stub
		
	}

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
	public void onClick(View v) {
		dismiss();
	}
}
