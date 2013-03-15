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
 Class: Codes
 The location codes for a point. Includes building code, floor code, and index.
 Also includes constants for buildings and non-numerical floors.

 Author:
 Brandon Kase
 */
public class WaypointID {

	public final int buildingIndex;
	public final int floorIndex;
	public final int pointIndex;

	/*
	 * Constructor: Codes
	 * 
	 * Parameters: buildingCode - The building code floorCode - The floor code
	 * floorIndex - The index
	 */
	public WaypointID(int buildingIndex, int floorIndex, int pointIndex) {
		this.buildingIndex = buildingIndex;
		this.floorIndex = floorIndex;
		this.pointIndex = pointIndex;
	}
	
	public WaypointID() {
		this.buildingIndex = -1;
		this.floorIndex = -1;
		this.pointIndex = -1;
	}

	/*
	 * Function: parseWaypoint
	 *
	 * Parameters:
	 * waypointString - waypoint in the form of "buildingIndex,floorIndex,pointIndex" 
	 * 
	 * Returns:
	 * The WaypointID for the given string
	 */
	public static WaypointID parseWaypoint(String waypointString) {
		String[] items = waypointString.split(",");
		
		int buildingIndex = Integer.parseInt(items[0]);
		int floorIndex = Integer.parseInt(items[1]);
		int pointIndex = Integer.parseInt(items[2]);

		return new WaypointID(buildingIndex, floorIndex, pointIndex);
	}

	/*
	 * Function: equals Tests if two codes are equal
	 * 
	 * Parameters: obj - The other Codes
	 * 
	 * Returns: true if the two codes are equal, false otherwise
	 */
	public boolean equals(Object obj) {
        if (!(obj instanceof WaypointID))
            return false;
		WaypointID other = (WaypointID) obj;
		return this.buildingIndex == other.buildingIndex
				&& this.floorIndex == other.floorIndex
				&& this.pointIndex == other.pointIndex;
	}

	/*
	 * Function: hashCode HashCode algorithm from
	 * http://eternallyconfuzzled.com/tuts/algorithms/jsw_tut_hashing.aspx Bob
	 * Jenkins' "One-at-a-Time hash" (mostly)
	 * 
	 * Returns: The hashCode of the Codes
	 */
	public int hashCode() {
		char[] idBytes = { (char) (buildingIndex), (char) (floorIndex),
				(char) (pointIndex) }; // it will always be composed of bytes
		int hash = 0;

        for (char idByte : idBytes) {
            hash += idByte;
            hash += (hash << 10);
            hash ^= (hash >> 6);
        }
		hash += (hash << 3);
		hash ^= (hash >> 11);
		hash += (hash << 15);

		return hash; // take the id and multiply it by a large prime
	}

	public int getPointIndex() {
		return this.pointIndex;
	}

	public int getFloorIndex() {
		return this.floorIndex;
	}

	public int getBuildingIndex() {
		return this.buildingIndex;
	}

	public String toString() {
		return "" + buildingIndex + "," + floorIndex + "," + pointIndex + "";
	}
}
