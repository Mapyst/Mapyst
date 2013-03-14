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


//toggles between print preview mode and main mode
function enterPrintPreview() {
	if (route) {
		$(document.body).css("overflow-y", "scroll");
		$("#bottom").hide();
		$("#printing")[0].style.visibility = "visible";

		createPrintMaps(route);

		printMode = !printMode;
	}
}

function exitPrintPreview() {
	$(document.body).css("overflow-y", "hidden");
	$("#bottom").show();
	$("#printing-content").html("");
	$("#printing")[0].style.visibility = "hidden";

	printMode = !printMode;
}

function printPage() {
	window.print();
}

//creates the print preview view for the current route
function createPrintMaps(route) {
	//creates html for the route overview
	var routeOverview = "From:  " + route.startText + "</br>To:       " + route.endText + "</b></br><i>Total: " + formatTime(calcRouteTime() / 1000) + "</i>";
	var html = "<div id=\"route\" class=\"print_direction\"><p>" + routeOverview + "</p></br><div class=\"print_map\"></div></div>";

	//creates the html for each of the route's directions
	for (var i = 0; i < route.directions.length; i++) {
		var direction = route.directions[i];
		html += "<div class=\"print_direction\"><p>" + direction.text + "</br><i>" + formatTime(direction.time / 1000) + "</i></p></br><div class=\"print_map\"></div></div>";
	}

	$("#printing-content").html(html);

	var direction_divs = $(".print_direction");
	var map_divs = $(".print_map");
	for (var i = 0; i < map_divs.length; i++) {
		//creates a map for each of the print tile
		var latlng = new google.maps.LatLng(-34.397, 150.644);
		var myOptions = {
			zoom: 8,
			center: latlng,
			mapTypeId: google.maps.MapTypeId.HYBRID,
			minZoom: 16,
			tilt: 0
		};
		var map = new google.maps.Map(map_divs[i], myOptions);

		//copies the current map for viewing directions into each of the print tiles
		copyMap(map, i - 1);

		direction_divs[i].style.top = (315 * i) + "px"; 
	}
}
