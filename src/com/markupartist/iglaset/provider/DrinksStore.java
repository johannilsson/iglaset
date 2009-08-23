package com.markupartist.iglaset.provider;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.util.Log;

public class DrinksStore {
    private static String TAG = "DrinksStore";

    public ArrayList<Drink> searchDrinks(String query) {
        final ArrayList<Drink> drinks = new ArrayList<Drink>();
        try {
            URL endpoint = new URL("http://api.iglaset.se/api/articles/xml/?page=1&search=" 
                    + URLEncoder.encode(query));
            DrinksParser drinksParser = new DrinksParser();
            drinksParser.parseDrinks(endpoint.openStream(), drinks);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "Malformed URL: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Failed to read data: " + e.getMessage());
        }

        return drinks;
    }
}
