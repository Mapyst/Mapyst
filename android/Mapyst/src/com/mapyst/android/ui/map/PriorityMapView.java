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

package com.mapyst.android.ui.map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.maps.MapView;

public class PriorityMapView extends MapView {

	public boolean active = true;

	public PriorityMapView(Context arg0, String arg1) {
		super(arg0, arg1);
	}

	public PriorityMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PriorityMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!active)
			return false;
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			this.getParent().requestDisallowInterceptTouchEvent(true);
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			this.getParent().requestDisallowInterceptTouchEvent(false);
		}
		super.onTouchEvent(event);
		return true;
	}

}
