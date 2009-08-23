package com.markupartist.iglaset.activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.markupartist.iglaset.R;
import com.markupartist.iglaset.provider.Comment;
import com.markupartist.iglaset.provider.CommentsStore;
import com.markupartist.iglaset.provider.Drink;
import com.markupartist.iglaset.provider.Drink.Volume;

public class DrinkDetailActivity extends ListActivity {
    static String TAG = "DrinkDetailActivity";
    private CommentsStore mCommentsStore = new CommentsStore();
    private ArrayAdapter<Comment> mCommentsAdapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.drink_details);

        Bundle extras = getIntent().getExtras();
        Drink drink = extras.getParcelable("com.markupartist.iglaset.Drink");

        TextView nameTextView = (TextView) findViewById(R.id.drink_name);
        nameTextView.setText(drink.getName());

        TextView originTextView = (TextView) findViewById(R.id.drink_origin);
        originTextView.setText(drink.getOrigin());

        TextView originCountryTextView = (TextView) findViewById(R.id.drink_origin_country);
        originCountryTextView.setText(drink.getOriginCountry());

        TextView alcoholPercentTextView = (TextView) findViewById(R.id.drink_alcohol_percent);
        alcoholPercentTextView.setText(drink.getAlcoholPercent());

        TextView yearTextView = (TextView) findViewById(R.id.drink_year);
        yearTextView.setText(drink.getYear() == 0 ? "" : String.valueOf(drink.getYear()));

        RatingBar drinkRatingBar = (RatingBar) findViewById(R.id.drink_rating);
        drinkRatingBar.setRating(Float.parseFloat(drink.getRating()));

        ImageView imageView = (ImageView) findViewById(R.id.drink_image);
        imageView.setImageBitmap(drink.loadImage());

        //TextView descriptionTextView = (TextView) findViewById(R.id.drink_description);
        //descriptionTextView.setText(Html.fromHtml(drink.getDescription()));

        HashMap<String, ArrayList<String>> tags = drink.getTags();
        for (String type : tags.keySet()) {
            ArrayList<String> nameList = tags.get(type);
            mSectionedAdapter.addSection(0, type, new ArrayAdapter<String>(this, 
                    R.layout.simple_row, nameList));
        }

        ArrayList<Volume> volumes = drink.getVolumes();
        if (!volumes.isEmpty()) {
            ArrayList<HashMap<String, String>> volumeList = new ArrayList<HashMap<String, String>>();
            for (Volume volume : volumes) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("id", String.valueOf(volume.getArticleId()));
                map.put("amount", String.valueOf(volume.getVolume()) + " ml");
                map.put("price", volume.getPriceSek() + " kr");
                volumeList.add(map);
            }

            SimpleAdapter volumeAdapter = new SimpleAdapter(this, volumeList, 
                    R.layout.volume_row,
                    new String[] { "id", "amount", "price" },
                    new int[] { 
                        R.id.volume_id,
                        R.id.volume_amount, 
                        R.id.volume_price
                    }
            );

            mSectionedAdapter.addSection(1, "Förpackningar", volumeAdapter);
        }

        new SearchCommentsTask().execute(drink.getId());

        mCommentsAdapter = new ArrayAdapter<Comment>(this, R.layout.simple_row);
        mSectionedAdapter.addSection(2, "Kommentarer", mCommentsAdapter);

        setListAdapter(mSectionedAdapter);
    }

    private void updateComments(ArrayList<Comment> result) {
        for (Comment comment : result) {
            mCommentsAdapter.add(comment);
        }
        mSectionedAdapter.notifyDataSetChanged();
    }

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

    private class SearchCommentsTask extends AsyncTask<Integer, Void, ArrayList<Comment>> {

        @Override
        protected ArrayList<Comment> doInBackground(Integer... params) {
            publishProgress();
            return mCommentsStore.getComments(params[0]);
        }

        @Override
        public void onProgressUpdate(Void... values) {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected void onPostExecute(ArrayList<Comment> result) {
            setProgressBarIndeterminateVisibility(false);
            updateComments(result);
        }
    }
}
