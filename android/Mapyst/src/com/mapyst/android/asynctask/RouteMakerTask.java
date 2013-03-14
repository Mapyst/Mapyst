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

package com.mapyst.android.asynctask;

import android.os.AsyncTask;

public class RouteMakerTask extends	AsyncTask<RouteMakerTaskPrefs, Integer, RouteMakerTaskPrefs> {

	@Override
	protected void onPreExecute() { // This runs on the UI thread
		return;
	}

	// This runs in the background
	@Override
	protected RouteMakerTaskPrefs doInBackground(RouteMakerTaskPrefs... prefs) { 
		for (RouteMakerTaskPrefs pref : prefs) {
			pref.app.route = pref.app.getRouteFinder().makeRoute(pref.startResult, pref.endResult, pref.routePrefs);
		}

		return prefs[0];
	}

	@Override
	protected void onProgressUpdate(Integer... progress) { // Called from background thread to UI thread
		return;
	}

	@Override
	protected void onPostExecute(RouteMakerTaskPrefs result) { // Called UI thread
		result.loaderContext.displayRoute();
		return;
	}

}
