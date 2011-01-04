package com.markupartist.iglaset.provider;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import android.text.TextUtils;
import android.util.Log;

import com.markupartist.iglaset.util.HttpManager;

public class BarcodeStore {
    private static final String TAG = BarcodeStore.class.getSimpleName();
    private static final String BARCODE_SUGGEST_URI = "http://www.iglaset.se/barcodes/suggest_ean.xml?user_credentials=%s";
    private static final String BARCODE_SEARCH_URI = "http://www.iglaset.se/barcodes/show_by_ean/%s.xml?page=%d";
    private static BarcodeStore sInstance;

    private BarcodeStore() {
    }

    public static BarcodeStore getInstance() {
        if (sInstance == null) {
            sInstance = new BarcodeStore();
        }
        return sInstance;
    }

    public boolean suggest(String barcode, Drink drink, AuthStore.Authentication authentication)
            throws IOException {
        Log.d(TAG, "Suggesting barcode " + barcode);

        final HttpPost post = new HttpPost(String.format(BARCODE_SUGGEST_URI, authentication.v2.token));
        
        ArrayList<NameValuePair> payload = new ArrayList<NameValuePair>(1);
        payload.add(new BasicNameValuePair("article_id", String.valueOf(drink.getId())));
        payload.add(new BasicNameValuePair("ean", barcode));
        
        post.setEntity(new UrlEncodedFormEntity(payload, "utf-8"));
        final HttpResponse response = HttpManager.execute(post);
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

        String searchUri = String.format(BARCODE_SEARCH_URI, searchCriteria.getBarcode(), searchCriteria.getPage());
        if (searchCriteria.getAuthentication() != null
                && !TextUtils.isEmpty(searchCriteria.getAuthentication().v2.token))
            searchUri += "&user_credentials=" + searchCriteria.getAuthentication().v2.token;

        final HttpGet get = new HttpGet(searchUri);
        HttpEntity entity = null;

        final HttpResponse response = HttpManager.execute(get);
        entity = response.getEntity();
        DrinksParser drinksParser = new DrinksParser();
        return drinksParser.parse(entity.getContent());
    }
}
