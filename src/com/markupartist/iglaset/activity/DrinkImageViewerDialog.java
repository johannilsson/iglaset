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
import com.markupartist.iglaset.util.ImageLoader;

/**
 * @author marco
 *
 */
public class DrinkImageViewerDialog extends Dialog implements ImageLoader.EventHandler, View.OnClickListener {

	private ImageView imageView;
	private ProgressBar progressBar;
	private String url;

	/**
	 * DrinkImageViewerDialog constructor. Creates the dialog and starts downloading
	 * the specified image. Note that the dialog is not shown automatically.
	 * @param context Calling context.
	 * @param url If not null then the image downloading process will start
	 * immediately, otherwise it will have to be started manually later via
	 * DrinkImageViewerDialog.loadImage.
	 */
	public DrinkImageViewerDialog(Context context, String url) {
		super(context);
		this.url = url;

    	setCanceledOnTouchOutside(true);
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	setContentView(R.layout.article_image_view);
    	
    	imageView = (ImageView) findViewById(R.id.drink_image);
    	imageView.setOnClickListener(this);
    	progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    	
    	if(null != this.url) {
    		loadImage(this.url);
    	}
	}
    
    /**
     * Convenience method for creating an OnClickListener which will create and
     * display a new DrinkImageViewerDialog object.
     * @param ctx Calling context.
     * @param drink Drink object containing the image to show.
     * @return OnClickListener object.
     */
    public static View.OnClickListener createListener(final Context ctx, final Drink drink) {
    	return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new DrinkImageViewerDialog(ctx, drink.getLargestImageUrl()).show();
			}
		};
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
            		viewerDialog.loadImage(viewerDialog.url);
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
}
