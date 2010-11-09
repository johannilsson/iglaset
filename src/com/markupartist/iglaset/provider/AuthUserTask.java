package com.markupartist.iglaset.provider;

import java.io.IOException;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Task that authenticates a user.
 */
public class AuthUserTask extends AsyncTask<Context, Void, Boolean> {
    public interface OnAuthorizeListener {
    	public void onAuthorizationFailed(Exception exception);
    	public void onAuthorizationSuccessful();
    }
    
	private Exception mException;    
	private OnAuthorizeListener mListener;
	
    public AuthUserTask(OnAuthorizeListener listener) {
    	this.mListener = listener;
    }
    
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
    	if(null != mListener) {
	        if (mException != null) {
	            mListener.onAuthorizationFailed(mException);
	        } else {
	        	mListener.onAuthorizationSuccessful();
	        }
    	}
    }
}