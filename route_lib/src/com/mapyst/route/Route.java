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

import java.util.ArrayList;

import com.mapyst.campus.Building;
import com.mapyst.campus.Campus;
import com.mapyst.campus.Location;

public class Route {
	private Waypoint2D[] points;
	private int[] terrains, times; //terrain and time for each edge/arc (connection of two points)
	private Direction[] directions;
	public String startText, endText;
	
	//for a single point search
	public Waypoint2D singleWaypoint;
	public String singlePointText;
	
	public Route(InterpretResult onePointResult, RouteFinder graphManager)  {
		this.singleWaypoint = graphManager.getWaypoint2D(onePointResult.getPointID());
		this.singlePointText = onePointResult.getText();
	}
	
	public Route(Waypoint2D[] points, int[] terrains, int[] times, String startText, String endText, Campus campus) {
		this.points = points;
		this.terrains = terrains;
		this.times = times;
		
		this.startText = startText;
		this.endText = endText;
		
		//checks to make sure that all the points are non-null
		for (int i = 0; i < points.length; i++) {
			//System.out.println("orig point: " + points[i]);
			if (points[i] == null)
				return; 
		}
		
		directions = calcDirs(endText, campus);
	}
	
	//steps through route's points/terrains
	//breaks route into directions
	private Direction[] calcDirs(String endText, Campus campus) {
		ArrayList<Direction> directions = new ArrayList<Direction>(10);
		Waypoint2D dirStart = points[0];
		//int dirIndex = 0;
		String text = "";
		
		Location[] directionEndLocations = campus.getDirectionEndLocations();
		
		int i;
		for (i = 1; i < points.length; i++) {
			//add a "go through floor" direction when it first hits a elevator, stairs, or ramp
			if (terrains[i-1] == Arc.Terrains.ELEVATOR || terrains[i-1] == Arc.Terrains.INSIDE_STAIRS || terrains[i-1] == Arc.Terrains.RAMP) {
				if (i == 1 || (i-2 >= 0 && terrains[i-2] != terrains[i-1])) {
					int b = points[i-1].getId().getBuildingIndex();
					int f = points[i-1].getId().getFloorIndex();
					
					String floorText = campus.getFloor(b, f).name;
					String buildingText = campus.buildings[b].names[0];
					text = "Go Through " + buildingText + " Floor " + floorText;
					if (terrains[i-1] == Arc.Terrains.ELEVATOR) {
						if (i == 1) {
							directions.add(new Direction(text, dirStart, points[i-1], this, Direction.SAME_FLOOR));
							dirStart = points[i-1];
						}
						else {
							directions.add(new Direction(text, dirStart, points[i-2], this, Direction.SAME_FLOOR));
							dirStart = points[i-2];
						}
					}
					else {
						directions.add(new Direction(text, dirStart, points[i-1], this, Direction.SAME_FLOOR));
						dirStart = points[i-1];
					}
					//dirIndex++;
				}
			}
			//add elevator/stairs/ramp direction after no longer in elevator/stairs/ramp
			//if statement above catches currently in an elevator/stairs/ramp
			else if (i-2 >= 0 && (terrains[i-2] == Arc.Terrains.ELEVATOR || terrains[i-2] == Arc.Terrains.INSIDE_STAIRS || terrains[i-2] == Arc.Terrains.RAMP)) {
				dirStart = insertSpecialDir(i, dirStart, directions, campus);
				//dirIndex++;
			}
			//add direction if building changes
			else if (points[i].getId().getBuildingIndex() != points[i-1].getId().getBuildingIndex()) {
				int b = points[i-1].getId().getBuildingIndex();
				int f = points[i-1].getId().getFloorIndex();
				
				String floorText = campus.getFloor(b, f).name;
			
				if (campus.buildingIsOutside(b)) {
					Building nextBuilding = campus.buildings[points[i].getId().getBuildingIndex()]; 
					text = "Walk Outside to " + nextBuilding.names[0];
				}
				else 
					text = "Go Through " + campus.buildings[b].names[0] + " Floor " + floorText;
				directions.add(new Direction(text, dirStart, points[i-1], this, Direction.SAME_FLOOR));
				dirStart = points[i-1];
				//dirIndex++;
			}
			//add direction for landmarks
			else {
				for (int j = 0; j < directionEndLocations.length; j++) {
					Location location = directionEndLocations[j];
					if (location.waypoints.length > 0) {
						for (int k = 0; k < location.waypoints.length; k++) {
							WaypointID waypointID = location.waypoints[k];
							if (waypointID.equals(points[i].getId()) && !text.equals("Go to " + location.names[0])) {
								text = "Go to " + location.names[0];
								directions.add(new Direction(text, dirStart, points[i], this, Direction.SAME_FLOOR));
								dirStart = points[i];
								//dirIndex++;
								break;
							}
						}
					}
				}
			}
		}
		
		//add direction to end point
		if (i-2 >= 0 && (terrains[i-2] == Arc.Terrains.ELEVATOR || terrains[i-2] == Arc.Terrains.INSIDE_STAIRS || terrains[i-2] == Arc.Terrains.RAMP)) {
			dirStart = insertSpecialDir(i, dirStart, directions, campus);
			//dirIndex++;
		}
		directions.add(new Direction("Destination: " + endText, dirStart, points[points.length-1], this, Direction.SAME_FLOOR));
		
		return directions.toArray(new Direction[0]);
	}

	private Waypoint2D insertSpecialDir(int i, Waypoint2D dirStart, ArrayList<Direction> directions, Campus campus) {
		int b1 = dirStart.getId().getBuildingIndex();
		int f1 = dirStart.getId().getFloorIndex();
		String floorText1 = campus.getFloor(b1, f1).name;
		
		int b2 = points[i-1].getId().getBuildingIndex();
		int f2 = points[i-1].getId().getFloorIndex();
		String floorText2 = campus.getFloor(b2, f2).name;
		String buildingText2 = campus.buildings[b2].names[0]; 
		
		int type = -1;
		String text = "";
		if (terrains[i-2] == Arc.Terrains.ELEVATOR) {
			text = "Take " + buildingText2 + " Elevator: Floor " + floorText1 + " to " + floorText2;
			type = Direction.ELEVATOR;
		}
		else if (terrains[i-2] == Arc.Terrains.INSIDE_STAIRS) {
			text = "Take " + buildingText2 + " Stairs: Floor " + floorText1 + " to " + floorText2;
			type = Direction.STAIRS;
		}
		else if (terrains[i-2] == Arc.Terrains.RAMP) {
			text = "Take " + buildingText2 + " Ramp: Floor " + floorText1 + " to " + floorText2;
			type = Direction.RAMP;
		}
		directions.add(new Direction(text, dirStart, points[i-1], this, type));
		return points[i-1];
	}
	
	//get a list of points between start and end
	public LatLngPoint[] getPoints(Waypoint2D start, Waypoint2D end) {
		ArrayList<LatLngPoint> geoPoints = new ArrayList<LatLngPoint>(10);
		int startIndex = getIndex(start);
		int endIndex = getIndex(end);
		
		for (int i = startIndex; i <= endIndex; i++){
			geoPoints.add(points[i].getPoint());
		}
		return geoPoints.toArray(new LatLngPoint[0]);
	}
	
	//calculates the total time between two points: start,end
	public int getTime(Waypoint2D start, Waypoint2D end) {
		int time = 0;
		int startIndex = getIndex(start);
		int endIndex = getIndex(end);
		
		for (int i = startIndex; i < endIndex; i++){
			time += times[i];
		}
		return time;
	}
	
	private int getIndex(Waypoint2D point) {
		int index = -1;
		for (int i = 0; i < points.length; i++) {
			if (points[i].equals(point)){
				index = i;
				break;
			}
		}
		return index;
	}
	
	public Direction[] getDirections() {
		return directions;
	}
	
	public String toString() {
		if (singleWaypoint != null)
			return "One Point:" + singlePointText;
		String s = "";
		for (Waypoint2D point: points) {
			s += ", " + point.toString();
		}
		return s;
	}
	
	public int getTime() {
		if (directions == null)
			return 0;
		int time = 0;
		for (int i = 0; i < directions.length; i++) {
			time += directions[i].getTime();
		}
		return time;
	}
	
	public void clean() {
		points = null;
		terrains = null;
		times = null;
	}
}
