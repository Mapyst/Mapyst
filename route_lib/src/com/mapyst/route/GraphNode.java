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

/*
 Class: GraphNode
 A node in a graph of nodes.  Includes public fields for data (the data stored in the graph node) and arclist (the list of arcs that originate from this graph node)

 Author:
 Brandon Kase

 Parameters:
 <E> - The type of data stored in the graph node
 */
public class GraphNode<E> {
	public E data;
	public ArrayList<Arc<E>> arcList;

	/*
	 * Constructor: GraphNode
	 * 
	 * Parameters: data - The data to store in the graph node
	 */
	public GraphNode(E data) {
		this.data = data;
		arcList = new ArrayList<Arc<E>>();
	}

	/*
	 * Function: addArc Adds an arc to the list of arcs
	 * 
	 * Parameters: node - The node the arc will point towards distance - The
	 * distance of the arc from this node to the node it points towards terrain
	 * - The terrain in the arc
	 */
	public void addArc(GraphNode<E> node, int distance, int terrain) {
		arcList.add(new Arc<E>(node, distance, terrain));
	}
	
	//This is an O(n) remove, to make it O(1) use a HashMap of Arcs
	//Returns true if successfully removed
	public boolean removeArc(Arc<E> arc) {
		return arcList.remove(arc);
	}
	
	public boolean removeArc(GraphNode<E> node) {
		for (int i = 0; i < arcList.size(); i++) {
			if (arcList.get(i).getConnectedNode().equals(node)) {
				arcList.remove(i);
				return true;
			}
		}
		return false;
	}
	
	//Removes all arcs connected to a node
	public void removeAllArcs() {
		arcList = new ArrayList<Arc<E>>();
	}

	/*
	 * Function: hashCode
	 * 
	 * Returns: the hash code of the data
	 */
	public int hashCode() {
		return data.hashCode();
	}
	
	/*
	 * Function: equals
	 * 
	 * Parameters: obj - The object that is being tested against this
	 * 
	 * Returns: true if obj equals this, false otherwise
	 */
	@SuppressWarnings("rawtypes")
	public boolean equals(Object obj) {
		if (!(obj instanceof GraphNode))
			return false;
		return (((GraphNode)obj).data.equals(this.data));
	}

	/*
	 * Function: toString
	 * 
	 * Returns: a string representation of the graph node and all of its arcs
	 */
	public String toString() {
		String s = "";
		for (Arc<E> arc : arcList) {
			s += this.data.toString();
			s += "-(" + arc.getDistance() + ")->" + arc.getConnectedNode().data;
			s += "\n";
		}
		return s;
		// return this.data.toString();
	}

}
