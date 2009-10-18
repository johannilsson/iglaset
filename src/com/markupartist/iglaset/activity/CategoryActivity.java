package com.markupartist.iglaset.activity;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.markupartist.iglaset.R;

public class CategoryActivity extends ListActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_list);
        setTitle("Kategorier");

        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<Category>(
                this, R.layout.category_list_row, createCategories());
        this.setListAdapter(categoryAdapter);
    }

    /** Called when the list is clicked. */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Category category = (Category) getListAdapter().getItem(position);

        Intent i = new Intent(this, SearchResultActivity.class);
        i.putExtra("com.markupartist.sthlmtraveling.searchCategory", category.getId());
        startActivity(i);
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
