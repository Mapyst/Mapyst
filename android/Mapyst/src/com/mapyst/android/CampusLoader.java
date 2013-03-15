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
import java.util.Collections;

import com.mapyst.android.asynctask.CampusLoaderTask;
import com.mapyst.android.asynctask.CampusLoaderTaskPrefs;
import com.mapyst.android.ui.LocationsListView;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class CampusLoader {

	private Mapyst app;
	private MainScreen main;
	private CampusLoaderTask clTask;

	private CampusLoadedListener campusLoadedListener;

	public interface CampusLoadedListener {
		public void campusLoaded();
	}

	public CampusLoader(MainScreen main, Mapyst app, CampusLoadedListener campusLoadedListener) {
		this.app = app;
		this.main = main;
		this.campusLoadedListener = campusLoadedListener;
		this.clTask = new CampusLoaderTask();
	}

	public void load(int campus_id) {
		CampusLoaderTaskPrefs pref = new CampusLoaderTaskPrefs(campus_id, app, main, this);
		this.clTask.execute(pref);
	}

	public void finishedLoading() {
		createAutoCompleteList();

		LocationsListView locsList = (LocationsListView) main.findViewById(R.id.locationsList);
		locsList.setup(main, app);
		locsList.update();
		campusLoadedListener.campusLoaded();
	}

	private void createAutoCompleteList() {
		ArrayList<String> locations = new ArrayList<String>(100);

		// adds all the campus locations to the list for auto complete
		for (int i = 0; i < app.campus.location_types.length; i++) {
			for (int j = 0; j < app.campus.location_types[i].locations.length; j++) {
                Collections.addAll(locations, app.campus.location_types[i].locations[j].names);
			}
		}
		locations.add("Current Location");

		ArrayAdapter<String> locsAdapter = new ArrayAdapter<String>(main,R.layout.list_item, locations);

		AutoCompleteTextView startTextView = (AutoCompleteTextView) main.findViewById(R.id.startText);
		startTextView.setAdapter(locsAdapter);

		AutoCompleteTextView endTextView = (AutoCompleteTextView) main.findViewById(R.id.endText);
		endTextView.setAdapter(locsAdapter);
	}
}
