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

package com.mapyst.android;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class Compass implements SensorEventListener {
	// Evan

	// nicely implements a compass in its own thread, with some extra stuff
	// (like smoothing)

	public boolean debug = false;

	private double smoothRotDeg = 0; // smoothed out rotation (less jittery)
	private double rotDegVel = 0; // velocity for changing smooth rotation

	private SensorManager mSensorManager; // manages the sensors
	private Sensor accelerometer; // basically tells us which direction is down
									// (gravity)
	private Sensor magnetometer; // tells us the magnetic declination from the
									// phone's current orientation

	private float[] mGravity; // all the gravity data (accelerometer)
	private float[] mGeomagnetic; // all the magnetic data (magnetometer)

	// if it is needed, the current degree rotation can be manually requested
	public double getDegreeRotation() {
		return smoothRotDeg;
	}

	// this is where we'll send rotation data automatically
	private CompassUpdateListener compassUpdateListener;

	public Compass(Context context) {
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}

	public void setCompassUpdateListener(CompassUpdateListener compassUpdateListener) {
		this.compassUpdateListener = compassUpdateListener;
	}

	public void stop() {
		mSensorManager.unregisterListener(this);
		mSensorManager.unregisterListener(this);
	}

	public void start() {
		mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
	}

	// provides an interface so data can be sent in a multithread, event like
	// manner
	public interface CompassUpdateListener {
		public void compassUpdate(double compassRotDegs);
	}

	// turn our raw data into nice degree data, and send it to the automatic
	// listener
	public void updateCompass(double rotRad, double pitch, double roll) {
		if (rotRad < 0) // negative angles included in raw. This fixes that.
			rotRad = Math.PI * 2 + rotRad;

		final double rotDeg = (rotRad) / Math.PI * 180; // simple degree value
														// [0,...)

		double rotDegVelMod = 0; // value is used for smoothing.

		// the purpose of the next few lines is to get velocity in the
		// correct direction. For example, if we have smoothrot at 358 degs,
		// and rotDeg at 4 degs, we obviously want to increase smoothrot
		// with a positive velocity
		double r1 = rotDeg - smoothRotDeg;
		if (r1 < 0)
			r1 += 360;
		double r2 = smoothRotDeg - rotDeg;
		if (r2 < 0)
			r2 += 360;
		if (r1 < r2) {
			rotDegVelMod = r1;
		} else {
			rotDegVelMod = -r2;
		}

		// divV effects the influence of the velocity. a value of
		// 1 would mean we would just be setting smoothrotdeg to
		// rot deg. 4 means it adjusts slowly, cutting out some
		// noise.
		int divV = 4;

		rotDegVelMod /= divV;
		rotDegVel = rotDegVel / divV + rotDegVelMod;
		smoothRotDeg += rotDegVel;

		if (smoothRotDeg < 0) // just incase we're below 0, normalize
			smoothRotDeg += 360;
		smoothRotDeg %= 360; // in case we went over 360, go back to our normal
								// range

		if (debug)
			logCompassDebug(rotRad, rotDeg, pitch, roll); // debug info

		if (compassUpdateListener != null)
			compassUpdateListener.compassUpdate(smoothRotDeg); // send data in
																// an event like
																// manner to a
																// set location
																// to
																// be consumed

	}

	public void logCompassDebug(double rotRad, double rotDeg, double pitch,
			double roll) {
		final String str0 = "ROT RAD:\t\t\t\t\t" + (rotRad);
		final String str1 = "ROT DEG:\t\t\t\t\t" + rotDeg;
		final String str2 = "SMOOTH ROT DEG:\t" + smoothRotDeg;
		final String str3 = "PITCH:\t\t\t\t\t" + pitch;
		final String str4 = "ROLL:\t\t\t\t\t\t" + roll;
		String str = str0 + "\n" + str1 + "\n" + str2 + "\n" + str3 + "\n"
				+ str4 + "\n";
		str += "\n\n";
		Log.d("LOLOLOLOL: Compass Debug", str);
	}

	// this is what android calls to give us orientation stuff
	@Override
	public void onSensorChanged(SensorEvent event) {

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			if (event.values != null)
				mGravity = event.values.clone(); // clone for continuity
		}
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			if (event.values != null)
				mGeomagnetic = event.values.clone(); // clone for continuity
		}
		if (mGravity != null && mGeomagnetic != null) { // we got data!
			float R[] = new float[9]; // will store matrix data
			float I[] = new float[9]; // will store matrix data
			// following line uses knowledge of direction of gravity and
			// magnetic field to get the orientation data
			boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
					mGeomagnetic);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation); // pull useful
																// stuff from R
				double rotRad = 1.0 * orientation[0]; // azimut (rotation /
														// compass)
				double pitch = 1.0 * orientation[1]; // pitch
				double roll = 1.0 * orientation[2]; // roll
				// System.out.println(orientation[0] + " " + orientation[1] +
				// " " + orientation[2]);
				updateCompass(rotRad, pitch, roll);
			}
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
}
