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

import android.graphics.Rect;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class MapUtils {
	public static Rect getCurrBounds(MapView map) {
		int longSpan = map.getLongitudeSpan();
		int latSpan = map.getLatitudeSpan();
		GeoPoint currentCenter = map.getMapCenter();
		return new Rect(currentCenter.getLongitudeE6() - longSpan / 2,
				currentCenter.getLatitudeE6() - latSpan / 2,
				currentCenter.getLongitudeE6() + longSpan / 2,
				currentCenter.getLatitudeE6() + latSpan / 2);
	}
}
