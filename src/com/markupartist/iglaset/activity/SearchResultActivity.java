package com.markupartist.iglaset.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.SimpleAdapter.ViewBinder;

import com.markupartist.iglaset.R;
import com.markupartist.iglaset.provider.Drink;
import com.markupartist.iglaset.provider.DrinksStore;

public class SearchResultActivity extends ListActivity {
    static String TAG = "SearchResultActivity";
    DrinksStore drinksStore = new DrinksStore();
    private SimpleAdapter mListAdapter;
    private ArrayList<Drink> mDrinks;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.search_result);

        // Check if already have some data, used if screen is rotated.
        @SuppressWarnings("unchecked")
        final ArrayList<Drink> data = (ArrayList<Drink>) getLastNonConfigurationInstance();
        if (data == null) {
            final Intent queryIntent = getIntent();
            final String queryAction = queryIntent.getAction();
            if (Intent.ACTION_SEARCH.equals(queryAction)) {
                final String queryString = queryIntent.getStringExtra(SearchManager.QUERY);
                // Record the query string in the recent queries suggestions provider.
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, 
                        SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
                suggestions.saveRecentQuery(queryString, null);

                TextView searchText = (TextView) findViewById(R.id.search_progress_text);
                searchText.setText(searchText.getText() + " '" + queryString + "'");

                new SearchDrinksTask().execute(queryString);
            } else {
                Log.d(TAG, "no ACTION_SEARCH intent");
            }            
        } else {
            updateList(data);
        }
    }

    /**
     * Called before this activity is destroyed, returns the previous search 
     * result. This list is used if the screen is rotated. Then we don't need
     * to search for it again.
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        return mDrinks;
    }

    private void updateList(ArrayList<Drink> drinks) {
        mDrinks = drinks;

        if (drinks.isEmpty()) {
            TextView emptyResult = (TextView) findViewById(R.id.search_empty);
            emptyResult.setVisibility(View.VISIBLE);
        }

        LinearLayout progressBar = (LinearLayout) findViewById(R.id.search_progress);
        progressBar.setVisibility(View.GONE);

        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        for (Drink drink : drinks) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("name", drink.getName());
            map.put("origin", drink.getOrigin());
            map.put("country", drink.getOriginCountry());
            map.put("rating", drink.getRating());
            map.put("year", String.valueOf(drink.getYear()));
            map.put("alcoholPercent", drink.getAlcoholPercent());
            map.put("image_url", drink.loadImage());
            list.add(map);
        }

        mListAdapter = new SimpleAdapter(this, list, 
                R.layout.drink_detail,
                new String[] { "name", "origin", "country", "rating", "year", "alcoholPercent", "image_url" },
                new int[] { 
                    R.id.drink_name,
                    R.id.drink_origin, 
                    R.id.drink_origin_country, 
                    R.id.drink_rating,
                    R.id.drink_year,
                    R.id.drink_alcohol_percent,
                    R.id.drink_image
                }
        );

        mListAdapter.setViewBinder(new ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data,
                    String textRepresentation) {
                switch (view.getId()) {
                case R.id.drink_name:
                    TextView nameView = (TextView) view;
                    nameView.setText(textRepresentation);
                    return true;
                case R.id.drink_year:
                    TextView yearView = (TextView) view;
                    textRepresentation = textRepresentation.equals("0") ? "" : textRepresentation; 
                    yearView.setText(textRepresentation);
                    return true;
                case R.id.drink_origin:
                    TextView originView = (TextView) view;
                    originView.setText(textRepresentation);
                    return true;
                case R.id.drink_origin_country:
                    TextView originCountryView = (TextView) view;
                    originCountryView.setText(textRepresentation);
                    return true;
                case R.id.drink_rating:
                    RatingBar rateView = (RatingBar) view;
                    rateView.setRating(Float.parseFloat(textRepresentation));
                    return true;
                case R.id.drink_alcohol_percent:
                    TextView alcoholView = (TextView) view;
                    alcoholView.setText(textRepresentation);
                    return true;
                case R.id.drink_image:
                    ImageView imageView = (ImageView) view;
                    imageView.setImageBitmap((Bitmap) data);
                    return true;
                }
                return false;
            }
        });

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

    private class SearchDrinksTask extends AsyncTask<String, Void, ArrayList<Drink>> {

        @Override
        protected ArrayList<Drink> doInBackground(String... params) {
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
            updateList(result);
        }
    }
}