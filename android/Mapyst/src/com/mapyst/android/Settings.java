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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;

public class Settings extends PreferenceActivity {

	Boolean state = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		boolean showNotificationBar = prefs.getBoolean("notificationBox", true);
		if (!showNotificationBar) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.layout.settings);

		Preference notificationBox = (Preference) findPreference("notificationBox");
		notificationBox.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
						state = prefs.getBoolean("notificationBox", true);
						return true;
					}
				});

		Preference compassBox = (Preference) findPreference("compassBox");
		compassBox.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
						state = prefs.getBoolean("compassBox", false);
						return true;
					}
				});
	}
}