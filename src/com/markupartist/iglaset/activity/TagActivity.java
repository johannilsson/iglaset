package com.markupartist.iglaset.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.IntentAction;
import com.markupartist.iglaset.R;
import com.markupartist.iglaset.provider.Tag;
import com.markupartist.iglaset.provider.TagsStore;
import com.markupartist.iglaset.util.ListUtils;
import com.markupartist.iglaset.util.MultiHashMap;
import com.markupartist.iglaset.util.StringUtils;
import com.markupartist.iglaset.widget.SearchAction;
import com.markupartist.iglaset.widget.SectionedAdapter;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author marco
 * Activity responsible for displaying a drink category's attached tags and allowing the user to
 * do detailed tag searching.
 */
public class TagActivity extends ListActivity implements View.OnClickListener {
	
    static final String EXTRA_CATEGORY_ID = "com.markupartist.iglaset.search.categoryId";
    static final String EXTRA_CATEGORY_NAME = "com.markupartist.iglaset.search.categoryName";
    private static final int DIALOG_SEARCH_NETWORK_PROBLEM = 0;

    private SectionedAdapter sectionedAdapter;
    private MultiHashMap<String, Tag> tagMap;
    private GetTagsTask getTagsTask;
    private int categoryId;
    
    /**
     * @author marco
     * Data holder for passing data between screen rotations.
     */
    private static class InstanceHolder {
    	/**
    	 * Selected items on rotation.
    	 */
    	SparseBooleanArray selectedItems;
    	
    	/**
    	 * Available tags on rotation.
    	 */
    	MultiHashMap<String, Tag> tags;
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();

        setContentView(R.layout.tag_list);

        ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        actionBar.setHomeAction(new IntentAction(this, StartActivity.createIntent(this), R.drawable.ic_actionbar_home_default));
        actionBar.setTitle(extras.getString(EXTRA_CATEGORY_NAME));
        actionBar.addAction(new SearchAction() {
            @Override
            public void performAction(View view) {
                onSearchRequested();
            }
        });

        categoryId = extras.getInt(EXTRA_CATEGORY_ID);
        
        View tagSearchLayout = this.findViewById(R.id.tagSearchLayout);
        tagSearchLayout.setOnClickListener(this);
        
        sectionedAdapter = new SectionedAdapter() {
            protected View getHeaderView(Section section, int index, View convertView, ViewGroup parent) {
                TextView textView = (TextView) convertView;
                if(null == textView) {
                    textView = (TextView) getLayoutInflater().inflate(R.layout.header, null);
                }

                textView.setText(section.caption);
                return textView;
            }
        };
        
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
        // Use old values if available
        final InstanceHolder holder = (InstanceHolder) getLastNonConfigurationInstance();
        if(null != holder && null != holder.tags) {
        	setTagMap(holder.tags);
        	updateSelected(holder.selectedItems);
        	updateSelectedTagText();
        } else {
        	launchGetTagsTask(categoryId);
    	}
    }
    
    @Override
    protected void onDestroy() {
    	cancelGetTagsTask();
    	super.onDestroy();
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle state) {
    	super.onRestoreInstanceState(state);
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
    	InstanceHolder holder = new InstanceHolder();
    	holder.tags = this.tagMap;
    	holder.selectedItems = getListView().getCheckedItemPositions();
    	return holder;
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
        case DIALOG_SEARCH_NETWORK_PROBLEM:
        	return DialogFactory.createNetworkProblemDialog(
        			this,
        			new OnClickListener() {
		                @Override
		                public void onClick(DialogInterface dialog, int which) {
		                	switch(which) {
		                	case Dialog.BUTTON_POSITIVE:
		                		launchGetTagsTask(categoryId);
			                    break;
		                	case Dialog.BUTTON_NEGATIVE:
		                		finish();
		                		break;
		                	}
		                }
		             });
        }
        
        return null;
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        updateSelectedTagText();
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.tagSearchLayout:
			doSearch();
			break;
		default:
			break;
		}
	}

	private void doSearch() {
        Intent searchIntent = new Intent(this, SearchResultActivity.class);
        searchIntent.putExtra(SearchResultActivity.EXTRA_SEARCH_CATEGORY_ID, categoryId);

        // Transfer the IDs of the checked items to an array
        SparseBooleanArray checkedItems = getListView().getCheckedItemPositions();
        ArrayList<Integer> checkedIds = new ArrayList<Integer>();
        for(int i=0; i<checkedItems.size(); ++i) {
        	if(true == checkedItems.valueAt(i)) {
	        	Tag tag = (Tag) getListView().getItemAtPosition(checkedItems.keyAt(i));
	        	checkedIds.add(tag.getId());
        	}
        }
        
        TextView numSelectedView = (TextView) findViewById(R.id.tagSearchSelectedText);
        
        searchIntent.putExtra(SearchResultActivity.EXTRA_SEARCH_TAGS, checkedIds);
        searchIntent.putExtra(SearchResultActivity.EXTRA_SEARCH_TAGS_SELECTED, numSelectedView.getText());
        startActivity(searchIntent); 
	}
	
    /**
     * Update ListView selection. This is necessary since the ListView will retain the selected
     * items on screen rotation but there's no way of fetching those items from the program.
     * As a result you will see items checked but a call to getListView().getCheckedItemPositions
     * will return an empty list.
     * @param selected Checkbox state before rotation.
     */
    private void updateSelected(SparseBooleanArray selected) {
    	for(int i=0; i<selected.size(); ++i) {
    		getListView().setItemChecked(
    				selected.keyAt(i),
    				selected.valueAt(i));
    	}
    }
	
	/**
	 * Update the text showing the selected tags (if any).
	 */
	private void updateSelectedTagText() {
        TextView numSelectedView = (TextView) findViewById(R.id.tagSearchSelectedText);
        StringBuilder builder = new StringBuilder();
        
        SparseBooleanArray selected = getListView().getCheckedItemPositions();
        if(0 == selected.size()) {
        	builder.append(getText(R.string.no_selected_tags));
        } else {
        	// Concatenate the name of the selected tags.
        	ArrayList<String> selectedList = new ArrayList<String>();
        	for(int i=0; i<selected.size(); ++i) {
        		if(true == selected.valueAt(i)) {
        			Tag tag = (Tag) getListView().getItemAtPosition(selected.keyAt(i));
        			selectedList.add(tag.getName());
        		}
        	}
        	
        	builder.append(Integer.toString(selectedList.size())).append(" ");
        	builder.append(selectedList.size() == 1 ? getText(R.string.tag) : getText(R.string.tags)).append(": ");
        	builder.append(StringUtils.join(selectedList.toArray(), ", "));        			
        }
        
        numSelectedView.setText(builder.toString());
	}
	
	private void setTagMap(MultiHashMap<String, Tag> tagMap) {
        LinearLayout progressBar = (LinearLayout) findViewById(R.id.search_progress);
        progressBar.setVisibility(View.GONE);
        
		this.tagMap = tagMap;
		if(tagMap.size() == 0) {
			// Instantly search if we have no tag categories
			doSearch();
			finish();
		} else {
			populateList(this.tagMap);
		}
	}
	
	private void populateList(MultiHashMap<String, Tag> tagMap) {
		int sectionId = 1;
		for(Map.Entry<String, List<Tag>> entry : tagMap.entrySet()) {
			ArrayAdapter<Tag> adapter =
				new ArrayAdapter<Tag>(this, android.R.layout.simple_list_item_multiple_choice, entry.getValue()) {
				@Override
				public long getItemId(int position) {
					return getItem(position).getId();
				}
			};
			
			sectionedAdapter.addSection(sectionId++, entry.getKey(), adapter);
		}
		
		// notifyDataSetChanged causes null references in the adapter's
		// findViewById for some reason.
		setListAdapter(sectionedAdapter);
	}
	
    /**
     * Launch an async task to fetch tags assigned to a specified category. If a tag fetch task is
     * already running then that task will get canceled.
     * @param category Category tags to fetch.
     */
    private void launchGetTagsTask(int category) {
    	cancelGetTagsTask();
        getTagsTask = new GetTagsTask();
        getTagsTask.execute(category);
    }
    
    /**
     * Cancel the getTagsTask if one is running. If the task has not been created or if it's
     * not executing then this will do nothing.
     */
    private void cancelGetTagsTask() {
    	if(null != getTagsTask && getTagsTask.getStatus() == AsyncTask.Status.RUNNING) {
    		getTagsTask.cancel(true);
    		getTagsTask = null;
    	}
    }
	
	private class GetTagsTask extends AsyncTask<Integer, Void, MultiHashMap<String, Tag>> {

		@Override
		protected MultiHashMap<String, Tag> doInBackground(Integer... params) {
			MultiHashMap<String, Tag> tagMap = null;
			
			try {
				ArrayList<Tag> categoryTags = TagsStore.getTags(params[0]);
				tagMap = ListUtils.toMultiHashMap(categoryTags);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return tagMap;
		}
		
        @Override
        public void onProgressUpdate(Void... values) {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected void onPostExecute(MultiHashMap<String, Tag> result) {
            setProgressBarIndeterminateVisibility(false);
            
            if(null != result) {
            	setTagMap(result);
            } else {
                showDialog(DIALOG_SEARCH_NETWORK_PROBLEM);
            }
        }
	}
}
