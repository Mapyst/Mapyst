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
import java.util.HashSet;
import java.util.Map.Entry;

import com.mapyst.campus.Building;
import com.mapyst.campus.Campus;

/*
 * Class: RouteFinder
 * Prepares to find the shortest path based on preferences and then executes to find the route
 * NOTE: Make sure you do NOT load any files twice as this will ruin the path finding
 */
public class RouteFinder {

	//the hashmap of the loaded nodes
	public HashMap<WaypointID, GraphNode<Waypoint2D>> loadedNodes;
	
	private Campus campus;

	private static final int ELEVATOR_CONSTANT = 50000; //50 seconds for getting on an elevator
	
	//Graph cost modification constants
	private static final boolean NO_REMOVE = false;
	private static final boolean REMOVE_ARC = true;
	private static final int MODIFIER_CONSTANT = 4;
	private static final double THRESHOLD_MULTIPLIER = 1.5;

	public RouteFinder(Campus campus) {
		this.campus = campus;
	}

	/*
	 * Function: makeRoute
	 * Calculates the most appropriate route using Dijkstra's algorithm after parsing the input
	 * and loading the necessary files.
	 *
	 * Parameters:
	 * start - user input for the starting node
	 * end - user input for the ending node
	 * preferences - the preferences that are selected by the user in the settings menu (use the Constants.Preferences in the array)
	 * 
	 * Returns:
	 * Whether the route was found successfully.
	 */
	public Route makeRoute(InterpretResult startResult, InterpretResult endResult, RoutePreferences prefs) {
		
		//loads the necessary graph data
		loadedNodes = new HashMap<WaypointID, GraphNode<Waypoint2D>>();
		loadGraphFiles(startResult, endResult);
		
		fixElevators();

		//modify HashMap with preferences
		for (Entry<WaypointID, GraphNode<Waypoint2D>> e: loadedNodes.entrySet()) {
			ArrayList<Arc<Waypoint2D>> a = e.getValue().arcList; 
			for (int i = 0; i < a.size(); i++) {
				if (modifyCost(a.get(i), prefs, e.getValue().arcList))
					a.remove(i--);
			}
		}
		
		//computes the shortest path
		ArrayList<WaypointID> dijkstras = ShortestPath.doDijkstras(loadedNodes, startResult.getPointID(), endResult.getWaypoints());
		
		//unmodify the time
		unmodifyCost(prefs, dijkstras);
		
		//formats the shortest path into a list of graph nodes
		ArrayList<GraphNode<Waypoint2D>> dijkstrasNodes = new ArrayList<GraphNode<Waypoint2D>>();
        for (WaypointID id : dijkstras) { //for each point in the path
            GraphNode<Waypoint2D> tempNode = loadedNodes.get(id);
            dijkstrasNodes.add(tempNode);
        }
		
		//initializes arrays that will be sent to the route object
		Waypoint2D[] points = new Waypoint2D[dijkstras.size()];
		int[] terrains = new int[dijkstras.size()-1];
		int[] times = new int[dijkstras.size()-1];

		//fills those arrays with the lists of points, terrains, and times for calculation of the route
        GraphNode<Waypoint2D> currentNode = loadedNodes.get(startResult.getPointID());
		for (int i = 0; i < dijkstrasNodes.size()-1; i++) {
			currentNode = dijkstrasNodes.get(i);
			for (Arc<Waypoint2D> arc: currentNode.arcList) {
				if (arc.getConnectedNode().data.getId().equals(dijkstrasNodes.get(i+1).data.getId())) {
					
					terrains[i] = arc.getTerrain();
					times[i] = arc.getDistance();
					points[i] = currentNode.data;
					break;
				}
			}
		}
		points[points.length-1] = dijkstrasNodes.get(dijkstrasNodes.size()-1).data;

		//computes the route
		Route route = new Route(points, terrains, times, startResult.getText(), endResult.getText(), campus);

		loadedNodes = null; //FREE THE MASSIVE AMOUNT OF MEMORY
		return route;	
	}

	private void fixElevators() {
		int roomIndex = -10;//negative room indices indicate points that were added to allow us 
							//to modify times to be appropriate for elevators 
		ArrayList<GraphNode<Waypoint2D>> nodesToAdd = new ArrayList<GraphNode<Waypoint2D>>();
		for (GraphNode<Waypoint2D> currentNode : loadedNodes.values()) {
			if (isRelevant(currentNode)) {
				nodesToAdd.add(elevatorUpdate(currentNode, roomIndex));
				roomIndex--;
			}
		}
		for (GraphNode<Waypoint2D> node : nodesToAdd)
			loadedNodes.put(node.data.getId(), node);
	}

	private boolean isRelevant(GraphNode<Waypoint2D> currentNode) {
		for (Arc<Waypoint2D> arc: currentNode.arcList) {
			if (arc.getTerrain() == Arc.Terrains.ELEVATOR) //if you found an elevator then use this
				return true;
		}
		return false;
	}

	private GraphNode<Waypoint2D> elevatorUpdate(GraphNode<Waypoint2D> currentNode, int roomIndex) {
		WaypointID currentID = currentNode.data.getId();
		LatLngPoint point = currentNode.data.getPoint().copy();
		Waypoint2D point2D = new Waypoint2D(point, currentID.getBuildingIndex(), currentID.getFloorIndex(), roomIndex, "fake");
		GraphNode<Waypoint2D> newNode = new GraphNode<Waypoint2D>(point2D);

		int size = currentNode.arcList.size();
		for (int i = 0; i < size; i++) {
			Arc<Waypoint2D> arc = currentNode.arcList.get(i);
			//if its an elevator terrain leave it
			if (arc.getTerrain() == Arc.Terrains.ELEVATOR) {
				newNode.addArc(arc.getConnectedNode(), arc.getDistance(), arc.getTerrain());
				currentNode.removeArc(arc.getConnectedNode());
				size--;
				i--;
			}
		}
		
		//add the invisible edge
		currentNode.addArc(newNode, ELEVATOR_CONSTANT, Arc.Terrains.INVISIBLE);

		return newNode;
	}
	
	//undo the cost changes that modifyCost did to the routeNodes hashMap
	//WARNING: does not undo ALL changes made (only those changes that were included in the final path)
	private void unmodifyCost(RoutePreferences prefs, ArrayList<WaypointID> dijkstras) {
		for(WaypointID key: dijkstras) {
			for (Arc<Waypoint2D> a: loadedNodes.get(key).arcList) {
				int currDistance = a.getDistance();
				//change the arc if that preference is chosen
				if (prefs.outside) {
					if (a.getTerrain() == Arc.Terrains.INSIDE || 
							a.getTerrain() == Arc.Terrains.ELEVATOR || 
							a.getTerrain() == Arc.Terrains.INSIDE_STAIRS ||
							a.getTerrain() == Arc.Terrains.CROWDED_INSIDE ||
							a.getTerrain() == Arc.Terrains.RESTRICTED_ACCESS)
						a.setDistance(currDistance / MODIFIER_CONSTANT); 
				}
				if (prefs.inside) {
					if (a.getTerrain() == Arc.Terrains.OUTSIDE || 
							a.getTerrain() == Arc.Terrains.OUTSIDE_STAIRS ||
							a.getTerrain() == Arc.Terrains.CROWDED_OUTSIDE)
						a.setDistance(currDistance / MODIFIER_CONSTANT); 
				}
				if (prefs.elevators) {
					if (a.getTerrain() == Arc.Terrains.INSIDE_STAIRS || 
							a.getTerrain() == Arc.Terrains.OUTSIDE_STAIRS)
						a.setDistance(currDistance / MODIFIER_CONSTANT);
				}
				if (prefs.stairs) {
					if (a.getTerrain() == Arc.Terrains.ELEVATOR)
						a.setDistance(currDistance * MODIFIER_CONSTANT);
				}
			}
		}
	}

	private boolean modifyCost(Arc<Waypoint2D> a, RoutePreferences prefs, ArrayList<Arc<Waypoint2D>> arcList) {
		int currDistance = a.getDistance();
		//change the arc if that preference is chosen
		if (prefs.outside) {
			if (a.getTerrain() == Arc.Terrains.INSIDE || 
					a.getTerrain() == Arc.Terrains.ELEVATOR || 
					a.getTerrain() == Arc.Terrains.INSIDE_STAIRS ||
					a.getTerrain() == Arc.Terrains.CROWDED_INSIDE ||
					a.getTerrain() == Arc.Terrains.RESTRICTED_ACCESS)
				a.setDistance(currDistance * MODIFIER_CONSTANT); 
		}
		if (prefs.inside) {
			if (a.getTerrain() == Arc.Terrains.OUTSIDE || 
					a.getTerrain() == Arc.Terrains.OUTSIDE_STAIRS ||
					a.getTerrain() == Arc.Terrains.CROWDED_OUTSIDE)
				a.setDistance(currDistance * MODIFIER_CONSTANT); 
		}
		if (prefs.elevators) {
			if (a.getTerrain() == Arc.Terrains.INSIDE_STAIRS || 
					a.getTerrain() == Arc.Terrains.OUTSIDE_STAIRS)
				//actually remove the connections of the stairs
				a.setDistance(currDistance * MODIFIER_CONSTANT);
		}
		if (prefs.stairs) {
			if (a.getTerrain() == Arc.Terrains.ELEVATOR)
				a.setDistance(currDistance * MODIFIER_CONSTANT);
		}
		if (prefs.hand) {
			if (a.getTerrain() == Arc.Terrains.INSIDE_STAIRS || 
					a.getTerrain() == Arc.Terrains.OUTSIDE_STAIRS)
				//actually remove the connections of the stairs
				return REMOVE_ARC;
		}
		return NO_REMOVE;
	}

	private void loadGraphFiles(InterpretResult startResult, InterpretResult endResult) {
		HashSet<String> floorGraphFiles = new HashSet<String>();
		HashSet<String> buildingGraphFiles = new HashSet<String>();
		
		WaypointID start = startResult.getPointID();
		WaypointID end = getFarthestEnd(startResult, endResult);
		
		//distance threshold = 1.5 distance between start and end buildings
		Building startBuilding = campus.buildings[start.getBuildingIndex()];
		Building endBuilding = campus.buildings[end.getBuildingIndex()];
		double distanceThreshold = DistanceCalculator.buildingDistance(startBuilding, endBuilding) * THRESHOLD_MULTIPLIER;
		
		//loops through buildings
		for (int buildingIndex = 0; buildingIndex < campus.buildings.length; buildingIndex++) {
			//does not load if distance between the start to the current to the end building is more than the threshold defined above
			Building building = campus.buildings[buildingIndex];
			double distance = DistanceCalculator.buildingDistance(startBuilding, building, endBuilding);
			if (distance <= distanceThreshold || campus.buildingIsOutside(start.getBuildingIndex()) || campus.buildingIsOutside(end.getBuildingIndex())) {
				boolean onlyLoadConnectionFloors = !(building == startBuilding || building == endBuilding);
				queueBuilding(buildingIndex, floorGraphFiles, buildingGraphFiles, onlyLoadConnectionFloors);
			}
		}

		queueOutsideBuildings(floorGraphFiles, buildingGraphFiles);

//		printFiles(floorGraphFiles);
//		printFiles(buildingGraphFiles);
		
		//loads the graph files determined above
		for (String fileName : floorGraphFiles) {
			DataParser.parseFile(fileName, loadedNodes, Campus.fileHandler);
		}
		for (String fileName : buildingGraphFiles) {
			DataParser.parseFile(fileName, loadedNodes, Campus.fileHandler);
		}
		DataParser.parseFile("building_connections.ncmg", loadedNodes, Campus.fileHandler);
	}
	
//	private void printFiles(HashSet<String> graphFiles) {
//		System.out.println("GRAPH FILES:");
//		for (String fileString : graphFiles) {
//			System.out.println("loaded file: " + fileString);
//		}
//	}
	
	private void queueOutsideBuildings(HashSet<String> floorGraphFiles, HashSet<String> buildingGraphFiles) {
		Building[] outsideBuildings = campus.getOutsideBuildings();
        for (Building outsideBuilding : outsideBuildings) {
            for (int j = 0; j < outsideBuilding.floors.length; j++) {
                floorGraphFiles.add(campus.getFloorFile(outsideBuilding, j, "ncmg"));
            }
            buildingGraphFiles.add(campus.getBuildingFile(outsideBuilding, "ncmg"));
        }
	}
	
	//a connection floor is one that is necessary to allow the shortest path algorithm to search all possible routes
	//this is defined by a floor's load_if_close variable
	private void queueBuilding(int building, HashSet<String> floorGraphFiles, HashSet<String> buildingGraphFiles, boolean onlyLoadConnectionFloors) {
		for (int floor = 0; floor < campus.buildings[building].floors.length; floor++) {
			if (!onlyLoadConnectionFloors || (campus.getFloor(building, floor).load_if_close))
				floorGraphFiles.add(campus.getFloorFile(building, floor, "ncmg"));
		}
		buildingGraphFiles.add(campus.getBuildingFile(building, "ncmg"));
	}
	
	private WaypointID getFarthestEnd(InterpretResult startResult, InterpretResult endResult) {
		WaypointID farthestEnd = endResult.getWaypoints().iterator().next();
		double distanceToFarthest = 0;
		
		Building startBuilding = campus.buildings[startResult.getPointID().getBuildingIndex()];
		
		for (WaypointID pointID: endResult.getWaypoints()) {
			Building endBuilding = campus.buildings[pointID.getBuildingIndex()];
			double distance = DistanceCalculator.buildingDistance(startBuilding, endBuilding); 
			if (distance > distanceToFarthest) {
				distanceToFarthest = distance;
				farthestEnd = pointID;
			}
		}
		
		return farthestEnd;
	}
	
	public static void loadOutsideBuildings(Campus campus, HashMap<WaypointID, GraphNode<Waypoint2D>> nodes) {
		Building[] outsideBuildings = campus.getOutsideBuildings();
        for (Building outsideBuilding : outsideBuildings) {
            loadBuilding(outsideBuilding, campus, nodes);
        }
	}
	
	public static void loadBuilding(Building building, Campus campus, HashMap<WaypointID, GraphNode<Waypoint2D>> nodes) {
		for (int j = 0; j < building.floors.length; j++) {
			DataParser.parseFile(campus.getFloorFile(building, j, "ncmg"), nodes, Campus.fileHandler);
		}
		DataParser.parseFile(campus.getBuildingFile(building, "ncmg"), nodes, Campus.fileHandler);
	}

	public Waypoint2D getWaypoint2D(WaypointID pointID) {
		loadedNodes = new HashMap<WaypointID, GraphNode<Waypoint2D>>();
		
		Building building = campus.buildings[pointID.getBuildingIndex()];
		loadBuilding(building, campus, loadedNodes);
		
		GraphNode<Waypoint2D> node = loadedNodes.get(pointID);
		return node.data;
	}
	
	public static void printHashMap(HashMap<WaypointID, GraphNode<Waypoint2D>> nodes) {
		System.out.println("printing hashmap size: " + nodes.size());
		for (Entry<WaypointID, GraphNode<Waypoint2D>> e: nodes.entrySet()) {
			System.out.println("node: " + e.getValue().data);
			System.out.println("arcs: from: " + e.getKey().toString());
			for (Arc<Waypoint2D> arc : e.getValue().arcList) {
				System.out.println("to: " + arc.getConnectedNode().data.getId() + " time: " + arc.getDistance() + " terrain: " + arc.getTerrain());
			}
		}
	}
	
	public static void printHashMap(HashMap<WaypointID, GraphNode<Waypoint2D>> nodes, int building, int floor) {
		System.out.println("printing hashmap size: " + nodes.size());
		for (Entry<WaypointID, GraphNode<Waypoint2D>> e: nodes.entrySet()) {
			if (e.getKey().getBuildingIndex() == building && e.getKey().getFloorIndex() == floor) {
				System.out.println("node: " + e.getValue().data);
				System.out.println("arcs: from: " + e.getKey().toString());
				for (Arc<Waypoint2D> arc : e.getValue().arcList) {
					System.out.println("to: " + arc.getConnectedNode().data.getId() + " time: " + arc.getDistance() + " terrain: " + arc.getTerrain());
				}
			}
		}
	}
}
