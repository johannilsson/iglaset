package com.markupartist.iglaset.activity;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.SimpleAdapter.ViewBinder;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.IntentAction;
import com.markupartist.iglaset.IglasetApplication;
import com.markupartist.iglaset.R;
import com.markupartist.iglaset.provider.AuthStore;
import com.markupartist.iglaset.provider.AuthStore.Authentication;
import com.markupartist.iglaset.provider.AuthenticationException;
import com.markupartist.iglaset.provider.BarcodeStore;
import com.markupartist.iglaset.provider.Comment;
import com.markupartist.iglaset.provider.CommentsStore;
import com.markupartist.iglaset.provider.Drink;
import com.markupartist.iglaset.provider.DrinksStore;
import com.markupartist.iglaset.provider.Drink.Volume;
import com.markupartist.iglaset.provider.Tag;
import com.markupartist.iglaset.util.ListUtils;
import com.markupartist.iglaset.util.MultiHashMap;
import com.markupartist.iglaset.widget.DrinkDescriptionAdapter;
import com.markupartist.iglaset.widget.SearchAction;
import com.markupartist.iglaset.widget.SectionedAdapter;
import com.markupartist.iglaset.widget.SectionedAdapter.Section;
import com.markupartist.iglaset.widget.VolumeAdapter;

public class DrinkDetailActivity extends ListActivity implements View.OnClickListener {
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
     * The id for showing the drink image dialog.
     */
    private static final int DIALOG_SHOW_DRINK_IMAGE = 5;
    /**
     * The id for showing the comment add dialog.
     */
    private static final int DIALOG_ADD_COMMENT = 6;
    /**
     * The id for the network problem dialog.
     */
    private static final int DIALOG_SEARCH_NETWORK_PROBLEM = 7;
    /**
     * The request code for indicating that settings has been changed
     */
    protected static final int REQUEST_CODE_SETTINGS_CHANGED = 0;

    /**
     * The log tag
     */
    private static final String TAG = DrinkDetailActivity.class.getSimpleName();
    private CommentsStore mCommentsStore = new CommentsStore();
    private SimpleAdapter mCommentsAdapter;
    private ArrayList<Comment> mComments;
    private static Drink sDrink;
    private UserRatingAdapter mUserRatingAdapter;
    private AuthStore.Authentication mAuthentication;
    private GetDrinkTask mGetDrinkTask;
    private GetCommentsTask mGetCommentsTask;
    private DrinkViewHolder mViewHolder;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.drink_details);

        mAuthentication = getAuthentication();
        
        Bundle extras = getIntent().getExtras();
        final Drink drink = extras.getParcelable(Intents.EXTRA_DRINK);
        sDrink = drink;

        ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        actionBar.setHomeAction(new IntentAction(this, StartActivity.createIntent(this), R.drawable.ic_actionbar_home_default));
        actionBar.setTitle(sDrink.getName());
        actionBar.addAction(new SearchAction() {
            @Override
            public void performAction(View view) {
                onSearchRequested();
            }
        });
        
        // Populate drink details
        View detailsLayout = this.findViewById(R.id.drink_detail_layout);
        mViewHolder = new DrinkViewHolder(detailsLayout);
        mViewHolder.populate(this, drink, mImageClickListener);
    	float rating = (drink.hasEstimatedRating() ? drink.getEstimatedRating() : drink.getAverageRating());
    	mViewHolder.getRateView().setRating(rating);
        
        mUserRatingAdapter = new UserRatingAdapter(this, 0);
        this.updateUserRatingInUi(drink.getUserRating());
        mSectionedAdapter.addSectionFirst(0, getText(R.string.my_rating), mUserRatingAdapter);

        if (mAuthentication != null && mAuthentication.looksValid()) {
        	launchGetDrinkTask(drink);
        }

        if(true == drink.hasDescription()) {
        	DrinkDescriptionAdapter adapter = new DrinkDescriptionAdapter(this, drink.getDescription());
        	mSectionedAdapter.addSection(0, getText(R.string.manufacturers_description), adapter);
        }
        
        ArrayList<Tag> tags = drink.getTags();
	    if(tags.isEmpty() == false) {
	    	MultiHashMap<String, Tag> tagMap = ListUtils.toMultiHashMap(tags);
	        Set<Entry<String, List<Tag>>> nameSet = tagMap.entrySet();
	        for (Entry<String, List<Tag>> entry : nameSet) {
	            mSectionedAdapter.addSection(0, entry.getKey(),
	            		new ArrayAdapter<Tag>(this, 
	                    R.layout.simple_row, entry.getValue()));
	        }
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
        if (comments != null) {
        	updateCommentsInUi(comments);
        } else if(drink.getCommentCount() == 0) {
        	// Update the comment list with an empty array
        	updateCommentsInUi(new ArrayList<Comment>());
        } else {
            launchGetCommentsTask(drink);
        }

        setListAdapter(mSectionedAdapter);
        
        // Orphan barcode handler layout
		View orphanLayout = findViewById(R.id.orphan_barcode_layout);
		
		TextView textView = (TextView) orphanLayout.findViewById(R.id.orphan_barcode_text);
		textView.setText(String.format(this.getString(R.string.add_suggested_barcode), getApp().getOrphanBarcode()));
		
		Button buttonAdd = (Button) orphanLayout.findViewById(R.id.btn_add_orphan_code);
		buttonAdd.setOnClickListener(this);
		
		Button buttonForget = (Button) orphanLayout.findViewById(R.id.btn_forget_orphan_barcode);
		buttonForget.setOnClickListener(this);
		
		Button buttonCancel = (Button) orphanLayout.findViewById(R.id.btn_cancel);
		buttonCancel.setOnClickListener(this);
        
        this.registerReceiver(mBroadcastReceiver, new IntentFilter(Intents.ACTION_PUBLISH_DRINK));
    }
    
    @Override
    public void onClick(View view) {
    	switch(view.getId()) {
    	case R.id.btn_add_orphan_code:
    		hideOrphanBarcodeLayout();
            new SuggestBarcodeTask().execute(getApp().getOrphanBarcode(), sDrink, mAuthentication);            
            break;
    	case R.id.btn_forget_orphan_barcode:
    		hideOrphanBarcodeLayout();
    		getApp().clearOrphanBarcode();            
    		break;
    	case R.id.btn_cancel:
            hideOrphanBarcodeLayout();
    		break;
    	}
    }
    
    private final View.OnClickListener mImageClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			showDialog(DIALOG_SHOW_DRINK_IMAGE);
		}
	};
    
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Intents.ACTION_PUBLISH_DRINK)) {
				final Drink drink = (Drink) intent.getExtras().get(Intents.EXTRA_DRINK);
				DrinkDetailActivity.this.onUpdatedDrink(drink);
			}
		}
    };
    
    private void showOrphanBarcodeLayout() {
		View orphanLayout = findViewById(R.id.orphan_barcode_layout);
		orphanLayout.setVisibility(View.VISIBLE);
		Animation animation = AnimationUtils.loadAnimation(getBaseContext(), R.anim.push_up_in);
		orphanLayout.startAnimation(animation);		
    }
    
    private void hideOrphanBarcodeLayout() {
		final View orphanLayout = findViewById(R.id.orphan_barcode_layout);
		Animation animation = AnimationUtils.loadAnimation(getBaseContext(), R.anim.push_down_out);
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation anim) {
				orphanLayout.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation anim) {}

			@Override
			public void onAnimationStart(Animation anim) {}
		});
		orphanLayout.startAnimation(animation);
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
    	super.onPostCreate(savedInstanceState);
    	
    	if(shouldShowOrphanBarcodeLayout()) {
    		showOrphanBarcodeLayout();
    	}
    }
    
    private boolean shouldShowOrphanBarcodeLayout() {
    	return !TextUtils.isEmpty(getApp().getOrphanBarcode()) && isLoggedIn();
    }
    
    private IglasetApplication getApp() {
    	return (IglasetApplication) getApplication();
    }
    
    private AuthStore.Authentication getAuthentication() {
        try {
			return AuthStore.getInstance().getAuthentication(this);
		} catch (AuthenticationException e) {
			return null;
		}
    }
    
    private boolean isLoggedIn() {
    	return mAuthentication != null && mAuthentication.looksValid();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuthentication = getAuthentication();
    }

    @Override
    protected void onDestroy() {
    	cancelGetDrinkTask();
    	cancelGetCommentsTask();
    	
    	unregisterReceiver(mBroadcastReceiver);
 
        super.onDestroy();
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
        if (savedInstanceState.containsKey(Intents.EXTRA_BARCODE)) {
            mBarcode = savedInstanceState.getString(Intents.EXTRA_BARCODE);
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
        outState.putString(Intents.EXTRA_BARCODE, mBarcode);
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
        mViewHolder.update(this, drink);
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

        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        if (comments.isEmpty()) {
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
                        R.id.comment_rating,
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
                        createdView.setText(textRepresentation);
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
            startActivity(createInventoryIntent(volume));
        } else if (item instanceof Tag) {
        	Tag tag = (Tag) item;
        	ArrayList<Integer> tags = new ArrayList<Integer>();
        	tags.add(tag.getId());
            Intent searchIntent = new Intent(this, SearchResultActivity.class);
            searchIntent.putExtra(SearchResultActivity.EXTRA_SEARCH_TAGS, tags);
            searchIntent.putExtra(SearchResultActivity.EXTRA_SEARCH_TAGS_SELECTED, tag.getName());
            startActivity(searchIntent); 
        } else if (section.adapter instanceof UserRatingAdapter) {
        	tryShowAuthenticatedDialog(DIALOG_RATE);
        } else if (section.adapter instanceof DrinkDescriptionAdapter) {
        	DrinkDescriptionAdapter description = (DrinkDescriptionAdapter) section.adapter;
        	description.toggleVisibility();
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
        
        //TODO is this ("Search") really needed?
        menu.removeItem(4);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_goto_iglaset:
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
            	tryShowAuthenticatedDialog(DIALOG_RATE);
                return true;
            case R.id.menu_scan:
            	tryShowAuthenticatedDialog(DIALOG_CHOOSE_ADD_BARCODE_METHOD);
                return true;
            case R.id.menu_add_comment:
            	tryShowAuthenticatedDialog(DIALOG_ADD_COMMENT);
            	return true;
            case R.id.menu_home:
                startActivity(new Intent(this, StartActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Try to show a dialog which requires that the user is logged in. If the
     * user is logged in then the specified dialog will be shown, otherwise the
     * standard authentication (DIALOG_NOT_AUTHENTICATED) dialog will be shown.
     * @param dialog
     */
    private void tryShowAuthenticatedDialog(int dialog) {
    	if(isLoggedIn()) {
    		showDialog(dialog);
    	} else {
    		showDialog(DIALOG_NOT_AUTHENTICATED);
    	}
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
        case DIALOG_SUGGEST_BARCODE_FAIL:
            return new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Ett fel inträffade")
                .setMessage("Misslyckades med att spara streckkoden")
                .setPositiveButton(R.string.retry, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new SuggestBarcodeTask().execute(mBarcode, sDrink, mAuthentication);                        
                    }
                })
                .setNegativeButton(getText(android.R.string.cancel), new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if(shouldShowOrphanBarcodeLayout()) {
							showOrphanBarcodeLayout();
						}
						dismissDialog(DIALOG_SUGGEST_BARCODE_FAIL);
					}	
                })
                .create();
            case DIALOG_RATE:
                float userRating = mUserRatingAdapter.getUserRating();
                final View layout = getLayoutInflater().inflate(R.layout.user_rating_dialog, null);
                final TextView ratingValue = (TextView) layout.findViewById(R.id.add_user_rating_value);
                ratingValue.setText("Ditt betyg " + userRating);
                final RatingBar ratingBar = (RatingBar) layout.findViewById(R.id.add_user_rating);
                ratingBar.setNumStars(5);
                ratingBar.setStepSize(0.5f);
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
                .setNegativeButton(R.string.back, null)
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
                    .setTitle(getText(R.string.add_barcode))
                    .setView(addBarcodeLayout)
                    .setPositiveButton("Spara", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mBarcode = addBarcodeEditText.getText().toString();
                            new SuggestBarcodeTask().execute(mBarcode, sDrink, mAuthentication);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create();
            case DIALOG_SHOW_DRINK_IMAGE:
            	return new DrinkImageViewerDialog(this, sDrink);
            case DIALOG_ADD_COMMENT:
            	final View addCommentLayout = getLayoutInflater().inflate(R.layout.add_comment_dialog, null);
            	final EditText commentText = (EditText) addCommentLayout.findViewById(R.id.add_comment_text);
            	
            	final AlertDialog dialog = new AlertDialog.Builder(this)
            		.setTitle(R.string.add_comment)
            		.setView(addCommentLayout)
            		.setIcon(android.R.drawable.sym_action_chat)
            		.setPositiveButton(android.R.string.ok, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							new AddCommentTask().execute(
									commentText.getText().toString(),
									sDrink,
									mAuthentication);
						}
            			
            		})
            		.setNegativeButton(android.R.string.cancel, null)
            		.create();
             	
            	// Attach listener which will toggle the "Ok" button's activeness
            	// based on if the text view is empty or not.
            	commentText.addTextChangedListener(new TextWatcher() {
            		@Override
					public void afterTextChanged(Editable editable) {
						View button = dialog.getButton(Dialog.BUTTON_POSITIVE);
						button.setEnabled(editable.length() > 0);
					}

					@Override
					public void beforeTextChanged(CharSequence text, int start, int count, int after) {}
					@Override
					public void onTextChanged(CharSequence text, int start, int before, int cout) {}
            		 
            	 });
            	
            	return dialog;
            	
            case DIALOG_SEARCH_NETWORK_PROBLEM:
            	return DialogFactory.createNetworkProblemDialog(
            			this,
            			new OnClickListener() {
    		                @Override
    		                public void onClick(DialogInterface dialog, int which) {
    		                	switch(which) {
    		                	case Dialog.BUTTON_POSITIVE:
    		                    	launchGetDrinkTask(sDrink);
    			                    break;
    		                	case Dialog.BUTTON_NEGATIVE:
    		                		break;
    		                	}
    		                }
    		             });
        }
        return null;
    }
    
    @Override
    protected void onPrepareDialog(final int id, final Dialog dialog) {
    	switch(id) {
    	case DIALOG_ADD_COMMENT:
    		AlertDialog alert = (AlertDialog) dialog;
        	final EditText textView = (EditText) dialog.findViewById(R.id.add_comment_text);
        	if(null != textView) {
        		alert.getButton(Dialog.BUTTON_POSITIVE).setEnabled(textView.length() > 0);
        	}
    		break;
    	case DIALOG_SHOW_DRINK_IMAGE:
    		DrinkImageViewerDialog imageViewer = (DrinkImageViewerDialog) dialog;
    		imageViewer.load();
    		break;
    	default:
    		break;
    	}
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_SETTINGS_CHANGED:
            	cancelGetDrinkTask();
            	launchGetDrinkTask(sDrink);
                break;
            case IntentIntegrator.REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                    if (scanResult != null) {
                        Log.d(TAG, "contents: " + scanResult.getContents());
                        Log.d(TAG, "formatName: " + scanResult.getFormatName());
                        mBarcode = scanResult.getContents();
                        new SuggestBarcodeTask().execute(scanResult.getContents(), sDrink, mAuthentication);
                    } else {
                        Log.d(TAG, "NO SCAN RESULT");
                    }   
                }
                break;
        }
    }

    /**
     * Create and execute a task to fetch the current drink's comments.
     */
    private void launchGetCommentsTask(Drink drink) {
        mGetCommentsTask = new GetCommentsTask();
        mGetCommentsTask.execute(drink);
    }
    
    /**
     * Cancel current GetCommentsTask if it has been created and if it is
     * currently executing.
     */
    private void cancelGetCommentsTask() {
    	if(null != mGetCommentsTask && mGetCommentsTask.getStatus() == AsyncTask.Status.RUNNING) {
    		mGetCommentsTask.cancel(true);
    		mGetCommentsTask = null;
    	}
    }
    
    /**
     * Background task for fetching comments
     */
    private class GetCommentsTask extends AsyncTask<Drink, Void, ArrayList<Comment>> {

        @Override
        protected ArrayList<Comment> doInBackground(Drink... params) {
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
            DrinksStore.getInstance().rateDrink(sDrink, params[0], mAuthentication);
            return params[0];
        }

        @Override
        public void onProgressUpdate(Void... values) {
            Toast.makeText(DrinkDetailActivity.this, getText(R.string.adding_rating), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Float rating) {
            Toast.makeText(DrinkDetailActivity.this, getText(R.string.rating_added), Toast.LENGTH_SHORT).show();
            
            final boolean hadUserRating = sDrink.hasUserRating();       
            sDrink.setUserRating(rating);
            if(hadUserRating != sDrink.hasUserRating()) {
            	// If the drink had a rating before but not now then decrease, otherwise increase
            	final int offset = hadUserRating ? -1 : 1;
            	sDrink.setRatingCount(sDrink.getRatingCount() + offset);            	
            }
            
        	sendBroadcast(Intents.createPublishDrinkIntent(sDrink));
        }
    }
    
    /**
     * @author marco
     * Background task for adding a comment.
     */
    private class AddCommentTask extends AsyncTask<Object, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Object... params) {
			try {
				String comment = (String) params[0];
				Drink drink = (Drink) params[1];
				AuthStore.Authentication authentication = (Authentication) params[2];
				return DrinksStore.getInstance().commentDrink(drink, comment, authentication);
			} catch(IOException e) {
				return false;
			}
		}
    	
		@Override
		public void onProgressUpdate(Void... values) {
			Toast.makeText(DrinkDetailActivity.this, getText(R.string.adding_comment), Toast.LENGTH_SHORT).show();
		}
		
		@Override
		protected void onPostExecute(Boolean response) {
			if(true == response) {
				Toast.makeText(DrinkDetailActivity.this, R.string.comment_saved, Toast.LENGTH_SHORT).show();
				
				sDrink.setCommentCount(sDrink.getCommentCount() + 1);
				
				// TODO: We might want to inject the comment directly into the adapter,
				// but just refresh all the comments for now.
				launchGetCommentsTask(sDrink);
				
				sendBroadcast(Intents.createPublishDrinkIntent(sDrink));
			} else {
				// TODO: Show a retry/cancel here or maybe a better text than just "failed"?
				Toast.makeText(DrinkDetailActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
			}
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
            	String barcode = (String) params[0];
            	Drink drink = (Drink) params[1];
            	AuthStore.Authentication authentication = (Authentication) params[2];
                return BarcodeStore.getInstance().suggest(barcode, drink, authentication);
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        public void onProgressUpdate(Void... values) {
            Toast.makeText(DrinkDetailActivity.this, "Sparar streckkod...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Boolean response) {
            if (response == true) {
                Toast.makeText(DrinkDetailActivity.this, "Streckkod sparad", Toast.LENGTH_SHORT).show();
                
                // Remove any orphan barcodes to prevent the "add orphan code?" dialog from showing again
                getApp().clearOrphanBarcode();
            } else {
                showDialog(DIALOG_SUGGEST_BARCODE_FAIL);
            }
        }
    }
    
    /**
     * Create and execute a task to fetch the current drink.
     */
    private void launchGetDrinkTask(Drink drink) {
        mGetDrinkTask = new GetDrinkTask();
        mGetDrinkTask.execute(drink.getId());
    }
    
    /**
     * Cancel current GetDrinkTask if it has been created and if it is
     * currently executing.
     */
    private void cancelGetDrinkTask() {
    	if(null != mGetDrinkTask && mGetDrinkTask.getStatus() == AsyncTask.Status.RUNNING) {
    		mGetDrinkTask.cancel(true);
    		mGetDrinkTask = null;
    	}
    }

    private Intent createInventoryIntent(Volume volume) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        String county = sharedPreferences
                .getString("sb_search_county", "0");

        Uri uri = Uri.parse(String.format(
                "http://mobil.systembolaget.se/SokDryck/SokDryck.aspx?artnr=%s&lan=%s",
                String.valueOf(volume.getArticleId()), county));

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        return intent;
    }

    /**
     * Background task for fetching a drink.
     */
    private class GetDrinkTask extends AsyncTask<Integer, Void, Drink> {

        @Override
        protected Drink doInBackground(Integer... params) {
            publishProgress();
            return DrinksStore.getInstance().getDrink(params[0], mAuthentication);
        }

        @Override
        protected void onPostExecute(Drink drink) {
        	if(null != drink) {
        		onUpdatedDrink(drink);
        	} else {
                showDialog(DIALOG_SEARCH_NETWORK_PROBLEM);
        	}
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
