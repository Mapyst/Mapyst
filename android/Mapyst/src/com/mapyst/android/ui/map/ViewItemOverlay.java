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

package com.mapyst.android.ui.map;

import java.util.ArrayList;

import android.os.Handler;
import android.view.View;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class ViewItemOverlay extends Overlay {

	private ArrayList<View> viewItems;

	private MapView map;

	private Handler handler;

	public ViewItemOverlay(MapView map) {
		viewItems = new ArrayList<View>();
		this.map = map;
		handler = new Handler();
	}

	public void addView(final View view, GeoPoint point, int gravity) {
		final MapView.LayoutParams params = new MapView.LayoutParams(
				MapView.LayoutParams.WRAP_CONTENT, MapView.LayoutParams.WRAP_CONTENT, point, gravity);
		viewItems.add(view);
		handler.post(new Runnable() {
			public void run() {
				map.addView(view, params);
			}
		});
		map.postInvalidate();
	}

	public void removeView(View view, boolean refresh) {
		int i = -1;
		if ((i = viewItems.indexOf(i)) != -1) {
			removeView(i, refresh);
		}
		if (refresh)
			map.postInvalidate();
	}

	public void removeView(final int i, boolean refresh) {
		final View v = viewItems.get(i);
		handler.post(new Runnable() {
			public void run() {
				map.removeView(v);
			}
		});
		viewItems.remove(i);
		if (refresh)
			map.postInvalidate();
	}

	public void clearViews() {
		while (viewItems.size() > 0)
			removeView(0, false);
		map.postInvalidate();
	}

	public int size() {
		return viewItems.size();
	}
}
