package com.markupartist.iglaset.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.SearchRecentSuggestions;
import android.widget.Toast;

import com.markupartist.iglaset.R;
import com.markupartist.iglaset.provider.AuthStore;

public class BasicPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    private static final String TAG = "BasicPreferenceActivity";
    private static final int DIALOG_CLEAR_SEARCH_HISTORY = 0;
    private static final int DIALOG_AUTH_FAILED = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals("preference_clear_search_history")) {
            showDialog(DIALOG_CLEAR_SEARCH_HISTORY);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(this);    
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
        case DIALOG_AUTH_FAILED:
            return new AlertDialog.Builder(this)
            .setTitle("Inloggning misslyckades")
            .setMessage("Kunde inte logga in, kontrollera användarnamn och lösenord.")
            .setPositiveButton("Ok", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ;
                }
            })
            .create();
        }

        return null;
    }

    /**
     * Update the token that is given after authentication.
     * @param token the token retrieved after authentication
     */
    private void updateAuthToken(String token) {        
        if (token == null) {
            showDialog(DIALOG_AUTH_FAILED);
        } else {
            Toast.makeText(BasicPreferenceActivity.this, "Inloggad", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Task that authenticates a user.
     */
    private class AuthUserTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            publishProgress();
            String token = new AuthStore().authenticateUser(params[0], params[1]);
            return token;
        }

        @Override
        public void onProgressUpdate(Void... values) {
        }

        @Override
        protected void onPostExecute(String result) {
            updateAuthToken(result);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        if (key.equals("preference_password")) {
            Toast.makeText(BasicPreferenceActivity.this, 
                    "Loggar in", Toast.LENGTH_SHORT).show();
            new AuthUserTask().execute(
                    sharedPreferences.getString("preference_username", ""), 
                    sharedPreferences.getString("preference_password", ""));
        } else if (key.equals("preference_username") 
                && sharedPreferences.contains("preference_password")) {
            Toast.makeText(BasicPreferenceActivity.this, 
                    "Loggar in", Toast.LENGTH_SHORT).show();
            new AuthUserTask().execute(
                    sharedPreferences.getString("preference_username", ""), 
                    sharedPreferences.getString("preference_password", ""));
        }
    }
}
