package com.markupartist.iglaset.activity;

import com.google.android.imageloader.ImageLoader;
import com.google.android.imageloader.ImageLoader.BindResult;
import com.markupartist.iglaset.R;
import com.markupartist.iglaset.provider.Drink;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class DrinkViewHolder {
    private View layout;
    private TextView nameView;
    private TextView originCountryView;
    private RatingBar rateView;
    private TextView alcoholView;
    private ImageView imageView;
    private Drawable glassIcon = null;
    private TextView commentCountView;
    private TextView ratingCountView;

    public DrinkViewHolder(View layout) {
        this.layout = layout;
    }
    
    /**
     * Populate the view holder with a drink's data. This will set every child
     * view with the appropriate drink data, with the exception of the drink
     * rating since that differs slightly depending on where the view is used.
     * 
     * @param context Calling context.
     * @param drink Drink to use for updating.
     * @param imageClickListener Click listener to attach to thumbnail.
     */
    public void populate(Context context, Drink drink, View.OnClickListener imageClickListener) {
    	update(context, drink);

        getImageView().setTag(drink);
        getImageView().setOnClickListener(imageClickListener);

        final int w = getImageView().getDrawable().getIntrinsicWidth();
        final int h = getImageView().getDrawable().getIntrinsicHeight();
        
        ImageLoader.get(context).unbind(getImageView());
        BindResult result = ImageLoader.get(context).bind(getImageView(), drink.getThumbnailUrl(w, h), imageLoaderCallback);
        if(result == ImageLoader.BindResult.LOADING || result == ImageLoader.BindResult.ERROR) {
			getImageView().setImageResource(R.drawable.noimage);
        }
    }
    
    /**
     * Update the view holder with refreshed drink data. Note that this will
     * not reload the image since that is expected to be static.
     * 
     * @param context Calling context.
     * @param drink Drink data to use for updating.
     */
    public void update(Context context, Drink drink) {
        getNameView().setText(drink.getName());
        getOriginCountryView().setText(drink.getConcatenatedOrigin());
        getAlcoholView().setText(drink.getAlcoholPercent());
        getRatingCountView().setText(String.valueOf(drink.getRatingCount()));
        getCommentCountView().setText(String.valueOf(drink.getCommentCount()));

        if(drink.hasUserRating()) {
    		getNameView().setCompoundDrawables(null, null, getGlassIcon(context), null);
    	} else {
    		getNameView().setCompoundDrawables(null, null, null, null);
    	}
    }

    private ImageLoader.Callback imageLoaderCallback = new ImageLoader.Callback() {
		
		@Override
		public void onImageLoaded(ImageView view, String url) {}
		
		@Override
		public void onImageError(ImageView view, String url, Throwable error) {
			view.setImageResource(R.drawable.noimage);
		}
	};
    
    public TextView getNameView() {
        if (nameView == null) {
            nameView = (TextView) layout.findViewById(R.id.drink_name);
        }
        return nameView;
    }

    public TextView getOriginCountryView() {
        if (originCountryView == null) {
            originCountryView = (TextView) layout.findViewById(R.id.drink_origin_country);
        }
        return originCountryView;
    }

    public RatingBar getRateView() {
        if (rateView == null) {
            rateView = (RatingBar) layout.findViewById(R.id.drink_rating);
        }
        return rateView;
    }

    public TextView getAlcoholView() {
        if (alcoholView == null) {
            alcoholView = (TextView) layout.findViewById(R.id.drink_alcohol_percent);
        }
        return alcoholView;
    }

    public ImageView getImageView() {
        if (imageView == null) {
            imageView = (ImageView) layout.findViewById(R.id.drink_image);
        }
        return imageView;
    }
    
    public Drawable getGlassIcon(Context ctx) {
    	if(null == glassIcon) {
    		glassIcon = ctx.getResources().getDrawable(R.drawable.glass_icon);
    		glassIcon.setBounds(0, 0, glassIcon.getIntrinsicWidth(), glassIcon.getIntrinsicHeight());
    	}
    	return glassIcon;
    }
    
    public TextView getCommentCountView() {
    	if(null == commentCountView) {
    		commentCountView = (TextView) layout.findViewById(R.id.drink_comment_count);
    	}
    	return commentCountView;
    }
    
    public TextView getRatingCountView() {
    	if(null == ratingCountView) {
    		ratingCountView = (TextView) layout.findViewById(R.id.drink_rating_count);
    	}
    	return ratingCountView;
    }
}
