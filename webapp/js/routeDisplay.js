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


var curIndex = 0; //the index of the current direction
var polylines, blueprint, icons, routeOverlay; //graphics placed on the map

var UP = 0, DOWN = 1; //direction directions (when a stair, elevator, or ramp direction goes up or down a floor)

function getCanvasSupport() {
	try {
        this.canvas_compatible = !!(document.createElement('canvas').getContext('2d')); // S60
        } catch(e) {
        this.canvas_compatible = !!(document.createElement('canvas').getContext); // IE
    } 
    return this.canvas_compatible;
}
var canvasSupport = getCanvasSupport();
console.log("canvasSupport:" + canvasSupport);

//displays a calculated route
function loadRoute() {

	if (route.singleWaypoint) { //not a route, but instead a single point
		showSinglePoint(route);

		if (canvasSupport) {
			if (routeOverlay) {
				routeOverlay.setMap(null);
			}
			routeOverlay = new RouteOverlay(route, icons, gmap);
		}
	}
	else { //a route between two points
		showDirControls();
		calcIndexOfSameFloors();
		createPolylines();
		createIcons();
		makeDirectionsList(route);

		if (canvasSupport) {
			if (routeOverlay) {
				routeOverlay.setMap(null);
			}
			routeOverlay = new RouteOverlay(route, icons, gmap);
		}

		curIndex = 0;
		setDirection(0);
		showDirectionsTab();
	}
}

//removes all graphics from the google map object, clears the list of directions
function clearMap() {
	if (polylines) {
		for (var i = 0; i < polylines.length; i++) {
			polylines[i].setMap(null);
		}
		polylines = [];
	}
	if (icons) {
		for (var j = 0; j < icons.length; j++) {
			icons[j].setMap(null);
		}
		icons = [];
	}

	if (blueprint) {
		blueprint.setMap(null);
		blueprint = null;
	}

	if (routeOverlay) {
		routeOverlay.setMap(null);
		routeOverlay = null;
	}

	$("#sidebar-directions").html("");
}

//displays a single point on google maps
function showSinglePoint() {
	//adds an icon to google maps
	icons = [];
	if (singlePointStart)
		var icon = "start.png";
	else
		var icon = "end.png";

	addIcon(icon, route.singleWaypoint.point);

	//sets the current direction text to the single point's text
	$("#map-controls-current").html(route.singlePointText);
	centerCurDirText();
	//changes between arrows and the start,end icons
	setDirectionControls();
	showDirControls();

	//shows the blueprint that the point is on
	var pointId = route.singleWaypoint.id;
	if (!canvasSupport)
		setBlueprint(pointId.buildingIndex, pointId.floorIndex, gmap);

	updateFloorInfo(pointId.buildingIndex, pointId.floorIndex);

	animateMap(gmap, {points: [route.singleWaypoint.point]});
}

function copyMap(map, dirIndex) {
	var polylinesCopy = [];
	for (var i = 0; i < polylines.length; i++) {
		var direction = route.directions[i];

		var opacity = getOpacity(i);
		var line = new google.maps.Polyline({strokeColor: "black", strokeOpacity: opacity, strokeWeight: 5});
		line.setPath(polylines[i].getPath());
		line.setMap(map);
		polylinesCopy.push(line);
	}

	var iconsCopy = [];
	for (var i = 0; i < icons.length; i++) {
		var icon = new google.maps.Marker({
	    	position: icons[i].getPosition(),
	    	map: map,
	    	icon: icons[i].getIcon()
	  	});

		icon.setMap(map);
		iconsCopy.push(icon);
	}
	
	if (dirIndex >= 0) {
		var direction = route.directions[dirIndex];
		polylinesCopy[dirIndex].setOptions({strokeColor: "blue", strokeOpacity: 1});
		
		var building = mapyst_dirBuildingIndex(direction);
		var floor = mapyst_dirFloorIndex(direction);
		if (!mapyst_outsideBuilding(campus, building)) { //only loads the blueprint if it is an inside floor
			var url = mapyst_blueprint_url(building, floor);
			var bounds = getFloorBounds(building, floor);
			var dirBlueprint = new google.maps.GroundOverlay(url, bounds);
			dirBlueprint.setMap(map);
		}

		var bounds = calcDirectionBounds(direction);
		map.fitBounds(bounds);
	}
	else {
		var bounds = calcRouteBounds(route);
		map.fitBounds(bounds);
	}
}

//creates the google maps polylines for all the directions
function createPolylines() {
	polylines = [];
	for (var i = 0; i < route.directions.length; i++) {
		var direction = route.directions[i];
		if (direction.type == 0) { //type==same floor (does not draw lines for stairs, elevator, or ramp directions)
			var opacity = getOpacity(i);
			var polyline = new google.maps.Polyline({strokeColor: "black", strokeOpacity: opacity, strokeWeight: 5});
			
			//adds direction's points to the polyline's points
			var points = [];
			for (var j = 0; j < direction.points.length; j++) {
				var point = direction.points[j];
				var googlePoint = mapyst_pointToLatLng(point);
				points.push(googlePoint);
			}
			polyline.setPath(points);

			//places polyline on the map
			if (!canvasSupport)
				polyline.setMap(gmap);
			//adds polyline to global list of polylines
			polylines.push(polyline);
		}
		else { //same floor direction
			polylines.push(new google.maps.Polyline());
		}
	}
}

var images;

//adds icons to google maps for the start point, end point, and stairs, elevators, and ramp directions
function createIcons() {
	images = [
		[],//same floor direction type - no icons for same floor directions
		//UP images        DOWN image 
		["stairs_up.png", "stairs_down.png"],
		["elevator_up.png", "elevator_down.png"],
		["ramp_up.png", "ramp_down.png"]
	];
	var startImage = "start.png";
	var endImage = "end.png";

	var directions = route.directions;
	icons = [];

	//adds an icon for the route's start
	addIcon(startImage, directions[0].points[0]);
	
	//adds icons for stairs, elevator, and ramp directions
	//determines the appropriate image using the images matrix defined above
	for (var i = 0; i < directions.length; i++){
		var type = directions[i].type;
		var points = directions[i].points;
		addIcon(getDirectionImage(i), points[Math.round(points.length / 2) - 1]);
	}

	//adds an icon for the route's end
	addIconForDirEnd(endImage, directions[directions.length - 1]);
	
	return icons;
}

function getDirectionImage(i) {
	var type = route.directions[i].type;
	if (type == SAME_FLOOR) {
		return "dirNum" + (route.directions[i].indexOfSameFloors + 1) + ".png";
	}
	else {
		var upOrDown = getTransitionDir(route.directions[i]); //whether the direction goes up(floor 4 to 3) or down(3 to 4)
		return images[type][upOrDown];
	}
}

//finds a direction's index out of "SAME_FLOOR" directions (not elevators, stairs, and ramp directions)
function calcIndexOfSameFloors() {
	var countSameFloors = 0;
	for (var i = 0; i < route.directions.length; i++) {
		var type = route.directions[i].type;
		if (type == SAME_FLOOR) {
			route.directions[i].indexOfSameFloors = countSameFloors;
			countSameFloors++;
		}
	}
}

//determines whether the direction goes up(floor 4 to 3) or down(3 to 4)
function getTransitionDir(direction){
	var start = direction.start.floorIndex;
	var end = direction.end.floorIndex;

	if (start > end){
		return DOWN;
	}
	else{
		return UP;
	}
}

//adds an icon positioned at the last point of the given direction
function addIconForDirEnd(image, direction) {
	var dirPoints = direction.points;
	addIcon(image, dirPoints[dirPoints.length - 1]);
}

//adds an icon to google maps at the given location
function addIcon(image, location) {
  	var latlng = mapyst_pointToLatLng(location);
  	var url = "images/map_icons/" + image;
  	//sizes marker and sets the anchor in the middle (image is centered over the point it is placed at)
  	var image = new google.maps.MarkerImage(
		url,
      	new google.maps.Size(60, 60),//image size
     	new google.maps.Point(0, 0),//origin
      	new google.maps.Point(15, 15),//anchor
      	new google.maps.Size(30, 30)//scaled size
    );

    var map = gmap;
    if (canvasSupport)
    	map = null;
  	var newIcon = new google.maps.Marker({
    	position: latlng,
    	map: map,
    	icon: image
  	});
  	icons.push(newIcon);
}

//creates a google maps bounds object for a floor's bounds
function getFloorBounds(building, floor) {
	var floor = campus.buildings[building].floors[floor];
	var southWest = new google.maps.LatLng(floor.southEast.lat / MILLION, floor.northWest.lng / MILLION);
	var northEast = new google.maps.LatLng(floor.northWest.lat / MILLION, floor.southEast.lng / MILLION);
	var bounds = new google.maps.LatLngBounds(southWest, northEast);
	return bounds;
}

function previousDirection() {
	if (route.directions && curIndex > 0)
		setDirection(curIndex - 1);
}

function nextDirection() {
	if (route.directions && curIndex < route.directions.length - 1)
		setDirection(curIndex + 1);
}

function currentDirection() {
	//moves the map to the current direction with animations
	animateMap(gmap, route.directions[curIndex]);
}

//changes the current direction from the old direction
function setDirection(newIndex) {
	var oldIndex = curIndex;
	curIndex = newIndex;

	if (canvasSupport)
		var oldOpacity = 0;//invisible - shown using RouteOverlay
	else
		var oldOpacity = getOpacity(oldIndex);
	//changes the color of the old current direction to black
	polylines[oldIndex].setOptions({strokeColor: "black", strokeOpacity: oldOpacity});
	//changes the color of the new current direction to black
	if (canvasSupport)
		var opacity = 0;//invisible - shown using RouteOverlay
	else
		var opacity = 1;
	polylines[newIndex].setOptions({strokeColor: "blue", strokeOpacity: opacity});

	var items = $("#sidebar-directions li");
	//changes the style of the old current direction to the default style
	items[oldIndex + 1].className = "dirListItem";
	//changes the style of the new current direction to the selected style
	items[newIndex + 1].className = "activeDirListItem";

	//sets the background blueprint appropriately
	var curDirection = route.directions[curIndex];
	var building = mapyst_dirBuildingIndex(curDirection);
	var floor = mapyst_dirFloorIndex(curDirection);
	if (!canvasSupport) {
		setBlueprint(building, floor, gmap);
	}

	//changes the current direction text and centers it
	$("#map-controls-current").html(curDirection.text);
	centerCurDirText();
	//changes between arrows and the start,end icons
	setDirectionControls();

	updateFloorInfo(building, floor);

	//moves the map to the new direction with animations
	animateMap(gmap, curDirection);

	if (canvasSupport) {
		routeOverlay.setDirection(curIndex);
	}
}

function getOpacity(dirIndex) {
	//inside, non-current directions are 50% transparent
	if (mapyst_outsideDir(route.directions[dirIndex]) || dirIndex == curIndex)
		return 1;
	else
		return 0.5;
}

function updateFloorInfo(building, floor) {
	var buildingText = campus.buildings[building].acronym;
	var floorText = campus.buildings[building].floors[floor].name;
	$("#map-floor").html(buildingText + " Floor " + floorText);

	if (mapyst_outsideBuilding(campus, building)) {
		$("#map-floor").css("visibility", "hidden");
	}
	else {
		$("#map-floor").css("visibility", "visible");
	}
}

//replaces the left arrow with the start icon if on the first direction
//replaces the right arrow with the end icon if on the last direction
function setDirectionControls() {
	if (route.singleWaypoint) {
		$("#map-controls-previous").hide();
		$("#map-controls-next").hide();
	}
	else {
		if (curIndex == 0)
			$("#map-controls-previous").css("background-image", "url(images/buttons/startButton.png)");
		else
			$("#map-controls-previous").css("background-image", "url(images/buttons/arrowLeft.png)");

		if (curIndex == route.directions.length - 1)
			$("#map-controls-next").css("background-image", "url(images/buttons/endButton.png)");
		else
			$("#map-controls-next").css("background-image", "url(images/buttons/arrowRight.png)");

		$("#map-controls-previous").show();
		$("#map-controls-next").show();
	}
}

//loads the blueprint for the given building and floor
function setBlueprint(building, floor, map) {
	//removes blueprint from last direction
	if (blueprint)
		blueprint.setMap(null);

	if (!mapyst_outsideBuilding(campus, building)) { //only loads the blueprint if it is an inside floor
		var url = mapyst_blueprint_url(building, floor);
		var bounds = getFloorBounds(building, floor);
		blueprint = new google.maps.GroundOverlay(url, bounds);
		blueprint.setMap(gmap);
	}
}

//hides the controls to change the current direction
function hideDirControls() {
	var directionControls = $("#map-controls");
	directionControls.css("visibility", "hidden");
}

//shows the controls to change the current direction
function showDirControls() {
	var directionControls = $("#map-controls");
	directionControls.css("visibility", "visible");
}

//centers the current direction's text within the map
function centerCurDirText() {
	var currentDirection = $("#map-controls-current");
	var textWidth = currentDirection.width();
	var textHeight = currentDirection.height();

	var controlsElement = $("#map-controls");
	var controlsWidth = controlsElement.width();
	var controlsHeight = controlsElement.height();

	currentDirection.css("left", ((controlsWidth / 2) - (textWidth / 2)) + "px");
	currentDirection.css("top", ((controlsHeight / 2) - (textHeight / 2)) + "px");
}