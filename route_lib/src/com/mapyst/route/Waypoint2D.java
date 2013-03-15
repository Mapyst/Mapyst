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


/*
 Class: MyPoint2D
 A point in 2D space on campus with an (x, y);
 a code for it's building, floor, and index; a label;
 and the distance from the starting point (for Dijkstra's algorithm).

 Author:
 Brandon Kase
 */
public class Waypoint2D implements Prioritizable {

	private LatLngPoint point;
	private WaypointID id;
	private int distance;
	private String label;

	/*
	 * Constructor: MyPoint2D
	 * 
	 * Parameters: x - The x coordinate of the point y - The y coordinate of the
	 * point buildingCode - The code for the building floorCode - The code for
	 * the floor floorIndex - The code for the index label - The label for the
	 * point
	 */
	public Waypoint2D(LatLngPoint point, int buildingCode, int floorCode,
			int floorIndex, String label) {
		this.point = point;
		this.id = new WaypointID(buildingCode, floorCode, floorIndex);
		this.label = label;
		this.distance = 0;
	}

	public Waypoint2D() {
		this.point = new LatLngPoint();
		this.id = new WaypointID();
		this.label = "";
		this.distance = 0;
	}

	public LatLngPoint getPoint() {
		return this.point;
	}

	/*
	 * Function: getDistance
	 * 
	 * Returns: The distance from the starting point (for Dijkstra's algorithm)
	 */
	public int getDistance() {
		return this.distance;
	}

	/*
	 * Function: getLabel
	 * 
	 * Returns: The label of the point
	 */
	public String getLabel() {
		return this.label;
	}

	/*
	 * Function: setLabel Sets the label of the point
	 * 
	 * Parameters: label - The new label of the point
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/*
	 * Funtion: getId
	 * 
	 * Retuns: The ID of the point
	 */
	public WaypointID getId() {
		return this.id;
	}

	/*
	 * Function: equals Tests if two points are equal
	 * 
	 * Parameters: other - The other point to test against for equality
	 * 
	 * Returns: true if the two points are equal, false otherwise
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof Waypoint2D))
			return false;
		Waypoint2D other = (Waypoint2D) obj;
		return (this.point.lng == other.getPoint().lng) && (this.point.lat == other.getPoint().lat && this.id.equals(other.getId()));
	}
	
	public int hashCode() {
		return this.id.hashCode();
	}

	public String toString() {
		return "" + "lat: " + this.point.lat + ",   lng: " + this.point.lng + ", id: " + this.id
				+ ", Label: " + this.label;
	}

	/*
	 * Function: getPriority
	 * 
	 * Returns: The distance (the priority) from the start point
	 * 
	 * See Also: Prioritizable
	 */
	public int getPriority() {
		return this.distance;
	}

	/*
	 * Function: setPriority Sets the priority (or distance) (for priority
	 * queue)
	 * 
	 * Parameters: newPrior - The new distance (the new priority) from the start
	 * point
	 * 
	 * See Also: Prioritizable
	 */
	public void setPriority(int newPrior) {
		this.distance = newPrior;
	}
}
