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

import android.graphics.PointF;
import android.os.SystemClock;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.mapyst.android.LocationFinder;
import com.mapyst.android.MainScreen;
import com.mapyst.android.Mapyst;
import com.mapyst.route.InterpretedInfo;
import com.mapyst.route.Interpreter;
import com.mapyst.route.Waypoint2D;

public class LocationChooserOverlay extends ViewItemOverlay {

	private Mapyst app;
    private MapView mapView;

	private MainScreen mainScreen;

	private class OnLocationButtonClickListener implements LocationBalloon.LocationButtonClickListener {

		private String location;

		public OnLocationButtonClickListener(String location) {
			this.location = location;
		}

		@Override
		public void startClicked() {
			mainScreen.setStartText(location);
			clearViews();
		}

		@Override
		public void endClicked() {
			mainScreen.setEndText(location);
			clearViews();
		}

		@Override
		public void cancelClicked() {
			clearViews();
		}
	}

	public LocationChooserOverlay(Mapyst app, MainScreen mainScreen, MapView mapView, LocationFinder locFinder) {
		super(mapView);
		downPoint = new PointF();
		this.app = app;
		this.mapView = mapView;
		this.mainScreen = mainScreen;
	}

	private PointF downPoint;
	private float farthestPoint = 0;
	private long downTime;

	public void onClickEvent(float x, float y) {
		if (app.campus == null)
			return;
		if (size() > 0) {
			clearViews();
			return;
		}
        Interpreter interpreter = new Interpreter(app.campus);
		GeoPoint touchPoint = mapView.getProjection().fromPixels((int) x, (int) y);
		InterpretedInfo ii = interpreter.interpretLatLng(touchPoint.getLatitudeE6(), touchPoint.getLongitudeE6()); // loc

		Waypoint2D waypt2D = app.getRouteFinder().getWaypoint2D(ii.getResult().getPointID());

		String locName = "lat:" + (waypt2D.getPoint().lat * 1. / 1E6) + " lng:"	+ (waypt2D.getPoint().lng * 1. / 1E6);

        this.addView(new LocationBalloon(app.getApplicationContext(), locName,
				new OnLocationButtonClickListener(locName)), touchPoint,
				MapView.LayoutParams.CENTER | MapView.LayoutParams.BOTTOM);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			downPoint.x = event.getX();
			downPoint.y = event.getY();
			downTime = SystemClock.uptimeMillis();
			farthestPoint = 0;
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			float dist = (float) Math.pow(Math.pow(event.getX() - downPoint.x, 2) + Math.pow(event.getY() - downPoint.y, 2), .5);
			if (dist > farthestPoint)
				farthestPoint = dist;
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			// System.out.println(farthestPoint + " " +
			// (SystemClock.uptimeMillis() - downTime));
			if (farthestPoint < 100	&& SystemClock.uptimeMillis() - downTime < 250)
				onClickEvent(event.getX(), event.getY());
		}

		// System.out.println("ARst");
		// return super.onTouchEvent(event, mapView);
		return super.onTouchEvent(event, mapView);
	}
}
