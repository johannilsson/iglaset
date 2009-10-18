package com.markupartist.iglaset.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.markupartist.iglaset.R;

public class StartActivity extends Activity {
    private static final int DIALOG_ABOUT = 0;
    private static final String TAG = "StartActivity";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button searchButton = (Button) findViewById(R.id.btn_search);
        searchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearchRequested();
            }
        });

        Button listButton = (Button) findViewById(R.id.btn_lists);
        listButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(StartActivity.this, CategoryActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onSearchRequested() {
        startSearch(null, false, null, false); 
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                showDialog(DIALOG_ABOUT);
                return true;
            case R.id.menu_preferences:
                Intent launchPreferencesIntent = new Intent().setClass(this, BasicPreferenceActivity.class);
                startActivity(launchPreferencesIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
        case DIALOG_ABOUT:
            PackageManager pm = getPackageManager();
            String version = "";
            try {
                PackageInfo pi = pm.getPackageInfo(this.getPackageName(), 0);
                version = pi.versionName;
            } catch (NameNotFoundException e) {
                Log.e(TAG, "Could not get the package info.");
            }

            Dialog aboutDialog = new Dialog(this);
            aboutDialog.setCanceledOnTouchOutside(true);
            aboutDialog.setContentView(R.layout.about_dialog);
            aboutDialog.setTitle(getText(R.string.app_name) + " " + version);
            return aboutDialog;
        }
        return null;
    }
}
