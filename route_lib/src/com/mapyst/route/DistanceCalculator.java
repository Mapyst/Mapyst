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

import com.mapyst.campus.Building;

//calculates distances between points and buildings
public class DistanceCalculator {
	
	public static double distance(LatLngPoint p1, LatLngPoint p2) {
		return Math.sqrt(Math.pow(p1.lng - p2.lng, 2) + Math.pow(p1.lat - p2.lat, 2));
	}
	
	public static double buildingDistance(Building b1, Building b2) {
		LatLngPoint p1 = b1.location;
		LatLngPoint p2 = b2.location;
		
		return distance(p1, p2);
	}
	
	//distance from one building to another building plus distance from that building to yet another building
	public static double buildingDistance(Building b1, Building b2, Building b3) {
		LatLngPoint p1 = b1.location;
		LatLngPoint p2 = b2.location;
		LatLngPoint p3 = b3.location;
		
		double dist1 = distance(p1, p2);
		double dist2 = distance(p2, p3);
		return dist1 + dist2;
	}

	/*public static double buildingDistance(Waypoint2D node1, Waypoint2D node2) {
		double dx1, dy1;
		dx1 = node1.getPoint().lngInt() - node2.getPoint().lngInt();
		dy1 = node1.getPoint().latInt() - node2.getPoint().latInt();
		double dist1 = Math.sqrt(Math.pow(dx1, 2.0) + Math.pow(dy1, 2.0));
		return dist1;
	}*/

	/*public static double angle3Points(Waypoint2D p1, Waypoint2D p2, Waypoint2D p3) {
		double a = distance(p2.getPoint(), p3.getPoint());
		double b = distance(p1.getPoint(), p2.getPoint());
		double c = distance(p1.getPoint(), p3.getPoint());
		return Math.acos((Math.pow(a,2) + Math.pow(b,2) - Math.pow(c,2))/(2*a*b));
	}*/
}
