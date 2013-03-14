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

var gmap, campus, route;

var autoOpen = false;
var printMode = false;

function initialize() {
	//checks for parameters in the url and sets them in the ui
	var urlVars = getUrlVars();
	if (urlVars['start'])
		$("#header-inputs-start").val(urlVars['start']);
	if (urlVars['end'])
		$("#header-inputs-end").val(urlVars['end']);
	if (urlVars['search'])
		$("#header-inputs-search").val(urlVars['search']);
	
	//ui initialization
	initPrefs(urlVars);
	sizeHTML();
	$("#sidebar-directions").hide()
	hideDirControls();
	$("#header-inputs-start").Watermark("Start");
	$("#header-inputs-end").Watermark("Destination");
	$("#header-inputs-search").Watermark("Search");
	initMap();

	//downloads the json file for the campus and then sets up the ui with the campus info
	loadCampus();

	//fires get directions when the user presses enter
	//problem: also happens when autocompleting
	document.onkeyup = (function(event){
		if (event.keyCode == 13){//enter key
			if ($("#header-inputs-search")[0] == document.activeElement) {
				doSearch();
			}
			else {
				getDirections();
			}
		}
	});

	window.onresize = resizePage;

	if (screen.width < 950 || screen.height < 600) {
		alert("Change your browser's zoom for a better experience. (Press the Control and Minus Key)");
	}
}

function initMap() {
	//some random latlng (changes when campus loads)
	var latlng = new google.maps.LatLng(-34.397, 150.644);
	var myOptions = {
		zoom: 8,
		center: latlng,
		mapTypeId: google.maps.MapTypeId.HYBRID,
		minZoom: 16,
		tilt: 0
	};
	gmap = new google.maps.Map($("#map-canvas")[0], myOptions);
}

//sizes the map, map controls, sidebar based on the size of the document body
function sizeHTML() {
	var bodyWidth = $(document.body).width();
	var bodyHeight = $(document.body).height();

	if (bodyWidth < 950) {
		$(document.body).css("overflow-x", "scroll");
		bodyWidth = 950;
	}
	else {
		$(document.body).css("overflow-x", "hidden");	
	}
	if (bodyHeight < 600) {
		$(document.body).css("overflow-y", "scroll");
		bodyHeight = 600;
	}
	else {
		$(document.body).css("overflow-y", "hidden");	
	}

	$("#map-canvas").css("width", (bodyWidth - 270) + "px");
	$("#map-canvas").css("height", (bodyHeight - 120) + "px");

	$("#map-controls").css("top", (bodyHeight - 120 - 78 - 25) + "px");
	var left = (bodyWidth - 270)/2 - (500/2);
	if (left < 40) left = 40;
	$("#map-controls").css("left", left + "px");

	$("#sidebar").css("height", (bodyHeight - 150) + "px");
	$("#sidebar-directions").css("height", (bodyHeight - 150) + "px");
}

function resizePage(event) {
	sizeHTML();
	centerCurDirText();
}

//creates a hashset of all the parameters in the url
function getUrlVars() {
	var vars = [], hash;
	var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
	for(var i = 0; i < hashes.length; i++)
	{
		hash = hashes[i].split('=');
		vars.push(hash[0]);
		vars[hash[0]] = hash[1];
	}
	return vars;
}

//downloads the campus json file, when downloaded it sets up the ui with this new info
function loadCampus() {
	mapyst_campus(
		function(data) {
			campus = data;
			
			gmap.panTo(new google.maps.LatLng(campus.location.lat / 1000000, campus.location.lng / 1000000));
			gmap.setZoom(16);

			initLocationList(campus);
			setupAutocomplete();
		}
	); 
}

//creates array of all the strings for the autocomplete
//and attaches this to the start and end text boxes (uses the jQuery UI autocomplete)
function setupAutocomplete() {
	var autocompletions = [];

	//adds all locations to the autocompletions
	for (var i = 0; i < campus.location_types.length; i++) {
		for (var j = 0; j < campus.location_types[i].locations.length; j++) {
			for (var k = 0; k < campus.location_types[i].locations[j].names.length; k++) {
				autocompletions.push(campus.location_types[i].locations[j].names[k]);
			}
		}
	}
	//adds non-outside buildings to autocompletions
	// for (var i = 0; i < campus.buildings.length; i++) {
	// 	if (!mapyst_outsideBuilding(campus, i)) {
	// 		autocompletions.push(campus.buildings[i].names[0] + " ("
	// 			+ campus.buildings[i].acronym + ") Room# ");
	// 	}
	// }
	autocompletions.push("Current Location");

	$("#header-inputs-start").autocomplete({
		source: autocompletions,
		open: function(event, ui) { autoOpen = true; }
	});
	$("#header-inputs-end").autocomplete({
		source: autocompletions,
		open: function(event, ui) { autoOpen = true; }
	});
	$("#header-inputs-search").autocomplete({
		source: autocompletions,
		open: function(event, ui) { autoOpen = true; }
	});
}

var startText, endText, searchText;
var curLocHelper, searchOrDirections;
var START = 0, END = 1, SEARCH = 2, DIRECTIONS = 1;

//finds the user's current location (if applicable) and then calls finishGetDirections
function getDirections() {
	searchOrDirections = DIRECTIONS;
	startText = $("#header-inputs-start").val();
	endText = $("#header-inputs-end").val();

	if (startText.toLowerCase().replace(" ", "") == "currentlocation") {
		curLocHelper = START;
		navigator.geolocation.getCurrentPosition(getCurLocation);
	}
	else if (endText.toLowerCase().replace(" ", "") == "currentlocation") {
		curLocHelper = END;
		navigator.geolocation.getCurrentPosition(getCurLocation);
	}
	else {
		finishGetDirections();
	}
}

//finds the user's current location (if applicable) and then calls finishGetDirections
function doSearch() {
	searchOrDirections = SEARCH;
	searchText = $("#header-inputs-search").val();

	if (searchText.toLowerCase().replace(" ", "") == "currentlocation") {
		curLocHelper = SEARCH;
		navigator.geolocation.getCurrentPosition(getCurLocation);
	}
	else {
		finishGetDirections();
	}
}

//callback to get user's location
//startOrEnd - which text contains current location
function getCurLocation(position){
	if (curLocHelper == START) {
		startText = "lat:" + position.coords.latitude + " lng:" + position.coords.longitude;
	}
	else if (curLocHelper == END) {
		endText = "lat:" + position.coords.latitude + " lng:" + position.coords.longitude;	
	}
	else {
		searchText = "lat:" + position.coords.latitude + " lng:" + position.coords.longitude;	
	}
	finishGetDirections(); 
} 

//necessary becuase if the user types current location, then it requires a callback to get the user's location
function finishGetDirections() {
	var url, params, prefs;
	var startEmpty = (startText == "" || startText == "Start");
	var endEmpty = (endText == "" || endText == "Destination");
	var searchEmpty = (searchText == "" || searchText == "Search");

	if (searchOrDirections == DIRECTIONS) {
		if (startEmpty && endEmpty) {
			alert("Enter a Start and Destination");
			return;
		}
		else if (startEmpty) {
			alert("Enter a Start");
			return;
		}
		else if (endEmpty) {
			alert("Enter a Destination");
			return;
		}
		else {
			searchText = "";
			prefs = getPrefs();
		}
	}
	else {
		if (searchEmpty) {
			alert("Enter something to Search");
			return;
		}
		else {
			startText = "";
			endText = "";
			prefs = "";
			singlePointStart = false;
		}
	}
	
	mapyst_route(
		startText, endText, searchText, prefs,
		function(data) {
			try {
				if (data.indexOf("unsuccessful") != -1) {
					showUnsuccessful();
					return;
				}
			}
			catch (e) {}
			
			//clears the map of all graphics
			clearMap();

			//checks if the input has multiple interpretations
			//if so show the suggestions determined on the server
			//otherwise display the calculated route
			if (data.suggestions) {
				showSuggestions(data);
			}
			else {
				route = data;
				loadRoute(route, campus, gmap);
			}
		}
	);
}

function showLocationsTab() {
	$("#sidebar-directions").hide();
	$("#sidebar-locations").show();

	$("#sidebar-tabs-directions").css("background-position", "0px 0px");
	$("#sidebar-tabs-locations").css("background-position", "0px -30px");
}

function showDirectionsTab() {
	$("#sidebar-directions").show();
	$("#sidebar-locations").hide();

	$("#sidebar-tabs-directions").css("background-position", "0px -30px");
	$("#sidebar-tabs-locations").css("background-position", "0px 0px");
}

//displays a popup window that gives a link to the user that represents the current state of the webapp
// function showLink() {
// 	var start = $("#header-inputs-start").val();
// 	var end = $("#header-inputs-end").val();
// 	var link = dataPath + "index.html?" + "campus_id=" + campus_id + "&prefs=" + getPrefs();
// 	if (start && start != "Start" && start != "")
// 		link += "&start=" + start;
// 	if (end && end != "Destination" && end != "")
// 		link += "&end=" + end;

// 	var infoText = $("#info-text");
// 	var info = $("#info");

// 	//creates a textbox containing the previously determined link
// 	infoText.html("<b>This link allows you to share the current route and preferences:</b></br><input type=\"text\" value=\"" + link + "\" style=\"width: 240px\" />");
	
// 	//sizes, positions, and shows popup window
// 	infoText.css("height", "80px");
// 	info.css("height", "40px");
// 	info.css("top", "-25px");
// 	info.css("left", "850px");
// 	info.css("visibility", "visible");
// }

function showUnsuccessful() {
	var infoText = $("#info-text");
	var info = $("#info");

	//creates a textbox containing the previously determined link
	infoText.html("<p>Sorry, we were unable to interpret your input.</p>");
	
	//sizes, positions, and shows popup window
	infoText.css("height", "80px");
	info.css("height", "40px");
	info.css("top", "50px");
	info.css("left", "300px");
	info.css("visibility", "visible");
}

//closes the info popup window
function closeInfo() {
	var infoText = $("#info-text");
	var info = $("#info");

	if (infoText.html() != "") {
		info.css("visibility", "hidden");
		infoText.html("");
	}
}