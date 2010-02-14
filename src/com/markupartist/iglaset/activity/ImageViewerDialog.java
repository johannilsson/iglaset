package com.markupartist.iglaset.activity;

import android.app.AlertDialog;
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

public class ImageViewerDialog extends Dialog implements ImageLoader.EventHandler, View.OnClickListener {

	private ImageView imageView;
	private ProgressBar progressBar;
	private String url;
	
	public ImageViewerDialog(Context context, String url) {
		super(context);
		this.url = url;

    	setCanceledOnTouchOutside(true);
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	setContentView(R.layout.article_image_view);
    	
    	imageView = (ImageView) findViewById(R.id.drink_image);
    	imageView.setOnClickListener(this);
    	progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    	
    	loadImage(this.url);
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
    
    private void loadImage(String url) {
    	show();
    	ImageLoader.getInstance().load(imageView, url, true, this);
    }

	@Override
	public void onDownloadError() {
		dismiss();
		
		new AlertDialog.Builder(getContext())
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle("Ett fel inträffade")
        .setMessage("Kunde inte ansluta till servern. Försök igen, eller Cancel för att gå tillbaka till föregående vy.")
        .setPositiveButton("Försök igen", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	ImageViewerDialog viewerDialog = ImageViewerDialog.this;
            	viewerDialog.loadImage(viewerDialog.url);
            }
        }).setNegativeButton(getContext().getText(android.R.string.cancel), new OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                dismiss();
            }
        }).create().show();
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
