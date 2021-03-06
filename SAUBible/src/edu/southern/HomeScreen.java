package edu.southern;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import edu.southern.R;
import edu.southern.data.BEngineFileMover;
import edu.southern.resources.*;

// Inherit from Sliding Fragment
public class HomeScreen extends SlidingFragmentActivity {
	private int ActionBarView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_screen);
		// copy files to device
		// Bind navigation fragment to the SlidingMenu Drawer -- set it as the
		// Behind View
		setBehindContentView(R.layout.fragment_nav_drawer);
		EditText referenceSearch = (EditText) (findViewById(R.id.referenceInput));
		// set a listener on the reference search input so that pressing the
		// enter key will trigger a press of the go button
		referenceSearch.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
						|| (actionId == EditorInfo.IME_ACTION_DONE)) {
					findViewById(R.id.referenceGo).performClick();
				}
				return false;
			}
		});

		// Set attributes of the menu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		// sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Add Home fragment to view group as default view
		// We only want to run this if the app is being run for the first time
		if (savedInstanceState == null) {
			transferAssetFiles();
			initiallizeSharedPreferences(); // place Genesis 1:1 in preferences
											// and set text in nav drawer
			FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
			Home homeFragment = new Home();
			fragmentTransaction.add(R.id.homeFragmentContainer, homeFragment);
			fragmentTransaction.commit();
			ActionBarView = R.layout.actionbar_home;
		} else {
			ActionBarView = savedInstanceState.getInt("ActionBarView");
		}
		setActionBarView(ActionBarView);
	}

	@Override
	protected void onSaveInstanceState(Bundle save) {
		super.onSaveInstanceState(save);
		save.putInt("ActionBarView", ActionBarView);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Intercept back button presses
		// Pop a fragment off the back stack if possible,
		// otherwise close the activity
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (getFragmentManager().getBackStackEntryCount() == 0) {
				this.finish();
				return false;
			} else {
				getFragmentManager().popBackStack();
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public void setActionBarView(int viewId) {
		ActionBar bar = getActionBar();
		ActionBarView = viewId;
		bar.setCustomView(ActionBarView);
	}

	/**
	 * Replace the current fragment being displayed with a new fragment and add
	 * the current fragment to the backstack
	 * 
	 * @param fragmentId
	 *            ID of the fragment layout to display
	 */
	public void replaceFragment(Fragment newFrag) {
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.homeFragmentContainer, newFrag);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	// Toggle Drawer on Up button in action bar
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle(); // Show / Hide Sliding Menu Fragment
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	// click hander for the nav drawer reference search button
	public void onReferenceGoClick(View v) {
		// retrieve the desired reference from the textbox
		EditText input = (EditText) findViewById(R.id.referenceInput);
		String text = input.getText().toString();
		if (text.equals(""))
			return;
		// retrieve the engine from the application
		// use a bible helper to parse the reference
		BibleHelper helper = new BibleHelper();
		// get the keyboard for later use

		try {
			Reference reference = helper.parseReference(text);
			// Save the value of the chapter selected in SharePreferences
			SharedPreferences settings = getSharedPreferences("edu.southern", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = settings.edit();
			// I'd like to refactor how the numbers are handled eventually
			// But I'm conforming to the in-place model for now
			// ~Isaac Hermens
			editor.putInt("book_value", reference.getBookNumber());
			editor.putInt("chapter_value", reference.getChapterNumber() - 1);
			editor.putInt("verse_value", reference.getVerseNumber() - 1);
			editor.commit();

			// Create new fragment and transaction
			Fragment readerFragment = new BibleReader();
			replaceFragment(readerFragment);
			// Commit the transaction
			setActionBarView(R.layout.actionbar_reading);

			// Hide the drawer
			getSlidingMenu().toggle();
			input.setText("");
			// hide the keyboard
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		} catch (ReferenceStringException e) {
			// TODO prevent keyboard from closing on enter key press if possible
			//CharSequence toastText = e.getMessage();
			//int duration = Toast.LENGTH_LONG;
			Toast.makeText(HomeScreen.this, "Reference not found, please enter a reference such as \"Exodus\", \"John 3\" or \"Matthew 3 17\"", Toast.LENGTH_LONG).show();
			
		}
	}

	// on click handler for navigation buttons in the drawer
	public void onDrawerItemSelection(View v) {
		// Switch to appropriate fragment
		// and swap out views in the action bar
		switch (v.getId()) {
		case R.id.HomeButton:
			Home homeFragment = new Home();
			replaceFragment(homeFragment);
			break;
		case R.id.BibleButton:
			Bible bibleFragment = new Bible();
			replaceFragment(bibleFragment);
			break;
		case R.id.ReadingButton:
			BibleReader readerFragment = new BibleReader();
			replaceFragment(readerFragment);
			break;
		case R.id.BookmarksButton:
			Bookmarks bookmarkFragment = new Bookmarks();
			replaceFragment(bookmarkFragment);
			break;
		case R.id.SettingsButton:
			Settings settingsFragment = new Settings();
			replaceFragment(settingsFragment);
			break;
		case R.id.SearchButton:
			Search searchFragment = new Search();
			replaceFragment(searchFragment);
			break;
		default:
			break;
		}
		getSlidingMenu().toggle(); // hide the drawer
	}

	// click handler for action bar buttons
	public void onActionBarButtonClick(View v) {
		Fragment fragment;
		switch (v.getId()) {
		case R.id.ActionBarHome:
			break;

		case R.id.ActionBarBookmarks:
			break;

		case R.id.ActionBarBible:
			break;

		case R.id.ActionBarBook:
			fragment = new Bible();
			replaceFragment(fragment);
			break;
		case R.id.ActionBarChapter:
			fragment = new ChapterSelection();
			replaceFragment(fragment);
			break;
		case R.id.ActionBarReading:
			fragment = new Bible();
			replaceFragment(fragment);
			break;
		case R.id.ActionBarSearch:
			break;

		case R.id.ActionBarSettings:
			break;

		default:
			break;
		}
		return;
	}

	/**
	 * Copy the asset files used by the bible engine onto the device
	 */
	protected void transferAssetFiles() {
		BEngineFileMover fileMover = new BEngineFileMover(
				getApplicationContext());
		fileMover.copyDataFiles();
	}

	/**
	 * Attempt to retrieve book, chapter, and verse values from the shared
	 * preferences If values have not been stored, place defaults (Genesis 1:1)
	 * into the shared preferences Update the text currently reading button in
	 * the nav drawer
	 */
	private void initiallizeSharedPreferences() {
		SharedPreferences prefs = getSharedPreferences("edu.southern",
				Context.MODE_PRIVATE);
		int book = prefs.getInt("book_value", -1);
		// If no book number has been stored in the preferences
		// Initiallize it to Genesis 1:1
		if (book == -1) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt("book_value", 0);
			editor.putInt("chapter_value", 0);
			editor.putInt("verse_value", 0);
			editor.commit();
			book = 0;
		}
		int chapter = prefs.getInt("chapter_value", 0) + 1;
		int verse = prefs.getInt("verse_value", 0) + 1;
		updateCurrentlyReading(book, chapter, verse);
	}

	/**
	 * Set the text of the Currently Reading button in the nav drawer To show
	 * what book, chapter, and verse the user has selected
	 * 
	 * @param bookNumber
	 *            The 0-based number of the book to display
	 * @param chapterNumber
	 *            The chapter number to display
	 * @param verseNumber
	 *            The verse number to display
	 */
	public void updateCurrentlyReading(int bookNumber, int chapterNumber,
			int verseNumber) {
		BibleHelper helper = new BibleHelper();
		String book = helper.getBookName(bookNumber);
		String currentLocation = "Read: "
				.concat(book)
				.concat(" ")
				.concat(Integer.toString(chapterNumber).concat(":")
						.concat(Integer.toString(verseNumber)));
		((TextView) findViewById(R.id.CurrentlyReadingText))
				.setText(currentLocation);
	}
}
