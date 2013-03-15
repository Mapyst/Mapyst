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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.HashMap;

import com.mapyst.FileHandlerInterface;

//parses mapyst data files containing a graph
//each file represents either a floor, building, or campus
public class DataParser {

	private static final int FLOOR = 0;
	private static final int BUILDING = 1;
	private static final int CAMPUS = 2;

	private static final int FILE_VERSION = 1;
	private static final int FILE_VERIFICATION = 0x2417DC43;

	public static void parseFile(String file, HashMap<WaypointID, GraphNode<Waypoint2D>> allNodes, FileHandlerInterface fileHandler) {

		int fileType;
		int buildingIndex;
		int floorIndex;

		try {
			BufferedInputStream in = new BufferedInputStream(fileHandler.getInputStream("graph/" + file));
	
			//check to make sure the file starts with 0x2417DC43
			int verification = reads32(in);
			if (verification != FILE_VERIFICATION)
				throw new IOException("File verification number is not the expected value: 0x" + Integer.toHexString(verification) + " it should be " + Integer.toHexString(FILE_VERIFICATION));
	
			//check to make sure the file version number is the current version
			int version = in.read();
			if (version != FILE_VERSION)
				throw new IOException("File version number is not correct: " + version + " it should be " + FILE_VERSION);
	
			fileType = in.read();
			buildingIndex = in.read();
			floorIndex = in.read();
	
			if (fileType == FLOOR)
				loadFloorVertices(in, allNodes, buildingIndex, floorIndex);
	
			loadEdges(in, fileType, allNodes, buildingIndex, floorIndex);
	
			in.close();
		} catch (IOException e) {
			
		}
	}

	private static void loadEdges(BufferedInputStream in, int fileType,
			HashMap<WaypointID, GraphNode<Waypoint2D>> allNodes, int buildingIndex, int floorIndex) throws IOException {

		int numEdges = readu16(in);
		for (int i = 0; i < numEdges; i++) {
			int b1Index = -1;
			int b2Index = -1;
			int f1Index = -1;
			int f2Index = -1;

			if (fileType == CAMPUS) {
				b1Index = in.read();
				b2Index = in.read();
			}
			else {
				b1Index = buildingIndex;
				b2Index = buildingIndex;
			}
			
			if (fileType == BUILDING || fileType == CAMPUS) {
				f1Index = in.read();
				f2Index = in.read();
			}
			else {
				f1Index = floorIndex;
				f2Index = floorIndex;
			}

			int v1Index = readu16(in);
			int v2Index = readu16(in);
			
			int time1to2 = reads32(in);
			int time2to1 = reads32(in);
			
			int terrain = in.read();
			
			WaypointID id1 = new WaypointID(b1Index, f1Index, v1Index);
			WaypointID id2 = new WaypointID(b2Index, f2Index, v2Index);
			GraphNode<Waypoint2D> node1 = allNodes.get(id1);
			GraphNode<Waypoint2D> node2 = allNodes.get(id2);
			if (node1 != null && node2 != null) {
				node1.addArc(node2, time1to2, terrain);
				node2.addArc(node1, time2to1, terrain);
			}
		}


	}

	private static void loadFloorVertices(BufferedInputStream in,
			HashMap<WaypointID, GraphNode<Waypoint2D>> allNodes, int buildingIndex, int floorIndex) throws IOException {

		int numVertices = readu16(in);
		int latitude = -1;
		int longitude = -1;
		String str = "";

		//run loop for each vertex
		for (int i = 0; i < numVertices; i++) {
			latitude = reads32(in);
			longitude = reads32(in);
			str = readString(in);

			Waypoint2D point = new Waypoint2D(new LatLngPoint(latitude, longitude), buildingIndex, floorIndex,
					i, str); // get the Waypoint2D object
			allNodes.put(point.getId(), new GraphNode<Waypoint2D>(point));
		}

	}

	private static int reads32(BufferedInputStream in) throws IOException {
		int number = 0;
		for (int i = 0; i < 4; i++) {
			number = number << 8;
			number += in.read();
		}

		return number;
	}

	private static int readu16(BufferedInputStream in) throws IOException {
		int number = 0;
		for (int i = 0; i < 2; i++) {
			number = number << 8;
			number += in.read();
		}

		return number;
	}

	private static String readString(BufferedInputStream in) throws IOException {
		StringBuilder buf = new StringBuilder();
		char c = (char)in.read();

		while (c != '\0') {
			buf.append(c);
			c = (char)in.read();
		}

		return buf.toString();
	}
}
