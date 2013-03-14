/*
 * Copyright (C) 2013 Mapyst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mapyst.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mapyst.route.Direction;
import com.mapyst.route.InterpretResult;
import com.mapyst.route.InterpretedInfo;
import com.mapyst.route.Interpreter;
import com.mapyst.route.LatLngPoint;
import com.mapyst.route.Route;
import com.mapyst.route.RoutePreferences;
import com.mapyst.route.Waypoint2D;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.Overlay;
import com.mapyst.android.asynctask.RouteMakerTaskPrefs;
import com.mapyst.android.ui.CenteredToastFactory;
import com.mapyst.android.ui.CompassOverlay;
import com.mapyst.android.ui.DirectionIcon;
import com.mapyst.android.ui.DrawingHelpers;
import com.mapyst.android.ui.LocationsListView;
import com.mapyst.android.ui.RouteMapOverlay;
import com.mapyst.android.ui.SlidingScrollView;
import com.mapyst.android.ui.map.LocationChooserOverlay;
import com.mapyst.android.ui.map.MapViewMover;
import com.mapyst.android.ui.map.OnMapTouchLimiterListener;
import com.mapyst.android.ui.map.PriorityMapView;
import com.mapyst.android.ui.map.ViewItemOverlay;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainScreen extends MapActivity {

	// Global Info
	private Mapyst app;

	// Device Location and Orientation
	private LocationFinder locFinder;
	private Compass compass;

	// UI Elements
	private AutoCompleteTextView startTextView;
	private AutoCompleteTextView endTextView;
	private IntentAction listAction;
	private Action backAction;

	private ViewItemOverlay viewItemOverlay;

	private Button rightArrow;
	private Button leftArrow;

	private ImageView directionEnd;
	private ImageView directionStart;

	private Button getDirectionsButton;

	// Mapping
	private PriorityMapView mapView;
	private MapController mcontrol;

	// Routing
	private RouteMapOverlay graphOverlay;
	private int curDir;

	// Screen Size
	private int height;
	private int width;

	private SlidingScrollView slidingView;

	private Handler handler;

	private CompassOverlay compassOverlay = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		app = (Mapyst) (this.getApplication());
		handler = new Handler();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		boolean showNotificationBar = prefs.getBoolean("notificationBox", true);
		Log.d("show bar", showNotificationBar + "");
		if (!showNotificationBar) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		setContentView(R.layout.main_screen);
		setupActionBar();

		CampusLoader loader = new CampusLoader(this, app, new OnCampusLoadedListener());
		loader.load(26);

		locFinder = new LocationFinder(app);
		locFinder.setupLocService();

		setupUI();

	}

	private class OnCampusLoadedListener implements
			CampusLoader.CampusLoadedListener {
		@Override
		public void campusLoaded() {
			getDirectionsButton.setEnabled(true);
			MapViewMover.smoothFitToRect(mapView, handler, getBounds(), 2, null);
			mapView.setOnTouchListener(new OnMapTouchLimiterListener(mapView, getBounds()));
		}
	}

	public Rect bounds;

	public Rect getBounds() {
		if (bounds != null)
			return bounds;
		bounds = new Rect(Integer.MAX_VALUE, Integer.MAX_VALUE,	Integer.MIN_VALUE, Integer.MIN_VALUE);
		for (int i = 0; i < app.campus.buildings.length; i++) {
			LatLngPoint p = app.campus.buildings[i].location;
			if (p.lng < bounds.left)
				bounds.left = p.lng;
			if (p.lng > bounds.right)
				bounds.right = p.lng;
			if (p.lat < bounds.top)
				bounds.top = p.lat;
			if (p.lat > bounds.bottom)
				bounds.bottom = p.lat;
		}
		return bounds;
	}

	public void setStartText(String text) {
		startTextView.setText(text);
	}

	public void setEndText(String text) {
		endTextView.setText(text);
	}

	private void setupActionBar() {
		ActionBar actionBar = (ActionBar) findViewById(R.id.mainActionBar);
		actionBar.setTitle("Mapyst");
		Action settingsAction = new IntentAction(this, createSettingsIntent(), R.drawable.settings);
		actionBar.addAction(settingsAction);
	}

	private Intent createSettingsIntent() {
		Intent i = new Intent(this, Settings.class);
		return i;
	}

	private void setupUI() {
		setupPrefs();

		getDirectionsButton = (Button) this.findViewById(R.id.getDirectionsButton);
		getDirectionsButton.setEnabled(false);
		slidingView = (SlidingScrollView) this.findViewById(R.id.slidingScrollView);
		slidingView.setOnActiveViewChangeListener(new OnActiveViewChangeListener());
		startTextView = (AutoCompleteTextView) findViewById(R.id.startText);
		endTextView = (AutoCompleteTextView) findViewById(R.id.endText);

		endTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView arg0, int arg1,
							KeyEvent arg2) {
						if (arg1 == EditorInfo.IME_ACTION_SEARCH) {

							onClickHandler(findViewById(R.id.getDirectionsButton));
							return true;
						}
						return false;
					}
				});

		rightArrow = (Button) findViewById(R.id.rightArrow);
		leftArrow = (Button) findViewById(R.id.leftArrow);
		directionEnd = (ImageView) this.findViewById(R.id.directionEnd);
		directionStart = (ImageView) this.findViewById(R.id.directionStart);

		directionEnd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				moveToEndPosition();
			}
		});

		directionStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				moveToStartPosition();
			}
		});

		Button locsButton = (Button) this.findViewById(R.id.locsButton);
		Button prefsButton = (Button) this.findViewById(R.id.prefsButton);
		Display display = getWindowManager().getDefaultDisplay();
		locsButton.setWidth(display.getWidth() / 2);
		prefsButton.setWidth(display.getWidth() / 2);

		// Creates a map view
		mapView = (PriorityMapView) findViewById(R.id.mapView);
		mapView.getController().setZoom(16);
		// Enable satellite view
		mapView.setSatellite(true);

		viewItemOverlay = new LocationChooserOverlay(app, this, mapView, locFinder);
		mapView.getOverlays().add(viewItemOverlay);
	}

	private class OnActiveViewChangeListener implements	SlidingScrollView.ActiveViewChangeListener {

		@Override
		public void activeSet(int active, View activeV) {
			System.out.println(active);
			if (active == 1) {
				mapView.active = true;
				startTextView.setFocusable(true);
				startTextView.setFocusableInTouchMode(true);
				endTextView.setFocusable(true);
				endTextView.setFocusableInTouchMode(true);
			} else {
				mapView.active = false;
				startTextView.setFocusable(false);
				startTextView.setFocusableInTouchMode(false);
				endTextView.setFocusable(false);
				endTextView.setFocusableInTouchMode(false);
			}
		}

	}

	private void displayMenu() {
		app.route = null;

		mapView.getController().setCenter(DrawingHelpers.convertPointToGeo(app.campus.location));
		mapView.getController().setZoom(16);

		List<Overlay> mapOverlays = mapView.getOverlays();
		mapOverlays.clear();

		ActionBar actionBar = (ActionBar) findViewById(R.id.mainActionBar);
		actionBar.setTitle("Mapyst");
		if (listAction != null) {
			actionBar.removeAction(listAction);
			listAction = null;
		}
		if (backAction != null) {
			actionBar.removeAction(backAction);
			actionBar.clearHomeAction();
			backAction = null;
		}

		locFinder.setupLocService();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		boolean isCompassEnabled = prefs.getBoolean("compassBox", false);
		if (isCompassEnabled)
			compass.stop();

		getDirectionsButton.setEnabled(true);

		RelativeLayout directionLayout = (RelativeLayout) findViewById(R.id.directionLayout);
		directionLayout.setVisibility(View.GONE);

		RelativeLayout inputLayout = (RelativeLayout) findViewById(R.id.inputLayout);
		inputLayout.setVisibility(View.VISIBLE);

		LinearLayout locsPrefsLayout = (LinearLayout) findViewById(R.id.locsPrefsLayout);
		locsPrefsLayout.setVisibility(View.VISIBLE);

		LinearLayout prefsLayout = (LinearLayout) this.findViewById(R.id.prefsLayout);
		prefsLayout.setVisibility(View.VISIBLE);

		recycleBitmaps();
		mapView.getOverlays().clear();
		mapView.getOverlays().add(viewItemOverlay);
	}

	private void recycleBitmaps() {
		graphOverlay.recycleBitmap();
		graphOverlay = null;
		// System.gc();
	}

	private void setupPrefs() {
		final ToggleButton toggleElevators = (ToggleButton) findViewById(R.id.elevatorsPref);
		final ToggleButton toggleStairs = (ToggleButton) findViewById(R.id.stairsPref);
		final ToggleButton toggleHand = (ToggleButton) findViewById(R.id.handPref);
		final ToggleButton toggleInside = (ToggleButton) findViewById(R.id.insidePref);
		final ToggleButton toggleOutside = (ToggleButton) findViewById(R.id.outsidePref);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		toggleElevators.setChecked(prefs.getBoolean("elevators", false));
		toggleStairs.setChecked(prefs.getBoolean("stairs", false));
		toggleHand.setChecked(prefs.getBoolean("hand", false));
		toggleInside.setChecked(prefs.getBoolean("inside", false));
		toggleOutside.setChecked(prefs.getBoolean("outside", false));

		toggleElevators.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (toggleElevators.isChecked()) {
					toggleStairs.setChecked(false);
					toggleHand.setChecked(false);
				}
				saveRoutePrefs();
			}
		});

		toggleStairs.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (toggleStairs.isChecked()) {
					toggleElevators.setChecked(false);
					toggleHand.setChecked(false);
				}
				saveRoutePrefs();
			}
		});

		toggleHand.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (toggleHand.isChecked()) {
					toggleStairs.setChecked(false);
					toggleElevators.setChecked(false);
				}
				saveRoutePrefs();
			}
		});

		toggleOutside.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (toggleOutside.isChecked())
					toggleInside.setChecked(false);
				saveRoutePrefs();
			}
		});

		toggleInside.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (toggleInside.isChecked())
					toggleOutside.setChecked(false);
				saveRoutePrefs();
			}
		});
	}

	private void saveRoutePrefs() {
		ToggleButton toggleElevators = (ToggleButton) findViewById(R.id.elevatorsPref);
		ToggleButton toggleStairs = (ToggleButton) findViewById(R.id.stairsPref);
		ToggleButton toggleHand = (ToggleButton) findViewById(R.id.handPref);
		ToggleButton toggleInside = (ToggleButton) findViewById(R.id.insidePref);
		ToggleButton toggleOutside = (ToggleButton) findViewById(R.id.outsidePref);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("elevators", toggleElevators.isChecked());
		editor.putBoolean("stairs", toggleStairs.isChecked());
		editor.putBoolean("hand", toggleHand.isChecked());
		editor.putBoolean("inside", toggleInside.isChecked());
		editor.putBoolean("outside", toggleOutside.isChecked());
		editor.commit();
	}

	private RoutePreferences getRoutePrefs() {
		ToggleButton toggleElevators = (ToggleButton) findViewById(R.id.elevatorsPref);
		ToggleButton toggleStairs = (ToggleButton) findViewById(R.id.stairsPref);
		ToggleButton toggleHand = (ToggleButton) findViewById(R.id.handPref);
		ToggleButton toggleInside = (ToggleButton) findViewById(R.id.insidePref);
		ToggleButton toggleOutside = (ToggleButton) findViewById(R.id.outsidePref);

		return new RoutePreferences(toggleElevators.isChecked(),
				toggleStairs.isChecked(), toggleHand.isChecked(),
				toggleInside.isChecked(), toggleOutside.isChecked());
	}

	public void makeAndStartCompass() {
		compass = new Compass(this.getApplicationContext());
		compassOverlay = new CompassOverlay(this.getApplicationContext(), mapView, compass);
		compass.start();
		compassOverlay.enable();
	}

	public void displayRoute() {
		if (app.route == null) {
			CenteredToastFactory.makeToastAndShow(this,	"An unexpected error occurred when calculating the route.", 1000);
			return;
		}

		System.gc();

		if (slidingView.getActiveView() != 1) {
			slidingView.setActiveView(1);
		}

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(endTextView.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(startTextView.getWindowToken(), 0);

		List<Overlay> mapOverlays = mapView.getOverlays();
		viewItemOverlay.clearViews();
		mapView.getOverlays().clear();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		boolean isCompassEnabled = prefs.getBoolean("compassBox", false);

		if (isCompassEnabled) {
			makeAndStartCompass();
		}

		locFinder.endLocService();

		RelativeLayout directionLayout = (RelativeLayout) findViewById(R.id.directionLayout);
		directionLayout.setVisibility(View.VISIBLE);

		RelativeLayout inputLayout = (RelativeLayout) findViewById(R.id.inputLayout);
		inputLayout.setVisibility(View.GONE);

		LinearLayout locsPrefsLayout = (LinearLayout) findViewById(R.id.locsPrefsLayout);
		locsPrefsLayout.setVisibility(View.GONE);

		LinearLayout prefsLayout = (LinearLayout) this.findViewById(R.id.prefsLayout);
		prefsLayout.setVisibility(View.INVISIBLE);

		final ActionBar actionBar = (ActionBar) findViewById(R.id.mainActionBar);
		backAction = new ActionBar.FunctionAction(new Runnable() {
			@Override
			public void run() {
				displayMenu();
			}
		}, R.drawable.back);
		actionBar.setHomeAction(backAction);

		Display display = getWindowManager().getDefaultDisplay();
		width = display.getWidth();
		height = display.getHeight();

		Waypoint2D singlePoint = new Waypoint2D();
		LatLngPoint geoSinglePoint = new LatLngPoint();

		if (app.route.singleWaypoint != null) {
			directionLayout.setVisibility(View.INVISIBLE);
			singlePoint = app.route.singleWaypoint;

			int building = singlePoint.getId().getBuildingIndex();
			int floor = singlePoint.getId().getFloorIndex();

			geoSinglePoint = singlePoint.getPoint();

			if (app.campus.buildingIsOutside(building))
				actionBar.setTitle(app.route.singlePointText);
			else {
				String floorText = app.campus.getFloor(building, floor).name;
				String buildingText = app.campus.buildings[building].names[0];
				actionBar.setTitle(app.route.singlePointText + " on Floor "	+ floorText + " of " + buildingText);
			}

			mcontrol = mapView.getController();
			mcontrol.setCenter(DrawingHelpers.convertPointToGeo(geoSinglePoint));
			mcontrol.setZoom(20);

			graphOverlay = new RouteMapOverlay(this, app, singlePoint, Images.Icons.START, mapView.getProjection(), width, height);
			mapOverlays.add(graphOverlay);

		} else {
			final Direction[] directions = app.route.getDirections();
			graphOverlay = new RouteMapOverlay(this, app, directions, mapView.getProjection(), width, height);
			mapOverlays.add(graphOverlay);

			if (directions != null) {
				listAction = new IntentAction(this, createListIntent(),	R.drawable.listicon);
				actionBar.addAction(listAction);
			}

			curDir = 0;
			setUI(curDir, true);
			rightArrow.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// 1 if directions are null, length otherwise
					if (curDir >= ((directions == null) ? 1 : directions.length) - 1)
						return;
					if (v != null) {
						if (directions != null) {
							setUI(curDir + 1, true);
						}
					}
					v.invalidate();
				}

			});
			leftArrow.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (curDir <= 0)
						return;
					if (v != null) {
						if (directions != null) {
							setUI(curDir - 1, true);
						}
					}
					v.invalidate();
				}
			});
		}
		if (isCompassEnabled) {
			mapOverlays.add(compassOverlay);
		}
	}

	public void setUI(int dirIndex, boolean moveView) {
		if (graphOverlay != null) {
			Direction[] directions = app.route.getDirections();
			curDir = dirIndex;
			graphOverlay.setFloor(dirIndex);
			DirectionIcon directionIcon = (DirectionIcon) findViewById(R.id.directionIcon);
			directionIcon.setDirection(app, this, dirIndex, directions);
			final ActionBar actionBar = (ActionBar) findViewById(R.id.mainActionBar);
			actionBar.setTitle(directions[dirIndex].getText());

			if (curDir == 0) {
				leftArrow.setVisibility(View.GONE);
				directionStart.setVisibility(View.VISIBLE);

			} else {
				leftArrow.setVisibility(View.VISIBLE);
				directionStart.setVisibility(View.GONE);
			}

			if (curDir == directions.length - 1) {
				rightArrow.setVisibility(View.GONE);
				directionEnd.setVisibility(View.VISIBLE);
			} else {
				rightArrow.setVisibility(View.VISIBLE);
				directionEnd.setVisibility(View.GONE);
			}

			if (moveView) {
				moveToCurrentDirection();
			}
		}
	}

	public void moveToCurrentDirection() {
		MapViewMover.smoothFitToDirection(mapView, handler, 1.5,
				app.route.getDirections()[curDir]);
	}

	public void moveToStartPosition() {
		Direction d = app.route.getDirections()[0];
		MapViewMover.smoothFitToPoint(mapView, handler, 1.5, d.getPoints()[0]);
	}

	public void moveToEndPosition() {
		Direction d = app.route.getDirections()[app.route.getDirections().length - 1];
		MapViewMover.smoothFitToPoint(mapView, handler, 1.5, d.getPoints()[d.getPoints().length - 1]);
	}

	private Intent createListIntent() {
		Intent intent = new Intent();
		intent.setClass(MainScreen.this, DirectionsList.class);
		return intent;

	}

	public void onClickHandler(View view) {

		try {
			String start = startTextView.getText().toString();
			String end = endTextView.getText().toString();

			switch (view.getId()) {

			case R.id.getDirectionsButton: {
				getDirections(start, end);
				break;
			}

			case R.id.startCurLoc: {
				if (startTextView.getText().toString()
						.equals("Current Location"))
					startTextView.setText("");
				else {
					startTextView.setText("Current Location");
					startTextView.dismissDropDown();
				}
				break;
			}

			case R.id.locsButton: {
				LinearLayout locationsLayout = (LinearLayout) findViewById(R.id.locationsListLayout);
				if (slidingView.getActiveView() == slidingView.getIndex(locationsLayout)) {
					slidingView.setActiveView(1);
				} else {
					slidingView.setActiveView(locationsLayout);

				}
				break;
			}

			case R.id.prefsButton: {
				LinearLayout prefsLayout = (LinearLayout) this.findViewById(R.id.prefsLayout);
				if (prefsLayout.getVisibility() == View.VISIBLE)
					prefsLayout.setVisibility(View.GONE);
				else
					prefsLayout.setVisibility(View.VISIBLE);
				break;
			}
			case R.id.locationsBack: {
				LocationsListView locsList = (LocationsListView) findViewById(R.id.locationsList);
				locsList.curLocTypeIndex = -1;
				locsList.update();
			}
			}
		} catch (Exception e) {
			CenteredToastFactory.makeToastAndShow(this, "Something went wrong :(", Toast.LENGTH_SHORT);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			LocationsListView locsList = (LocationsListView) findViewById(R.id.locationsList);
			if (locsList.curLocTypeIndex != -1) {
				locsList.curLocTypeIndex = -1;
				locsList.update();
				return true;
			} else if (slidingView != null && slidingView.getActiveView() != 1) {
				slidingView.setActiveView(1);
				return true;
			} else if (app.route == null) {
				this.finish();
				return true;
			} else {
				displayMenu();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(endTextView.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(startTextView.getWindowToken(), 0);
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_screen_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.gotocampus) {
			MapViewMover.smoothFitToRect(mapView, handler, getBounds(), 2, null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void getDirections(String start, String end) {
		if (start.equals("") && end.equals(""))
			return;
		else if (start.equals("")) {
			onePoint(end);
		} else if (end.equals("")) {
			onePoint(start);
		} else {
			final InterpretedInfo startInfo = interpret(start, InterpretedInfo.START);
			final InterpretedInfo endInfo = interpret(end, InterpretedInfo.END);

			if (endInfo.getResult().getWaypoints().contains(startInfo.getResult().getPointID())) {
				CenteredToastFactory.makeToastAndShow(this, "Please enter two different locations", Toast.LENGTH_SHORT);
				return;
			}

			if (startInfo.showDialog() && endInfo.showDialog()) {
				showSuggestionsList(startInfo, start, false);
				showSuggestionsList(endInfo, end, true);
			} else if (startInfo.showDialog())
				showSuggestionsList(startInfo, start, true);
			else if (endInfo.showDialog())
				showSuggestionsList(endInfo, end, true);
			// checks to make sure the interpretations were successful
			else if (!startInfo.successful()) {
				CenteredToastFactory.makeToastAndShow(this, "Sorry, there was a problem interpreting your start location.", Toast.LENGTH_SHORT);
			} else if (!endInfo.successful()) {
				CenteredToastFactory.makeToastAndShow(this, "Sorry, there was a problem interpreting your end location.", Toast.LENGTH_SHORT);
			}
			// checks to make sure the start and end are not the same
			else if (startInfo.getResult().getPointID().equals(endInfo.getResult().getPointID())) {
				CenteredToastFactory.makeToastAndShow(this, "Please enter two different locations.", Toast.LENGTH_SHORT);
				return;
			} else {
				RoutePreferences prefs = getRoutePrefs();
				RouteMakerTaskPrefs routePrefs = new RouteMakerTaskPrefs(startInfo.getResult(), endInfo.getResult(), prefs, app, this);
				getDirectionsButton.setEnabled(false);
				new RouteMakerTask().execute(routePrefs);
			}
		}
	}

	private class RouteMakerTask extends
			AsyncTask<RouteMakerTaskPrefs, Integer, RouteMakerTaskPrefs> {

		@Override
		protected void onPreExecute() { // This runs on the UI thread
			return;
		}

		@Override
		protected RouteMakerTaskPrefs doInBackground(
				RouteMakerTaskPrefs... prefs) { // This runs in the background
			for (RouteMakerTaskPrefs pref : prefs) {
				pref.app.route = pref.app.getRouteFinder().makeRoute(pref.startResult, pref.endResult, pref.routePrefs);
			}

			return prefs[0];
		}

		@Override
		protected void onProgressUpdate(Integer... progress) { // Called from
																// background
																// thread to UI
																// thread
		}

		@Override
		protected void onPostExecute(RouteMakerTaskPrefs result) { // Called UI
																	// thread
			result.loaderContext.displayRoute();
			return;
		}

	}

	public void onePoint(String text) {
		InterpretedInfo info = interpret(text, InterpretedInfo.START);

		if (info.showDialog()) {
			showSuggestionsList(info, text, true);
		} else if (info.successful()) {
			app.route = new Route(info.getResult(), app.getRouteFinder());
			displayRoute();
		} else {
			CenteredToastFactory.makeToastAndShow(this, "Sorry, there was a problem interpreting your input.", Toast.LENGTH_SHORT);
		}
	}

	private InterpretedInfo interpret(String text, boolean interpretType) {
		InterpretedInfo info;
		Interpreter interpreter = new Interpreter(app.campus);

		Locale locale = Locale.ENGLISH;
		if (text.toLowerCase(locale).replace(" ", "").equals("currentlocation"))
			info = locFinder.getCurrentLocation();
		else
			info = interpreter.interpret(text, interpretType);

		return info;
	}

	private void showSuggestionsList(InterpretedInfo info, final String originalInput, final boolean relaunchGetDirections) {
		ArrayList<InterpretResult> suggestions = info.getSuggestions();
		if (suggestions.size() > 1) {
			final String[] labels = new String[suggestions.size()];
			for (int i = 0; i < labels.length; i++) {
				labels[i] = suggestions.get(i).getText();
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Unable to find " + originalInput);
			builder.setCancelable(true);
			builder.setItems(labels, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int index) {
					String startText = startTextView.getText().toString();
					String endText = endTextView.getText().toString();

					if (startText.equals(originalInput))
						startTextView.setText(labels[index]);
					else if (endText.equals(originalInput))
						endTextView.setText(labels[index]);

					startText = startTextView.getText().toString();
					endText = endTextView.getText().toString();

					if (relaunchGetDirections)
						getDirections(startText, endText);
				}
			});

			AlertDialog alert = builder.create();
			alert.show();
		}
	}

	public int getZoom() {
		return mapView.getZoomLevel();
	}

	public View getMapView() {
		return mapView;
	}

	public int getSelected() {
		return curDir;
	}

	/* this is GUARENTEED to be called before cleanup by OS */
	@Override
	protected void onPause() {
		locFinder.endLocService();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		List<Overlay> mapOverlays = mapView.getOverlays();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		boolean isCompassEnabled = prefs.getBoolean("compassBox", false);
		if (isCompassEnabled)
			if (compass == null) {
				makeAndStartCompass();
				mapOverlays.add(compassOverlay);
			} else
				compass.start();
		else {
			compass = null;
			mapOverlays.remove(compassOverlay);
		}

		if (app.backFromDirList) {
			setUI(app.currentDir, true);
			app.backFromDirList = false;
		}
	}

	@Override
	protected void onRestart() {
		locFinder.setupLocService();
		super.onRestart();
	}

	@Override
	protected void onStop() {
		locFinder.endLocService();
		if (compass != null)
			compass.stop();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		locFinder.endLocService();
		super.onDestroy();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return true;
	}
}
