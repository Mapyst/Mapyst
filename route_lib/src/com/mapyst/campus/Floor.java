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

package com.mapyst.campus;

import com.mapyst.route.LatLngPoint;

public class Floor {
	public String name;
	public LatLngPoint northWest, southEast;
	
	//the route finder will only load a floor's data into the graph if appropriate
	//if the start or end is in a floor's building, then the floor will be loaded
	//if the floor is close to the start and end (and therefore could potentially
	//contribute to the fastest route), then it will be loaded if load_if_close is true
	//load_if_close should be true if it is possible that the user would
	//be on the floor for the fastest route (or other preferred route)
	//example: the top floor of a building (if not connected to another building)
	//		would be set to false
	public boolean load_if_close;
	
	public int getCenterLat() {
		return (northWest.lat + southEast.lat) / 2;
	}
	
	public int getCenterLng() {
		return (northWest.lng + southEast.lng) / 2;
	}
}
