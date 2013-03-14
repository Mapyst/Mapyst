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

var curLocTypeIndex = -1, curLocType; //current location type (location type=category)
var curLoc; //current location

function initLocationList() {
	var locationsList = $("#sidebar-locations-list")[0];
	locationsList.onclick = itemClicked;
	
	updateLocationsList();
}

//changes the locations tab's title and the items in the locations list
function updateLocationsList() {
	var locationsTitle = $("#sidebar-locations-title");
	var locationsList = $("#sidebar-locations-list")[0];
	var locs = [];
	
	if (curLocTypeIndex == -1) { //display the list categorties
		for (var i = 0; i < campus.location_types.length; i++) {
			locs.push("+ " + campus.location_types[i].name);
		}
		locationsTitle.html("Categories");

		$("#sidebar-locations-back").css("visibility", "hidden");
	}
	else { //display the list of locations in the selected category
		for (var i = 0; i < campus.location_types[curLocTypeIndex].locations.length; i++) {
			var location = campus.location_types[curLocTypeIndex].locations[i];
			locs.push(location.names[0]);
		}
		locationsTitle.html(curLocType.name);

		$("#sidebar-locations-back").css("visibility", "visible");
	}
	
	//clears the locations list
	// locationsList.options = [];
	for (var j = locationsList.length - 1; j >= 0; j--) {
		locationsList.remove(j);
	}
	//adds all the locations added to the array previously
	for (var k = 0; k < locs.length; k++) {
		var option = new Option(locs[k], locs[k]);
		locationsList.add(option);
	}
}

//goes back to the list of the categories when the user clicks the back arrow
function locationsBack() {
	if (curLocTypeIndex != -1) {
		curLocTypeIndex = -1;
		curLocType = null;
		deselectLocation();
		updateLocationsList();
	}
}

//fired when an item in the locations list is clicked
function itemClicked(e) {
	var selectedIndex = $("#sidebar-locations-list")[0].selectedIndex;
	if (curLocTypeIndex == -1) { //if user selected a category
		curLocType = campus.location_types[selectedIndex];
		curLocTypeIndex = selectedIndex; //sets the current location type (location type=category)
		updateLocationsList();
	}
	else { //if user selected a location
		selectLocation(campus.location_types[curLocTypeIndex].locations[selectedIndex]);
	}
}

//shows the location info popup window when the user selects a location from the list
function selectLocation(loc) {
	var infoText = $("#info-text");
	var info = $("#info");
	if (loc.hours || loc.description) { //only shows the popup if the location has some kind of info

		//adds new html for the name of the location, it's hours, and it's description
		var html = "<b>" + loc.names[0] + "</b>";
		if (loc.hours)
			html += "</br></br><b>Hours:</b>" + formatHours(loc.hours);
		if (loc.description)
			html += "</br></br>" + loc.description;
		infoText.html(html);

		//sizes, positions, and shows window
		infoText.css("height", "260px");
		info.css("height", "300px");
		info.css("top", "90px");
		info.css("left", "240px");
		info.css("visibility", "visible");
	}
	else {
		closeInfo();
	}

	curLoc = loc;
}

//hides and empties location info window
function deselectLocation() {
	var infoText = $("#info-text");
	var info = $("#info");
	info.css("visibility", "hidden");
	infoText.html("");
	curLoc = null;
}

//formats the hours string from the campus data file to a readable format
function formatHours(hours) {
	hours = hours.replace("H:", "");
	hours = hours.replace("MTWThFSaS:", "</br>Mon-Sun: ");
	hours = hours.replace("MTWThFSa:", "</br>Mon-Sat: ");
	hours = hours.replace("MTWThF:", "</br>Mon-Fri: ");
	hours = hours.replace("MTWTh:", "</br>Mon-Thurs: ");
	hours = hours.replace("MTW:", "</br>Mon-Wed: ");
	hours = hours.replace("WThF:", "</br>Wed-Fri: ");
	hours = hours.replace("MWF:", "</br>Mon,Web,Fri: ");
	hours = hours.replace("TTh:", "</br>Tues,Thurs: ");
	hours = hours.replace("SaS:", "</br>Sat,Sun: ");
	hours = hours.replace("MT:", "</br>Mon,Tues: ");
	hours = hours.replace("WF:", "</br>Wed,Fri: ");
	hours = hours.replace("F:", "</br>Fri: ");
	hours = hours.replace("Sa:", "</br>Sat: ");
	hours = hours.replace("S:", "</br>Sun: ");
	hours = hours.replace("Th:", "</br>Thurs: ");
	return hours;
}

//sets the text of the start input text box to the name of the currently selected location
function setAsStart() {
	var start = $("#header-inputs-start");
	if (curLoc) {
		start.val(curLoc.names[0]);
		closeInfo();
		getDirections();
	}
}

//sets the text of the end input text box to the name of the currently selected location
function setAsEnd() {
	var end = $("#header-inputs-end");
	if (curLoc) {
		end.val(curLoc.names[0]);
		closeInfo();
		getDirections();
	}
}