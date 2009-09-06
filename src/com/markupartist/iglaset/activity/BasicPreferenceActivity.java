package com.markupartist.iglaset.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.SearchRecentSuggestions;
import android.widget.Toast;

import com.markupartist.iglaset.R;

public class BasicPreferenceActivity extends PreferenceActivity {
    private static final int DIALOG_CLEAR_SEARCH_HISTORY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        Preference clearCache = (Preference) findPreference("clear_search_history_preference");
        clearCache.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showDialog(DIALOG_CLEAR_SEARCH_HISTORY);
                return true;
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
        case DIALOG_CLEAR_SEARCH_HISTORY:
            return new AlertDialog.Builder(this)
                .setTitle(R.string.clear_search_history_preference)
                .setMessage(getText(R.string.clear_search_history_summary_preference))
                .setCancelable(true)
                .setPositiveButton(getText(R.string.yes), new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
                                BasicPreferenceActivity.this, 
                                SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
                        suggestions.clearHistory();
                        Toast.makeText(BasicPreferenceActivity.this, getText(R.string.search_history_cleared), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getText(R.string.no), new OnClickListener() {                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();
        }
        return null;
    }
}
