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

import com.mapyst.route.Direction;
import com.mapyst.android.Images;
import com.mapyst.android.MainScreen;
import com.mapyst.android.Mapyst;
import com.mapyst.android.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class DirectionIcon extends FrameLayout {

	private Direction[] directions;

	private int position;

	private BitmapFactory.Options opts;
	private static Bitmap academic;
	private static Bitmap outside;
	private static Bitmap upStairs;
	private static Bitmap downStairs;
	private static Bitmap upElevators;
	private static Bitmap downElevators;

	private Context context;
	private Mapyst app;

	private TextView textView;
	private ImageView imageView;

	public DirectionIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, null);
	}

	public DirectionIcon(Context context) {
		super(context);
		init(context, null);
	}

	public DirectionIcon(Context context, Mapyst app, MainScreen mainScreen, int position, Direction[] directions) {
		super(context);
		init(context, app);
		setDirection(app, mainScreen, position, directions);
	}

	public void init(Context context, Mapyst app) {
		this.context = context;
		this.app = app;

		imageView = new ImageView(context);
		this.addView(imageView);

		textView = new TextView(context);
		this.addView(textView);

		opts = new BitmapFactory.Options();
		opts.inPreferredConfig = Bitmap.Config.RGB_565;
		if (outside == null)
			outside = BitmapFactory.decodeResource(getResources(), R.drawable.outside, opts);
		if (academic == null)
			academic = BitmapFactory.decodeResource(getResources(), R.drawable.buildings, opts);
		if (upStairs == null)
			upStairs = BitmapFactory.decodeResource(getResources(), Images.Icons.UP_STAIRS_H, opts);
		if (downStairs == null)
			downStairs = BitmapFactory.decodeResource(getResources(), Images.Icons.DOWN_STAIRS_H, opts);
		if (upElevators == null)
			upElevators = BitmapFactory.decodeResource(getResources(), Images.Icons.UP_ELEVATOR_H, opts);
		if (downElevators == null)
			downElevators = BitmapFactory.decodeResource(getResources(), Images.Icons.DOWN_ELEVATOR_H, opts);
	}

	public void setDirection(Mapyst app, final MainScreen mainScreen, int position, Direction[] directions) {
		this.app = app;
		this.position = position;
		this.directions = directions;

		imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mainScreen.moveToCurrentDirection();
			}
		});

		Resources resources = getResources();
		float dipValue = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90, resources.getDisplayMetrics());

		FrameLayout.LayoutParams layout = new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, (int) dipValue);
		imageView.setLayoutParams(layout);

		layout = new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL	| Gravity.CENTER_VERTICAL);
		textView.setBackgroundColor(Color.TRANSPARENT);
		textView.setTextColor(Color.WHITE);
		textView.setTextSize(18);
		textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		textView.setLayoutParams(layout);

		setImage();
		setText();
	}

	public void setImage() {

		int selected = -1;
		selected = ((MainScreen) context).getSelected();

		Bitmap img = null;

		if (position == selected) {

			if (directions[position].getType() == Direction.STAIRS) {
				if (Icon.getDirection(directions[position]) == Icon.UP) {
					img = upStairs;
				} else {
					img = downStairs;
				}
			} else if (directions[position].getType() == Direction.ELEVATOR) {
				// determine if elevators go up or down
				if (Icon.getDirection(directions[position]) == Icon.UP) {
					img = upElevators;
				} else {
					img = downElevators;
				}
			} else if (directions[position].getType() == Direction.RAMP) {
				// determine if elevators go up or down
				if (Icon.getDirection(directions[position]) == Icon.UP) {
					img = BitmapFactory.decodeResource(getResources(),
							Images.Icons.UP_RAMP, opts);
				} else {
					img = BitmapFactory.decodeResource(getResources(),
							Images.Icons.DOWN_RAMP, opts);
				}
			} else if (directions != null && !directions[position].isOutside(app.campus)) {
				img = academic;
			} else if (directions != null && directions[position].isOutside(app.campus)) {
				img = outside;
			}
		}
		imageView.setImageBitmap(img);
	}

	public void setText() {
		int buildingIndex = directions[position].getBuilding();
		int floorIndex;
		// if directions exist and its on the same floor and current direction
		// is not outside

		String str = "";

		if ((directions[position].getType() == Direction.SAME_FLOOR && !app.campus
				.buildingIsOutside(buildingIndex))) {
			floorIndex = directions[position].getFloor();
			String buildingText = app.campus.buildings[buildingIndex].acronym;
			String floorText = app.campus.getFloor(buildingIndex, floorIndex).name;
			str = buildingText + " " + floorText;
		}
		textView.setText(str);
	}

}
