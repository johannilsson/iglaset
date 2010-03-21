package com.markupartist.iglaset.provider;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.text.TextUtils;
import android.util.Log;

import com.markupartist.iglaset.util.HttpManager;

public class BarcodeStore {
    private static final String TAG = "BarcodeStore";
    private static final String BARCODES_BASE_URI = "http://api.iglaset.se/api/barcodes";
    private static BarcodeStore sInstance;

    private BarcodeStore() {
    }

    public static BarcodeStore getInstance() {
        if (sInstance == null) {
            sInstance = new BarcodeStore();
        }
        return sInstance;
    }

    public boolean suggest(String barcode, int drinkId, String authToken)
            throws IOException {
        // http://api.iglaset.se/api/barcodes/suggest/[ean]/[article_id]/[auth_token]
        Log.d(TAG, "Suggesting barcode " + barcode);
        String suggestUri = String.format("%s/suggest/%s/%s/%s",
                BARCODES_BASE_URI, barcode, drinkId, authToken);

        final HttpGet get = new HttpGet(suggestUri);
        final HttpResponse response = HttpManager.execute(get);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            Log.d(TAG, "added");
            return true;
        } else {
            Log.d(TAG, "Failed " + response.getStatusLine().getStatusCode());
        }

        return false;
    }

    public ArrayList<Drink> search(SearchCriteria searchCriteria)
            throws IOException {
        final ArrayList<Drink> drinks = new ArrayList<Drink>();

        // http://api.iglaset.se/api/barcodes/xml/[ean]/?page=[page]
        String searchUri = String.format("%s/xml/%s/?page=%s",
                BARCODES_BASE_URI, searchCriteria.getBarcode(),
                searchCriteria.getPage());
        if (!TextUtils.isEmpty(searchCriteria.getToken()))
            searchUri += "&token=" + searchCriteria.getToken();

        final HttpGet get = new HttpGet(searchUri);
        HttpEntity entity = null;

        final HttpResponse response = HttpManager.execute(get);
        entity = response.getEntity();
        DrinksParser drinksParser = new DrinksParser();
        drinksParser.parseDrinks(entity.getContent(), drinks);

        return drinks;
    }

    /**
     * Builds a the uri for search from a SearchCriteria
     * @param searchCriteria the search criteria
     * @return the search uri
     */
    private String buildSearchUri(SearchCriteria searchCriteria) {
        Log.d(TAG, "building search uri from " + searchCriteria);
        String searchUri = BARCODES_BASE_URI + "?";
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
