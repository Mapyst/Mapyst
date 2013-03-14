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

package com.mapyst.route;

public class LatLngPoint {
	//latitude can be thought of as y
	//longitude can be thought of as x
	public int lat, lng; //multiplied by a million
	
	public LatLngPoint() {
		lat = 0;
		lng = 0;
	}
	
	//note: use Google's GeoPoint convention lat then lng
	public LatLngPoint(int lat, int lng) {
		this.lng = lng;
		this.lat = lat;
	}
	
	public String toString() {
		return "lat: " + lat + ";   lng: " + lng;
	}
	
	public LatLngPoint copy() {
		return new LatLngPoint(this.lat, this.lng);
	}
}
