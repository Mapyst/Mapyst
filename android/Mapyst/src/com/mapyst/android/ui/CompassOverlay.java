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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.mapyst.android.Compass;

public class CompassOverlay extends Overlay {
	// Evan

	// this class manages the UI for the compass

    private float imageRotDeg = 0; // current compass data. Updated when compass calls our custom listener

	private Path compassPath; // stores the compass path data. Fractional [-1,1] so must be scaled on use
	private Paint compassPaint; // paint for compass
	private Paint circlePaintOuter; // paint for ring around circle
	private Paint circlePaintInner; // paint for inside of circle (provides contrast)

	private Compass compass;
	private MapView mapView;

	private boolean drawing = false;

	public CompassOverlay(Context context, MapView mapView, Compass compass) {
		this.mapView = mapView;
		this.compass = compass;
		//set the listener so we can get updates in a multithreaded manner
		compass.setCompassUpdateListener(new OnCompassUpdateListener());

		compassPath = new Path();
		compassPath.moveTo(0, -5 / 6.f); // front point
		compassPath.lineTo(-2 / 6.f, 5 / 6.f);
		compassPath.lineTo(0, 4 / 6.f); // middle indented point
		compassPath.lineTo(2 / 6.f, 5 / 6.f);
		compassPath.close();

		compassPaint = new Paint();
		compassPaint.setAntiAlias(true);
		compassPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		compassPaint.setPathEffect(new CornerPathEffect(5.0f));
        int blue = Color.rgb(72, 196, 255);
        compassPaint.setColor(blue);

		circlePaintOuter = new Paint();
		circlePaintOuter.setAntiAlias(true);
		circlePaintOuter.setStrokeWidth(2.0f);
		circlePaintOuter.setStyle(Style.STROKE);
		circlePaintOuter.setPathEffect(new CornerPathEffect(5.0f));

		circlePaintInner = new Paint();
		circlePaintInner.setAntiAlias(true);
		circlePaintInner.setStrokeWidth(1.0f);
		circlePaintInner.setStyle(Style.FILL);
        int semiTransparentWhite = Color.argb(170, 255, 255, 255);
        circlePaintInner.setColor(semiTransparentWhite);
		circlePaintInner.setPathEffect(new CornerPathEffect(5.0f));

	}

	// next 2 should be able to be called from the ui as the user prefers
	// stops drawing
	// for now, we'll have it also stop the compass
	public void disable() {
		compass.stop();
		drawing = false;
	}

	// starts drawing again
	// for now, we'll have this also start the compass
	public void enable() {
		compass.start();
		drawing = true;
	}

	// called whenever the compass class has an update (multithreading win)
	private class OnCompassUpdateListener implements
			Compass.CompassUpdateListener {
		@Override
		public void compassUpdate(double compassRotDegs) {
			imageRotDeg = (float) compassRotDegs;
			mapView.invalidate();
		}
	}

	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);

		if (drawing) {
			int compassSize = canvas.getWidth() / 6;

			canvas.save(); // we're going to want to restore later, so save the
							// state...
			canvas.translate(canvas.getWidth() - compassSize / 2 - 10,
					compassSize / 2 + 10); // move the center of the canvas
			canvas.rotate(imageRotDeg); // rotate the canvas around the new center

			Matrix matrix = new Matrix();
			matrix.postScale(compassSize / 2, compassSize / 2); // compass path is stored fractionally. This scales it up to normal

			Path mPath2 = new Path(); // temporary path for storing the compass at the correct size
			compassPath.transform(matrix, mPath2); // we apply the matrix, so the compass is the right size
			canvas.drawCircle(0, 0, compassSize / 2, circlePaintInner); // first, draw the inside (now a semi transparent white for contrast)
			canvas.drawCircle(0, 0, compassSize / 2, circlePaintOuter); // then, draw the outside (a black circle around the area)
			canvas.drawPath(mPath2, compassPaint); // finally, draw the compass arrow on top of everything

			canvas.restore(); // restore back to normal so if the canvas is needed again it will be usable
		}
	}
}
