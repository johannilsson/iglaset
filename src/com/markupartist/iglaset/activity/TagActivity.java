package com.markupartist.iglaset.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import com.markupartist.iglaset.R;
import com.markupartist.iglaset.provider.Tag;
import com.markupartist.iglaset.provider.TagsStore;
import com.markupartist.iglaset.widget.SectionedAdapter;
import com.markupartist.iglaset.widget.TagAdapter;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class TagActivity extends ListActivity implements View.OnClickListener {
	
    static final String EXTRA_CATEGORY_ID = "com.markupartist.iglaset.search.categoryId";
    static final String EXTRA_CATEGORY_NAME = "com.markupartist.iglaset.search.categoryName";

    private SectionedAdapter sectionedAdapter;
    private TreeMap<String, ArrayList<Tag>> tagMap;
    private GetTagsTask getTagsTask;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();

        setContentView(R.layout.tag_list);
        setTitle(extras.getString(EXTRA_CATEGORY_NAME));
        
        View tagSearchLayout = this.findViewById(R.id.tagSearchLayout);
        tagSearchLayout.setOnClickListener(this);
        
        sectionedAdapter = new SectionedAdapter() {
            protected View getHeaderView(Section section, int index, View convertView, ViewGroup parent) {
                TextView result = (TextView) convertView;

                if(null == result) {
                    result = (TextView) getLayoutInflater().inflate(R.layout.header, null);
                }

                result.setText(section.caption);
                return result;
            }
        };
        
        this.setListAdapter(sectionedAdapter);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
        // Use old values if available
        @SuppressWarnings("unchecked")
		final TreeMap<String, ArrayList<Tag>> tagMap = (TreeMap<String, ArrayList<Tag>>) getLastNonConfigurationInstance();
        if(null != tagMap) {
        	setTagMap(tagMap);
        	updateSelectedCount();
        } else {
            int categoryId = extras.getInt(EXTRA_CATEGORY_ID);
        	launchGetTagsTask(categoryId);
    	}
    }
    
    @Override
    protected void onDestroy() {
    	cancelGetTagsTask();
    	super.onDestroy();
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
        return this.tagMap;
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        updateSelectedCount();
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.tagSearchLayout:
			break;
		}
	}
	
	private void updateSelectedCount() {
        // Update number of selected tags
        TextView numSelectedView = (TextView) findViewById(R.id.tagSearchSelectedText);
        int numSelected = getListView().getCheckItemIds().length;
        if(0 == numSelected) {
        	numSelectedView.setText("0 markerade taggar (sök allt)");
        } else if(1 == numSelected) {
            numSelectedView.setText("1 markerad tagg");
        } else {
        	numSelectedView.setText(Integer.toString(numSelected) + " markerade taggar");
        }
	}
	
	private void setTagMap(TreeMap<String, ArrayList<Tag>> tagMap) {
		this.tagMap = tagMap;
		populateList(this.tagMap);
	}
	
	private void populateList(TreeMap<String, ArrayList<Tag>> tagMap) {
		int sectionId = 1;
		for(String tagType : tagMap.keySet()) {
			TagAdapter adapter = new TagAdapter(this, tagMap.get(tagType));
			sectionedAdapter.addSection(sectionId++, tagType, adapter);
		}
		
		// notifyDataSetChanged causes null references in the adapter's
		// findViewById for some reason.
		setListAdapter(sectionedAdapter);
	}
	
    private void launchGetTagsTask(int category) {
    	cancelGetTagsTask();
        getTagsTask = new GetTagsTask();
        getTagsTask.execute(category);
    }
    
    private void cancelGetTagsTask() {
    	if(null != getTagsTask && getTagsTask.getStatus() == AsyncTask.Status.RUNNING) {
    		getTagsTask.cancel(true);
    		getTagsTask = null;
    	}
    }
	
	private class GetTagsTask extends AsyncTask<Integer, Void, TreeMap<String, ArrayList<Tag>>> {

		@Override
		protected TreeMap<String, ArrayList<Tag>> doInBackground(Integer... params) {
			TreeMap<String, ArrayList<Tag>> tagMap = new TreeMap<String, ArrayList<Tag>>();
			
			try {
				ArrayList<Tag> categoryTags = TagsStore.getTags(params[0]);
				// Move the tags over to a map to have one section per tag type
				
				for(Tag tag : categoryTags) {
					ArrayList<Tag> list = tagMap.get(tag.getType());
					if(null == list) {
						list = new ArrayList<Tag>();
						tagMap.put(tag.getType(), list);
					}
					
					list.add(tag);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return tagMap;
		}
		
        @Override
        public void onProgressUpdate(Void... values) {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected void onPostExecute(TreeMap<String, ArrayList<Tag>> result) {
            setProgressBarIndeterminateVisibility(false);
            setTagMap(result);
        }
	}
}
