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

import com.mapyst.route.Direction;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mapyst.android.ui.DirectionsListAdapter;
import com.mapyst.android.ui.DirectionsListItem;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

public class DirectionsList extends Activity {

	@Override
	protected void onCreate(Bundle bn) {
		super.onCreate(bn);

		final Mapyst app = (Mapyst) this.getApplication();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		boolean showNotificationBar = prefs.getBoolean("notificationBox", true);
		if (!showNotificationBar) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		setContentView(R.layout.directions_list);

		final ActionBar actionBar = (ActionBar) findViewById(R.id.listDirectionsActionBar);
		actionBar.setHomeAction(new ActionBar.FunctionAction(new Runnable() {
			@Override
			public void run() {
				finish();
			}
		}, R.drawable.back));

		actionBar.setTitle("Directions");

		final Action shareAction = new IntentAction(this, createSettingsIntent(), R.drawable.settings);
		actionBar.addAction(shareAction);

		Direction[] directions = app.route.getDirections();
		ArrayList<DirectionsListItem> listItems = new ArrayList<DirectionsListItem>();
		listItems.add(new DirectionsListItem("From:  " + app.route.startText + "\nTo:       " + app.route.endText, formatTime(app.route.getTime() / 1000), true));
        for (Direction direction : directions) {
            listItems.add(new DirectionsListItem(direction.getText(), formatTime(direction.getTime() / 1000), false));
        }

		ListView directionsList = (ListView) findViewById(R.id.directionsList);
		DirectionsListAdapter dadapter = new DirectionsListAdapter(this, R.layout.directions_list_item, listItems);

		directionsList.setAdapter(dadapter);
		directionsList.setBackgroundColor(Color.WHITE);
		directionsList.setOnItemClickListener(new ListView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> adView, View view,
							int position, long id) {

						if (position == 0)
							return;

						app.currentDir = position - 1;
						app.backFromDirList = true;

						finish();
					}

				});

	}

	private String formatTime(int seconds) {
		String time = "";

		// if minutes != 0
		if (seconds / 60 != 0)
			time = "" + (seconds / 60) + " min ";

		time = time + (seconds % 60) + " sec";
		return time;
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private Intent createSettingsIntent() {
        return new Intent(this, Settings.class);
	}

	@SuppressWarnings("unused")
	private Intent createHomeIntent(Context context) {
		Intent i = new Intent(context, MainScreen.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return i;
	}
}
