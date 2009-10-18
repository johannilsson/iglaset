package com.markupartist.iglaset.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.markupartist.iglaset.R;
import com.markupartist.iglaset.provider.AuthStore;
import com.markupartist.iglaset.provider.Drink;
import com.markupartist.iglaset.provider.DrinksStore;
import com.markupartist.iglaset.provider.SearchCriteria;
import com.markupartist.iglaset.util.ImageLoader;

public class SearchResultActivity extends ListActivity {
    static String TAG = "SearchResultActivity";
    DrinksStore drinksStore = new DrinksStore();
    private DrinkAdapter mListAdapter;
    private ArrayList<Drink> mDrinks;
    private String mToken;
    private static SearchCriteria sSearchCriteria;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.search_result);

        mToken = AuthStore.getInstance().getStoredToken(this);

        // Check if already have some data, used if screen is rotated.
        @SuppressWarnings("unchecked")
        final ArrayList<Drink> data = (ArrayList<Drink>) getLastNonConfigurationInstance();

        if (data == null) {
            final Intent queryIntent = getIntent();
            final String queryAction = queryIntent.getAction();
            final TextView searchText = (TextView) findViewById(R.id.search_progress_text);
            if (Intent.ACTION_SEARCH.equals(queryAction)) {
                final String queryString = queryIntent.getStringExtra(SearchManager.QUERY);
                // Record the query string in the recent queries suggestions provider.
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, 
                        SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
                suggestions.saveRecentQuery(queryString, null);

                String searchingText = searchText.getText() + " \"" + queryString + "\"";
                searchText.setText(searchingText);
                setTitle(searchingText);

                sSearchCriteria = new SearchCriteria();
                sSearchCriteria.setQuery(queryString);
                sSearchCriteria.setToken(mToken);
                new SearchDrinksTask().execute(sSearchCriteria);
            } else if (queryIntent.hasExtra("com.markupartist.sthlmtraveling.searchCategory")) {
                final int category = queryIntent.getExtras()
                    .getInt("com.markupartist.sthlmtraveling.searchCategory");
                setTitle(searchText.getText());
                sSearchCriteria = new SearchCriteria();
                sSearchCriteria.setCategory(category);
                sSearchCriteria.setToken(mToken);
                new SearchDrinksTask().execute(sSearchCriteria);
            }            
        } else {
            initList(data);
        }
    }

    /**
     * Called before this activity is destroyed, returns the previous search 
     * result. This list is used if the screen is rotated. Then we don't need
     * to search for it again.
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        Log.d(TAG, "onRetainNonConfigurationInstance");
        return mDrinks;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO: See if we can get the previous search criteria from here.
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
    }

    private void initList(ArrayList<Drink> drinks) {
        mDrinks = drinks;

        if (drinks.isEmpty()) {
            TextView emptyResult = (TextView) findViewById(R.id.search_empty);
            emptyResult.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(sSearchCriteria.getQuery())) {
            setTitle(getText(R.string.search_results) + " \"" + sSearchCriteria.getQuery() + "\"");
        } else {
            setTitle(getText(R.string.search_results));
        }

        LinearLayout progressBar = (LinearLayout) findViewById(R.id.search_progress);
        progressBar.setVisibility(View.GONE);

        mListAdapter = new DrinkAdapter(this, drinks);

        setListAdapter(mListAdapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Drink drink = mDrinks.get(position);

        Intent i = new Intent(this, DrinkDetailActivity.class);
        i.putExtra("com.markupartist.iglaset.Drink", drink);

        startActivity(i);
    }

    @Override
    public boolean onSearchRequested() {
        startSearch(null, false, null, false);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu_search_result, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                onSearchRequested();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Background task to search for drinks.
     */
    private class SearchDrinksTask extends AsyncTask<SearchCriteria, Void, ArrayList<Drink>> {

        @Override
        protected ArrayList<Drink> doInBackground(SearchCriteria... params) {
            publishProgress();
            return drinksStore.searchDrinks(params[0]);
        }

        @Override
        public void onProgressUpdate(Void... values) {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected void onPostExecute(ArrayList<Drink> result) {
            setProgressBarIndeterminateVisibility(false);
            initList(result);
        }
    }

    /**
     * List adapter for drinks. Deals with pagination internally.
     */
    class DrinkAdapter extends ArrayAdapter<Drink> {
        private AtomicBoolean mShouldAppend = new AtomicBoolean(true);
        private AtomicInteger mPage = new AtomicInteger(1);
        private int mMaxResult = 100;

        public DrinkAdapter(Context context, List<Drink> objects) {
            super(context, R.layout.drink_detail, objects);
        }

        private boolean shouldAppend(int position) {
            return (position == super.getCount() - 1 
                    && mShouldAppend.get()
                    && super.getCount() >= 10
                    && super.getCount() <= mMaxResult);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            DrinkViewHolder dvh = null;
            if (shouldAppend(position)) {
                Toast.makeText(SearchResultActivity.this, "Hämtar", Toast.LENGTH_LONG).show();
                mPage.addAndGet(1);
                new AppendTask().execute();
            }

            if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.drink_detail, null);
                    dvh = new DrinkViewHolder(convertView);
                    convertView.setTag(dvh);
            } else {
                dvh = (DrinkViewHolder) convertView.getTag();    
            }

            Drink drink = getItem(position);
            if (drink != null && dvh != null) {
                String year = drink.getYear() == 0 ? "" : " " + String.valueOf(drink.getYear());
                dvh.getNameView().setText(drink.getName() + year);
                //dvh.getYearView().setText(drink.getYear() == 0 ? "" : String.valueOf(drink.getYear()));
                dvh.getOriginView().setText(drink.getOrigin());
                dvh.getOriginCountryView().setText(drink.getOriginCountry());
                dvh.getRateView().setRating(Float.parseFloat(drink.getRating()));
                dvh.getAlcoholView().setText(drink.getAlcoholPercent());

                ImageLoader.getInstance().load(dvh.getImageView(), 
                        drink.getImageUrl(), true, R.drawable.noimage);
            }

            return convertView;
        }

        /**
         * Append drinks to the list.
         * @param drinks drinks to add to the list
         * @return if we should append more or not
         */
        protected boolean append(ArrayList<Drink> drinks) {
            if (drinks.isEmpty()) {
                mShouldAppend.set(false);
                Toast.makeText(SearchResultActivity.this, 
                        "Inga fler artiklar", Toast.LENGTH_SHORT).show();
                return mShouldAppend.get();
            }

            for (Drink drink : drinks) {
                add(drink);
            }

            return mShouldAppend.get();
        }

        /**
         * A background task that will be run when there is a need to append more
         * data.
         */
        class AppendTask extends AsyncTask<Void, Void, ArrayList<Drink>> {
            @Override
            protected ArrayList<Drink> doInBackground(Void... params) {
                publishProgress();
                sSearchCriteria.setPage(mPage.get());
                return drinksStore.searchDrinks(sSearchCriteria);
            }

            @Override
            public void onProgressUpdate(Void... values) {
                setProgressBarIndeterminateVisibility(true);
            }

            @Override
            protected void onPostExecute(ArrayList<Drink> drinks) {
                append(drinks);
                setProgressBarIndeterminateVisibility(false);
            }
        }
    }
}