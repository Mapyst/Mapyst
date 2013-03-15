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


/*
 Class: ShortestPath
 The implementation of Dijkstra's algorithm to find the 
 shortest path between two points on the graph.

 Author:
 Brandon Kase
 */
public class ShortestPath {	
	
	/*
	 * Function: doDijkstras Does Dijkstra's algorithm to find the shortest path
	 * between the source node and the destination node, using a hash map of all
	 * the nodes to look through
	 * 
	 * Parameters: allNodes - The hash map of all the graph nodes. A graph node
	 * can be accessed by hashing its ID. sourceNodeWaypointID - The ID for the
	 * source node destinationNodeWaypointID - The ID for the destination node
	 * 
	 * Returns: A hash map of IDs as keys and IDs as values. The value given a
	 * certain key is the previous position in the shortest path. Call
	 * previousToWaypointID on the returned hash map to get a more useful path.
	 * 
	 * See Also: previousToPath(HashMap, WaypointID)
	 */
	public static ArrayList<WaypointID> doDijkstras(
			HashMap<WaypointID, GraphNode<Waypoint2D>> allNodes,
			WaypointID sourceNodeWaypointID, HashSet<WaypointID> destinationNodeWaypointIDs) {
		// Always get the min, then remove it
		PriorityQ<Waypoint2D> distancesQ = new PriorityQ<Waypoint2D>();
		// an array with the same indices as our BIG array except integers
		// referring to other indices in the array (-1 = null)
		HashMap<WaypointID, WaypointID> previousOnes = new HashMap<WaypointID, WaypointID>(allNodes.size());
		boolean foundSource = false;

		// populate previousOne array with -1s and set all Waypoint2D's distances
		// to MAX_INTEGER EXCEPT for the first one
		for (WaypointID key : allNodes.keySet()) {
			// previousOnes.put(key, new WaypointID(-1, -1, -1));
			if (!key.equals(sourceNodeWaypointID)) {
				allNodes.get(key).data.setPriority(Integer.MAX_VALUE);
			} else {
				allNodes.get(key).data.setPriority(0);
				foundSource = true;
			}
			distancesQ.enqueue(allNodes.get(key).data); // keep same reference so that it can be mutated
		}
		if (!foundSource)
			throw new RuntimeException("Source Node does not exist");
		// we need this outside to for-loop in the returning
		Waypoint2D currPoint;
		while (!distancesQ.isEmpty()) {
			currPoint = distancesQ.dequeue();
			// if the lowest is infinity, then those can't be reached
			if (currPoint.getDistance() == Integer.MAX_VALUE ||
                    destinationNodeWaypointIDs.contains(currPoint.getId())) {
				break;
			}
			// get currGNode as a graph node
			GraphNode<Waypoint2D> currGNode = allNodes.get(currPoint.getId());

			// loop through the arraylist of arcs
			ArrayList<Arc<Waypoint2D>> arcs = currGNode.arcList;
			for (Arc<Waypoint2D> curr : arcs) {
				// get the sum of the current plus this arc
				int sum = currPoint.getDistance() + (curr.getDistance());
				// get the next index of the node connected to the current arc
				WaypointID nextId = ((Waypoint2D) curr.getConnectedNode().data).getId();
				
				// if this is smaller replace it
				if (sum < allNodes.get(nextId).data.getDistance()) {
					allNodes.get(nextId).data.setPriority(sum);
					
					if (distancesQ.reprioritize(allNodes.get(nextId).data, sum)) // must do this to fix Q
						// "About to overwrite previousOnes...");
						previousOnes.put(nextId, currGNode.data.getId());
						// distancesQ);
				}
			}
		}
		return previousToPath(previousOnes, destinationNodeWaypointIDs);
	}
	
	/*
	 * Function: previousToPath Converts the hash map of previous IDs to an
	 * arraylist of the path in correct order
	 * 
	 * Parameters: pathIndices -The hash map of previous IDs end - The ending ID
	 * 
	 * Returns: An arraylist of the path in correct order
	 */
	public static ArrayList<WaypointID> previousToPath(
            HashMap<WaypointID, WaypointID> pathIndices,
            HashSet<WaypointID> ends
    ) {
		WaypointID curr = null;
		ArrayList<WaypointID> toReturn = new ArrayList<WaypointID>();
		for (Entry<WaypointID, WaypointID> e: pathIndices.entrySet()) {
			if (ends.contains(e.getKey())) {
				curr = e.getKey();
				toReturn.add(curr); //add the end
				break;
			}
		}
		while (curr != null) {
			toReturn.add(pathIndices.get(curr));
			curr = previous(pathIndices, curr);
		}
		if (toReturn.size() <= 1)
			throw new RuntimeException("Path not found");

		if (toReturn.get(toReturn.size() - 1) != null)
			throw new RuntimeException("Arraylist doesn't end in null");
		toReturn.remove(toReturn.size() - 1); //remove the final null
		//throw an exception if no path could be found
		
		return reverse(toReturn);
	}
	
	private static ArrayList<WaypointID> reverse(ArrayList<WaypointID> list) {
		ArrayList<WaypointID> toReturn = new ArrayList<WaypointID>();
        for (WaypointID id : list) {
            toReturn.add(0, id);
        }
		return toReturn;
	}

	private static WaypointID previous(HashMap<WaypointID, WaypointID> pathIndices, WaypointID curr) {
		return pathIndices.get(curr);
	}
}
