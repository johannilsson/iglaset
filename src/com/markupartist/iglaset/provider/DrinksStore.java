package com.markupartist.iglaset.provider;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import android.text.TextUtils;
import android.util.Log;

import com.markupartist.iglaset.util.HttpManager;

public class DrinksStore {
    private static DrinksStore mInstance;
    private static String TAG = "DrinksStore";
    private static String ARTICLES_BASE_URI = "http://api.iglaset.se/api/articles/xml/";
    private static String RATE_BASE_URI = "http://api.iglaset.se/api/rate/";

    private DrinksStore() {
    }

    public static DrinksStore getInstance() {
        if (mInstance == null) {
            mInstance = new DrinksStore();
        }
        return mInstance;
    }

    public ArrayList<Drink> searchDrinks(SearchCriteria searchCriteria) {
        final ArrayList<Drink> drinks = new ArrayList<Drink>();

        final HttpGet get = new HttpGet(buildSearchUri(searchCriteria));
        HttpEntity entity = null;

        try {
            final HttpResponse response = HttpManager.execute(get);
            entity = response.getEntity();
            DrinksParser drinksParser = new DrinksParser();
            drinksParser.parseDrinks(entity.getContent(), drinks);
        } catch (IOException e) {
            Log.d(TAG, "Failed to read data: " + e.getMessage());
        }

        return drinks;        
    }

    public Drink getDrink(int id) {
        return getDrink(id, null);
    }

    public Drink getDrink(int id, String token) {
        String searchUri = ARTICLES_BASE_URI + id;
        if (!TextUtils.isEmpty(token)) {
            searchUri += "/?token=" + token;
        }

        final HttpGet get = new HttpGet(searchUri);
        final ArrayList<Drink> drinks = new ArrayList<Drink>();

        try {
            final HttpResponse response = HttpManager.execute(get);
            HttpEntity entity = response.getEntity();
            DrinksParser drinksParser = new DrinksParser();
            drinksParser.parseDrinks(entity.getContent(), drinks);
        } catch (IOException e) {
            Log.d(TAG, "Failed to read data: " + e.getMessage());
        }

        Drink drink = null;
        if (drinks.size() == 1) {
            drink = drinks.get(0);
        }

        return drink;
    }

    public void rateDrink(Drink drink, float grade, String token) {
        final HttpGet get = new HttpGet(RATE_BASE_URI + drink.getId() 
                + "/" + (int)grade + "/" + token);
        HttpEntity entity = null;

        try {
            final HttpResponse response = HttpManager.execute(get);
            entity = response.getEntity();
            entity.getContent();
        } catch (IOException e) {
            Log.d(TAG, "Failed to read data: " + e.getMessage());
        }
    }

    /**
     * Builds a the uri for search from a SearchCriteria
     * @param searchCriteria the search criteria
     * @return the search uri
     */
    private String buildSearchUri(SearchCriteria searchCriteria) {
        Log.d(TAG, "building search uri from " + searchCriteria);
        String searchUri = ARTICLES_BASE_URI + "?";
        if (!TextUtils.isEmpty(searchCriteria.getQuery()))
            searchUri += "&search=" + URLEncoder.encode(searchCriteria.getQuery());
        if (!TextUtils.isEmpty(searchCriteria.getToken()))
            searchUri += "&token=" + searchCriteria.getToken();
        if (searchCriteria.getCategory() > 0)
            searchUri += "&category=" + searchCriteria.getCategory();
        searchUri += "&page=" + searchCriteria.getPage();
        return searchUri;
    }
}
