package com.markupartist.iglaset.provider;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import com.markupartist.iglaset.util.StringUtils;

public class DrinksStore {
    private static String TAG = DrinksStore.class.getSimpleName();
    private static String ARTICLES_BASE_URI =
    	"http://www.iglaset.se/articles.xml";
    private static String ARTICLE_DETAILS_URI =
    	"http://www.iglaset.se/articles/%d.xml";
    private static String RATE_URI =
    	"http://www.iglaset.se/articles/%d/rate.xml?rating=%d&user_credentials=%s";
    /**
     * Base URI for adding an article comment.
     */
    private static String COMMENT_URI =
    	"http://www.iglaset.se/comments.xml?user_credentials=%s";
    private static String USER_RECOMMENDATIONS_URI =
    	"http://www.iglaset.se/articles.xml?order_by=recommendation&recommendations=1";
    private static String USER_RATINGS_URI =
    	"http://www.iglaset.se/users/%d.xml?show=ratings";

    private DrinksStore() {
    }
    
    private static class SingletonHolder {
    	public static final DrinksStore instance = new DrinksStore();
    }

    public static DrinksStore getInstance() {
    	return SingletonHolder.instance;
    }

    public ArrayList<Drink> searchDrinks(SearchCriteria searchCriteria)
            throws IOException {
        final ArrayList<Drink> drinks = new ArrayList<Drink>();
        final HttpGet get = new HttpGet(buildSearchUri(searchCriteria));
        HttpEntity entity = null;

        final HttpResponse response = HttpManager.execute(get);
        entity = response.getEntity();
        DrinksParser drinksParser = new DrinksParser();
        drinksParser.parseDrinks(entity.getContent(), drinks);

        return drinks;
    }

    /**
     * Fins recommendations for the user.
     * @param searchCriteria the search criteria
     * @return list of recommendations
     * @throws IOException on connection problem
     */
    public ArrayList<Drink> findRecommendations(
            RecommendationSearchCriteria searchCriteria)
                throws IOException {
        final ArrayList<Drink> drinks = new ArrayList<Drink>();

        final HttpGet get = new HttpGet(
        		String.format(USER_RECOMMENDATIONS_URI, searchCriteria.getUserId())
        		+ "&user_credentials=" + searchCriteria.getAuthentication().v2.token
        		+ "&page=" + searchCriteria.getPage());
        HttpEntity entity = null;

        final HttpResponse response = HttpManager.execute(get);
        entity = response.getEntity();
        DrinksParser drinksParser = new DrinksParser();
        drinksParser.parseDrinks(entity.getContent(), drinks);

        return drinks;
    }

    /**
     * Find all drinks that the user has rated.
     * @param searchCriteria the search criteria
     * @return list of rated drinks
     * @throws IOException on connection problem
     */
    public ArrayList<Drink> findRatedDrinks(RatingSearchCriteria searchCriteria)
            throws IOException {
        final ArrayList<Drink> drinks = new ArrayList<Drink>();

        final HttpGet get = new HttpGet(
        		String.format(USER_RATINGS_URI, searchCriteria.getUserId())
                + "&page=" + searchCriteria.getPage()
                + "&user_credentials=" + searchCriteria.getAuthentication().v2.token);
        HttpEntity entity = null;

        final HttpResponse response = HttpManager.execute(get);
        entity = response.getEntity();
        DrinksParser drinksParser = new DrinksParser();
        drinksParser.parseDrinks(entity.getContent(), drinks);

        return drinks;
    }
    
    public Drink getDrink(int id) {
        return getDrink(id, null);
    }

    public Drink getDrink(int id, AuthStore.Authentication authentication) {
        String searchUri = String.format(ARTICLE_DETAILS_URI, id);
        if (authentication != null && !TextUtils.isEmpty(authentication.v2.token)) {
            searchUri += "?user_credentials=" + authentication.v2.token;
        }

        final HttpGet get = new HttpGet(searchUri);
        final ArrayList<Drink> drinks = new ArrayList<Drink>();

        try {
            final HttpResponse response = HttpManager.execute(get);
            HttpEntity entity = response.getEntity();
            DrinksParser drinksParser = new DrinksParser();
            drinksParser.parseDrinks(entity.getContent(), drinks);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read data: " + e.getMessage());
        }

        Drink drink = null;
        if (drinks.size() == 1) {
            drink = drinks.get(0);
        }

        return drink;
    }

    public void rateDrink(Drink drink, float grade, AuthStore.Authentication authentication) {
        final HttpGet get = new HttpGet(
        		String.format(RATE_URI, drink.getId(), (int) grade, authentication.v2.token)); 
        HttpEntity entity = null;

        try {
            final HttpResponse response = HttpManager.execute(get);
            entity = response.getEntity();
            entity.getContent();
        } catch (IOException e) {
            Log.e(TAG, "Failed to read data: " + e.getMessage());
        }
    }

    /**
     * Add a new comment to a drink. This will, unlike Android Market, add
     * an additional comment. It will not overwrite the user's previous one.
     * @param drink Drink to comment.
     * @param comment Comment to add.
     * @param authentication Authentication data.
     * @return Returns true if successful, false otherwise.
     * @throws IOException on connection problem.
     */
    public Boolean commentDrink(Drink drink, String comment, AuthStore.Authentication authentication) throws IOException {
    	final HttpPost post = new HttpPost(String.format(COMMENT_URI, authentication.v2.token));
    	
        ArrayList<NameValuePair> payload = new ArrayList<NameValuePair>(1);
        payload.add(new BasicNameValuePair("comment[article_id]", String.valueOf(drink.getId())));
        payload.add(new BasicNameValuePair("comment[text]", comment));
        
        try {
			post.setEntity(new UrlEncodedFormEntity(payload, "utf-8"));
			final HttpResponse response = HttpManager.execute(post);
	        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	            return true;
	        } else {
	            Log.w(TAG, "Request failed, http status code was not OK.");
	            return false;
	        }
			
		} catch (UnsupportedEncodingException e) {
			// TODO What to do? :(
			e.printStackTrace();
			return false;
		}
    }
    
    /**
     * Builds a the uri for search from a SearchCriteria
     * @param searchCriteria the search criteria
     * @return the search uri
     */
    private String buildSearchUri(SearchCriteria searchCriteria) {
        Log.d(TAG, "building search uri from " + searchCriteria);
          
    	StringBuilder builder = new StringBuilder();    	
        builder.append(ARTICLES_BASE_URI).append("?");
 
        builder.append("page=").append(searchCriteria.getPage());

        if (!TextUtils.isEmpty(searchCriteria.getQuery()))
        	builder.append("&str=").append(URLEncoder.encode(searchCriteria.getQuery()));
        if (searchCriteria.getCategory() > 0)
        	builder.append("&category=").append(searchCriteria.getCategory());
        if (searchCriteria.getAuthentication() != null
                && !TextUtils.isEmpty(searchCriteria.getAuthentication().v2.token))
        	builder.append("&user_credentials=").append(searchCriteria.getAuthentication().v2.token);
        
        // Append sorting
    	SearchCriteria.Sort mode = searchCriteria.getSortMode();
    	builder.append("&order_by=");
    	switch(mode) {
    	case Name:
    		builder.append("name");
    		break;
    	case Producer:
    		builder.append("producer");
    		break;
    	case Recommendation:
    		builder.append("recommendation");
    		break;
    	case Created:
    	case Undefined:
		default:
			builder.append("default");
			break;
    	}

        ArrayList<Integer> tags = searchCriteria.getTags();
        if (null != tags && tags.size() > 0) {
            builder.append("&tag_filter=or&tags[]=");
            builder.append(StringUtils.join(tags.toArray(), "&tags[]="));
        }
        
        return builder.toString();
    }
}
