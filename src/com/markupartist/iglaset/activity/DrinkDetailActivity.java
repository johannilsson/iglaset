package com.markupartist.iglaset.activity;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SimpleAdapter.ViewBinder;

import com.markupartist.iglaset.R;
import com.markupartist.iglaset.activity.SectionedAdapter.Section;
import com.markupartist.iglaset.provider.AuthStore;
import com.markupartist.iglaset.provider.Comment;
import com.markupartist.iglaset.provider.CommentsStore;
import com.markupartist.iglaset.provider.Drink;
import com.markupartist.iglaset.provider.DrinksStore;
import com.markupartist.iglaset.provider.Drink.Volume;
import com.markupartist.iglaset.util.ImageLoader;

public class DrinkDetailActivity extends ListActivity {
    private static final int RATE_DIALOG = 0;
    private static final int NOT_AUTHENTICATED_DIALOG = 1;
    protected static final int SETTINGS_CHANGED_REQUEST = 0;
    static String TAG = "DrinkDetailActivity";
    private CommentsStore mCommentsStore = new CommentsStore();
    private SimpleAdapter mCommentsAdapter;
    private ArrayList<Comment> mComments;
    private static Drink sDrink;
    private UserRatingAdapter mUserRatingAdapter;
    private String mToken;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        // Remove when we have progress bar near the comments
        requestWindowFeature(Window.FEATURE_NO_TITLE); 

        setContentView(R.layout.drink_details);

        mToken = AuthStore.getInstance().getStoredToken(this);
        
        Bundle extras = getIntent().getExtras();
        Drink drink = extras.getParcelable("com.markupartist.iglaset.Drink");

        mUserRatingAdapter = new UserRatingAdapter(this, 0);
        mSectionedAdapter.addSectionFirst(0, "Mitt betyg", mUserRatingAdapter);

        if (mToken != null) {
            new GetDrinkTask().execute(drink.getId());
        }

        TextView nameTextView = (TextView) findViewById(R.id.drink_name);
        String yearText = drink.getYear() == 0 ? "" : " " + String.valueOf(drink.getYear());
        nameTextView.setText(drink.getName() + yearText);

        TextView originTextView = (TextView) findViewById(R.id.drink_origin);
        originTextView.setText(drink.getOrigin());

        TextView originCountryTextView = (TextView) findViewById(R.id.drink_origin_country);
        originCountryTextView.setText(drink.getOriginCountry());

        TextView alcoholPercentTextView = (TextView) findViewById(R.id.drink_alcohol_percent);
        alcoholPercentTextView.setText(drink.getAlcoholPercent());

        //TextView yearTextView = (TextView) findViewById(R.id.drink_year);
        //yearTextView.setText(drink.getYear() == 0 ? "" : String.valueOf(drink.getYear()));

        RatingBar drinkRatingBar = (RatingBar) findViewById(R.id.drink_rating);
        drinkRatingBar.setRating(Float.parseFloat(drink.getRating()));

        ImageView imageView = (ImageView) findViewById(R.id.drink_image);
        ImageLoader.getInstance().load(imageView, drink.getImageUrl(), 
                true, R.drawable.noimage);

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
            VolumeAdapter volumeAdapter = new VolumeAdapter(this, volumes);
            mSectionedAdapter.addSection(1, (String) getText(R.string.packings), volumeAdapter);
        }

        // This is temporary till we have fixed a proper comments adapter. 
        mSectionedAdapter.addSection(2, (String) getText(R.string.comments), 
                createLoadingAdapter(getText(R.string.loading_comments)));

        // Check if already have some data, used if screen is rotated.
        @SuppressWarnings("unchecked")
        final ArrayList<Comment> comments = (ArrayList<Comment>) getLastNonConfigurationInstance();
        if (comments == null) {
            new GetCommentsTask().execute(drink.getId());
        } else {
            updateCommentsInUi(comments);
        }

        setListAdapter(mSectionedAdapter);
        sDrink = drink;
    }

    /**
     * Called before this activity is destroyed, returns the previous search 
     * result. This list is used if the screen is rotated. Then we don't need
     * to search for it again.
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        return mComments;
    }

    private SimpleAdapter createLoadingAdapter(CharSequence loadingText) {
        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("text", loadingText);
        list.add(map);

        SimpleAdapter commentsAdapter = new SimpleAdapter(this, list, 
                R.layout.progress_bar,
                new String[] { "text" },
                new int[] { 
                    R.id.search_progress_text
                }
        );
        return commentsAdapter;
    }

    private void onUpdatedDrink(Drink drink) {
        updateUserRatingInUi(drink.getUserRating());
    }

    private void updateUserRatingInUi(float rating) {
        final RatingBar userRatingBar = (RatingBar) findViewById(R.id.user_drink_rating);
        mUserRatingAdapter.setUserRating(rating);
        if (userRatingBar != null) {
            userRatingBar.setRating(rating);
        }
    }

    /**
     * Update comments view.
     * @param comments the comments
     */
    private void updateCommentsInUi(ArrayList<Comment> comments) {
        // Save the result to return it from onRetainNonConfigurationInstance.
        mComments = comments;

        if (comments.isEmpty()) {
            ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("text", getText(R.string.no_comments));
            list.add(map);

            mCommentsAdapter = new SimpleAdapter(this, list, 
                    R.layout.simple_row,
                    new String[] { "text" },
                    new int[] { 
                        R.id.simple_row_text
                    }
            );
        } else {
            ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            for (Comment comment : comments) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("nickname", comment.getNickname());
                map.put("created", comment.getCreated());
                map.put("comment", comment.getComment());
                map.put("rating", comment.getRating());
                list.add(map);
            }

            mCommentsAdapter = new SimpleAdapter(this, list, 
                    R.layout.comment_row,
                    new String[] { "nickname", "created", "comment", "rating" },
                    new int[] { 
                        R.id.comment_nickname,
                        R.id.comment_created, 
                        R.id.comment_comment,
                        R.id.comment_rating
                    }
            );
    
            mCommentsAdapter.setViewBinder(new ViewBinder() {
                @Override
                public boolean setViewValue(View view, Object data,
                        String textRepresentation) {
                    switch (view.getId()) {
                    case R.id.comment_nickname:
                        TextView nicknameView = (TextView) view;
                        nicknameView.setText(textRepresentation);
                        return true;
                    case R.id.comment_created:
                        TextView createdView = (TextView) view;
                        createdView.setText(((Time) data).format("%Y-%m-%d"));
                        return true;
                    case R.id.comment_comment:
                        TextView commentView = (TextView) view;
                        commentView.setText(textRepresentation);
                        return true;
                    case R.id.comment_rating:
                        RatingBar rateView = (RatingBar) view;
                        rateView.setRating(Float.parseFloat(textRepresentation));
                        return true;
                    }
                    return false;
                }
            });
        }

        mSectionedAdapter.removeSection(2);
        mSectionedAdapter.addSection(2, (String) getText(R.string.comments), mCommentsAdapter);
        // This is really ugly, but notifyDataSetChanged is crashing on some items...
        setListAdapter(mSectionedAdapter);
        //mSectionedAdapter.notifyDataSetChanged();
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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Section section = mSectionedAdapter.getSection(position);
        Object item = mSectionedAdapter.getItem(position);

        if (item instanceof Volume) {
            Volume volume = (Volume) item;
            if (!volume.isRetired()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://systembolaget.se/SokDrycker/Produkt?VaruNr="
                                + volume.getArticleId()));
                startActivity(browserIntent);
            }
        } else if (section.adapter instanceof UserRatingAdapter) {
            String mToken = AuthStore.getInstance().getStoredToken(this);
            if (mToken != null) {
                showDialog(RATE_DIALOG);
            } else {
                showDialog(NOT_AUTHENTICATED_DIALOG);
            }
        }
    }

    @Override
    public boolean onSearchRequested() {
        startSearch(null, false, null, false);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu_drink_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_goto_iglaset:
                String name = URLEncoder.encode(sDrink.getName());
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                        Uri.parse("http://www.iglaset.se/dryck/" + name + "/" + sDrink.getId()));
                startActivity(browserIntent);
                return true;
            case R.id.menu_search:
                onSearchRequested();
                return true;
            case R.id.menu_rate:
                if (mToken != null) {
                    showDialog(RATE_DIALOG);
                } else {
                    showDialog(NOT_AUTHENTICATED_DIALOG);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
            case RATE_DIALOG:
                //final RatingBar userRatingBar = (RatingBar) findViewById(R.id.user_drink_rating);
                float userRating = mUserRatingAdapter.getUserRating();

                final RatingBar ratingBar = new RatingBar(this);
                ratingBar.setNumStars(5);
                ratingBar.setStepSize((float) 0.5);
                ratingBar.setRating(userRating / 2);
                LinearLayout layout = new LinearLayout(this);
                layout.setLayoutParams(new LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                layout.addView(ratingBar);

                return new AlertDialog.Builder(this)
                    .setTitle("Sätt betyg")
                    .setMessage("Ditt betyg görs om till en 10-gradig skala.")
                    .setView(layout)
                    .setPositiveButton("Ok", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new SetUserRatingTask().execute(ratingBar.getRating() * 2);
                        }
                    })
                    .setNegativeButton("Cancel", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ;
                        }
                    })
                    .create();
            case NOT_AUTHENTICATED_DIALOG:
                return new AlertDialog.Builder(this)
                .setTitle("Ej inloggad")
                .setMessage("För att sätta betyg måste du först logga in.")
                .setPositiveButton("Logga in", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(DrinkDetailActivity.this, BasicPreferenceActivity.class);
                        startActivityForResult(i, SETTINGS_CHANGED_REQUEST);
                    }
                })
                .setNegativeButton("Tillbaka", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ;
                    }
                })
                .create();
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETTINGS_CHANGED_REQUEST) {
            new GetDrinkTask().execute(sDrink.getId());
        }
    }

    /**
     * Background task for fetching comments
     */
    private class GetCommentsTask extends AsyncTask<Integer, Void, ArrayList<Comment>> {

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
            updateCommentsInUi(result);
        }
    }

    /**
     * Background task for setting a user rating.
     */
    private class SetUserRatingTask extends AsyncTask<Float, Void, Float> {

        @Override
        protected Float doInBackground(Float... params) {
            publishProgress();

            new DrinksStore().rateDrink(sDrink, params[0], mToken);

            return params[0];
        }

        @Override
        public void onProgressUpdate(Void... values) {
            //setProgressBarIndeterminateVisibility(true);
            Toast.makeText(DrinkDetailActivity.this, "Sätter betyg", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Float rating) {
            //setProgressBarIndeterminateVisibility(false);
            Toast.makeText(DrinkDetailActivity.this, "Betyg satt", Toast.LENGTH_SHORT).show();
            updateUserRatingInUi(rating);
        }
    }

    /**
     * Background task for fetching comments
     */
    private class GetDrinkTask extends AsyncTask<Integer, Void, Drink> {

        @Override
        protected Drink doInBackground(Integer... params) {
            publishProgress();
            return new DrinksStore().getDrink(params[0], mToken);
        }

        @Override
        public void onProgressUpdate(Void... values) {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected void onPostExecute(Drink drink) {
            setProgressBarIndeterminateVisibility(false);
            onUpdatedDrink(drink);
        }
    }

    private class VolumeAdapter extends ArrayAdapter<Volume> {
        public VolumeAdapter(Context context, List<Volume> objects) {
            super(context, R.layout.volume_row, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Skip recycling for now...
            convertView = getLayoutInflater().inflate(R.layout.volume_row, parent, false);

            Volume volume = getItem(position);
            TextView id = (TextView) convertView.findViewById(R.id.volume_id);
            id.setText(String.valueOf(volume.getArticleId()));

            TextView amount = (TextView) convertView.findViewById(R.id.volume_amount);
            amount.setText(String.valueOf(volume.getVolume()));

            TextView price = (TextView) convertView.findViewById(R.id.volume_price);
            price.setText(volume.getPriceSek() + " kr"); // TODO should fix hard coded string

            if (volume.isRetired()) {
                id.setPaintFlags(id.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                amount.setPaintFlags(amount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                price.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            return convertView;
        }
    }

    private class UserRatingAdapter extends ArrayAdapter<String> {
        private float mUserRating;
        public UserRatingAdapter(Context context, float userRating) {
            super(context, R.layout.user_rating, new ArrayList<String>());
            add("1");
            this.mUserRating = userRating;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.user_rating, parent, false);
            }
            RatingBar ratingBar = (RatingBar) convertView.findViewById(R.id.user_drink_rating);
            ratingBar.setRating(mUserRating);

            return convertView;
        }

        public void setUserRating(float userRating) {
            mUserRating = userRating;
        }

        public float getUserRating() {
            return mUserRating;
        }

    }
}
