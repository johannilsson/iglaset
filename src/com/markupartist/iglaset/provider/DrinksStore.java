package com.markupartist.iglaset.provider;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import android.util.Log;

import com.markupartist.iglaset.util.HttpManager;

public class DrinksStore {
    private static String TAG = "DrinksStore";
    private static String ARTICLES_BASE_URI = "http://api.iglaset.se/api/articles/xml/";
    private static String RATE_BASE_URI = "http://api.iglaset.se/api/rate/";

    public ArrayList<Drink> searchDrinks(String query, int page) {
        return searchDrinks(query, page, null);
    }

    public ArrayList<Drink> searchDrinks(String query, int page, String token) {
        final ArrayList<Drink> drinks = new ArrayList<Drink>();

        String searchUri = ARTICLES_BASE_URI + "?page=" + page 
            + "&search=" + URLEncoder.encode(query);
        if (token != null) {
            searchUri += "&token=" + token;
        }

        final HttpGet get = new HttpGet(searchUri);
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
        if (token != null) {
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

        Log.d(TAG, "Got drink " + drink);
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
}
