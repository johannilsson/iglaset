package com.markupartist.iglaset.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.IntentFilter;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.IntentAction;
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
import com.markupartist.iglaset.provider.TextSearchCriteria;
import com.markupartist.iglaset.widget.SearchAction;
import com.markupartist.iglaset.IglasetApplication;

public class SearchResultActivity extends ListActivity implements
        SearchDrinkCompletedListener, SearchDrinkProgressUpdatedListener, SearchDrinkErrorListener {

	/**
	 * Class used to store data between orientation switches.
	 */
	private static class ConfigurationData {
		ArrayList<Drink> drinks;
		SearchCriteria searchCriteria;
	}
	
	/**
	 * Log tag.
	 */
	static final String TAG = SearchResultActivity.class.getSimpleName();
	
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
    static final String EXTRA_SEARCH_TAGS =
    	"com.markupartist.iglaset.search.tags";
    static final String EXTRA_SEARCH_TAGS_SELECTED =
        "com.markupartist.iglaset.search.tagsString";
    static final String EXTRA_CLICKED_DRINK =
        "com.markupartist.iglaset.search.clickedDrink";

    static final int DIALOG_SEARCH_NETWORK_PROBLEM = 0;
    static final int DIALOG_DRINK_IMAGE = 1;  
    static final int DIALOG_SELECT_SORTING = 2;
    private DrinkAdapter mListAdapter;
    private ArrayList<Drink> mDrinks;
    private AuthStore.Authentication mAuthentication;
    private SearchCriteria mSearchCriteria;
    
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

	/**
	 * Footer progress.
	 */
	private View mFooterProgressView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.search_result);

        ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        actionBar.setTitle(R.string.search_results);
        actionBar.setHomeAction(new IntentAction(this, StartActivity.createIntent(this), R.drawable.ic_actionbar_home_default));
        actionBar.addAction(new SearchAction() {
            @Override
            public void performAction(View view) {
                onSearchRequested();
            }
        });
        
        try {
            mAuthentication = AuthStore.getInstance().getAuthentication(this);
        } catch (AuthenticationException e) {
            Log.e(TAG, "User not authenticated...");
            mAuthentication = null;
        }

        mImageClickListener = new View.OnClickListener() {
    		
    		@Override
    		public void onClick(View v) {
    			mClickedDrink = (Drink) v.getTag();
    			showDialog(DIALOG_DRINK_IMAGE);
    		}
    	};

        // Check if already have some data, used if screen is rotated.
        ConfigurationData data = (ConfigurationData) getLastNonConfigurationInstance();
        if(data != null) {
        	mDrinks = data.drinks;
        	mSearchCriteria = data.searchCriteria;
        }      

        if(mDrinks != null) {
        	onDrinkData(mDrinks);
        } else if(mSearchCriteria != null) {
            setProgressBarIndeterminateVisibility(true);
        	createSearchDrinksTask().execute(mSearchCriteria);
        } else {
            final Intent queryIntent = getIntent();
            final String queryAction = queryIntent.getAction();

            // Search actions
            if (Intent.ACTION_SEARCH.equals(queryAction)) {
                final String queryString = queryIntent.getStringExtra(SearchManager.QUERY);
                // Record the query string in the recent queries suggestions provider.
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, 
                        SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
                suggestions.saveRecentQuery(queryString, null);

                final TextView searchText = (TextView) findViewById(R.id.search_progress_text);
                String searchingText = searchText.getText() + " \"" + queryString + "\"";
                searchText.setText(searchingText);
                actionBar.setTitle(searchingText);

                mSearchCriteria = new TextSearchCriteria();
                mSearchCriteria.setQuery(queryString);
                
            } else if (ACTION_USER_RECOMMENDATIONS.equals(queryAction)) {
                actionBar.setTitle(R.string.recommendations_label);

                mSearchCriteria = new RecommendationSearchCriteria();
                ((RecommendationSearchCriteria) mSearchCriteria).setUserId(
                        mAuthentication.v2.userId);
            } else if (ACTION_USER_RATINGS.equals(queryAction)) {
                actionBar.setTitle(R.string.rated_articles_label);

                mSearchCriteria = new RatingSearchCriteria();
                ((RatingSearchCriteria) mSearchCriteria).setUserId(mAuthentication.v2.userId);
            } else {
            	Log.d(TAG, "Unknown search criteria. Falling back to empty text.");
            	mSearchCriteria = new TextSearchCriteria();
            }
            
            // Search parameters
            if (queryIntent.hasExtra(EXTRA_SEARCH_CATEGORY_ID)) {
                final int category = queryIntent.getExtras()
                    .getInt(EXTRA_SEARCH_CATEGORY_ID);

                actionBar.setTitle(R.string.search_results);
                mSearchCriteria.setCategory(category);
            }
            
            if (queryIntent.hasExtra(EXTRA_SEARCH_BARCODE)) {
                String barcode = queryIntent.getStringExtra(EXTRA_SEARCH_BARCODE);

                actionBar.setTitle(R.string.search_results);
                mSearchCriteria.setBarcode(barcode);
            }
            
            if (queryIntent.hasExtra(EXTRA_SEARCH_TAGS)) {
                actionBar.setTitle(R.string.search_results);
                ArrayList<Integer> tags = queryIntent.getIntegerArrayListExtra(EXTRA_SEARCH_TAGS);

                TextView selectedTags = (TextView) findViewById(R.id.tagSearchSelectedText);
                selectedTags.setText(queryIntent.getStringExtra(EXTRA_SEARCH_TAGS_SELECTED));
                selectedTags.setVisibility(View.VISIBLE);

                mSearchCriteria.setTags(tags);
            }
            
            if(mSearchCriteria.supportsSorting()) {
            	int mode = getApp().getSearchSortMode(
            			mSearchCriteria.getClass(),
            			mSearchCriteria.getDefaultSortMode());
            	mSearchCriteria.setSortMode(mode);
            }
            
            mSearchCriteria.setAuthentication(mAuthentication);
            setProgressBarIndeterminateVisibility(true);
            createSearchDrinksTask().execute(mSearchCriteria);
        }
        
        this.registerReceiver(mBroadcastReceiver, new IntentFilter(Intents.ACTION_PUBLISH_DRINK));
    }

    private IglasetApplication getApp() {
    	return (IglasetApplication) getApplication();
    }
    
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			// Publish an updated drink in the UI.
			if(intent.getAction().equals(Intents.ACTION_PUBLISH_DRINK)) {
				Bundle extras = intent.getExtras();
				if(extras != null && extras.containsKey(Intents.EXTRA_DRINK)) {
					final Drink drink = (Drink) intent.getExtras().get(Intents.EXTRA_DRINK);
					onUpdatedDrink(drink);
				} else {
					Log.e(TAG, "No drink data available");
				}	
			}
		}
    };
    
    private SearchDrinksTask createSearchDrinksTask() {
    	if(null != mSearchDrinksTask && mSearchDrinksTask.getStatus() == AsyncTask.Status.RUNNING) {
    		mSearchDrinksTask.cancel(true);
    	}
    	
		mSearchDrinksTask = new SearchDrinksTask();
		mSearchDrinksTask.setSearchDrinkCompletedListener(this);
		mSearchDrinksTask.setSearchDrinkProgressUpdatedListener(this);
		mSearchDrinksTask.setSearchDrinkErrorListener(this);
		return mSearchDrinksTask;
    }

    public void onUpdatedDrink(Drink updatedDrink) {
		// The incoming drink is most likely pointing to one of the drinks in
		// our list but we can't be sure of that. Brute force find it.
		for(Drink drink : mDrinks) {
			if(drink.getId() == updatedDrink.getId()) {
				drink.setUserRating(updatedDrink.getUserRating());
				drink.setRatingCount(updatedDrink.getRatingCount());
				drink.setCommentCount(updatedDrink.getCommentCount());
				
				DrinkAdapter adapter = (DrinkAdapter) getListAdapter();
				adapter.notifyDataSetChanged();
				break;
			}
		}
    }
    
    /**
     * Called before this activity is destroyed, returns the previous search 
     * result. This list is used if the screen is rotated. Then we don't need
     * to search for it again.
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
    	ConfigurationData data = new ConfigurationData();
    	data.drinks = mDrinks;
    	data.searchCriteria = mSearchCriteria;
    	return data;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO: See if we can get the previous search criteria from here.
        //Log.d(TAG, "onResume");
    }

    @Override
    protected void onDestroy() {
    	if(null != mSearchDrinksTask && mSearchDrinksTask.getStatus() == AsyncTask.Status.RUNNING) {
    		mSearchDrinksTask.cancel(true);
    	}
    	
    	unregisterReceiver(mBroadcastReceiver);
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

        mFooterProgressView = getLayoutInflater().inflate(R.layout.loading_row, null);
        getListView().addFooterView(mFooterProgressView, null, false);

        mListAdapter = new DrinkAdapter(this, drinks);
        setListAdapter(mListAdapter);

        if (drinks.isEmpty()) {
        	View emptyLayout = findViewById(R.id.search_empty_layout);
        	emptyLayout.setVisibility(View.VISIBLE);
            
            // Show a more verbose message if the user was browsing
            // the recommendations
            if(mSearchCriteria instanceof RecommendationSearchCriteria) {
            	TextView emptyText = (TextView) findViewById(R.id.search_empty);
            	emptyText.setText(R.string.no_recommendations_result);
            }
            
            if(mSearchCriteria.hasBarcode()) {
            	// Store the current orphan barcode so others can use it if necessary.
            	((IglasetApplication) getApplication()).storeOrphanBarcode(mSearchCriteria.getBarcode());
            }

            Button searchButton = (Button) findViewById(R.id.btn_search);
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSearchRequested();
                }
            });
        }

        if (!TextUtils.isEmpty(mSearchCriteria.getQuery())) {
            setTitle(getText(R.string.search_results)
                    + " \"" + mSearchCriteria.getQuery() + "\"");
        }

        LinearLayout progressBar = (LinearLayout) findViewById(R.id.search_progress);
        progressBar.setVisibility(View.GONE);
        getListView().removeFooterView(mFooterProgressView);
    }

    private void displayDrinkDetails(Drink drink) {        
        Intent i = new Intent(this, DrinkDetailActivity.class);
        i.putExtra(Intents.EXTRA_DRINK, drink);
        startActivity(i);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        displayDrinkDetails(mDrinks.get(position));
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
        
        if(mSearchCriteria.supportsSorting() == false) {
        	menu.removeItem(R.id.menu_sort);
        }
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_sort:
        	showDialog(DIALOG_SELECT_SORTING);
        	return true;
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
        if (mFooterProgressView != null) {
            getListView().removeFooterView(mFooterProgressView);
        }
        onDrinkData(result);
    }
    
    private void onDrinkData(ArrayList<Drink> drinks) {
        if(drinks.size() == 1) {
    		// Open the drink details and close the activity
    		displayDrinkDetails(drinks.get(0));
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
		                		setProgressBarIndeterminateVisibility(true);
		                		createSearchDrinksTask().execute(mSearchCriteria);
			                    break;
		                	case Dialog.BUTTON_NEGATIVE:
		                		finish();
		                		break;
		                	default:
		                		break;
		                	}
		                }
		             });
        
        case DIALOG_DRINK_IMAGE:
        	return new DrinkImageViewerDialog(this, mClickedDrink);
        	
        case DIALOG_SELECT_SORTING:
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setTitle("Sortering");
        	builder.setSingleChoiceItems(
        			mSearchCriteria.getSortModeNames(this),
        			mSearchCriteria.getSortIndexFromMode(mSearchCriteria.getSortMode()),
        			new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int item) {					
					int sortMode = mSearchCriteria.getSortModeFromIndex(item);
					mSearchCriteria.setSortMode(sortMode);
					mSearchCriteria.setPage(1);
					getApp().storeSearchSortMode(mSearchCriteria.getClass(), item);
					setProgressBarIndeterminateVisibility(true);
		            createSearchDrinksTask().execute(mSearchCriteria);
		            removeDialog(DIALOG_SELECT_SORTING);
				}
			});
        	return builder.create();
        default:
        	return null;
        }
    }
    
    protected void onPrepareDialog(int id, Dialog dialog) {
    	switch(id) {
    	case DIALOG_DRINK_IMAGE:
    		DrinkImageViewerDialog imageDialog = (DrinkImageViewerDialog) dialog;
    		imageDialog.setDrink(mClickedDrink);
    		imageDialog.load();
    		break;
    	default:
    		break;
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
                getListView().addFooterView(mFooterProgressView);
                getListView().forceLayout();
                mSearchCriteria.setPage(mPage.addAndGet(1));
                SearchDrinksTask task = createSearchDrinksTask();
                task.setSearchDrinkCompletedListener(this);
                setProgressBarIndeterminateVisibility(true);
                task.execute(mSearchCriteria);
            }

            if (convertView == null) {
	            convertView = getLayoutInflater().inflate(R.layout.drink_detail, null);
	            dvh = new DrinkViewHolder(convertView);
	            convertView.setTag(dvh);
            } else {
                dvh = (DrinkViewHolder) convertView.getTag(); 

                // Change to the default image or else the thumbnail will show
                // an old image used in the reused view. The user won't notice it.
                dvh.getImageView().setImageResource(R.drawable.noimage);
            }

            final Drink drink = getItem(position);
            if (drink != null && dvh != null) {
            	dvh.populate(getContext(), drink, mImageClickListener);
                if(drink.hasUserRating()) {
                	dvh.getRateView().setRating(drink.getUserRating());
                } else {
                	float rating = (drink.hasEstimatedRating() ? drink.getEstimatedRating() : drink.getAverageRating());
                	dvh.getRateView().setRating(rating);
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
            if (mFooterProgressView != null) {
                getListView().removeFooterView(mFooterProgressView);
            }
        }

        public void onItemClick() {
            Log.d("onItemClick", "");
        }
    }
}
