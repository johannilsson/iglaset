package com.markupartist.iglaset.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.markupartist.iglaset.R;
import com.markupartist.iglaset.util.Tracker;

public class StartActivity extends Activity {
    private static final int DIALOG_ABOUT = 0;
    private static final String TAG = "StartActivity";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tracker.getInstance().start(this).trackPageView("start");

        setContentView(R.layout.main);

        final Button searchButton = (Button) findViewById(R.id.btn_search);
        searchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Tracker.getInstance().trackEvent(searchButton);
                onSearchRequested();
            }
        });

        final Button listButton = (Button) findViewById(R.id.btn_lists);
        listButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Tracker.getInstance().trackEvent(listButton);
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
                Tracker.getInstance().trackEvent(item);
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

            return new AlertDialog.Builder(this)
                .setTitle(getText(R.string.app_name) + " " + version)
                .setIcon(android.R.drawable.ic_dialog_info)
                //.setView(findViewById(R.id.about_dialog_text))
                .setMessage(getText(R.string.about_this_app))
                .setCancelable(true)
                .setPositiveButton(getText(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ;
                    }
                })
                .setNeutralButton(getText(R.string.donate), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://pledgie.com/campaigns/6528"));
                        startActivity(browserIntent);
                    }
                })
                .setNegativeButton(getText(R.string.feedback), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                        emailIntent .setType("plain/text");
                        emailIntent .putExtra(android.content.Intent.EXTRA_EMAIL,
                                new String[]{"iglaset@markupartist.com"});
                        emailIntent .putExtra(android.content.Intent.EXTRA_SUBJECT,
                                "iglaset feedback");
                        startActivity(Intent.createChooser(emailIntent,
                                getText(R.string.send_email)));
                    }
                })
                .create();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Tracker.getInstance().stop();
    }
}
