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

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.mapyst.android.MainScreen;
import com.mapyst.android.Mapyst;
import com.mapyst.campus.Campus;
import com.mapyst.route.Direction;
import com.mapyst.route.LatLngPoint;
import com.mapyst.route.Waypoint2D;

public class RouteMapOverlay extends Overlay {

	private Paint paint;
	private Paint insidePaint;

	private Paint bitmapPaint;
	private Paint shadePaint;

	private RectF destination;
	private Projection projection;
	private Bitmap blueprintBitmap;
	private int floor, building;

	private ArrayList<Icon> icons;

	private MainScreen main;

	// the list of directions for the route that is currently being displayed
	private Direction[] directions;
	// indicates the index of the current direction (the direction highlighted
	// with the blue line)
	private int curDir;

	// single points used for one point mode (just displaying a point on the
	// map, not a route)
	private GeoPoint singleGeoPoint;
	private Point singlePixelPoint;

	// stores an array of points for each direction
	private GeoPoint[][] geoPoints;
	private Point[][] pixelPoints;

	private int mode;

	private int screenWidth;
	private int screenHeight;

	private float gradient;

	public static final int ROUTE = 0;
	public static final int POINT = 1;
	public static final int BLANK = 2;

	private static final int PATH_COLOR1 = Color.BLUE;
	private static final int PATH_COLOR2 = Color.rgb(152, 224, 224);
	private static final int OUTSIDE_COLOR = Color.BLACK;

	public static final int UP = 1;
	public static final int DOWN = -1;

	public static final int DOWN_ARROW = 1;
	public static final int UP_ARROW = 0;
	private static final float GRADIENT_MAX = 100.0f;
	private static final float GRADIENT_SPACE = 30.0f;

	private int dashPathBase = 20;

	private Mapyst app;

	public RouteMapOverlay(MainScreen main, Mapyst app, Projection projection, int mode, int screenWidth, int screenHeight) {
		this.app = app;
		this.main = main;

		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;

		setupPaints();

		this.projection = projection;
		this.mode = mode;

		floor = -1;
		building = 0;
		curDir = 0;
	}

	public RouteMapOverlay(MainScreen main, Mapyst app, Direction[] directions,
			Projection projection, int screenWidth, int screenHeight) {

		this(main, app, projection, ROUTE, screenWidth, screenHeight);

		this.directions = directions;
		icons = Icon.collectIcons(main, directions);

		calculateGeoPoints();
		updateBitmaps();
	}

	public RouteMapOverlay(MainScreen main, Mapyst app, Waypoint2D waypoint,
			int icon, Projection projection, int screenWidth, int screenHeight) {

		this(main, app, projection, POINT, screenWidth, screenHeight);

		setPoint(waypoint, icon);
	}

	private void setupPaints() {
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStrokeWidth(8.0f);
		paint.setStyle(Style.STROKE);
		paint.setPathEffect(new CornerPathEffect(5.0f));
		paint.setColor(OUTSIDE_COLOR);

		bitmapPaint = new Paint();

		shadePaint = new Paint();
		shadePaint.setColor(Color.argb(255 / 3, 89, 89, 89));

		insidePaint = new Paint();
		insidePaint.setAntiAlias(true);
		insidePaint.setStrokeWidth(8.0f);
		insidePaint.setStyle(Style.STROKE);
		insidePaint.setPathEffect(new CornerPathEffect(5.0f));
		insidePaint.setColor(OUTSIDE_COLOR);
	}

	private void calculateGeoPoints() {
		pixelPoints = new Point[directions.length][];
		geoPoints = new GeoPoint[directions.length][];
		for (int i = 0; i < directions.length; i++) {
			if (directions[i].getType() == Direction.SAME_FLOOR) {
				LatLngPoint[] dirLatLngPoints = directions[i].getPoints();
				pixelPoints[i] = new Point[dirLatLngPoints.length];

				geoPoints[i] = new GeoPoint[dirLatLngPoints.length];
				for (int j = 0; j < dirLatLngPoints.length; j++) {
					geoPoints[i][j] = DrawingHelpers.convertPointToGeo(dirLatLngPoints[j]);
				}
			} else {
				LatLngPoint[] dirLatLngPoints = directions[i].getPoints();
				pixelPoints[i] = new Point[1];
				geoPoints[i] = new GeoPoint[1];
				geoPoints[i][0] = DrawingHelpers.convertPointToGeo(dirLatLngPoints[0]);
			}
		}
	}

	// path dash works with absolute pixel size, but we want
	// that to change, and dash size to be relative with zoom
	// level
	// - Evan
	public void setDashPathInnerPaint(int dirIndex) {
		float scalar = dashPathBase;

		Point[] curPixelPoints = pixelPoints[dirIndex];

		double pathLength = DrawingHelpers.distance(curPixelPoints[0], curPixelPoints[curPixelPoints.length - 1]);

		scalar = (float) (pathLength / scalar);

		DashPathEffect dashPath = new DashPathEffect(new float[] { scalar, scalar }, 1);
		insidePaint.setPathEffect(dashPath);
	}

	// this is where we're going to check if we should change the current
	// direction, based on the distance from the center point to a path
	// TODO: add a bool that toggles this mode, with a setting in the ui
	// - Evan
	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {

		if (directions == null)
			return super.onTouchEvent(event, mapView);

		Point center = new Point(mapView.getWidth() / 2, mapView.getHeight() / 2); // simple calc of center pixel

		int closestDir = curDir; // assume its the same
		double bestMin = Double.MAX_VALUE; // closest direction so far

		// the algorithm is as follows: find the shortest distance from the
		// center
		// of the screen to each direction. Then, change to the direction that
		// is
		// closest to the center

		for (int i = 0; i < directions.length; i++) {
			Point[] dirPixelPoints = pixelPoints[i];
			if (directions[i].getType() == Direction.SAME_FLOOR) {
				double avgDist = (DrawingHelpers.distance(dirPixelPoints[0],center)
						+ DrawingHelpers.distance(dirPixelPoints[dirPixelPoints.length / 2],center) + DrawingHelpers.distance(
						dirPixelPoints[dirPixelPoints.length - 1], center)) / 2.0;
				if (avgDist < bestMin) {
					bestMin = avgDist;
					closestDir = i;
				}
			}
		}

		// only changes the direction if the direction being automatically
		// switched to is and outside direction
		if (curDir != closestDir && // inefficient to setui if we don't need to
				app.campus.buildingIsOutside(directions[closestDir].getBuilding()) && // only change if moving between outside points
				app.campus.buildingIsOutside(directions[curDir].getBuilding())) {
			if (main != null) {
				main.setUI(closestDir, false); // calling this, we set the bitmaps + the icons
			} else {
				main.setUI(closestDir, false);
			}
		}
		return super.onTouchEvent(event, mapView); // not stealing the event
	}

	public Shader getPathShader(Point[] curPixelPoints) {
		float speed = 90.f; // INVERSE!! HIGHER VALUES = SLOWER SPEED
		gradient = (SystemClock.uptimeMillis() % (GRADIENT_SPACE * 2 * speed)) / speed + GRADIENT_MAX - GRADIENT_SPACE * 2;
		float dx = (int) (curPixelPoints[curPixelPoints.length - 1].x - curPixelPoints[0].x);
		float dy = (int) (curPixelPoints[curPixelPoints.length - 1].y - curPixelPoints[0].y);
		Shader shader = new LinearGradient(((gradient - GRADIENT_SPACE) / GRADIENT_MAX) * (dx)
						+ curPixelPoints[0].x, ((gradient - GRADIENT_SPACE) / GRADIENT_MAX) * (dy)
						+ curPixelPoints[0].y, (gradient / GRADIENT_MAX) * (dx)
						+ curPixelPoints[0].x, (gradient / GRADIENT_MAX) * (dy)
						+ curPixelPoints[0].y, PATH_COLOR1, PATH_COLOR2, TileMode.MIRROR);
		return shader;
	}

	public void drawPath(Canvas canvas) {
		Point[] curPixelPoints = pixelPoints[curDir];

		// setup gradient
		paint.setShader(getPathShader(curPixelPoints));

		// draw path
		Path path = new Path();
		path.setFillType(FillType.WINDING);
		path.moveTo(curPixelPoints[0].x, curPixelPoints[0].y);
		for (int i = 1; i < curPixelPoints.length; i += 1)
			path.lineTo(curPixelPoints[i].x, curPixelPoints[i].y);
		canvas.drawPath(path, paint);
	}

	public static ArrayList<Rect> debugRects = new ArrayList<Rect>();

	private static Paint debugPaint;

	private static void drawDebugRects(Canvas canvas) {
		if (debugPaint == null) {
			debugPaint = new Paint();
			debugPaint.setColor(Color.argb(100, 0, 0, 0));
		}
		if (debugRects != null) {
			for (int i = 0; i < debugRects.size(); i++) {
				canvas.drawRect(debugRects.get(i), debugPaint);
			}
		}
	}

	/*
	 * Function: draw Draws the outside points and arcs over the Google MapView.
	 */
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);

		drawDebugRects(canvas);

		applyProjection();

		if (mode == ROUTE) {
			// if inside draw gray background and floor's blueprint
			if (!app.campus.buildingIsOutside(building)) {
				// canvas.drawARGB(255/3, 89, 89, 89);//33% opaque dark gray
				// background
				// canvas.drawRect(entireScreenRect, shadePaint);
				canvas.drawBitmap(blueprintBitmap, null, destination, bitmapPaint);
			}
			drawDirections(canvas);

			drawPath(canvas);

			// if (main != null)
			// main.getMapView().postInvalidateDelayed(ANIMATION_DELAY);
		} else if (mode == POINT && singlePixelPoint != null) {
			if (!app.campus.buildingIsOutside(building)) {
				canvas.drawARGB(255 / 3, 89, 89, 89);// 33% opaque dark gray
														// background
				canvas.drawBitmap(blueprintBitmap, null, destination, paint);
			}
		}

		drawIcons(canvas);

		if (directions != null && directions[curDir].getType() != Direction.SAME_FLOOR) {
			drawTransitionDirection(canvas);
		}

	}

	private void drawTransitionDirection(Canvas canvas) {
		Paint rectPaint = new Paint();
		rectPaint.setColor(Color.argb(200, 89, 89, 89));

		float rectWidth = screenWidth * (2.0f / 3.0f);
		float rectHeight = screenHeight * (1.0f / 4.0f);
		RectF rect = new RectF(screenWidth / 2.0f - rectWidth / 2.0f, 75, screenWidth / 2.0f + rectWidth / 2.0f, 75 + rectHeight);

		canvas.drawRoundRect(rect, 15, 15, rectPaint);

		// draws border
		rectPaint.setColor(Color.argb(255, 0, 0, 0));
		rectPaint.setStyle(Paint.Style.STROKE);
		rectPaint.setStrokeWidth(4);
		canvas.drawRoundRect(rect, 15, 15, rectPaint);

		Paint textPaint = new Paint();
		textPaint.setColor(Color.WHITE);
		textPaint.setTypeface(Typeface.create("Arial", Typeface.BOLD));
		textPaint.setTextSize(screenHeight / 10);

		int startFloorIndex = directions[curDir].getStart().getFloorIndex();
		int startBuildingIndex = directions[curDir].getStart().getBuildingIndex();
		String startFloorText = app.campus.getFloor(startBuildingIndex, startFloorIndex).name;

		int endFloorIndex = directions[curDir].getEnd().getFloorIndex();
		int endBuildingIndex = directions[curDir].getEnd().getBuildingIndex();
		String endFloorText = app.campus.getFloor(endBuildingIndex, endFloorIndex).name;

		float row1Width = textPaint.measureText("Floor");
		float row2Width = textPaint.measureText(startFloorText + " to "	+ endFloorText);

		Rect bounds = new Rect();

		textPaint.getTextBounds("Floor", 0, 5, bounds);

		int textHeight = bounds.height();

		canvas.drawText("Floor", screenWidth / 2 - row1Width / 2, rect.centerY() - textHeight * .2f, textPaint);
		canvas.drawText(startFloorText + " to " + endFloorText, screenWidth / 2	- row2Width / 2, rect.centerY() + textHeight * 1.2f, textPaint);
	}

	private void drawIcons(Canvas canvas) {
		for (int i = 0; i < icons.size(); i++) {
			Point point = new Point();
			projection.toPixels(icons.get(i).geoPoint, point);
			RectF rect = new RectF(point.x - 20, point.y - 20, point.x + 20, point.y + 20);

			if (singleGeoPoint == null && curDir != icons.get(i).dirIndex) {
				paint.setAlpha(150);
				canvas.drawBitmap(icons.get(i).bitmap, null, rect, paint);
				paint.setAlpha(255);
			} else
				canvas.drawBitmap(icons.get(i).bitmap, null, rect, paint);
		}
	}

	private void drawDirections(Canvas canvas) {
		paint.setShader(null);
		for (int i = 0; i < directions.length; i++) {
			Point[] dirPixelPoints = pixelPoints[i];
			Path path = new Path();
			path.setFillType(FillType.WINDING);
			path.moveTo(dirPixelPoints[0].x, dirPixelPoints[0].y);
			for (int j = 1; j < dirPixelPoints.length; j++)
				path.lineTo(dirPixelPoints[j].x, dirPixelPoints[j].y);

			if (directions[i].isOutside(app.campus) && i != curDir) {
				canvas.drawPath(path, paint);
			} else if (!directions[i].isOutside(app.campus) && i != curDir) {
				setDashPathInnerPaint(i);
				canvas.drawPath(path, insidePaint);
			}
		}
	}

	private void updateBitmaps() {
		if (blueprintBitmap != null)
			blueprintBitmap.recycle();

		if (!app.campus.buildingIsOutside(building)) {
			String path;
			try {
				path = "blueprints/"
						+ app.campus.getFloorFile(building, floor, "png");
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPreferredConfig = Bitmap.Config.RGB_565;
				blueprintBitmap = BitmapFactory.decodeStream(Campus.fileHandler.getInputStream(path), null,	options);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// TODO this function is called too often, only call when google maps moves
	private void applyProjection() {
		if (mode != BLANK && !app.campus.buildingIsOutside(building)) {
			Point topLeft = new Point();
			Point botRight = new Point();
			projection.toPixels(DrawingHelpers.convertPointToGeo(app.campus.getFloor(building, floor).northWest), topLeft);
			projection.toPixels(DrawingHelpers.convertPointToGeo(app.campus.getFloor(building, floor).southEast), botRight);
			destination = new RectF(topLeft.x, topLeft.y, botRight.x, botRight.y);
		}

		if (mode == ROUTE) {
			for (int i = 0; i < directions.length; i++) {
				if (i == curDir) {
					GeoPoint[] curGeoPoints = geoPoints[i];
					Point[] curPixelPoints = pixelPoints[i];
					for (int j = 0; j < curGeoPoints.length; j++) {
						if (curPixelPoints[j] == null)
							curPixelPoints[j] = new Point();
						projection.toPixels(curGeoPoints[j], curPixelPoints[j]);
					}
				} else {
					GeoPoint[] dirGeoPoints = geoPoints[i];
					Point[] dirPixelsPoints = pixelPoints[i];
					for (int j = 0; j < dirGeoPoints.length; j++) {
						if (dirPixelsPoints[j] == null)
							dirPixelsPoints[j] = new Point();
						projection.toPixels(dirGeoPoints[j], dirPixelsPoints[j]);
					}
				}
			}
		} else if (mode == POINT) {
			if (singlePixelPoint == null)
				singlePixelPoint = new Point();
			projection.toPixels(singleGeoPoint, singlePixelPoint);
		}
	}

	public void setFloor(int curDir) {
		this.curDir = curDir;
		this.floor = directions[curDir].getFloor();
		this.building = directions[curDir].getBuilding();

		updateBitmaps();
	}

	public void setPoint(Waypoint2D waypoint, int icon) {
		this.singleGeoPoint = DrawingHelpers.convertPointToGeo(waypoint.getPoint());
		this.building = waypoint.getId().getBuildingIndex();
		this.floor = waypoint.getId().getFloorIndex();

		Context context = main;

		icons = new ArrayList<Icon>();
		Icon newIcon = new Icon();
		newIcon.bitmap = BitmapFactory.decodeResource(context.getResources(), icon);
		newIcon.geoPoint = singleGeoPoint;
		icons.add(newIcon);

		updateBitmaps();
	}

	public void recycleBitmap() {
		if (blueprintBitmap != null)
			blueprintBitmap.recycle();
		System.gc();
	}
}