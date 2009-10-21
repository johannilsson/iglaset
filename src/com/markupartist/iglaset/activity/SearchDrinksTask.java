package com.markupartist.iglaset.activity;

import java.util.ArrayList;

import android.os.AsyncTask;

import com.markupartist.iglaset.provider.Drink;
import com.markupartist.iglaset.provider.DrinksStore;
import com.markupartist.iglaset.provider.SearchCriteria;

/**
 * Background task to search for drinks.
 */
public class SearchDrinksTask extends AsyncTask<SearchCriteria, Void, ArrayList<Drink>> {

    private SearchDrinkProgressUpdatedListener mSearchDrinkProgressUpdatedListener;
    private SearchDrinkCompletedListener mSearchDrinkCompletedListener;

    @Override
    protected ArrayList<Drink> doInBackground(SearchCriteria... params) {
        publishProgress();
        DrinksStore drinksStore = DrinksStore.getInstance();
        return drinksStore.searchDrinks(params[0]);
    }

    @Override
    public void onProgressUpdate(Void... values) {
        if (mSearchDrinkProgressUpdatedListener != null) {
            mSearchDrinkProgressUpdatedListener.onSearchDrinkProgress();
        }
    }

    @Override
    protected void onPostExecute(ArrayList<Drink> result) {
        mSearchDrinkCompletedListener.onSearchDrinkComplete(result);
    }

    /**
     * Set a listener for progress updated.
     * @param listener the listener to set
     */
    public void setSearchDrinkProgressUpdatedListener(
            SearchDrinkProgressUpdatedListener listener) {
        mSearchDrinkProgressUpdatedListener = listener;
    }

    /**
     * Set a listener for search completed.
     * @param listener the listener to set
     */
    public void setSearchDrinkCompletedListener(
            SearchDrinkCompletedListener listener) {
        mSearchDrinkCompletedListener = listener;
    }
    /**
     * Listener for search progress updated.
     */
    public interface SearchDrinkProgressUpdatedListener {
        /**
         * Called when search progress is made.
         */
        public void onSearchDrinkProgress();
    }

    /**
     * Listener for search completed.
     */
    public interface SearchDrinkCompletedListener {
        /**
         * Called when search is completed.
         * @param result the search result
         */
        public void onSearchDrinkComplete(ArrayList<Drink> result);
    }

}