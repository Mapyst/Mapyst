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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapyst.android.R;

@SuppressLint("ViewConstructor")
public class LocationBalloon extends LinearLayout {

	private Paint rectPaint;
	private Paint arrowPaint;
	private Paint arrowOutlinePaint;
	private Paint rectOutlinePaint;

	private LocationButtonClickListener onButtonClickListener;

	public interface LocationButtonClickListener {
		public void startClicked();

		public void endClicked();

		public void cancelClicked();
	}

	public LocationBalloon(Context context, String location, LocationButtonClickListener onButtonClickListener) {
		super(context);
		init(context, location, onButtonClickListener);
	}

	public void init(Context context, String location, LocationButtonClickListener onButtonClickListener) {

		this.onButtonClickListener = onButtonClickListener;

		this.setOrientation(LinearLayout.VERTICAL);
		this.setWillNotDraw(false);
		this.setPadding(0, 0, 0, 20);

		rectPaint = new Paint();
		rectPaint.setStyle(Style.FILL);
		rectPaint.setColor(Color.argb(255, 0x66, 0x7F, 0xB2));

		arrowPaint = new Paint();
		arrowPaint.setColor(Color.argb(255, 0x32, 0x42, 0x63));

		rectOutlinePaint = new Paint();
		rectOutlinePaint.setColor(Color.argb(255, 0x32, 0x42, 0x63));

		rectOutlinePaint.setStyle(Style.STROKE);
		rectOutlinePaint.setStrokeWidth(10);

		arrowOutlinePaint = new Paint();
		arrowOutlinePaint.setColor(Color.BLACK);
		arrowOutlinePaint.setStyle(Paint.Style.STROKE);
		arrowOutlinePaint.setStrokeWidth(3);

		TextView textView = new TextView(context);
		textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		textView.setGravity(Gravity.CENTER);
		textView.setTextColor(Color.argb(255, 0xFF, 0xFF, 0xFF));
		textView.setText(location);
		textView.setPadding(10, 10, 10, 5);
		this.addView(textView);

		LinearLayout buttonLayout = new LinearLayout(context);
		buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
		buttonLayout.setGravity(Gravity.CENTER_HORIZONTAL);
		buttonLayout.setPadding(10, 0, 10, 10);

		Button startButton = new Button(context);
		startButton.setText("Set As Start");

		startButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.latlng_button_drawable));
		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LocationBalloon.this.onButtonClickListener.startClicked();
			}
		});
		buttonLayout.addView(startButton);

		Button endButton = new Button(context);
		endButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.latlng_button_drawable));
		endButton.setText("Set As End");
		endButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LocationBalloon.this.onButtonClickListener.endClicked();
			}
		});
		buttonLayout.addView(endButton);

		this.addView(buttonLayout);
	}

	@SuppressLint("DrawAllocation")
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// the arrow that points to where the geopoint is
		Path arrow = new Path();
		arrow.moveTo(this.getWidth() / 2, this.getHeight());
		arrow.lineTo(this.getWidth() / 2 - 20, this.getHeight() - 20);
		arrow.lineTo(this.getWidth() / 2 + 20, this.getHeight() - 20);
		arrow.lineTo(this.getWidth() / 2, this.getHeight());
		arrow.close();

		RectF r = new RectF(0, 0, this.getWidth(), this.getHeight() - 20);
		canvas.drawRoundRect(r, 10, 10, rectPaint);

		canvas.drawRoundRect(r, 10, 10, rectOutlinePaint);

		canvas.drawPath(arrow, arrowPaint);
	}
}
