package com.markupartist.iglaset.activity;

import com.markupartist.iglaset.R;
import com.markupartist.iglaset.provider.Drink;
import com.markupartist.iglaset.util.ImageLoader;

import android.content.Context;
import android.graphics.drawable.Drawable;
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
     * @param drink Drink to use for updating.
     * @param imageClickListener Click listener to attach to thumbnail.
     */
    public void populate(Context context, Drink drink, View.OnClickListener imageClickListener) {
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

        // Only load the image if it's not already loaded. Trying to load it
        // twice will cause the image's background (R.drawable.noimage) to
        // show for a brief second while the new image is applied.
        Drink tag = (Drink) getImageView().getTag();
        if(null == tag || tag.getId() != drink.getId()) {
	        final int w = getImageView().getDrawable().getIntrinsicWidth();
	        final int h = getImageView().getDrawable().getIntrinsicHeight();
	        ImageLoader.getInstance().load(getImageView(), drink.getThumbnailUrl(w, h), true, R.drawable.noimage, null);
	        getImageView().setTag(drink);
        }
        getImageView().setOnClickListener(imageClickListener);
    }

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
