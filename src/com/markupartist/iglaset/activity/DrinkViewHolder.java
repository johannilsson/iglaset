package com.markupartist.iglaset.activity;

import com.markupartist.iglaset.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class DrinkViewHolder {
    private View layout;
    private TextView nameView;
    //private TextView yearView;
    //private TextView originView;
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

    public TextView getNameView() {
        if (nameView == null) {
            nameView = (TextView) layout.findViewById(R.id.drink_name);
        }
        return nameView;
    }

    /*
    public TextView getYearView() {
        if (yearView == null) {
            yearView = (TextView) layout.findViewById(R.id.drink_year);
        }
        return yearView;
    }
    */

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
