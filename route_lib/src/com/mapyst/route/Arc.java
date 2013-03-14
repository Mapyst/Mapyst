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
 Class: Arc
 A one-way arc or edge of the graph, used to connect graph nodes.  Also includes constants for terrains.

 Author:
 Brandon Kase

 Parameters:
 <E> - The type of graph node that this arc connects
 */
public class Arc<E> {

	private GraphNode<E> node;
	private int distance;
	private int terrain;
	
	public boolean export = true;
	
	public final class Terrains {
		public static final int LENGTH = 8;

		public static final int OUTSIDE = 0;
		public static final int ELEVATOR = 1;
		public static final int OUTSIDE_STAIRS = 2;
		public static final int INSIDE_STAIRS = 3;
		public static final int INSIDE = 4;
		public static final int CROWDED_INSIDE = 5;
		public static final int CROWDED_OUTSIDE = 6;
		public static final int STREET = 7;
		public static final int RESTRICTED_ACCESS = 8;
		public static final int RAMP = 9;
		public static final int INVISIBLE = 10;
	}

	/*
	 * Constructor: Arc
	 * 
	 * Parameters: node - The node this arc points towards distance - The
	 * distance from the node the arc originates from towards the next node
	 * terrain - The terrain between the node the arc originates from to the
	 * next node
	 */
	public Arc(GraphNode<E> node, int distance, int terrain) {
		this.node = node;
		this.distance = distance;
		this.terrain = terrain;
	}

	/*
	 * Function: setDistance Sets the distance of the arc
	 * 
	 * Parameters: newDistance - The new distance of the arc to replace the
	 * previous one
	 * 
	 * Returns: The previous distance of the arc
	 */
	public int setDistance(int newDistance) {
		int temp = this.distance;
		this.distance = newDistance;
		return temp;
	}

	/*
	 * Function: getDistance
	 * 
	 * Returns: The distance of the arc
	 */
	public int getDistance() {
		return this.distance;
	}
	
	/*
	 * Function: getTerrain
	 * 
	 * Returns: The terrain of the arc
	 */
	public int getTerrain() {
		return this.terrain;
	}

	/*
	 * Function: getConnectedNode
	 * 
	 * Returns: The node this arc points towards
	 */
	public GraphNode<E> getConnectedNode() {
		return this.node;
	}

	/*
	 * Function: toString
	 * 
	 * Returns: A string representation of the arc.
	 */
	public String toString() {
		return "-(" + this.distance + ")->" + this.node.data;
	}

}
