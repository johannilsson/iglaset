package com.markupartist.iglaset.activity;

import java.io.IOException;
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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.SimpleAdapter.ViewBinder;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.markupartist.iglaset.R;
import com.markupartist.iglaset.activity.SectionedAdapter.Section;
import com.markupartist.iglaset.provider.AuthStore;
import com.markupartist.iglaset.provider.BarcodeStore;
import com.markupartist.iglaset.provider.Comment;
import com.markupartist.iglaset.provider.CommentsStore;
import com.markupartist.iglaset.provider.Drink;
import com.markupartist.iglaset.provider.DrinksStore;
import com.markupartist.iglaset.provider.Drink.Volume;
import com.markupartist.iglaset.util.ImageLoader;
import com.markupartist.iglaset.util.Tracker;

public class DrinkDetailActivity extends ListActivity {
    /**
     * Key to identify a barcode
     */
    private static final String EXTRA_BARCODE = "com.markupartist.iglaset.article.barcode";
    /**
     * The id for the rating dialog
     */
    private static final int DIALOG_RATE = 0;
    /**
     * The id for the not authenticated dialog
     */
    private static final int DIALOG_NOT_AUTHENTICATED = 1;
    /**
     * The id for suggest barcode failed dialog
     */
    private static final int DIALOG_SUGGEST_BARCODE_FAIL = 2;
    /**
     * The id for the choose add barcode method dialog
     */
    private static final int DIALOG_CHOOSE_ADD_BARCODE_METHOD = 3;
    /**
     * The id for the add barcode manual dialog 
     */
    private static final int DIALOG_ADD_BARCODE_MANUAL = 4;
    /**
     * The request code for indicating that settings has been changed
     */
    protected static final int REQUEST_CODE_SETTINGS_CHANGED = 0;

    /**
     * The log tag
     */
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
        Tracker.getInstance().trackPageView("article detail");

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_NO_TITLE); 

        setContentView(R.layout.drink_details);

        mToken = AuthStore.getInstance().getStoredToken(this);
        
        Bundle extras = getIntent().getExtras();
        final Drink drink = extras.getParcelable("com.markupartist.iglaset.Drink");

        mUserRatingAdapter = new UserRatingAdapter(this, 0);
        mSectionedAdapter.addSectionFirst(0, getText(R.string.my_rating), mUserRatingAdapter);

        if (mToken != null) {
            new GetDrinkTask().execute(drink.getId());
        }

        TextView nameTextView = (TextView) findViewById(R.id.drink_name);
        if(drink.hasUserRating()) {
        	Drawable icon = getResources().getDrawable(R.drawable.glass_icon);
        	icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        	nameTextView.setCompoundDrawables(null, null, icon, null);
        }

        String yearText = drink.getYear() == 0 ? "" : " " + String.valueOf(drink.getYear());
        nameTextView.setText(drink.getName() + yearText);
        
        TextView originCountryTextView = (TextView) findViewById(R.id.drink_origin_country);
        originCountryTextView.setText(drink.getConcatenatedOrigin());

        TextView alcoholPercentTextView = (TextView) findViewById(R.id.drink_alcohol_percent);
        alcoholPercentTextView.setText(drink.getAlcoholPercent());

        //TextView yearTextView = (TextView) findViewById(R.id.drink_year);
        //yearTextView.setText(drink.getYear() == 0 ? "" : String.valueOf(drink.getYear()));

        RatingBar drinkRatingBar = (RatingBar) findViewById(R.id.drink_rating);
        drinkRatingBar.setRating(Float.parseFloat(drink.getRating()));

        ImageView imageView = (ImageView) findViewById(R.id.drink_image);
        final Context ctx = this;
        imageView.setOnClickListener(new View.OnClickListener() {
        	@Override
			public
            void onClick(View view) {
        		ImageViewerDialog.showImage(ctx, drink.getLargestImageUrl());
        	}
        });
        
        ImageLoader.getInstance().load(imageView, drink.getThumbnailUrl(), 
                true, R.drawable.noimage, null);
        
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

    @Override
    protected void onResume() {
        super.onResume();
        mToken = AuthStore.getInstance().getStoredToken(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Tracker.getInstance().stop();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreLocalState(savedInstanceState);
    }

    /**
     * Restores the local state.
     * @param savedInstanceState the bundle containing the saved state
     */
    private void restoreLocalState(Bundle savedInstanceState) {
        restoreBarcode(savedInstanceState);
    }

    /**
     * Restores the barcode.
     * @param savedInstanceState the saved state
     */
    private void restoreBarcode(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(EXTRA_BARCODE)) {
            mBarcode = savedInstanceState.getString(EXTRA_BARCODE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveBarcodeState(outState);
    }

    /**
     * If there is any running search for routes, save it and process it later 
     * on.
     * @param outState the out state
     */
    private void saveBarcodeState(Bundle outState) {
        outState.putString(EXTRA_BARCODE, mBarcode);
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
    private String mBarcode;

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
            if (mToken != null) {
                showDialog(DIALOG_RATE);
            } else {
                showDialog(DIALOG_NOT_AUTHENTICATED);
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
                Tracker.getInstance().trackEvent(item);
                // Since iglaset.se does not really care about the name we just
                // removes the slash because otherwise iglaset.se will parse it
                // as a directory.
                String name = URLEncoder.encode(sDrink.getName().replace("/", ""));
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                        Uri.parse("http://www.iglaset.se/dryck/" + name + "/" + sDrink.getId()));
                startActivity(browserIntent);
                return true;
            case R.id.menu_search:
                onSearchRequested();
                return true;
            case R.id.menu_rate:
                if (mToken != null) {
                    showDialog(DIALOG_RATE);
                } else {
                    showDialog(DIALOG_NOT_AUTHENTICATED);
                }
                return true;
            case R.id.menu_scan:
                if (mToken != null) {
                    showDialog(DIALOG_CHOOSE_ADD_BARCODE_METHOD);
                } else {
                    showDialog(DIALOG_NOT_AUTHENTICATED);                    
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
        case DIALOG_SUGGEST_BARCODE_FAIL:
            return new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Ett fel inträffade")
                .setMessage("Misslyckades med att spara streckkoden")
                .setPositiveButton("Försök igen", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new SuggestBarcodeTask().execute(
                                mBarcode, sDrink.getId(), mToken);                        
                    }
                })
                .setNegativeButton(getText(android.R.string.cancel), null)
                .create();
            case DIALOG_RATE:
                float userRating = mUserRatingAdapter.getUserRating();
                final View layout = getLayoutInflater().inflate(R.layout.user_rating_dialog, null);
                final TextView ratingValue = (TextView) layout.findViewById(R.id.add_user_rating_value);
                ratingValue.setText("Ditt betyg " + userRating);
                final RatingBar ratingBar = (RatingBar) layout.findViewById(R.id.add_user_rating);
                ratingBar.setNumStars(5);
                ratingBar.setStepSize((float) 0.5);
                ratingBar.setRating(userRating / 2);
                ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
                    public void onRatingChanged(RatingBar ratingBar, float rating,
                            boolean fromUser) {
                        ratingValue.setText("Ditt betyg " + rating * 2);
                    }
                });

                return new AlertDialog.Builder(this)
                    .setTitle(getText(R.string.add_rating))
                    .setView(layout)
                    .setPositiveButton(android.R.string.ok, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            new SetUserRatingTask().execute(ratingBar.getRating() * 2);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ;
                        }
                    })
                    .create();
            case DIALOG_NOT_AUTHENTICATED:
                return new AlertDialog.Builder(this)
                .setTitle(getText(R.string.not_logged_in))
                .setMessage(R.string.login_to_proceed_message)
                .setPositiveButton("Logga in", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(DrinkDetailActivity.this, BasicPreferenceActivity.class);
                        startActivityForResult(i, REQUEST_CODE_SETTINGS_CHANGED);
                    }
                })
                .setNegativeButton(R.string.back, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ;
                    }
                })
                .create();
            case DIALOG_CHOOSE_ADD_BARCODE_METHOD:
                CharSequence[] methods = {"Scanna", "Manuellt"};
                return new AlertDialog.Builder(this)
                    .setTitle("Hur vill du lägga in koden?")
                    .setItems(methods, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                            case 0:
                                IntentIntegrator.initiateScan(DrinkDetailActivity.this);
                                break;
                            case 1:
                                showDialog(DIALOG_ADD_BARCODE_MANUAL);
                                break;
                            }
                        }
                    })
                    .create();
            case DIALOG_ADD_BARCODE_MANUAL:
                final View addBarcodeLayout = getLayoutInflater().inflate(
                        R.layout.add_barcode_dialog, null);
                final EditText addBarcodeEditText =
                        (EditText) addBarcodeLayout.findViewById(R.id.add_barcode);
                addBarcodeEditText.setSelected(true);
                return new AlertDialog.Builder(this)
                    .setTitle("Lägg in streckkod")
                    .setView(addBarcodeLayout)
                    .setPositiveButton("Spara", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mBarcode = addBarcodeEditText.getText().toString();
                            new SuggestBarcodeTask().execute(
                                    mBarcode, sDrink.getId(), mToken);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create();
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_SETTINGS_CHANGED:
                new GetDrinkTask().execute(sDrink.getId());
            case IntentIntegrator.REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                    if (scanResult != null) {
                        Log.d(TAG, "contents: " + scanResult.getContents());
                        Log.d(TAG, "formatName: " + scanResult.getFormatName());
                        mBarcode = scanResult.getContents();
                        new SuggestBarcodeTask().execute(
                                scanResult.getContents(), sDrink.getId(), mToken);
                    } else {
                        Log.d(TAG, "NO SCAN RESULT");
                    }   
                }
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

            DrinksStore.getInstance().rateDrink(sDrink, params[0], mToken);

            return params[0];
        }

        @Override
        public void onProgressUpdate(Void... values) {
            Toast.makeText(DrinkDetailActivity.this, getText(R.string.adding_rating), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Float rating) {
            Toast.makeText(DrinkDetailActivity.this, getText(R.string.rating_added), Toast.LENGTH_SHORT).show();
            updateUserRatingInUi(rating);
        }
    }

    /**
     * Background task for suggesting a new barcode.
     *
     */
    private class SuggestBarcodeTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            try {
                return BarcodeStore.getInstance().suggest(
                        (String)params[0], (Integer)params[1], (String)params[2]);
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        public void onProgressUpdate(Void... values) {
            Toast.makeText(DrinkDetailActivity.this, "Sparar streckkod", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Boolean response) {
            if (response == true) {
                Toast.makeText(DrinkDetailActivity.this, "Streckkod sparad", Toast.LENGTH_SHORT).show();
            } else {
                showDialog(DIALOG_SUGGEST_BARCODE_FAIL);
            }
        }
    }
    
    /**
     * Background task for fetching a drink.
     */
    private class GetDrinkTask extends AsyncTask<Integer, Void, Drink> {

        @Override
        protected Drink doInBackground(Integer... params) {
            publishProgress();
            return DrinksStore.getInstance().getDrink(params[0], mToken);
        }

        @Override
        protected void onPostExecute(Drink drink) {
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
            amount.setText(String.valueOf(volume.getVolume()) + " ml"); // TODO should fix hard coded string

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
