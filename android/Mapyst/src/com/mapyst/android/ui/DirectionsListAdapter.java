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

import com.mapyst.android.R;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DirectionsListAdapter extends ArrayAdapter<DirectionsListItem> {
	private ArrayList<DirectionsListItem> directionItems;

	public DirectionsListAdapter(Context context, int textViewResourceId, ArrayList<DirectionsListItem> directionItems) {
		super(context, textViewResourceId, directionItems);
		this.directionItems = directionItems;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.directions_list_item, null);
		}
		
		DirectionsListItem item = directionItems.get(position);
		if (item != null) {
			TextView dirName = (TextView) convertView.findViewById(R.id.directions_list_name);
			TextView dirText = (TextView) convertView.findViewById(R.id.directions_list_time);
			if (dirName != null) {
				dirName.setText(item.directionName);
			}
			if (dirText != null && !item.isTotalTime) {
				dirText.setText(Html.fromHtml("<i>" + item.directionTime + "s</i>"));
			} else {
				dirText.setText(Html.fromHtml("<i>Total: " + item.directionTime	+ "s</i>"));
				convertView.setBackgroundColor(Color.LTGRAY);
				dirName.setTextColor(Color.DKGRAY);
				dirText.setTextColor(Color.DKGRAY);
			}
		}
		return convertView;
	}
}
