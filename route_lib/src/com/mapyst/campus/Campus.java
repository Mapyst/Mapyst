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

package com.mapyst.campus;

import java.util.ArrayList;
import com.google.gson.Gson;
import com.mapyst.FileHandlerInterface;
import com.mapyst.route.LatLngPoint;


public class Campus {
	public String name;
	public String acronym;
	public String description;
	public String directory_url;
	public String phone;
	public LatLngPoint location; //center of campus

	public Building[] buildings;
	public Location_Type[] location_types;
	
	public static FileHandlerInterface fileHandler;

	public Floor getFloor(int buildingIndex, int floorIndex) {
		return buildings[buildingIndex].floors[floorIndex];
	}

	public String getFloorFile (int buildingIndex, int floorIndex, String fileExtension) {
		String fileName = buildingIndex + "-" + floorIndex + "." + fileExtension; 
		return fileName;
	}

	public String getFloorFile (Building building, int floorIndex, String fileExtension) {
		int buildingIndex = findBuilding(building);
		String fileName = buildingIndex + "-" + floorIndex + "." + fileExtension; 
		return fileName;
	}

	public String getBuildingFile (int buildingIndex, String fileExtension) {
		String fileName = buildingIndex + "." + fileExtension;
		return fileName;
	}

	public String getBuildingFile (Building building, String fileExtension) {
		int buildingIndex = findBuilding(building);
		String fileName = buildingIndex + "." + fileExtension;
		return fileName;
	}

	private int findBuilding(Building building) {
		for (int i = 0; i < buildings.length; i++) {
			if (buildings[i] == building)
				return i;
		}
		return -1;
	}

	public int parseFloorFromText(int buildingIndex, String floorText) {
		if (buildingIndex >= 0 && buildingIndex < buildings.length) {
			for (int i = 0; i < buildings[buildingIndex].floors.length; i++) {
				if (floorText.equals(getFloor(buildingIndex, i).name.toLowerCase()))
					return i;
			}
		}
		return -1;
	}

	public Building[] getOutsideBuildings() {
		ArrayList<Building> outsideBuildings = new ArrayList<Building>();
		for (int i = 0; i < buildings.length; i++) {
			if (buildingIsOutside(i)) {
				outsideBuildings.add(buildings[i]);
			}
		}
		return outsideBuildings.toArray(new Building[0]);
	}

	public boolean buildingIsOutside(int building) {
		if (buildings[building].type == Building.OUTSIDE) {
			return true;
		}
		return false;
	}

	public int findLocationType(String type) {
		for (int i = 0; i < location_types.length; i++) {
			if (location_types[i].name.toLowerCase().equals(type.toLowerCase()))
				return i;
		}
		return -1;
	}

	public Location[] getDirectionEndLocations() {
		ArrayList<Location> locations = new ArrayList<Location>();
		for (int i = 0; i < location_types.length; i++) {
			for (int j = 0; j < location_types[i].locations.length; j++) {
				Location location = location_types[i].locations[j]; 
				if (location.is_direction_end) {
					locations.add(location);
				}
			}
		}

		return locations.toArray(new Location[0]);
	}

	public Location getCampusLocation(String text) {
		for (int i = 0; i < location_types.length; i++) {
			for (int j = 0; j < location_types[i].locations.length; j++) {
				for (int k = 0; k < location_types[i].locations[j].names.length; k++) {
					if (text.equals(location_types[i].locations[j].names[k]))
						return location_types[i].locations[j];
				}
			}
		}
		return null;
	}




	public static Campus load(int campusId) {
		fileHandler.setCampusId(campusId);
		
		Gson gson = new Gson();
		String json = "";

		json = fileHandler.readCampusFile();
		Campus campus = gson.fromJson(json, Campus.class);

		return campus;
	}
}
