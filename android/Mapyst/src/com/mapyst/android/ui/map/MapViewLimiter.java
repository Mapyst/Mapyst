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

import com.google.android.maps.MapView;
import com.mapyst.android.ui.RouteMapOverlay;

public class MapViewLimiter {

	public static boolean tooFarOut(MapView map, Rect targetBounds) {
		Rect currBounds = MapUtils.getCurrBounds(map);
		Rect newTargetBounds = getNewTargetBounds(targetBounds, 4);
		return currBounds.width() > newTargetBounds.width() * .75;
	}

	public static Rect getNewTargetBounds(Rect targetBounds, float scale) {
		Rect newTargetBounds = new Rect(targetBounds);
		newTargetBounds.inset(-newTargetBounds.height() * 4,
				-newTargetBounds.height() * 4);
		return newTargetBounds;
	}

	public static boolean outOfBounds(MapView map, Rect targetBounds) {
		return outOfBounds(map, targetBounds, 4);
	}

	public static boolean outOfBounds(MapView map, Rect targetBounds,
			float scale) {
		Rect currBounds = MapUtils.getCurrBounds(map);
		Rect newTargetBounds = getNewTargetBounds(targetBounds, scale);
		if (RouteMapOverlay.debugRects.size() == 0)
			RouteMapOverlay.debugRects.add(newTargetBounds);
		boolean contains = newTargetBounds.contains(currBounds);
		return !contains;
	}
}
