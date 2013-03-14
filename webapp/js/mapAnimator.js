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


var STEPS = 100, PAN_ZOOM = 0, ZOOM_PAN  = 1;
var curStep = 0;
var animationOrder;

var startBounds, endBounds, startZoom, endZoom;
var listener;
var MILLION = 1000000;

var currentlyAnimating = false;

//the gmap.fitBounds function only animates if the target bounds are very close
//my current solution is to break the animation into small enough steps so that is always animates

//animates the map to the given direction
function animateMap(gmap, direction) {
	if (!currentlyAnimating) {
		curStep = 0;
		currentlyAnimating = true;

		//gets the current google map bounds
		startBounds = gmap.getBounds();
		//gets the target bounds
		endBounds = calcDirectionBounds(direction);

		//adds a listener that steps the animation after the bounds are finished changing
		listener = google.maps.event.addListener(gmap, 'bounds_changed', stepPan);

		animationOrder = findAnimationOrder();
		if (animationOrder == PAN_ZOOM) { //pan then zoom in
			stepPan();
		}
		else { //zoom out then pan
			changeZoom();
		}
	}
}

//pans then zooms if map needs to zoom in
//zooms then pans if map needs to zoom out
function findAnimationOrder() {
	var startWidth = boundsWidth(startBounds);
	var startHeight = boundsHeight(startBounds);
	var endWidth = boundsWidth(endBounds);
	var endHeight = boundsHeight(endBounds);
	
	var startMax = Math.max(startWidth, startHeight);
	var endMax = Math.max(endWidth, endHeight);

	if (startHeight > endHeight) {
		return PAN_ZOOM;
	}
	else {
		return ZOOM_PAN;
	}
}

function boundsWidth(bounds) {
	return Math.abs(bounds.getSouthWest().lng() - bounds.getNorthEast().lng());
}

function boundsHeight(bounds) {
	return Math.abs(bounds.getSouthWest().lat() - bounds.getNorthEast().lat());	
}

//pans the map by small steps to insure animation
function stepPan() {
	curStep++;
	var bounds = interpolateBounds(startBounds, endBounds, curStep / STEPS);
	gmap.panTo(bounds.getCenter());

	if (curStep >= STEPS) {
		if (animationOrder == PAN_ZOOM) {
			changeZoom();
		}
		else {
			currentlyAnimating = false;
		}
		google.maps.event.removeListener(listener);
	}
}

//zooms to the correct zoom level
function changeZoom() {
	if (animationOrder == PAN_ZOOM) {
		gmap.fitBounds(endBounds);
		currentlyAnimating = false;
	}
	else {
		var startZoomOutBounds = panBounds(endBounds, startBounds);
		gmap.fitBounds(startZoomOutBounds);
	}
}

//takes the width,height of the sizeBounds and centers it on the centerBounds
function panBounds(sizeBounds, centerBounds) {
	var sizeCenter = sizeBounds.getCenter();
	var centerCenter = centerBounds.getCenter();
	var latDiff = sizeCenter.lat() - centerCenter.lat();
	var lngDiff = sizeCenter.lng() - centerCenter.lng();

	var southWest = new google.maps.LatLng(sizeBounds.getSouthWest().lat() + latDiff, sizeBounds.getSouthWest().lng() + lngDiff);
	var northEast = new google.maps.LatLng(sizeBounds.getNorthEast().lat() + latDiff, sizeBounds.getNorthEast().lng() + lngDiff);
	var newBounds = new google.maps.LatLngBounds(southWest, northEast);
	return newBounds;
}

function interpolateBounds(startBounds, endBounds, percent) {
	var southWest = interpolateLatLng(startBounds.getSouthWest(), endBounds.getSouthWest(), percent);
	var northEast = interpolateLatLng(startBounds.getNorthEast(), endBounds.getNorthEast(), percent);
	var bounds = new google.maps.LatLngBounds(southWest, northEast);
	return bounds;
}

function interpolateLatLng(latlng1, latlng2, percent) {
	var lat = interpolateNums(latlng1.lat(), latlng2.lat(), percent);
	var lng = interpolateNums(latlng1.lng(), latlng2.lng(), percent);
	return new google.maps.LatLng(lat, lng);
}

//does an interpolation between two numbers
//for example if args are: 10, 20, and 0.3 then the result is 10 +(20-10)*0.3 = 13
function interpolateNums(num1, num2, percent) {
	return num1 + ((num2 - num1) * percent);
}

//calculates the bounds surrounding a direction
function calcDirectionBounds(direction) {
	var points = direction.points;
	return calcPointsBounds(points);
}

function calcPointsBounds(points) {
	var variableLimit = 1000000000;
	var maxY = -variableLimit;
	var maxX = -variableLimit;
	var minY = variableLimit;
	var minX = variableLimit;

	for (var i = 0; i < points.length; i++) {
		if (points[i].lng > maxX)
			maxX = points[i].lng;
		if (points[i].lng < minX)
			minX = points[i].lng;
		if (points[i].lat > maxY)
			maxY = points[i].lat;
		if (points[i].lat < minY)
			minY = points[i].lat;
	}
	
	var southWest = new google.maps.LatLng(minY / MILLION, minX / MILLION);
	var northEast = new google.maps.LatLng(maxY / MILLION, maxX / MILLION);
	var bounds = new google.maps.LatLngBounds(southWest, northEast);
	return bounds;
}

//calculates the bounds surrounding a route
function calcRouteBounds(route) {
	var points = [];
	for (var i = 0; i < route.directions.length; i++) {
		for (var j = 0; j < route.directions[i].points.length; j++) {
			points.push(route.directions[i].points[j]);
		}
	}
	return calcPointsBounds(points);
}