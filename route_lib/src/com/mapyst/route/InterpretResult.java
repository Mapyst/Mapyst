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

import java.util.HashSet;

public class InterpretResult {
	private String text;
	private HashSet<WaypointID> waypoints;
	
	public InterpretResult() {
		waypoints = new HashSet<WaypointID>();
	}
	
	//DEBUG CONSTRUCTOR
	public InterpretResult(WaypointID id) {
		this.waypoints = new HashSet<WaypointID>();
		this.waypoints.add(id);
	}
	
	public String toString() {
		return text + " " + waypoints.toString();
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public void addWaypoint(WaypointID waypoint) {
		waypoints.add(waypoint);
	}
	
	public void addWaypoints(HashSet<WaypointID> waypoints) {
		this.waypoints.addAll(waypoints);
	}
	
	public HashSet<WaypointID> getWaypoints() {
		return waypoints;
	}
	
	public WaypointID getPointID() {
		if (waypoints.size() > 0) {
			return waypoints.iterator().next();
		}
		else {
			return null;
		}
	}
}
