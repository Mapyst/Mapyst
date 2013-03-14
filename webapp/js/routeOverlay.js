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


var offset = 2000;

function RouteOverlay(route, icons, map) {
    if (route.singleWaypoint) {
        this.singleWaypoint = route.singleWaypoint;
        var pointId = route.singleWaypoint.id;

        if (!mapyst_outsideBuilding(campus, pointId.buildingIndex)) { //only loads the blueprint if it is an inside floor
            var url = mapyst_blueprint_url(pointId.buildingIndex, pointId.floorIndex);
            var bounds = getFloorBounds(pointId.buildingIndex, pointId.floorIndex);
            this.singleBlueprint = new Image();
            this.singleBlueprint.src = url;
        }
    }
    else {
        this.directions = route.directions;
        for (var i = 0; i < this.directions.length; i++) {
            var building = mapyst_dirBuildingIndex(this.directions[i]);
            var floor = mapyst_dirFloorIndex(this.directions[i]);
            if (!mapyst_outsideBuilding(campus, building)) { //only loads the blueprint if it is an inside floor
                var url = mapyst_blueprint_url(building, floor);
                var bounds = getFloorBounds(building, floor);
                this.directions[i].blueprint = new Image();
                this.directions[i].blueprint.src = url;
            }
        }
    }
    this.icons = icons;
    for (var i = 0; i < icons.length; i++) {
        icons[i].image = new Image();
        icons[i].image.src = icons[i].getIcon().url;
    }

    this.map = map;
    this.canvas = null;

    this.setMap(map);
}

RouteOverlay.prototype = new google.maps.OverlayView();

RouteOverlay.prototype.onAdd = function() {
    var canvas = document.createElement('canvas');
    canvas.style.position = "absolute";
    positionSizeCanvas(canvas);
    this.canvas = canvas;

    var panes = this.getPanes();
    panes.floatPane.appendChild(canvas);

    setTimeout("routeOverlay.draw()", 2000);
}

RouteOverlay.prototype.draw = function() {
    if (this.canvas) {
        var overlayProjection = this.getProjection();
        var context = this.canvas.getContext("2d");
        context.clearRect(0, 0, this.canvas.width, this.canvas.height);

        if (this.directions) {
            if (this.directions[curIndex].blueprint) {
                var building = mapyst_dirBuildingIndex(this.directions[curIndex]);
                var floor = mapyst_dirFloorIndex(this.directions[curIndex]);
                drawBlueprint(context, building, floor, this.directions[curIndex].blueprint, overlayProjection);
            }

            for (var i = 0; i < this.directions.length; i++) {
                drawDirection(context, this.directions[i], i == this.curIndex, overlayProjection);
            }
        }
        else {
            var pointId = route.singleWaypoint.id;
            if (this.singleBlueprint)
                drawBlueprint(context, pointId.buildingIndex, pointId.floorIndex, this.singleBlueprint, overlayProjection);
        }

        drawIcons(context, this.icons, overlayProjection);
    }
}

function drawDirection(context, direction, isCur, overlayProjection) {
    var pixelPoints = [];
    for (var i = 0; i < direction.points.length; i++) {
        var pixelPoint = overlayProjection.fromLatLngToDivPixel(mapyst_pointToLatLng(direction.points[i]));
        pixelPoints.push(pixelPoint);
    }
    
    context.beginPath();
    context.moveTo(pixelPoints[0].x + offset, pixelPoints[0].y + offset);
    
    for (var i = 1; i < pixelPoints.length; i++) {
        context.lineTo(pixelPoints[i].x + offset, pixelPoints[i].y + offset);
    }
    
    if (isCur) {
        // var grd = context.createLinearGradient(0, 0, 900, 900);
        // grd.addColorStop(0, "#8ED6FF"); // light blue
        // grd.addColorStop(1, "#004CB3"); // dark blue
        // context.strokeStyle = grd;
        context.strokeStyle = "#004CB3";
    }
    else {
        context.strokeStyle = "#000000";
    }
    context.lineWidth = 6;
    context.stroke();
}

function drawIcons(context, icons, overlayProjection) {
    for (var i = 0; i < icons.length; i++) {
        var point = overlayProjection.fromLatLngToDivPixel(icons[i].getPosition());
        context.drawImage(icons[i].image, Math.round(point.x + offset)-15, Math.round(point.y + offset)-15, 30, 30);
    }
}

function drawBlueprint(context, building, floor, blueprint, overlayProjection) {
    var bounds = getFloorBounds(building, floor);
    var northEast = overlayProjection.fromLatLngToDivPixel(bounds.getNorthEast());
    var southWest = overlayProjection.fromLatLngToDivPixel(bounds.getSouthWest());
    context.drawImage(blueprint, southWest.x + offset, northEast.y + offset, Math.abs(northEast.x - southWest.x), Math.abs(northEast.y - southWest.y));
}

RouteOverlay.prototype.setDirection = function(index) {
    this.curIndex = index;
    this.draw();
}

RouteOverlay.prototype.onRemove = function() {
  this.canvas.parentNode.removeChild(this.canvas);
  this.canvas = null;
}

function positionSizeCanvas(canvas) {
    canvas.style.top = "-" + offset + "px";
    canvas.style.left = "-" + offset + "px";

    // var bodyWidth = $(document.body).width();
    // var bodyHeight = $(document.body).height();

    // if (bodyWidth < 950)
    //     bodyWidth = 950;

    // canvas.style.width = (bodyWidth - 270) + "px";
    // canvas.style.height = (bodyHeight - 120) + "px";
    // canvas.setAttribute("width", bodyWidth - 270);
    // canvas.setAttribute("height", bodyHeight - 120);
    canvas.setAttribute("width", offset*2);
    canvas.setAttribute("height", offset*2);
}