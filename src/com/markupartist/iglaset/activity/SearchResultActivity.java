package com.markupartist.iglaset.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
import com.markupartist.iglaset.activity.SearchDrinksTask.SearchDrinkCompletedListener;
import com.markupartist.iglaset.activity.SearchDrinksTask.SearchDrinkErrorListener;
import com.markupartist.iglaset.activity.SearchDrinksTask.SearchDrinkProgressUpdatedListener;
import com.markupartist.iglaset.provider.AuthStore;
import com.markupartist.iglaset.provider.AuthenticationException;
import com.markupartist.iglaset.provider.Drink;
import com.markupartist.iglaset.provider.RatingSearchCriteria;
import com.markupartist.iglaset.provider.RecommendationSearchCriteria;
import com.markupartist.iglaset.provider.SearchCriteria;
import com.markupartist.iglaset.provider.AuthStore.Authentication;
import com.markupartist.iglaset.util.ImageLoader;

public class SearchResultActivity extends ListActivity implements
        SearchDrinkCompletedListener, SearchDrinkProgressUpdatedListener, SearchDrinkErrorListener {
    //static final String ACTION_CATEGORY_SEARCH = "com.markupartist.iglaset.action.CATEGORY";
    //static final String ACTION_BARCODE_SEARCH = "com.markupartist.iglaset.action.BARCODE";
    /**
     * Action for triggering a search for user recommendations.
     */
    static final String ACTION_USER_RECOMMENDATIONS =
        "com.markupartist.iglaset.action.USER_RECOMMENDATIONS";
    /**
     * Actions for triggering a search for rated articles.
     */
    static final String ACTION_USER_RATINGS =
        "com.markupartist.iglaset.action.USER_RATINGS";

    static final String EXTRA_SEARCH_BARCODE =
        "com.markupartist.iglaset.search.barcode";
    static final String EXTRA_SEARCH_CATEGORY_ID =
        "com.markupartist.iglaset.search.categoryId";
    static final String EXTRA_CLICKED_DRINK =
        "com.markupartist.iglaset.search.clickedDrink";

    static final int DIALOG_SEARCH_NETWORK_PROBLEM = 0;
    static final int DIALOG_DRINK_IMAGE = 1;
    static final String TAG = "SearchResultActivity";
    private DrinkAdapter mListAdapter;
    private ArrayList<Drink> mDrinks;
    private String mToken;
    private static SearchCriteria sSearchCriteria;
    
    /**
     * Common click listener used for all the images in the list. Note that
     * each image will have to come with its corresponding drink attached
     * as setTag. This will then be extracted in mImageClickListener.onClick.
     */
    private View.OnClickListener mImageClickListener;
    
    /**
     * Placeholder for the drink clicked on in the list. This will be set
     * in mImageClickListener's onClick event and will later be used to
     * create the drink image viewer dialog.
     */
    private Drink mClickedDrink;
    
	/**
	 * Task object to be executed when searching.
	 */
	private SearchDrinksTask mSearchDrinksTask;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.search_result);

        Authentication auth = null;
        try {
            auth = AuthStore.getInstance().getAuthentication(this);
            mToken = auth.token;
        } catch (AuthenticationException e) {
            Log.d(TAG, "User not authenticated...");
        }

        //mToken = AuthStore.getInstance().getStoredToken(this);

        mImageClickListener = new View.OnClickListener() {
    		
    		@Override
    		public void onClick(View v) {
    			mClickedDrink = (Drink) v.getTag();
    			showDialog(DIALOG_DRINK_IMAGE);
    		}
    	};
        
        // Check if already have some data, used if screen is rotated.
        @SuppressWarnings("unchecked")
        final ArrayList<Drink> data = (ArrayList<Drink>) getLastNonConfigurationInstance();
        
        if (data == null) {
            final Intent queryIntent = getIntent();
            final String queryAction = queryIntent.getAction();
            final TextView searchText = (TextView) findViewById(R.id.search_progress_text);

            mSearchDrinksTask = new SearchDrinksTask();
            mSearchDrinksTask.setSearchDrinkCompletedListener(this);
            mSearchDrinksTask.setSearchDrinkErrorListener(this);

            Log.d(TAG, "action: " + queryAction);
            
            if (Intent.ACTION_SEARCH.equals(queryAction)) {
                final String queryString = queryIntent.getStringExtra(SearchManager.QUERY);
                // Record the query string in the recent queries suggestions provider.
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, 
                        SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
                suggestions.saveRecentQuery(queryString, null);

                String searchingText = searchText.getText() + " \"" + queryString + "\"";
                mSearchDrinksTask.setSearchDrinkProgressUpdatedListener(this);
                searchText.setText(searchingText);
                setTitle(searchingText);

                sSearchCriteria = new SearchCriteria();
                sSearchCriteria.setQuery(queryString);
                sSearchCriteria.setToken(mToken);

                mSearchDrinksTask.execute(sSearchCriteria);
            } else if (queryIntent.hasExtra(EXTRA_SEARCH_CATEGORY_ID)) {
                final int category = queryIntent.getExtras()
                    .getInt(EXTRA_SEARCH_CATEGORY_ID);

                setTitle(R.string.search_results);

                sSearchCriteria = new SearchCriteria();
                sSearchCriteria.setCategory(category);
                sSearchCriteria.setToken(mToken);

                mSearchDrinksTask.execute(sSearchCriteria);
            } else if (queryIntent.hasExtra(EXTRA_SEARCH_BARCODE)) {
                String barcode = queryIntent.getStringExtra(EXTRA_SEARCH_BARCODE);

                setTitle(R.string.search_results);

                sSearchCriteria = new SearchCriteria();
                sSearchCriteria.setBarcode(barcode);
                sSearchCriteria.setToken(mToken);

                mSearchDrinksTask.execute(sSearchCriteria);
            } else if (ACTION_USER_RECOMMENDATIONS.equals(queryAction)) {
                setTitle(R.string.recommendations_label);

                sSearchCriteria = new RecommendationSearchCriteria();
                ((RecommendationSearchCriteria) sSearchCriteria).setUserId(
                        auth.userId);
                sSearchCriteria.setToken(mToken);

                mSearchDrinksTask.execute(sSearchCriteria); 
            } else if (ACTION_USER_RATINGS.equals(queryAction)) {
                setTitle(R.string.rated_articles_label);

                sSearchCriteria = new RatingSearchCriteria();
                ((RatingSearchCriteria) sSearchCriteria).setUserId(
                        auth.userId);
                sSearchCriteria.setToken(mToken);

                mSearchDrinksTask.execute(sSearchCriteria); 
            }
        } else {
        	onDrinkData(data);
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
    	mSearchDrinksTask.cancel(true);
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(null != mClickedDrink) {
        	outState.putParcelable(EXTRA_CLICKED_DRINK, mClickedDrink);
        }
        //Log.d(TAG, "onSaveInstanceState");
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	mClickedDrink = savedInstanceState.getParcelable(EXTRA_CLICKED_DRINK);
    }

    private void initList(ArrayList<Drink> drinks) {
        mDrinks = drinks;

        if (drinks.isEmpty()) {
            TextView emptyResult = (TextView) findViewById(R.id.search_empty);
            emptyResult.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(sSearchCriteria.getQuery())) {
            setTitle(getText(R.string.search_results)
                    + " \"" + sSearchCriteria.getQuery() + "\"");
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
            case R.id.menu_home:
                startActivity(new Intent(this, StartActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSearchDrinkComplete(ArrayList<Drink> result) {
        setProgressBarIndeterminateVisibility(false);
        onDrinkData(result);
    }
    
    private void onDrinkData(ArrayList<Drink> drinks) {
    	if(drinks.size() == 1) {
    		// Open the drink details and close the activity
            Intent i = new Intent(this, DrinkDetailActivity.class);
            i.putExtra("com.markupartist.iglaset.Drink", drinks.get(0));
            startActivity(i);
            finish();
        } else {
        	initList(drinks);
        }
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
        	return DialogFactory.createNetworkProblemDialog(
        			this,
        			new OnClickListener() {
		                @Override
		                public void onClick(DialogInterface dialog, int which) {
		                	switch(which) {
		                	case Dialog.BUTTON_POSITIVE:
			                    SearchDrinksTask searchDrinksTask = new SearchDrinksTask();
			                    searchDrinksTask.setSearchDrinkCompletedListener(SearchResultActivity.this);
			                    searchDrinksTask.setSearchDrinkProgressUpdatedListener(SearchResultActivity.this);
			                    searchDrinksTask.setSearchDrinkErrorListener(SearchResultActivity.this);
			                    searchDrinksTask.execute(sSearchCriteria);
			                    break;
		                	case Dialog.BUTTON_NEGATIVE:
		                		finish();
		                		break;
		                	}
		                }
		             });
        
        case DIALOG_DRINK_IMAGE:
        	return new DrinkImageViewerDialog(this, mClickedDrink);
        }
        return null;
    }
    
    protected void onPrepareDialog(int id, Dialog dialog) {
    	switch(id) {
    	case DIALOG_DRINK_IMAGE:
    		DrinkImageViewerDialog imageDialog = (DrinkImageViewerDialog) dialog;
    		imageDialog.setDrink(mClickedDrink);
    		imageDialog.load();
    	}
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

            final Drink drink = getItem(position);
            if (drink != null && dvh != null) {
                String year = drink.getYear() == 0 ? "" : " " + String.valueOf(drink.getYear());
                dvh.getNameView().setText(drink.getName() + year);
                if(drink.hasUserRating()) {
                	dvh.getRateView().setRating(drink.getUserRating());
                	dvh.getNameView().setCompoundDrawables(null, null, dvh.getGlassIcon(getContext()), null);
                } else {
                	dvh.getRateView().setRating(Float.parseFloat(drink.getRating()));
                	dvh.getNameView().setCompoundDrawables(null, null, null, null);
                }
                dvh.getOriginCountryView().setText(drink.getConcatenatedOrigin());
                dvh.getAlcoholView().setText(drink.getAlcoholPercent());

                ImageLoader.getInstance().load(dvh.getImageView(), drink.getThumbnailUrl(), true, R.drawable.noimage, null);
                dvh.getImageView().setTag(drink);
                dvh.getImageView().setOnClickListener(mImageClickListener);
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

        public void onItemClick() {
            Log.d("onItemClick", "");
        }
    }
}
