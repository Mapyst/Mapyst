
var API_KEY = "";

//downloads information about the campus
//callback is a function called after the response is received
//give callback a parameter which will be the campus information
function mapyst_campus(callback) {
	params = {
		api_key: API_KEY
	};
	$.getJSON("campus api url", params, callback);
}

//returns a url which can be used to download a blueprint image
//building is the index of the building according to the info in the campus info
//floor is the index of the floor in the building according to the info in the campus info
function mapyst_blueprint_url(building, floor) {
	return "blueprint api url?building=" + building + "&floor=" + floor + "&api_key=" + API_KEY;
}

//downloads a blueprint of a building's floor
//building is the index of the building according to the info in the campus info
//floor is the index of the floor in the building according to the info in the campus info
//callback is a function called after the response is received
//give callback a parameter which will be the blueprint image
function mapyst_blueprint(building, floor, callback) {
	params = {
		building: building,
		floor: floor,
		api_key: API_KEY
	};
	$.getJSON("blueprint api url", params, callback);
}

//start is text representing the start/beginning of a route
//end is text representing the end/destination of a route
//search is text representing any point
	//note that search has precedence (if search is not empty, it will be assumed to be a one point request)
//prefs is a comma separated list of booleans (true's, false's) representing the preferences
//callback is a function called after the response is received
//give callback a parameter which will be the route info
//all variables should have a value, even if empty string
function mapyst_route(start, end, search, prefs, callback) {
	params = {
		start: start,
		end: end,
		search: search,
		prefs: prefs,
		api_key: API_KEY
	};
	$.getJSON("route api url", params, callback);
}


/*
	Useful constants and functions for working with Mapyst data
*/


var ACADEMIC = 0, RESIDENCE = 1, ATHLETIC = 2, OTHER = 3, OUTSIDE = 4; //building types
var SAME_FLOOR = 0, STAIRS = 1, ELEVATOR = 2, RAMP = 3; //directions types
var MILLION = 1000000; //amount that the latlng data needs to be divided by (44000000 -> 44.000000)

//checks whether a building's type is outside or not
function mapyst_outsideBuilding(campus, buildingIndex) {
	if (campus.buildings[buildingIndex].type == OUTSIDE) {
		return true;
	}
	return false;
}

//converts a point in the campus data to a google maps latlng object
function mapyst_pointToLatLng(point) {
	return new google.maps.LatLng(point.lat / MILLION, point.lng / MILLION);
}

//determines whether a direction is outside
function mapyst_outsideDir(direction) {
	var building = direction.end.buildingIndex;
	return mapyst_outsideBuilding(campus, building);
}

//determines the building of a direction
function mapyst_dirBuildingIndex(direction) {
	return direction.end.buildingIndex;
}

//determines the floor of a direction
function mapyst_dirFloorIndex(direction) {
	return direction.end.floorIndex;
}