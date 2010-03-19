package com.markupartist.iglaset.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.markupartist.iglaset.R;
import com.markupartist.iglaset.provider.Drink;
import com.markupartist.iglaset.util.ImageLoader;

/**
 * @author marco
 *
 */
public class DrinkImageViewerDialog extends Dialog implements ImageLoader.EventHandler, View.OnClickListener {

	private ImageView imageView;
	private ProgressBar progressBar;
	private Drink drink;

	/**
	 * DrinkImageViewerDialog constructor. Creates the dialog and starts downloading
	 * the specified image. Note that the dialog is not shown automatically.
	 * @param context Dialog context.
	 * @param drink Drink containing the image to show. If the image is not null then
	 * the dialog will try to show the  drink's largest available image. If that image
	 * is not available on the server or if there is a network connection issue then an
	 * error will be shown, allowing the user to retry or abort.
	 */
	public DrinkImageViewerDialog(Context context, Drink drink) {
		super(context);
		this.drink = drink;

    	setCanceledOnTouchOutside(true);
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	setContentView(R.layout.article_image_view);

    	imageView = (ImageView) findViewById(R.id.drink_image);
    	imageView.setOnClickListener(this);
    	progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    	
    	if(null != this.drink) {
    		loadImage(this.drink.getLargestImageUrl());
    	}
	}
	
	/**
	 * Attach drink to dialog. This will, if drink is not null, immediately try
	 * to download the drink's largest image.
	 * @param drink Drink to attach to the image viewer.
	 */
	public void setDrink(Drink drink) {
		this.drink = drink;
		
		if(null != drink) {
			loadImage(drink.getLargestImageUrl());
		}
	}
    
    /**
     * Tells the ImageLoader helper to start loading a URL into this view.
     * @param url Image URL to load.
     */
    private void loadImage(String url) {
    	ImageLoader.getInstance().load(imageView, url, true, this);
    }

	@Override
	public void onDownloadError() {
		OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	switch(which) {
            	case Dialog.BUTTON_POSITIVE:
            		DrinkImageViewerDialog viewerDialog = DrinkImageViewerDialog.this;
            		viewerDialog.loadImage(viewerDialog.drink.getLargestImageUrl());
            		break;
            	case Dialog.BUTTON_NEGATIVE:
            		dismiss();
            		break;
            	}
   
            }
		};
		
		DialogFactory.createNetworkProblemDialog(getContext(), onClickListener).show();
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

	@Override
	public void onDecodeFailed() {
		dismiss();
		Toast.makeText(getContext(), R.string.image_decode_failed, Toast.LENGTH_LONG).show();
	}
}
