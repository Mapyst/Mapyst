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

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.mapyst.route.Direction;
import com.mapyst.route.LatLngPoint;

public class MapViewMover {

	public static void smoothFitToDirection(MapView mapView, Handler handler, double border, Direction direction) {
		float maxY = -Float.MAX_VALUE;
		float maxX = -Float.MAX_VALUE;
		float minY = Float.MAX_VALUE;
		float minX = Float.MAX_VALUE;
		LatLngPoint[] points = direction.getPoints();
        for (LatLngPoint point : points) {
            if (point.lng > maxX)
                maxX = point.lng;
            if (point.lng < minX)
                minX = point.lng;
            if (point.lat > maxY)
                maxY = point.lat;
            if (point.lat < minY)
                minY = point.lat;
        }
		MapViewMover.smoothFitToRect(mapView, handler, (int) minX, (int) maxX, (int) minY, (int) maxY, border, null);
	}

	public static void smoothFitToPoint(MapView mapView, Handler handler, double border, LatLngPoint point) {
		MapViewMover.smoothFitToRect(mapView, handler, point.lng - 10, point.lng + 10, point.lat - 10, point.lat + 10, border, null);
	}

	public static void smoothFitToRect(MapView map, Handler handler, Rect rect, double border, Running running) {
		smoothFitToRect(map, handler, rect.left, rect.right, rect.top, rect.bottom, border, running);
	}

	// only moves if not in view or view not large enough
	public static void smoothFitToRect(MapView mapView, Handler handler,
			float minX, float maxX, float minY, float maxY, double border, Running running) {

		GeoPoint minYminXGP = new GeoPoint((int) minY, (int) minX);
		GeoPoint minYmaxXGP = new GeoPoint((int) minY, (int) maxX);
		GeoPoint maxYmaxXGP = new GeoPoint((int) maxY, (int) maxX);
		GeoPoint maxYminXGP = new GeoPoint((int) maxY, (int) minX);

		GeoPoint geoPoint = new GeoPoint((int) ((maxY + minY) / 2.0f), (int) ((maxX + minX) / 2));

		boolean notWholeRouteInBounds = !isGeopointVisible(mapView, minYminXGP)
				|| !isGeopointVisible(mapView, minYmaxXGP)
				|| !isGeopointVisible(mapView, maxYmaxXGP)
				|| !isGeopointVisible(mapView, maxYminXGP);

		boolean dimsTooSmall = maxY - minY > mapView.getLatitudeSpan() || maxX - minX > mapView.getLongitudeSpan();
		float minSize = 1 / 3.f;
		boolean dimsTooBig = maxY - minY < mapView.getLatitudeSpan() * minSize || maxX - minX < mapView.getLongitudeSpan() * minSize;

		if (notWholeRouteInBounds || dimsTooSmall || dimsTooBig) {
			smoothMove(mapView, handler, geoPoint, (int) ((maxY - minY) * border), (int) ((maxX - minX) * border), true, running);
		}
	}

	public static void smoothMove(final MapView mapView, Handler handler, GeoPoint targetCenter, Running running) {
		smoothMove(mapView, handler, targetCenter, 0, 0, false, running);
	}

	public static void smoothMove(final MapView mapView, Handler handler, GeoPoint targetCenter, final int targetLongSpan,
			final int targetLatSpan, boolean zoom, Running running) {
		final MapController mControl = mapView.getController();

		// a Quick runnable to zoom in
		GeoPoint currentCenter = mapView.getMapCenter();

		// yes brandon, we need doubles.

		double currentLat = currentCenter.getLatitudeE6();
		double currentLong = currentCenter.getLongitudeE6();

		final int targetLat = targetCenter.getLatitudeE6();
		final int targetLong = targetCenter.getLongitudeE6();

		int rateOfChange = 100;

		double deltaLong = 0;
		double deltaLat = 0;

		deltaLat = -(currentLat - targetLat) / rateOfChange;
		deltaLong = -(currentLong - targetLong) / rateOfChange;

		long delay = 0;
		while (!equalWithinEpsilon(currentLat, targetLat, deltaLat * 2)	|| !equalWithinEpsilon(currentLong, targetLong, deltaLong * 2)) {

			if (!equalWithinEpsilon(currentLong, targetLong, deltaLong * 2)) {
				currentLong += deltaLong;
			}
			if (!equalWithinEpsilon(currentLat, targetLat, deltaLat * 2)) {
				currentLat += deltaLat;
			}
			final int newLong = (int) currentLong;
			final int newLat = (int) currentLat;

			handler.postDelayed(new RunnableMover(mControl, new GeoPoint(newLat, newLong), running), delay);

			delay += 5;
			if (delay > 1000) // just so we don't get stuck in an infinite loop
				break;
		}
		// System.out.println(delay);
		handler.postDelayed(new RunnableMover(mControl, new GeoPoint(targetLat, targetLong), running), delay);
		if (zoom) {
			AnimatedMapZoomer amz = new AnimatedMapZoomer(mapView, handler, targetLatSpan, targetLongSpan, delay);
			new Thread(amz).start();
		}

	}

	private static class RunnableMover implements Runnable {

		public RunnableMover(MapController mControl, GeoPoint target, Running running) {
			this.target = target;
			this.mControl = mControl;
			this.running = running;
		}

		private MapController mControl;
		private GeoPoint target;

		private Running running;

		@Override
		public void run() {
			// System.out.println(running.running);
			if (running == null || running.running)
				mControl.setCenter(target);
		}
	}

	public static class Running {
		public boolean running;
	}

	private static boolean equalWithinEpsilon(double a, double b, double epsilon) {
		return Math.abs(a - b) < Math.abs(epsilon);
	}

	public static boolean isGeopointVisible(MapView mapView, GeoPoint point) {
		Rect currentMapBoundsRect = new Rect();
		Point geopointPosition = new Point();

		mapView.getProjection().toPixels(point, geopointPosition);
		mapView.getDrawingRect(currentMapBoundsRect);

		return currentMapBoundsRect.contains(geopointPosition.x, geopointPosition.y);

	}
}
