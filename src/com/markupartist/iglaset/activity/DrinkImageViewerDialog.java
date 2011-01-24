package com.markupartist.iglaset.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.markupartist.iglaset.R;
import com.markupartist.iglaset.provider.Drink;
import com.google.android.imageloader.ImageLoader;
import com.google.android.imageloader.ImageLoader.BindResult;

/**
 * @author marco
 *
 */
public class DrinkImageViewerDialog extends Dialog implements ImageLoader.Callback, View.OnClickListener {

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
	 * 
	 * \see load
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
	}
	
	/**
	 * Attach drink to dialog. Note that this will not download the image. It has to
	 * be explicitly downloaded via DrinkImageViewerDialog.load
	 * @param drink Drink to attach to the image viewer.
	 */
	public void setDrink(Drink drink) {
		this.drink = drink;
	}
    
	/**
	 * Download the drink's largest image if a drink has been attached to this dialog.
	 */
	public void load() {
		if(null != drink) {
			BindResult result = ImageLoader.get(getContext()).bind(imageView, drink.getLargestImageUrl(), this);
			if(result == ImageLoader.BindResult.LOADING) {
				progressBar.setVisibility(ProgressBar.VISIBLE);
				imageView.setVisibility(ImageView.GONE);
			}
		}
	}

	@Override
	public void onClick(View v) {
		dismiss();
	}

	@Override
	public void onImageLoaded(ImageView view, String url) {
		progressBar.setVisibility(ProgressBar.GONE);
		imageView.setVisibility(ImageView.VISIBLE);
	}

	@Override
	public void onImageError(ImageView view, String url, Throwable error) {
		OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	switch(which) {
            	case Dialog.BUTTON_POSITIVE:
            		show();
            		load();
            		break;
            	case Dialog.BUTTON_NEGATIVE:
            		dismiss();
            		break;
            	}
   
            }
		};
		
		hide();
		DialogFactory.createNetworkProblemDialog(getContext(), onClickListener).show();
	}
}
