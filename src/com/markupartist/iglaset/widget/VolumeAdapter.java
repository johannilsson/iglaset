package com.markupartist.iglaset.widget;

import java.util.List;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.markupartist.iglaset.R;
import com.markupartist.iglaset.provider.Drink.Volume;

public class VolumeAdapter extends ArrayAdapter<Volume> {
	
	LayoutInflater layoutInflater;
	
    public VolumeAdapter(Context context, List<Volume> objects) {
        super(context, R.layout.volume_row, objects);
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	if(null == convertView) {
    		convertView = layoutInflater.inflate(R.layout.volume_row, parent, false);
    	}

        Volume volume = getItem(position);
        TextView id = (TextView) convertView.findViewById(R.id.volume_id);
        id.setText(String.valueOf(volume.getArticleId()));

        TextView amount = (TextView) convertView.findViewById(R.id.volume_amount);
        amount.setText(String.valueOf(volume.getVolume()) + " ml"); // TODO should fix hard coded string

        TextView price = (TextView) convertView.findViewById(R.id.volume_price);
        price.setText(volume.getPriceSek() + " kr"); // TODO should fix hard coded string

        if (volume.isRetired()) {
            id.setPaintFlags(id.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            amount.setPaintFlags(amount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            price.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        return convertView;
    }
}
