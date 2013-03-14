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

import com.mapyst.campus.Campus;

//a Direction represents one section of a Route
//the Route is broken up so that the route can be displayed and understood
public class Direction {
	private String text;
	private WaypointID start, end;
	private int startIcon, endIcon;
	private int type, time;
	private LatLngPoint[] points;
	
	//types of directions
	public static final int SAME_FLOOR = 0;
	public static final int STAIRS = 1;
	public static final int ELEVATOR = 2;
	public static final int RAMP = 3;
	
	public Direction() {
		startIcon = -1;
		endIcon = -1;
	}
	
	public Direction (String text, Waypoint2D start, Waypoint2D end, Route route, int type) {
		this.text = text;
		this.start = start.getId();
		this.end = end.getId();
		this.type = type;
		
		startIcon = -1;
		endIcon = -1;
		
		setPoints(route.getPoints(start, end));
		time = route.getTime(start, end);
	}
	
	public boolean isOutside(Campus campus) {
		return campus.buildingIsOutside(end.getBuildingIndex());
	}
	
	public int getBuilding() {
		return end.getBuildingIndex();
	}
	
	public int getFloor() {
		return end.getFloorIndex();
	}
	
	public String getText() {
		return text;
	}
	
	public WaypointID getStart() {
		return start;
	}
	
	public WaypointID getEnd() {
		return end;
	}
	
	public int getStartIcon() {
		return startIcon;
	}
	
	public int getEndIcon() {
		return endIcon;
	}
	
	public int getType() {
		return type;
	}
	
	public LatLngPoint[] getPoints() {
		return points;
	}
	
	public int getTime() {
		return time;
	}
	
	
	
	
	public void setText(String text) {
		this.text = text;
	}
	
	public void setStart(WaypointID start) {
		this.start = start;
	}
	
	public void setEnd(WaypointID end) {
		this.end = end;
	}
	
	public void setStartIcon(int startIcon) {
		this.startIcon = startIcon;
	}
	
	public void setEndIcon(int endIcon) {
		this.endIcon = endIcon;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public void setPoints(LatLngPoint[] points) {
		this.points = points;
	}
	
	public void setTime(int time) {
		this.time = time;
	}
	
	public void addTime(int added) {
		time += added;
	}
	
	public String toString() {
		return ("text: " + text + ", start: " + start.toString() + ", end: " +
				end.toString() + ", startIcon: " + startIcon + ", endIcon: " + endIcon +
				", type: " + type);
	}
}
