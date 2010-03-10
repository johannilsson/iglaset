package com.markupartist.iglaset.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
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
public class DrinkDescriptionAdapter extends BaseAdapter implements OnClickListener {

	private Context context;
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
		this.context = context;
		
		LayoutInflater inflater = LayoutInflater.from(context);
		this.view = inflater.inflate(R.layout.article_description, null);

		this.iconOpened = getDrawable(R.drawable.expander_ic_maximized);
		this.iconClosed = getDrawable(R.drawable.expander_ic_minimized);
		
		this.textRow = (TextView) this.view.findViewById(R.id.description_row);
		this.textView = (TextView) this.view.findViewById(R.id.description_text);
		this.textView.setText(android.text.Html.fromHtml(description));
		
		// Use the same click listener for both text views. This makes it easy
		// for the user to disable an opened description.
		this.textRow.setOnClickListener(this);
		this.textView.setOnClickListener(this);
	}

	private Drawable getDrawable(int id) {
		Drawable drawable = this.context.getResources().getDrawable(id);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		return drawable;
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

	@Override
	public void onClick(View view) {
		if(this.textView.getVisibility() == TextView.VISIBLE) {
			this.textRow.setText(R.string.show_description);
			this.textRow.setCompoundDrawables(this.iconClosed, null,null, null);
			this.textView.setVisibility(TextView.GONE);
		}
		else {
			this.textRow.setText(R.string.hide_description);
			this.textRow.setCompoundDrawables(this.iconOpened, null,null, null);
			this.textView.setVisibility(TextView.VISIBLE);
		}
	}

}
