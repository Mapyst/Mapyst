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

import android.os.Handler;

import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class AnimatedMapZoomer implements Runnable {

	private int targetLatSpan;
	private int targetLongSpan;

	private MapView mapView;
	private MapController mapController;

	private int startLatSpan;
	private int startLongSpan;

	private long time;

	private Handler handler;

	public AnimatedMapZoomer(MapView mapView, Handler handler, int targetLatSpan, int targetLongSpan, long time) {
		this.targetLatSpan = targetLatSpan;
		this.targetLongSpan = targetLongSpan;
		this.mapView = mapView;
		this.mapController = mapView.getController();

		this.startLatSpan = mapView.getLatitudeSpan();
		this.startLongSpan = mapView.getLongitudeSpan();
		this.time = time;
		this.handler = handler;
	}

	// putting in new thread for now, may improve performance
	@Override
	public void run() {
		int x = mapView.getZoomLevel();

		int dir = 1;
		if (startLatSpan < targetLatSpan || startLongSpan < targetLongSpan)
			dir = -1;

		for (int i = mapView.getZoomLevel(); i < 25 && i >= 0; i += dir) {
			mapController.setZoom(i);
			if (i == 24	|| i == 0 || (dir == 1 && (mapView.getLatitudeSpan() < targetLatSpan 
					|| mapView.getLongitudeSpan() < targetLongSpan))
					|| (dir == -1 && mapView.getLatitudeSpan() > targetLatSpan && mapView.getLongitudeSpan() > targetLongSpan)) {
				mapController.setZoom(x);
				final int dirF = dir;
				int numZooms = Math.abs(x - i);
				long delay = 0;
				if (dir == 1) {
					delay = time;
					numZooms -= 1;
				}
				for (int j = 0; j < numZooms; j++) {
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (dirF == -1) {
								mapController.zoomOut();
							} else if (dirF == 1) {
								mapController.zoomIn();
							}
						}
					}, delay);
				}
				break;

			}
		}
	}
}