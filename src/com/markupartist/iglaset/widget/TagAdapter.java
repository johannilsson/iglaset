package com.markupartist.iglaset.widget;

import java.util.ArrayList;

import com.markupartist.iglaset.R;
import com.markupartist.iglaset.provider.Tag;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TagAdapter extends ArrayAdapter<Tag> {

	private ArrayList<Tag> tags;
	Context context;
	
	public TagAdapter(Context context, ArrayList<Tag> tags) {
		super(context, 0, tags);
		this.tags = tags;
		this.context = context;		
	}
	
	@Override
	public int getCount() {
		return tags.size();
	}

	@Override
	public Tag getItem(int position) {
		return tags.get(position);
	}

	@Override
	public long getItemId(int position) {
		return tags.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(null == convertView) {
			convertView = LayoutInflater.from(context).inflate(R.layout.tag_list_row, parent, false);
		}

		Tag tag = tags.get(position);
		
		TextView name = (TextView) convertView.findViewById(R.id.tagName);
		name.setText(tag.getName());

		return convertView;
	}
}
