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

import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Rect;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class OnMapTouchLimiterListener implements OnTouchListener {

	// ================================================================
	public boolean limiting = false; // SETS WHETHER THE MAP IS LIMITED
	// ================================================================

	private MapView map;
	private Rect bounds;

	private boolean repairing = false;

	private static final int repairSpeed = 250;

	private int lastProbDirX = 0; // -1 left, 1 right
	private int lastProbDirY = 0; // -1 up, 1 down

    private Timer timer;

    private GeoPoint campusCenter;

	private long lastRepair = 0;
	private long startRepair = 0;

	private boolean endTouch = false;

	private float lastX;
	private float lastY;

	private class Repairer extends TimerTask {
		public void run() {
			if (MapViewLimiter.outOfBounds(map, bounds)	|| (repairing && MapViewLimiter.outOfBounds(map, bounds, 3f))) {
				if (SystemClock.uptimeMillis() - lastRepair > 200)
					startRepair = SystemClock.uptimeMillis();
				if (SystemClock.uptimeMillis() - startRepair > 2000) { // jump to fix
					GeoPoint newCenter = campusCenter;
					map.getController().setCenter(newCenter);
				} else { // gradual fix
					endTouch = true;
					repairing = true;
					map.getController().stopPanning();
					try {
						if (MapViewLimiter.tooFarOut(map, bounds)) {
							map.getController().zoomIn();
						} else { // at this point we know its a center issue
							float dlat = campusCenter.getLatitudeE6() - map.getMapCenter().getLatitudeE6();
							float dlong = campusCenter.getLongitudeE6()	- map.getMapCenter().getLongitudeE6();
							float dist = (float) Math.pow(Math.pow(dlat, 2)	+ Math.pow(dlong, 2), .5);
							dlat /= dist;
							dlong /= dist;
							dlat = repairSpeed * dlat;
							dlong = repairSpeed * dlong;

							if (Math.abs(dlong) > 100) {
								if (dlong > 0)
									lastProbDirX = -1;
								else
									lastProbDirX = 1;
							} else
								lastProbDirX = 0;
							if (Math.abs(dlat) > 100) {
								if (dlat > 0)
									lastProbDirY = 1;
								else
									lastProbDirY = -1;
							} else
								lastProbDirY = 0;

							GeoPoint newCenter = new GeoPoint(map.getMapCenter().getLatitudeE6()
									+ (int) dlat, map.getMapCenter().getLongitudeE6() + (int) dlong);
							map.getController().setCenter(newCenter);
						}
						lastRepair = SystemClock.uptimeMillis();
					} catch (java.lang.IllegalStateException e) {
						e.printStackTrace();
					}
				}
                int timerShortDelay = 20;
                timer.schedule(new Repairer(), timerShortDelay);
			} else {
				repairing = false;
                int timerLongDelay = 500;
                timer.schedule(new Repairer(), timerLongDelay);
			}
		}
	}

	public OnMapTouchLimiterListener(MapView map, Rect bounds) {
		this.map = map;
		this.bounds = bounds;
		timer = new Timer();
		campusCenter = new GeoPoint(bounds.centerY(), bounds.centerX());
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (!limiting)
			return false;

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			endTouch = false;
		}
		if (endTouch)
			return true;

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			lastX = event.getX();
			lastY = event.getY();
		} else {
			if (repairing || SystemClock.uptimeMillis() - lastRepair < 500) {
				if (lastProbDirX == Math.signum(lastX - event.getX())
						|| lastProbDirY == Math.signum(lastY - event.getY())) {
					return true;
				}
			}
			lastX = event.getX();
			lastY = event.getY();
		}

		if (MapViewLimiter.outOfBounds(map, bounds)) {
			timer.schedule(new Repairer(), 0);
			return true;
		}

		return false;
	}
}
