package com.markupartist.iglaset.provider;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;

import android.util.Log;

import com.markupartist.iglaset.util.HttpManager;

public class DrinksStore {
    private static String TAG = "DrinksStore";
    private static String ARTICLES_BASE_URI = "http://api.iglaset.se/api/articles/xml/";

    public ArrayList<Drink> searchDrinks(String query) {
        final ArrayList<Drink> drinks = new ArrayList<Drink>();

        final HttpGet get = new HttpGet(ARTICLES_BASE_URI + "?page=1&search=" + query);
        HttpEntity entity = null;

        try {
            final HttpResponse response = HttpManager.execute(get);
            entity = response.getEntity();
            DrinksParser drinksParser = new DrinksParser();
            drinksParser.parseDrinks(entity.getContent(), drinks);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "Failed to read data: " + e.getMessage());
        }

        return drinks;
    }

    public void rateDrink(Drink drink, int grade, Authenticate authCallback) {
        String token = authCallback.authenticate();
        Log.d(TAG, "TOKEN " + token);
        try {
            URL endpoint = new URL("http://api.iglaset.se/api/rate/" 
                    + drink.getId() + "/" + grade + "/" + token);
            // TODO: Parse response...
            endpoint.openStream();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "Malformed URL: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "Failed to read data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
