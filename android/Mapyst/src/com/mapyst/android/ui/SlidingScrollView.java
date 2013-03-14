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
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class SlidingScrollView extends HorizontalScrollView implements
		OnTouchListener {

	private int activeView = 1;

	private int mainView = 1;

	private LinearLayout linearLayout;

	private boolean isRefreshing = false;

	private ActiveViewChangeListener onActiveViewChangeListener;

	public void setOnActiveViewChangeListener(ActiveViewChangeListener onActiveViewChangeListener) {
		this.onActiveViewChangeListener = onActiveViewChangeListener;
	}

	public interface ActiveViewChangeListener {
		public void activeSet(int active, View activeV);
	}

	public SlidingScrollView(Context context) {
		super(context);
		init(context);
	}

	public SlidingScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);

	}

	private boolean locked = true;

	public void lock() {
		this.locked = true;
		this.invalidate();
	}

	public void unlock() {
		this.locked = false;
		this.invalidate();
	}

	public boolean isLocked() {
		return locked;
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		if (locked)
			return true;
		else
			return super.onTouchEvent(e);
	}

	public void init(Context context) {
		this.setFadingEdgeLength(0);
		this.setHorizontalScrollBarEnabled(false);
		this.setWillNotDraw(false);
		this.setOnTouchListener(this);

		linearLayout = new LinearLayout(context);
		linearLayout.setOrientation(LinearLayout.HORIZONTAL);
		super.addView(linearLayout);
		refreshActiveView();

	}

	@Override
	public void addView(View v) {
		linearLayout.addView(v, linearLayout.getChildCount());
		viewAdded();
	}

	@Override
	public void addView(View child, ViewGroup.LayoutParams params) {
		linearLayout.addView(child, linearLayout.getChildCount(), params);
		viewAdded();
	}

	private void viewAdded() {
		ensureAllChildrenLargeEnough();
		refreshActiveView();
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		refreshActiveView();
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public void onDraw(Canvas canvas) {
		refreshActiveView();
		super.onDraw(canvas);
	}

	public void setActiveView(View child) {
		setActiveView(linearLayout.indexOfChild(child));
	}

	public void setActiveView(int i) {
		activeView = i;
		refreshActiveView();
		if (onActiveViewChangeListener != null)
			onActiveViewChangeListener.activeSet(activeView, linearLayout.getChildAt(activeView));
	}

	public int getIndex(View view) {
		return linearLayout.indexOfChild(view);
	}

	public int getActiveView() {
		return activeView;
	}

	public void ensureAllChildrenLargeEnough() {
		for (int i = 0; i < linearLayout.getChildCount(); i++) {
			View child = linearLayout.getChildAt(i);
			ensureLargeEnough(child);
		}
	}

	public void ensureActiveLargeEnough() {
		View child = linearLayout.getChildAt(activeView);
		ensureLargeEnough(child);
	}

	public void ensureLargeEnough(View child) {
		if (linearLayout.indexOfChild(child) == mainView
				&& !(child.getLayoutParams() != null && child.getLayoutParams().width == this.getWidth())) {
			LinearLayout.LayoutParams layout = (LinearLayout.LayoutParams) child.getLayoutParams();
			if (layout == null)
				layout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
			layout.width = this.getWidth();
			child.setLayoutParams(layout);
		} else if (child != null && child.getWidth() < this.getWidth() * 2 / 3) {
			LinearLayout.LayoutParams layout = (LinearLayout.LayoutParams) child.getLayoutParams();
			if (layout == null)
				layout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
			layout.width = this.getWidth() * 2 / 3;
			child.setLayoutParams(layout);
		}
	}

	public void refreshActiveView() {
		if (isRefreshing)
			return;
		isRefreshing = true;
		ensureAllChildrenLargeEnough();
		int x = 0;
		for (int i = 0; i < linearLayout.getChildCount() - 1; i++) {
			View child = linearLayout.getChildAt(i);
			if (i == activeView) {
				break;
			}
			x += child.getWidth();
		}
		if (x < 0)
			x = 0;
		if (this.getScrollX() != x) {
			mSmoothScrollTo(x, 0);
		}
		isRefreshing = false;
	}

	private long mLastScroll;

	public void mSmoothScrollTo(final int x, final int y) {
		if (SystemClock.uptimeMillis() < mLastScroll)
			return;
		float cx = this.getScrollX();
		float cy = this.getScrollY();

		int rateOfChange = 45;

		float dx = (x - cx) / rateOfChange;
		float dy = (y - cy) / rateOfChange;

		int delay = 0;

		Handler handler = new Handler();

		while (!equalWithinEpsilon(cx, x, dx * 2)
				|| !equalWithinEpsilon(cy, y, dy * 2)) {
			cx += dx;
			cy += dy;
			final int fcx = (int) cx;
			final int fcy = (int) cy;
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					SlidingScrollView.this.scrollTo(fcx, fcy);
					invalidate();
				}
			}, delay);
			delay += 5;
			if (delay > 1000) // just so we don't get stuck in an infinite loop
				break;
		}
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				SlidingScrollView.this.scrollTo(x, y);
				invalidate();
			}
		}, delay);
		mLastScroll = SystemClock.uptimeMillis() + delay;
	}

	private boolean equalWithinEpsilon(double a, double b, double epsilon) {
		return Math.abs(a - b) <= Math.abs(epsilon);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int chosenOne = -1;
		for (int i = 0; i < linearLayout.getChildCount(); i++) {
			Rect rect = new Rect();
			linearLayout.getChildAt(i).getLocalVisibleRect(rect);
			int[] loc = new int[2];
			linearLayout.getChildAt(i).getLocationInWindow(loc);
			rect.offsetTo(loc[0], loc[1]);
			if (rect.contains((int) event.getX(), (int) event.getY())) {
				chosenOne = i;
				break;
			}
		}
		if (chosenOne != -1 && activeView != chosenOne) {
			this.setActiveView(chosenOne);
			return true;
		}
		return false;
	}
}
