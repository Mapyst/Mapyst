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

package com.mapyst.android.ui;

import java.util.ArrayList;

import com.mapyst.route.Direction;
import com.mapyst.route.LatLngPoint;

import com.google.android.maps.GeoPoint;
import com.mapyst.android.Images;
import com.mapyst.android.MainScreen;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Icon {
	public Bitmap bitmap;
	public GeoPoint geoPoint;
	public int dirIndex;

	public static int UP = 1;
	public static int DOWN = 2;

	public static ArrayList<Icon> collectIcons(MainScreen main,	Direction[] directions) {
		Bitmap startBitmap = BitmapFactory.decodeResource(main.getResources(), Images.Icons.START);
		Bitmap endBitmap = BitmapFactory.decodeResource(main.getResources(), Images.Icons.END);
		Bitmap upStairsBitmap = BitmapFactory.decodeResource(main.getResources(), Images.Icons.UP_STAIRS);
		Bitmap downStairsBitmap = BitmapFactory.decodeResource(main.getResources(), Images.Icons.DOWN_STAIRS);
		Bitmap upElevatorBitmap = BitmapFactory.decodeResource(main.getResources(), Images.Icons.UP_ELEVATOR);
		Bitmap downElevatorBitmap = BitmapFactory.decodeResource(main.getResources(), Images.Icons.DOWN_ELEVATOR);
		Bitmap upRampBitmap = BitmapFactory.decodeResource(main.getResources(),	Images.Icons.UP_RAMP);
		Bitmap downRampBitmap = BitmapFactory.decodeResource(main.getResources(), Images.Icons.DOWN_RAMP);

		ArrayList<Icon> icons = new ArrayList<Icon>();
		addIcon(icons, startBitmap, directions[0].getPoints()[0], 0);

		for (int i = 1; i < directions.length; i++) {
			if (directions[i].getType() == Direction.STAIRS) {
				// UP or DOWN
				if (getDirection(directions[i]) == UP) {
					// Assign end of previous direction a stair icon - going up
					directions[i - 1].setEndIcon(Images.Icons.UP_STAIRS);
					addIconForDirEnd(icons, upStairsBitmap, directions[i - 1], i);
				} else {
					directions[i - 1].setEndIcon(Images.Icons.DOWN_STAIRS);
					addIconForDirEnd(icons, downStairsBitmap, directions[i - 1], i);
				}
			} else if (directions[i].getType() == Direction.ELEVATOR) {
				// UP or DOWN
				if (getDirection(directions[i]) == UP) {
					// Assign end of previous direction a stair icon - going up
					directions[i - 1].setEndIcon(Images.Icons.UP_ELEVATOR);
					addIconForDirEnd(icons, upElevatorBitmap, directions[i - 1], i);
				} else {
					directions[i - 1].setEndIcon(Images.Icons.DOWN_ELEVATOR);
					addIconForDirEnd(icons, downElevatorBitmap, directions[i - 1], i);
				}
			}
			if (directions[i].getType() == Direction.RAMP) {
				// UP or DOWN
				if (getDirection(directions[i]) == UP) {
					// Assign end of previous direction a stair icon - going up
					directions[i - 1].setEndIcon(Images.Icons.UP_RAMP);
					addIconForDirEnd(icons, upRampBitmap, directions[i - 1], i);
				} else {
					directions[i - 1].setEndIcon(Images.Icons.DOWN_RAMP);
					addIconForDirEnd(icons, downRampBitmap, directions[i - 1], i);
				}
			}

		}

		// setting start icon
		directions[0].setStartIcon(Images.Icons.START);

		// setting end icon
		directions[directions.length - 1].setEndIcon(Images.Icons.END);
		addIconForDirEnd(icons, endBitmap, directions[directions.length - 1], directions.length - 1);

		return icons;
	}

	private static void addIconForDirEnd(ArrayList<Icon> icons, Bitmap bitmap, Direction direction, int dirIndex) {
		LatLngPoint[] dirPoints = direction.getPoints();
		addIcon(icons, bitmap, dirPoints[dirPoints.length - 1], dirIndex);
	}

	private static void addIcon(ArrayList<Icon> icons, Bitmap bitmap, LatLngPoint latlngPoint, int dirIndex) {
		Icon newIcon = new Icon();
		newIcon.bitmap = bitmap;
		newIcon.dirIndex = dirIndex;
		GeoPoint geoPoint = DrawingHelpers.convertPointToGeo(latlngPoint);
		newIcon.geoPoint = geoPoint;

		icons.add(newIcon);
	}

	public static int getDirection(Direction dir) {
		int start = dir.getStart().getFloorIndex();
		int end = dir.getEnd().getFloorIndex();

		if (start > end) {
			return DOWN;
		} else {
			return UP;
		}
	}
}
