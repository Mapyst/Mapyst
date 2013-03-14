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

package com.mapyst.android.ui;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapyst.android.MainScreen;
import com.mapyst.android.Mapyst;
import com.mapyst.android.R;
import com.mapyst.campus.Location;
import com.mapyst.campus.Location_Type;

public class LocationsListView extends ListView {

	private MainScreen main;
	private Mapyst app;

	public int curLocTypeIndex;
	private Location_Type curLocType;

	public LocationsListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOnItemClickListener(new LocationsListClickListener());
	}

	public void setup(MainScreen main, Mapyst app) {
		this.main = main;
		this.app = app;

		curLocTypeIndex = -1;
	}

	public void update() {
		if (main != null) {
			TextView locationsTitle = (TextView) main.findViewById(R.id.locationTypeText);
			ListView listView = (ListView) main.findViewById(R.id.locationsList);
			ArrayList<String> locs = new ArrayList<String>(100);
			if (curLocTypeIndex == -1) {
				for (int i = 0; i < app.campus.location_types.length; i++) {
					locs.add(app.campus.location_types[i].name);
				}
				locationsTitle.setText("Location Categories");

				Button locsBack = (Button) main.findViewById(R.id.locationsBack);
				locsBack.setVisibility(View.GONE);
			} else {
				for (int i = 0; i < app.campus.location_types[curLocTypeIndex].locations.length; i++) {
					locs.add(app.campus.location_types[curLocTypeIndex].locations[i].names[0]);
				}
				locationsTitle.setText(curLocType.name);

				Button locsBack = (Button) main.findViewById(R.id.locationsBack);
				locsBack.setVisibility(View.VISIBLE);
			}

			ArrayList<String> locHours = null;
			if (!(curLocTypeIndex == -1)) {
				locHours = getLocHours(locs);
			}
			listView.setAdapter(new LocationsAdapter(main, locs, locHours));
		}
	}

	public ArrayList<String> getLocHours(ArrayList<String> locs) {
		ArrayList<String> hours = new ArrayList<String>(locs.size());
		for (int i = 0; i < locs.size(); i++) {
			Location location = app.campus.getCampusLocation(locs.get(i));
			hours.add(location.hours);
		}
		return hours;
	}

	public String getLocInfo(Location location) {
		String info = formatHours(location.hours) + "\n\n" + location.description + "\n";
		return info;
	}

	public String formatHours(String hours) {
		hours = hours.replace("H:", "");
		hours = hours.replace("MTWThFSaS:", "\nMon-Sun: ");
		hours = hours.replace("MTWThFSa:", "\nMon-Sat: ");
		hours = hours.replace("MTWThF:", "\nMon-Fri: ");
		hours = hours.replace("MTWTh:", "\nMon-Thurs: ");
		hours = hours.replace("MTW:", "\nMon-Wed: ");
		hours = hours.replace("WThF:", "\nWed-Fri: ");
		hours = hours.replace("MWF:", "\nMon,Web,Fri: ");
		hours = hours.replace("TTh:", "\nTues,Thurs: ");
		hours = hours.replace("SaS:", "\nSat,Sun: ");
		hours = hours.replace("MT:", "\nMon,Tues: ");
		hours = hours.replace("WF:", "\nWed,Fri: ");
		hours = hours.replace("F:", "\nFri: ");
		hours = hours.replace("Sa:", "\nSat: ");
		hours = hours.replace("S:", "\nSun: ");
		hours = hours.replace("Th:", "\nThurs: ");

		if (hours.length() > 1) {
			hours = hours.substring(1);
		}
		return hours;
	}

	private class LocationsListClickListener implements
			AdapterView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int index, long arg3) {
			final String curSelectionText = (String) ((TextView) ((RelativeLayout) view).getChildAt(0)).getText();

			if (curLocTypeIndex == -1) {
				for (int i = 0; i < app.campus.location_types.length; i++) {
					if (curSelectionText.equals(app.campus.location_types[i].name))
						curLocType = app.campus.location_types[i];
				}
				curLocTypeIndex = index;
				update();
			} else {

				final Dialog dialog = new Dialog(adapterView.getContext(), R.style.locationsDialogStyle);
				dialog.setContentView(R.layout.locations_dialog);
				dialog.setTitle(curSelectionText);
				dialog.setCancelable(true);

				Location campusLocation = app.campus.getCampusLocation(curSelectionText);

				Button seeMap = (Button) dialog.findViewById(R.id.dialog_seeOnMap);
				seeMap.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						dialog.dismiss();

						main.getDirections(curSelectionText, "");
					}
				});

				Button setStart = (Button) dialog.findViewById(R.id.dialog_setStart);
				setStart.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						dialog.dismiss();

						AutoCompleteTextView startTextView = (AutoCompleteTextView) main.findViewById(R.id.startText);
						startTextView.setText(curSelectionText);
					}
				});

				Button setEnd = (Button) dialog.findViewById(R.id.dialog_setEnd);
				setEnd.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						dialog.dismiss();

						AutoCompleteTextView endTextView = (AutoCompleteTextView) main.findViewById(R.id.endText);
						endTextView.setText(curSelectionText);
					}
				});

				TextView content = (TextView) dialog.findViewById(R.id.dialog_content);
				String contentString = "";
				if (campusLocation != null) {
					contentString = getLocInfo(campusLocation);
				}
				content.setText(contentString);
				content.setPadding(20, 0, 20, 20);
				content.setTextSize(16.0f);

				dialog.show();
			}
		}
	}
}