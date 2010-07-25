package com.markupartist.iglaset.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.LinearLayout;

/**
 * @author marco
 * Custom view which displays a tag name and a toggle checkbox.
 */
public class CheckedTagView extends LinearLayout implements Checkable {

	private boolean checked;
	private CheckBox checkBox;
	
	public CheckedTagView(Context context) {
		super(context);
	}

	public CheckedTagView(Context context, AttributeSet attrs) { 
		super(context, attrs);
	}
	
	@Override
	public boolean isChecked() {
		return checked;
	}

	@Override
	public void setChecked(boolean checked) {
		this.checked = checked;
		getCheckBox().setChecked(checked);
		refreshDrawableState(); 
	}

	@Override
	public void toggle() {
		setChecked(!checked);
	}
	
	private CheckBox getCheckBox() {
		if(null == checkBox) {
			checkBox = (CheckBox) this.findViewById(android.R.id.checkbox);
		}
		
		return checkBox;
	}
}
