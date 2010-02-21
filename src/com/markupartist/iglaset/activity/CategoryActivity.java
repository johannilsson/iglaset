package com.markupartist.iglaset.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.markupartist.iglaset.R;
import com.markupartist.iglaset.activity.SectionedAdapter.Section;
import com.markupartist.iglaset.util.Tracker;

public class CategoryActivity extends ListActivity {
    private static final String TAG = "CategoryActivity";
    SectionedAdapter mSectionedAdapter = new SectionedAdapter() {
        protected View getHeaderView(Section section, int index, 
                View convertView, ViewGroup parent) {
            TextView result = (TextView) convertView;

            if (convertView == null)
                result = (TextView) getLayoutInflater().inflate(R.layout.header, null);

            result.setText(section.caption);
            return (result);
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Tracker.getInstance().trackPageView("categories");

        setContentView(R.layout.category_list);
        setTitle(getText(R.string.categories));

        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<Category>(
                this, R.layout.category_list_row, createCategories());

        mSectionedAdapter.addSection(0, "Mina listor", createUserListAdapter());
        mSectionedAdapter.addSection(1, "Kategorier", categoryAdapter);

        this.setListAdapter(mSectionedAdapter);
    }

    private SimpleAdapter createUserListAdapter() {
        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> recomendationMap = new HashMap<String, Object>();
        recomendationMap.put("text", "Rekommendationer");
        Map<String, Object> gradeMap = new HashMap<String, Object>();
        gradeMap.put("text", "Betygsatta artiklar");
        list.add(recomendationMap);
        list.add(gradeMap);

        return new SimpleAdapter(this, list, 
                R.layout.category_list_row,
                new String[] { "text" },
                new int[] { 
                    R.id.category_list_row
                });
    }
    
    /** Called when the list is clicked. */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Section section = mSectionedAdapter.getSection(position);
        if (section.id == 1) {
            Category category = (Category) getListAdapter().getItem(position);

            Intent i = new Intent(this, SearchResultActivity.class);
            i.putExtra(SearchResultActivity.EXTRA_SEARCH_CATEGORY_ID, category.getId());
            startActivity(i);
        } else {
            int sectionPosition = mSectionedAdapter.getSectionIndex(position);
            Log.d(TAG, "index: " + sectionPosition);
            switch (sectionPosition) {
            case 0:
                Intent recIntent = new Intent(this, SearchResultActivity.class);
                recIntent.setAction(SearchResultActivity.ACTION_USER_RECOMMENDATIONS);
                startActivity(recIntent);
                return;
            case 1:
                Intent ratingIntent = new Intent(this, SearchResultActivity.class);
                ratingIntent.setAction(SearchResultActivity.ACTION_USER_RATINGS);
                startActivity(ratingIntent);
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Tracker.getInstance().stop();
    }

    /** Create all categories */
    private ArrayList<Category> createCategories() {
        ArrayList<Category> categories = new ArrayList<Category>();
        categories.add(new Category(1, "Sprit"));
        categories.add(new Category(2, "Starkvin"));
        categories.add(new Category(3, "Öl"));
        categories.add(new Category(4, "Cider och blanddrycker"));
        categories.add(new Category(5, "Alkoholfritt"));
        categories.add(new Category(6, "Röda viner"));
        categories.add(new Category(7, "Vita viner"));
        categories.add(new Category(8, "Roséviner"));
        categories.add(new Category(9, "Mousserande viner"));
        return categories;
    }

    private class Category {
        private int mId;
        private String mName;

        public Category(int id, String name) {
            this.mId = id;
            this.mName = name;
        }

        public int getId() {
            return mId;
        }

        public String getName() {
            return mName;
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
