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

import com.mapyst.android.CampusLoader;
import com.mapyst.android.MainScreen;
import com.mapyst.android.Mapyst;

public class CampusLoaderTaskPrefs {
	public final int campus_id;
	public final Mapyst app;
	public final MainScreen main;
	public final CampusLoader loaderContext;

	public CampusLoaderTaskPrefs(int campus_id, Mapyst app, MainScreen main, CampusLoader loaderContext) {
		this.campus_id = campus_id;
		this.app = app;
		this.main = main;
		this.loaderContext = loaderContext;
	}
}
