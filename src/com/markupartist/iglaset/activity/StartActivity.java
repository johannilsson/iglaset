package com.markupartist.iglaset.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.markupartist.iglaset.R;
import com.markupartist.iglaset.provider.AuthStore;
import com.markupartist.iglaset.util.Tracker;

public class StartActivity extends ListActivity {
    private static final String TAG = "StartActivity";

    private static final int DIALOG_ABOUT = 0;
    private static final int DIALOG_NOT_AUTHENTICATED = 1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tracker.getInstance().start(this).trackPageView("start");

        setContentView(R.layout.start);

        ArrayAdapter<String> adapter =
            new ArrayAdapter<String>(this, R.layout.start_list_row, R.id.start_list_row);
        adapter.add("Sök");
        adapter.add("Scanna");
        adapter.add("Kategorier");
        adapter.add("Rekommendationer");
        adapter.add("Satta betyg");

        setListAdapter(adapter);
    }

    /*
     * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        switch (position) {
        case 0:
            onSearchRequested();
            break;
        case 1:
            IntentIntegrator.initiateScan(StartActivity.this);
            break;
        case 2:
            Intent i = new Intent(StartActivity.this, CategoryActivity.class);
            startActivity(i);
            break;
        case 3:
            if (AuthStore.getInstance().hasAuthentication(this)) {
                Intent recIntent = new Intent(this, SearchResultActivity.class);
                recIntent.setAction(SearchResultActivity.ACTION_USER_RECOMMENDATIONS);
                startActivity(recIntent);
            } else {
                showDialog(DIALOG_NOT_AUTHENTICATED);
            }
            break;
        case 4:
            if (AuthStore.getInstance().hasAuthentication(this)) {
                Intent ratingIntent = new Intent(this, SearchResultActivity.class);
                ratingIntent.setAction(SearchResultActivity.ACTION_USER_RATINGS);
                startActivity(ratingIntent);
            } else {
                showDialog(DIALOG_NOT_AUTHENTICATED);
            }
            break;
        }
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
        case DIALOG_NOT_AUTHENTICATED:
            return new AlertDialog.Builder(this)
                .setTitle(getText(R.string.not_logged_in))
                .setMessage(R.string.login_to_proceed_message)
                .setPositiveButton("Logga in", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent prefIntent =
                            new Intent(StartActivity.this, BasicPreferenceActivity.class);
                        startActivity(prefIntent);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case IntentIntegrator.REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    IntentResult scanResult =
                        IntentIntegrator.parseActivityResult(requestCode,
                                resultCode, data);
                    if (scanResult != null) {
                        Log.d(TAG, "contents: " + scanResult.getContents());
                        Log.d(TAG, "formatName: " + scanResult.getFormatName());
                        Intent i = new Intent(StartActivity.this,
                                SearchResultActivity.class);
                        i.putExtra(SearchResultActivity.EXTRA_SEARCH_BARCODE,
                                scanResult.getContents());
                        startActivity(i);
                    } else {
                        Log.d(TAG, "NO SCAN RESULT");
                    }   
                }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Tracker.getInstance().stop();
    }
}
