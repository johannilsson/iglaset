package com.markupartist.iglaset.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.markupartist.iglaset.R;

/**
 * Adapter which presents the drink description hidden under an expandable row.
 * From a user's perspective this works more or less the same as ExpandableListAdapter
 * (and its siblings) but is greatly simplified to suit the application's needs.
 * 
 * @author marco
 */
public class DrinkDescriptionAdapter extends BaseAdapter {

	private View view;
	private TextView textRow;
	private TextView textView;
	private Drawable iconOpened;
	private Drawable iconClosed;
	
	/**
	 * Constructor.
	 * @param context Calling context.
	 * @param description Drink description. Does not need to be sanitized from
	 * HTML.
	 */
	public DrinkDescriptionAdapter(Context context, String description) {
		
		LayoutInflater inflater = LayoutInflater.from(context);
		view = inflater.inflate(R.layout.article_description, null);

		iconOpened = context.getResources().getDrawable(R.drawable.expander_ic_maximized);
		iconOpened.setBounds(0, 0, iconOpened.getIntrinsicWidth(), iconOpened.getIntrinsicHeight());
		iconClosed = context.getResources().getDrawable(R.drawable.expander_ic_minimized);
		iconClosed.setBounds(0, 0, iconOpened.getIntrinsicWidth(), iconClosed.getIntrinsicHeight());
		
		textRow = (TextView) this.view.findViewById(R.id.description_row);
		textView = (TextView) this.view.findViewById(R.id.description_text);
		textView.setText(android.text.Html.fromHtml(description));
	}

	@Override
	public int getCount() {
		return 1;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return this.view;
	}

	/**
	 * Toggle visibility (expanded/collapsed) of the description text.
	 */
	public void toggleVisibility() {
		if(textView.getVisibility() == View.VISIBLE) {
			textRow.setText(R.string.show_description);
			textRow.setCompoundDrawables(this.iconClosed, null,null, null);
			textView.setVisibility(View.GONE);
		}
		else {
			textRow.setText(R.string.hide);
			textRow.setCompoundDrawables(this.iconOpened, null,null, null);
			textView.setVisibility(View.VISIBLE);
		}
	}
}
