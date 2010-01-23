package com.markupartist.iglaset.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.markupartist.iglaset.R;
import com.markupartist.iglaset.activity.SearchDrinksTask.SearchDrinkCompletedListener;
import com.markupartist.iglaset.activity.SearchDrinksTask.SearchDrinkErrorListener;
import com.markupartist.iglaset.activity.SearchDrinksTask.SearchDrinkProgressUpdatedListener;
import com.markupartist.iglaset.provider.AuthStore;
import com.markupartist.iglaset.provider.Drink;
import com.markupartist.iglaset.provider.SearchCriteria;
import com.markupartist.iglaset.util.ImageLoader;
import com.markupartist.iglaset.util.Tracker;

public class SearchResultActivity extends ListActivity implements
        SearchDrinkCompletedListener, SearchDrinkProgressUpdatedListener, SearchDrinkErrorListener {
    //static final String ACTION_CATEGORY_SEARCH = "com.markupartist.iglaset.action.CATEGORY";
    //static final String ACTION_BARCODE_SEARCH = "com.markupartist.iglaset.action.BARCODE";

    static final String EXTRA_SEARCH_BARCODE = "com.markupartist.iglaset.search.barcode";
    static final String EXTRA_SEARCH_CATEGORY_ID = "com.markupartist.iglaset.search.categoryId";
    static final int DIALOG_SEARCH_NETWORK_PROBLEM = 0;
    static final String TAG = "SearchResultActivity";
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

            SearchDrinksTask searchDrinksTask = new SearchDrinksTask();
            searchDrinksTask.setSearchDrinkCompletedListener(this);
            searchDrinksTask.setSearchDrinkProgressUpdatedListener(this);
            searchDrinksTask.setSearchDrinkErrorListener(this);

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

                searchDrinksTask.execute(sSearchCriteria);

                Tracker.getInstance().trackPageView("search result");
            } else if (queryIntent.hasExtra(EXTRA_SEARCH_CATEGORY_ID)) {
                final int category = queryIntent.getExtras()
                    .getInt(EXTRA_SEARCH_CATEGORY_ID);

                setTitle(R.string.search_results);

                sSearchCriteria = new SearchCriteria();
                sSearchCriteria.setCategory(category);
                sSearchCriteria.setToken(mToken);

                searchDrinksTask.execute(sSearchCriteria);

                Tracker.getInstance().trackPageView("search result category");
            } else if (queryIntent.hasExtra(EXTRA_SEARCH_BARCODE)) {
                String barcode = queryIntent.getStringExtra(EXTRA_SEARCH_BARCODE);

                setTitle(R.string.search_results);

                sSearchCriteria = new SearchCriteria();
                sSearchCriteria.setBarcode(barcode);
                sSearchCriteria.setToken(mToken);

                searchDrinksTask.execute(sSearchCriteria);

                Tracker.getInstance().trackPageView("search result barcode");
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
        //Log.d(TAG, "onRetainNonConfigurationInstance");
        return mDrinks;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO: See if we can get the previous search criteria from here.
        //Log.d(TAG, "onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Tracker.getInstance().stop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Log.d(TAG, "onSaveInstanceState");
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

    public void onSearchDrinkComplete(ArrayList<Drink> result) {
        setProgressBarIndeterminateVisibility(false);
        initList(result);
    }

    public void onSearchDrinkProgress() {
        setProgressBarIndeterminateVisibility(true);
    }

    @Override
    public void onSearchDrinkError(Exception exception) {
        setProgressBarIndeterminateVisibility(false);
        showDialog(DIALOG_SEARCH_NETWORK_PROBLEM);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
        case DIALOG_SEARCH_NETWORK_PROBLEM:
            return new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Ett fel inträffade")
                .setMessage("Kunde inte ansluta till servern. Försök igen, eller Cancel för att gå tillbaka till föregående vy.")
                .setPositiveButton("Försök igen", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SearchDrinksTask searchDrinksTask = new SearchDrinksTask();
                        searchDrinksTask.setSearchDrinkCompletedListener(SearchResultActivity.this);
                        searchDrinksTask.setSearchDrinkProgressUpdatedListener(SearchResultActivity.this);
                        searchDrinksTask.setSearchDrinkErrorListener(SearchResultActivity.this);
                        searchDrinksTask.execute(sSearchCriteria);
                    }
                })
                .setNegativeButton(getText(android.R.string.cancel), new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                    }
                })
                .create();
        }
        return null;
    }

    /**
     * List adapter for drinks. Deals with pagination internally.
     */
    class DrinkAdapter extends ArrayAdapter<Drink> implements SearchDrinkCompletedListener {
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
                Toast.makeText(SearchResultActivity.this, getText(R.string.loading_articles), Toast.LENGTH_LONG).show();
                sSearchCriteria.setPage(mPage.addAndGet(1));
                SearchDrinksTask searchDrinksTask = new SearchDrinksTask();
                searchDrinksTask.setSearchDrinkCompletedListener(this);
                searchDrinksTask.setSearchDrinkProgressUpdatedListener(SearchResultActivity.this);
                searchDrinksTask.setSearchDrinkErrorListener(SearchResultActivity.this);
                searchDrinksTask.execute(sSearchCriteria);
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
                dvh.getOriginCountryView().setText(drink.getConcatenatedOrigin());
                dvh.getAlcoholView().setText(drink.getAlcoholPercent());

                ImageLoader.getInstance().load(dvh.getImageView(), 
                        drink.getImageUrl(), true, R.drawable.noimage);
                
                ImageView image = dvh.getHasRatedImageView();
                if(drink.hasUserRating()) {
                	image.setVisibility(View.VISIBLE);
                	dvh.getRateView().setRating(drink.getUserRating());
                }
                else {
                	image.setVisibility(View.GONE);
                	dvh.getRateView().setRating(Float.parseFloat(drink.getRating()));
                }
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
                        getText(R.string.no_more_articles), Toast.LENGTH_SHORT).show();
                return mShouldAppend.get();
            }

            for (Drink drink : drinks) {
                add(drink);
            }

            return mShouldAppend.get();
        }

        public void onSearchDrinkComplete(ArrayList<Drink> result) {
            append(result);
            setProgressBarIndeterminateVisibility(false);
        }
    }
}