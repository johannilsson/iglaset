package com.markupartist.iglaset.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.markupartist.iglaset.R;

public class StartActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ImageButton imageButton = (ImageButton) findViewById(R.id.btn_search);
        imageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearchRequested();
            }
        });
    }

    @Override
    public boolean onSearchRequested() {
        startSearch(null, false, null, false); 
        return true;
    }
}
