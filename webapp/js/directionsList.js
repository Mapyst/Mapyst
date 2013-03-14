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


//creates the ui to display the list of directions in the calculated route
function makeDirectionsList() {
	var html = "";

	//creates route overview showing the start, end, and total time
	html += "<li><b>From:  " + route.startText + "</br>To:       " + route.endText + "</b></br><i><p>Total: " + formatTime(calcRouteTime() / 1000) + "</p></i></li>";

	//adds the html foreach direction to the list
	for (var i = 0; i < route.directions.length; i++) {
		html += makeDirectionElement(route.directions[i], i);
	}

	$("#sidebar-directions").html(html);

	//applys the appropriate classes to the directions list elements
	//(the header (overview), the active direction, the default directions)
	var items = $("#sidebar-directions li");
	for (var j = 0; j < items.length; j++) {
		if (j == 0)
			items[j].className = "dirListHeader";
		else if (j == 1)
			items[j].className = "activeDirListItem";
		else
			items[j].className = "dirListItem";

		if (j > 0)
			items[j].onclick = clickedDirection;//adds event handlers for each direction
	}
}

//creates html representing a direction
//the direction's text and the direction's time
function makeDirectionElement(direction, index) {
	var html = "<li><img src=\"images/map_icons/" + getDirectionImage(index) + "\" /><b>" + direction.text + "</b></br><i><p>" + formatTime(direction.time / 1000) + "</p></i></li>";
	return html;
}

//formats seconds to a minutes and seconds string
function formatTime(seconds) {
	var time = "";

	if (Math.floor(seconds/60) != 0) {
		time = "" + Math.round(seconds/60)+ " min ";
	}
	else {
		var s = Math.round((seconds % 60)/10)*10;
		if (s == 0)
			time = "5 sec";
		else
			time = "" + s + " sec";	
	}

	return time;
}

//sums the total time of a route (returns milliseconds)
function calcRouteTime() {
	if (route.directions == null)
		return 0;
	var time = 0;
	for (var i = 0; i < route.directions.length; i++) {
		time += route.directions[i].time;
	}
	return time;
}

//fired when a direction is clicked
//changes the current direction to the clicked direction
function clickedDirection(event) {
	var newIndex;
	//determines the index of the clicked element in the list of directions
	var items = $("#sidebar-directions li");
	for (var j = 0; j < items.length; j++) {
		if (elementHasElement(items[j], event.target) || items[j] == event.target) {
			newIndex = j - 1;
		}
	}

	//sets the current direction if the newIndex was found
	if (newIndex != undefined && newIndex > -1)
		setDirection(newIndex);
}

//determines whether an element has a child that is the lookingFor element
//or recursively whether its children contains the lookingFor element
function elementHasElement(parent, lookingFor) {
	for (var i = 0; i < parent.children.length; i++) {
		if (parent.children[i] == lookingFor) {
			return true;
		}
		else {
			if (elementHasElement(parent.children[i], lookingFor)) {
				return true;
			}
		}
	}
	return false;
}