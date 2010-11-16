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
import android.view.KeyEvent;
import android.widget.Toast;

import com.markupartist.iglaset.R;
import com.markupartist.iglaset.provider.AuthUserTask;

public class BasicPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener, AuthUserTask.OnAuthorizeListener {
    private static final int DIALOG_CLEAR_SEARCH_HISTORY = 0;
    private static final int DIALOG_AUTH_FAILED = 1;
    private AuthUserTask mAuthUserTask;

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
    protected void onDestroy() {
    	cancelAuthUserTask();
        super.onDestroy();
    }

    private void cancelAuthUserTask() {
    	if(null != mAuthUserTask && mAuthUserTask.getStatus() == AsyncTask.Status.RUNNING) {
    		mAuthUserTask.cancel(true);
    	}
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
        case DIALOG_CLEAR_SEARCH_HISTORY:
            return new AlertDialog.Builder(this)
                .setTitle(R.string.clear_search_history_preference)
                .setMessage(R.string.clear_search_history_summary_preference)
                .setCancelable(true)
                .setPositiveButton(R.string.yes, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
                                BasicPreferenceActivity.this, 
                                SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
                        suggestions.clearHistory();
                        Toast.makeText(BasicPreferenceActivity.this, R.string.search_history_cleared, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.no, new OnClickListener() {                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();
        case DIALOG_AUTH_FAILED:
            return new AlertDialog.Builder(this)
            .setTitle(R.string.login_failed)
            .setMessage(R.string.login_failed_message)
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        if (key.equals("preference_password") || key.equals("preference_username")) {
        	if(sharedPreferences.contains("preference_password") &&
        	   sharedPreferences.contains("preference_username")) {
	            Toast.makeText(BasicPreferenceActivity.this, 
	                    R.string.logging_in, Toast.LENGTH_SHORT).show();
	            cancelAuthUserTask();
	            mAuthUserTask = new AuthUserTask(this);
	            mAuthUserTask.execute(this);
        	}
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

	@Override
	public void onAuthorizationFailed(Exception exception) {
		showDialog(DIALOG_AUTH_FAILED);
	}

	@Override
	public void onAuthorizationSuccessful() {
        Toast.makeText(BasicPreferenceActivity.this, 
                R.string.login_success, Toast.LENGTH_SHORT).show();
	}
}
