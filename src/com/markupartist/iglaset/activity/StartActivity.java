package com.markupartist.iglaset.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.markupartist.iglaset.R;
import com.markupartist.iglaset.provider.AuthStore;
import com.markupartist.iglaset.util.ErrorReporter;

public class StartActivity extends Activity implements android.view.View.OnClickListener {
    private static final String TAG = "StartActivity";

    private static final int DIALOG_ABOUT = 0;
    private static final int DIALOG_NOT_AUTHENTICATED = 1;

    private AutoCompleteTextView mSearchView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ErrorReporter reporter = ErrorReporter.getInstance();
        reporter.checkErrorAndReport(this);

        setContentView(R.layout.start);

        // TODO: Handle the ime option actionSearch, for now we are just using actionDone.
        mSearchView = (AutoCompleteTextView) findViewById(R.id.search_text);
        mSearchView.setAdapter(new AutoCompleteSearchAdapter(this, R.layout.simple_list_row_inverted));

        ImageButton searchButton = (ImageButton) findViewById(R.id.btn_search);
        searchButton.setOnClickListener(this);
        Button scanButton = (Button) findViewById(R.id.btn_scan);
        scanButton.setOnClickListener(this);
        Button categoryButton = (Button) findViewById(R.id.btn_lists);
        categoryButton.setOnClickListener(this);
        Button recommendationButton = (Button) findViewById(R.id.btn_recommendations);
        recommendationButton.setOnClickListener(this);
        Button ratedDrinksButton = (Button) findViewById(R.id.btn_rated_drinks);
        ratedDrinksButton.setOnClickListener(this);
    }

    /**
     * We don't allow searches from this activity since we have a search at the
     * top already.
     */
    @Override
    public boolean onSearchRequested() {
        return false;
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
        case DIALOG_NOT_AUTHENTICATED:
            return new AlertDialog.Builder(this)
                .setTitle(R.string.not_logged_in)
                .setMessage(R.string.login_to_proceed_message)
                .setPositiveButton("Logga in", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent prefIntent =
                            new Intent(StartActivity.this, BasicPreferenceActivity.class);
                        startActivity(prefIntent);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
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
                .setMessage(R.string.about_this_app)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ;
                    }
                })
                .setNeutralButton(R.string.donate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://pledgie.com/campaigns/6528"));
                        startActivity(browserIntent);
                    }
                })
                .setNegativeButton(R.string.feedback, new DialogInterface.OnClickListener() {
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_search:
            Intent searchIntent = new Intent(this, SearchResultActivity.class);
            searchIntent.setAction(Intent.ACTION_SEARCH);
            searchIntent.putExtra(SearchManager.QUERY, mSearchView.getText().toString());
            startActivity(searchIntent);            
            break;
        case R.id.btn_lists:
            Intent i = new Intent(StartActivity.this, CategoryActivity.class);
            startActivity(i);
            break;
        case R.id.btn_recommendations:
            if (AuthStore.getInstance().hasAuthentication(this)) {
                Intent recIntent = new Intent(this, SearchResultActivity.class);
                recIntent.setAction(SearchResultActivity.ACTION_USER_RECOMMENDATIONS);
                startActivity(recIntent);
            } else {
                showDialog(DIALOG_NOT_AUTHENTICATED);
            }
            break;
        case R.id.btn_scan:
            IntentIntegrator.initiateScan(this);
            break;
        case R.id.btn_rated_drinks:
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

    /**
     * Adapter for the search view. Queries the recent suggestions database
     * internally.
     * @author johan
     */
    public class AutoCompleteSearchAdapter extends ArrayAdapter<String>
            implements Filterable {

        public AutoCompleteSearchAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public Filter getFilter() {
            Filter nameFilter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        Uri searchUri =
                            Uri.parse(String.format("content://%s/suggestions",
                                    SearchSuggestionProvider.AUTHORITY));

                        String[] args = new String [] { constraint + "%" };
                        Cursor cur = managedQuery(searchUri,
                                SearchRecentSuggestions.QUERIES_PROJECTION_1LINE,
                                "display1 LIKE ?", args, null);

                        startManagingCursor(cur);

                        ArrayList<String> list = new ArrayList<String>();
                        while (cur.moveToNext()) {
                            list.add(cur.getString(2));
                        }
                        filterResults.count = list.size();
                        filterResults.values = list;

                        stopManagingCursor(cur);
                    }
                    return filterResults;
                }

                // For the unchecked cast of the filter results value.
                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        clear();
                        for (String value : (List<String>)results.values) {
                            add(value);
                        }
                        notifyDataSetChanged();
                    }
                }
            };
            return nameFilter;
        }
    }
}
