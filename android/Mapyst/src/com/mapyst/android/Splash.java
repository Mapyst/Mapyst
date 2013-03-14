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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Splash extends Activity {
	private static final int SPLASH_DISPLAY_TIME = 500; // splash screen delay time

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {

				Intent intent = new Intent(Splash.this, MainScreen.class);

				Splash.this.startActivity(intent);
				Splash.this.finish();
				// transition from splash to next screen
				overridePendingTransition(R.anim.mainfadein, R.anim.splashfadeout);

			}
		}, SPLASH_DISPLAY_TIME);
	}
}
