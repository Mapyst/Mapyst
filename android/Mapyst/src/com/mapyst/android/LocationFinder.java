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

import com.mapyst.route.InterpretedInfo;
import com.mapyst.route.Interpreter;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class LocationFinder {

	private LocationManager mlocManager;
	private LocationListener mlocListenerGps;
	private LocationListener mlocListenerNetwork;
	private Location currentGpsLoc, currentNetworkLoc;

	private Mapyst app;

	public LocationFinder(Mapyst app) {
		this.app = app;
	}

	public void setupLocService() {
		mlocManager = (LocationManager) app
				.getSystemService(Context.LOCATION_SERVICE);
		// currentNetworkLoc =
		// mlocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		mlocListenerNetwork = new LocationListener() {
			@Override
			public void onLocationChanged(Location loc) {
				currentNetworkLoc = loc;
			}

			@Override
			public void onProviderDisabled(String provider) {
				// Toast.makeText(app, "Current Network Location Disabled",
				// Toast.LENGTH_LONG);
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
			}
		};
		mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mlocListenerNetwork);

		mlocListenerGps = new LocationListener() {
			@Override
			public void onLocationChanged(Location loc) {
				currentGpsLoc = loc;
			}

			@Override
			public void onProviderDisabled(String provider) {
				Toast.makeText(app, "Current GPS Location Disabled", Toast.LENGTH_LONG).show();
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}
		};
		mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListenerGps);
	}

	public void endLocService() {
		if (mlocManager != null) {
			mlocManager.removeUpdates(mlocListenerGps);
			mlocManager.removeUpdates(mlocListenerNetwork);
		}
	}

	public InterpretedInfo getCurrentLocation() {
		if (currentNetworkLoc == null && currentGpsLoc == null) {
			InterpretedInfo failure = new InterpretedInfo();// known to be
															// unsuccessful
															// because it has no
															// suggestions

			Toast.makeText(app,	"Sorry, we could not find your current location.", Toast.LENGTH_SHORT).show();
			return failure;
		}
		Location loc = currentGpsLoc;
		if (loc == null)
			loc = currentNetworkLoc;

		Interpreter interpreter = new Interpreter(app.campus);
		InterpretedInfo info = interpreter.interpretLatLng((int) (loc.getLatitude() * 1E6),	(int) (loc.getLongitude() * 1E6));

		return info;
	}
}
