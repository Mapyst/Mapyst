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
import java.util.HashMap;
import java.util.Map.Entry;

import com.mapyst.campus.Campus;

//interprets text to decide which point(s) (if any) in the graph it represents 
public class Interpreter {

	private Campus campus;
	private HashMap<WaypointID, GraphNode<Waypoint2D>> loadedNodes;

	private static final int STATE_BUILDING = 0;
	private static final int STATE_FLOOR_OR_ROOM = 1;
	private static final int STATE_ROOM = 2;

	public Interpreter(Campus campus) {
		this.campus = campus;
		clean();
	}

	public void clean() {
		loadedNodes = new HashMap<WaypointID, GraphNode<Waypoint2D>>();
	}

	//example input for CMU: lat=40441885, lng=-79942778
	public InterpretedInfo interpretLatLng(int lat, int lng) {
		RouteFinder.loadOutsideBuildings(campus, loadedNodes);
		
		InterpretResult result = new InterpretResult();
		result.addWaypoint(pointFromLoc(lat, lng));
		result.setText("Lat: " + ((double)lat)/1e6 + ", " + "Long: " + ((double)lng)/1e6);
		
		InterpretedInfo info = new InterpretedInfo();
		info.addSuggestion(result);
		
		return info;
	}

	public InterpretedInfo interpret(String input, boolean type) {
		InterpretedInfo info = new InterpretedInfo();
		info.setInput(input);
		preprocessInput(info);
		info.type = type;
		
		//DEBUG visualizer
		if (input.matches("#\\d+,\\d+,\\d+")) {
			input = input.substring(1);
			String[] indexStrings = input.split(",");
			int buildingIndex = Integer.parseInt(indexStrings[0]);
			int floorIndex = Integer.parseInt(indexStrings[1]);
			int roomIndex = Integer.parseInt(indexStrings[2]);
			
			info.addSuggestion(new InterpretResult(new WaypointID(buildingIndex, floorIndex, roomIndex)));
			return info;
		}
		
		if (input.startsWith("lat:")) {
			int lngStringIndex = input.indexOf(" lng:");
			int lat = (int)(Double.parseDouble(input.substring(4, lngStringIndex)) * 1E6);
			int lng = (int)(Double.parseDouble(input.substring(lngStringIndex + 5)) * 1E6);
			return interpretLatLng(lat, lng);
		}
		
		//match input to a building, floor, and room
		ArrayList<InterpretedInfo> infoList = parse(info, STATE_BUILDING);
		for(int i = 0; i < infoList.size(); i++) {
			info.addSuggestions(infoList.get(i).getSuggestions());
		}
		
		//match the input to a location
		if (infoList.size() == 0)
			matchCampusLocations(info);
		
		//combineMultiples(info);
		checkForExactMatch(info);
		
		return info;
	}
	
	private void preprocessInput(InterpretedInfo info) {
		String input = info.getInput().toLowerCase().trim();
		
		String[] stringsToRemove = {"hall", "room", "#", "floor", " "};
		
		//TODO: Examine if important to have spaces
		for (int i = 0; i < stringsToRemove.length; i++) {
			input = input.replace(stringsToRemove[i], "");
		}
		
//		int startParenthesis = input.indexOf("(");
//		int endParenthesis = input.indexOf(")");
//		if (startParenthesis > -1 && endParenthesis > -1) {
//			input = input.substring(0, startParenthesis) + input.substring(endParenthesis + 1, input.length());
//		}
		
		info.setInput(input);
	}
	
//	private void combineMultiples(InterpretedInfo info) {
//		ArrayList<InterpretResult> suggestions = info.getSuggestions();
//		//The runtime complexity of this is not great, but the size of the collection is tiny
//		for (int i = 0; i < suggestions.size(); i++) {
//			InterpretResult s1 = suggestions.get(i);
//			for (int j = i + 1; j < suggestions.size(); j++) {
//				InterpretResult s2 = suggestions.get(j);
//				if (s1.getText().equals(s2.getText())) {
//					s1.addWaypoints(s2.getWaypoints());
//					suggestions.remove(j);
//					j--;
//				}
//			}
//		}
//	}
	
	//if there is an exact match then make that the only suggestion
	private void checkForExactMatch(InterpretedInfo info) {
		ArrayList<InterpretResult> suggestions = info.getSuggestions(); 
		for (int i = 0; i < suggestions.size(); i++) {
			InterpretResult result = suggestions.get(i);
			if (result.getText().toLowerCase().equals(info.getOriginalInput().toLowerCase())) {
				suggestions.clear();
				suggestions.add(result);
				return;
			}
		}
	}

	private ArrayList<InterpretedInfo> parse(InterpretedInfo info, int state) {
		ArrayList<InterpretedInfo> infoList = new ArrayList<InterpretedInfo>();
		if (info.getInput().equals("")) {
			return infoList;
		}
		else {
			int numFloors = -1;
			if (info.buildingIndex != -1)
				numFloors = campus.buildings[info.buildingIndex].floors.length;
			
			switch(state) {
			case STATE_BUILDING:
				infoList.addAll(parseBuilding(info));
				break;

			case STATE_FLOOR_OR_ROOM:
				if (numFloors < 10) {
					InterpretedInfo roomInfo = parseRoom(info);
					if (roomInfo.getSuggestions().size() > 0)
						infoList.add(roomInfo);
				}
				else {
					infoList.addAll(parseFloor(info, numFloors));
				}
				break;

			case STATE_ROOM:
				if (numFloors < 10) {
					return infoList;
				}
				else {
					InterpretedInfo roomInfo = parseRoom(info);
					if (roomInfo.getSuggestions().size() > 0)
						infoList.add(roomInfo);
				}
				break;
			}
		}

		return infoList;
	}

	private ArrayList<InterpretedInfo> parseBuilding(InterpretedInfo info) {
		InterpretedInfo newInfo = null;
		ArrayList<InterpretedInfo> infoList = new ArrayList<InterpretedInfo>();
		for (int i = 0; i < campus.buildings.length; i++) { 
			//TODO: MAKE AUTHORING TOOL MAKE ACRONYMS HAVE NO SPACES
			String acronym = campus.buildings[i].acronym.toLowerCase().replaceAll(" ", "");
			//cut the acronym off the start or the end
			newInfo = tryBuildingStartAndEnd(info, acronym, i);

			if (newInfo != null) {
				infoList.addAll(parse(newInfo, STATE_FLOOR_OR_ROOM));
			}

			for (int j = 0; j < campus.buildings[i].names.length; j++) {
				String name = campus.buildings[i].names[j].toLowerCase().trim().replaceAll(" ", "");
				newInfo = tryBuildingStartAndEnd(info, name, i);
				if (newInfo != null) {
					infoList.addAll(parse(newInfo, STATE_FLOOR_OR_ROOM));
				}
			}
		}

		//this returns all successful interprets
		return infoList;
	}

	private InterpretedInfo tryBuildingStartAndEnd(InterpretedInfo info, String name, int buildingIndex) {
		InterpretedInfo newInfo = null;
		if (info.getInput().startsWith(name) && !name.equals("")) {
			newInfo = 
				new InterpretedInfo(info, 
						info.getInput().substring(name.length()).trim(), //input string with acronym removed 
						buildingIndex, -1);
		}
		else if (info.getInput().endsWith(name) && !name.equals("")) {
			newInfo = 
				new InterpretedInfo(info, 
						info.getInput().substring(0, info.getInput().length() - name.length()).trim(), //input string with acronym removed 
						buildingIndex, -1);
		}
		return newInfo;
	}

	private InterpretedInfo tryFloorStart(InterpretedInfo info, String name, int floorIndex) {
		InterpretedInfo newInfo = null;
		if (info.getInput().startsWith(name) && !name.equals("")) {
			newInfo = 
				new InterpretedInfo(info, 
						info.getInput().substring(name.length()).trim(), //input string with acronym removed 
						info.buildingIndex, floorIndex);
		}
		return newInfo;
	}

	/* This takes the info from the parseBuilding through the parse recursion */
	private ArrayList<InterpretedInfo> parseFloor(InterpretedInfo info, int numFloors) {
		InterpretedInfo newInfo = null;
		ArrayList<InterpretedInfo> infoList = new ArrayList<InterpretedInfo>();

		for (int i = 0; i < campus.buildings[info.buildingIndex].floors.length; i++) { 
			String floorName = campus.buildings[info.buildingIndex].floors[i].name.toLowerCase();
			//cut the floor name off the start of the input
			newInfo = tryFloorStart(info, floorName, i);
			
			if (newInfo != null) {
				infoList.addAll(parse(newInfo, STATE_ROOM));
			}
		}

		//this list is all possibilities for floors in a building (info.buildingIndex)
		return infoList;
	}	

	private InterpretedInfo parseRoom(InterpretedInfo info) {
		InterpretedInfo newInfo = new InterpretedInfo(info);
		int buildingIndex = info.buildingIndex;
		int floorIndex = info.floorIndex;
		
		if (loadedNodes.size() > 0)
			loadedNodes.clear();
		if (info.floorIndex == -1) {
			RouteFinder.loadBuilding(campus.buildings[buildingIndex], campus, loadedNodes);
		}
		else {
			DataParser.parseFile(campus.getFloorFile(buildingIndex, floorIndex, "ncmg"), loadedNodes, Campus.fileHandler);
		}
		
		StringBuilder inputRem = new StringBuilder(info.getInput());
		int originalLength = inputRem.length();
		boolean foundSome = false;

		//Start by removing no characters for the input (k=0)
		Outer: for (int k = 0; k < originalLength-1 && inputRem.length() >= 2; k++) {
			
			//For each of the points in loadedNodes
			for (Entry<WaypointID, GraphNode<Waypoint2D>> e : loadedNodes.entrySet()) {
				
				if (e.getValue().data.getLabel().toLowerCase().contains(inputRem.toString())){
						//&& e.getKey().getFloorIndex() == floorIndex
						//&& e.getKey().getBuildingIndex() == buildingIndex) {
					
					foundSome = true;

					//Construct end text and determine show dialog and modified additions
					for (String token : e.getValue().data.getLabel().toLowerCase().split(",")) {
						if (token.contains(inputRem.toString())) {
							int b = e.getKey().getBuildingIndex();
							
							InterpretResult result = new InterpretResult();
							
							result.addWaypoint(new WaypointID(buildingIndex, e.getKey().getFloorIndex(), e.getKey().getPointIndex()));
							
							String buildingAcronym = campus.buildings[b].acronym;
							result.setText(buildingAcronym + " " + token);
							
							newInfo.addSuggestion(result);
							
//							if (!info.getInput().toString().equals(token)) {
//								newInfo.addSuggestion(buildingAcronym + " " + token);	
//							}
						}
					}
					
					
				}
			}
			if (foundSome)
				break Outer;
			inputRem.deleteCharAt(inputRem.length() - 1); //remove a character (and then increment k in the for loop)
		}
		
		return newInfo;
	}	

	/*
	 * Checks if the input can be associated with any campus locations
	 * If so, it adds those locations to the info's suggestions
	 */
	private void matchCampusLocations(InterpretedInfo info) {
		for (int i = 0; i < campus.location_types.length; i++) {
			for (int j = 0; j < campus.location_types[i].locations.length; j++) {
				for (int k = 0; k < campus.location_types[i].locations[j].names.length; k++) {
					String locationName = campus.location_types[i].locations[j].names[k];
					String locNameForComparison = locationName.toLowerCase().trim().replaceAll(" ", ""); 
					if (locNameForComparison.contains(info.getInput()) || info.getInput().contains(locNameForComparison)) {
						
						WaypointID[] locationWaypoints = campus.location_types[i].locations[j].waypoints;
						
						InterpretResult result = new InterpretResult();
						result.setText(locationName);
						if (info.type == InterpretedInfo.END) {
							for (int w = 0; w < locationWaypoints.length; w++) {
								result.addWaypoint(locationWaypoints[w]);
							}
						}
						else {
							result.addWaypoint(locationWaypoints[0]);
						}
						
						info.addSuggestion(result);
					}
				}
			}
		}
	}

	private WaypointID pointFromLoc(int lat, int lng) {
		loadedNodes = new HashMap<WaypointID, GraphNode<Waypoint2D>>();
		LatLngPoint point = new LatLngPoint(lat, lng);


		double closest = Double.MAX_VALUE;
		WaypointID closestID = null;

		RouteFinder.loadOutsideBuildings(campus, loadedNodes);

		for (Entry<WaypointID, GraphNode<Waypoint2D>> e: loadedNodes.entrySet()) {
			Waypoint2D point2D = e.getValue().data;

			double dist = DistanceCalculator.distance(point, point2D.getPoint());
			if (dist < closest) {
				closest = dist;
				closestID = point2D.getId();
			}
		}

		return closestID;
	}

	public boolean startsWithRoom(String input) {
		return input.length() > 1 && Character.isDigit(input.charAt(1));
	}
}


