package com.markupartist.iglaset.activity;

import java.io.IOException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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
import android.view.KeyEvent;
import android.widget.Toast;

import com.markupartist.iglaset.R;
import com.markupartist.iglaset.provider.AuthStore;
import com.markupartist.iglaset.provider.AuthenticationException;
import com.markupartist.iglaset.util.Tracker;

public class BasicPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    private static final String TAG = "BasicPreferenceActivity";
    private static final int DIALOG_CLEAR_SEARCH_HISTORY = 0;
    private static final int DIALOG_AUTH_FAILED = 1;
    private AuthUserTask mAuthUserTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Tracker.getInstance().trackPageView("preferences");

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
    protected void onDestroy() {
        super.onDestroy();
        if (mAuthUserTask != null) {
            mAuthUserTask.cancel(true);
        }

        //Tracker.getInstance().stop();
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
            .setTitle(getText(R.string.login_failed))
            .setMessage(getText(R.string.login_failed_message))
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
    private void userAuthenticated() {        
        Toast.makeText(BasicPreferenceActivity.this, 
                getText(R.string.login_success), Toast.LENGTH_SHORT).show();
    }

    /**
     * Update the token that is given after authentication.
     * @param token the token retrieved after authentication
     */
    private void userAuthenticationFailed(Exception e) {        
        showDialog(DIALOG_AUTH_FAILED);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        if (key.equals("preference_password")) {
            Toast.makeText(BasicPreferenceActivity.this, 
                    "Loggar in", Toast.LENGTH_SHORT).show();
            mAuthUserTask = new AuthUserTask();
            mAuthUserTask.execute(this);
        } else if (key.equals("preference_username") 
                && sharedPreferences.contains("preference_password")) {
            Toast.makeText(BasicPreferenceActivity.this, 
                    "Loggar in", Toast.LENGTH_SHORT).show();
            mAuthUserTask = new AuthUserTask();
            mAuthUserTask.execute(this);
        }
    }

    /**
     * Called when a key is pressed. Overridden to catch if the back key was
     * pressed. Then setResult is called to allow activities to call this 
     * activity with startActivityForResult.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            setResult(RESULT_OK);
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }           

    /**
     * Task that authenticates a user.
     */
    private class AuthUserTask extends AsyncTask<Context, Void, Boolean> {
        private Exception mException;

        @Override
        protected Boolean doInBackground(Context... params) {
            publishProgress();

            try {
                AuthStore.getInstance().authenticateUser(params[0]);
            } catch (AuthenticationException e) {
                mException = e;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                mException = e;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (mException != null) {
                userAuthenticationFailed(mException);
            } else {
                userAuthenticated();
            }
        }
    }
}
