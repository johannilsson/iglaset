package com.markupartist.iglaset.provider;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import android.util.Log;

import com.markupartist.iglaset.util.HttpManager;

public class TagsStore {

	private static final String TAG = TagsStore.class.getSimpleName();
    private static final String TAGS_BASE_URI = "http://www.iglaset.se/tags/";
    
	/**
	 * Fetch all available tags for a specified category.
	 * 
	 * @param category Category on which to search for tags.
	 * @return List of tags attached to the category. Note that the list might be empty.
	 * @throws IOException
	 */
	public static ArrayList<Tag> getTags(int category) throws IOException {
		assert(category >= 0);
		
		StringBuilder uri = new StringBuilder(TAGS_BASE_URI);
		uri.append("tags_by_category/")
		   .append(Integer.toString(category))
		   .append(".xml");
		
		Log.d(TAG, "Reading tags from " + uri.toString());
		final HttpGet get = new HttpGet(uri.toString());
        HttpEntity entity = null;

        final HttpResponse response = HttpManager.execute(get);
        entity = response.getEntity();
        TagsParser parser = new TagsParser();
        
        return parser.parse(entity.getContent());		
	}
}
