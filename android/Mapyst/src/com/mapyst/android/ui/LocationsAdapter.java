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

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LocationsAdapter extends BaseAdapter {
	private ArrayList<String> locations, locsHours;
	private Context context;

	double openTime, closeTime;
	int hoursLeft;
	int minutesLeft;

	String timeLeft;

	public LocationsAdapter(Context context, ArrayList<String> locations, ArrayList<String> locsHours) {
		this.context = context;
		this.locations = locations;
		this.locsHours = locsHours;
	}

	public int getCount() {
		return locations.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		RelativeLayout row = new RelativeLayout(context);

		TextView name = new TextView(context);
		name.setText(locations.get(position));
		name.setTextSize(16);
		name.setGravity(Gravity.LEFT);
		name.setTextColor(Color.BLACK);

		TextView open = new TextView(context);
		open.setPadding(10, 12, 12, 10);
		open.setTextSize(14);
		open.setText("");

		if (locsHours != null && !locsHours.get(position).equals("")) {

			Calendar c = Calendar.getInstance();
			int day = c.get(Calendar.DAY_OF_WEEK);
			double curHour = c.get(Calendar.HOUR_OF_DAY) + (c.get(Calendar.MINUTE) / 60.0);
			String hours = locsHours.get(position);
			if (open(hours, day, curHour)) {
				timeLeft = "" + (closeTime - curHour);
				timeLeft();
				if (hoursLeft == 0 && !open(hours, day + 1, 0.01)) {
					open.setText(Html.fromHtml("<font color='green' size='14'> Open</font>"
									+ "<br /> <font color='gray' size='10'><i>"
									+ "(Closes in "
									+ minutesLeft
									+ " minutes"
									+ ")</i></font>"));
				} else
					open.setText(Html.fromHtml("<font color='green' size='14'> Open</font>"));
			} else {
				timeLeft = "" + (openTime - curHour);
				timeLeft();
				String[] temp = { hours };
				if (hoursLeft == 0 && openToday(c.get(Calendar.DAY_OF_WEEK), temp) != -1) {
					open.setText(Html.fromHtml("<font color='red' size='14'> Closed</font>"
									+ "<br />"
									+ "<font color='gray' size='8'><i> (Opens in "
									+ minutesLeft + " minutes" + ")</i></font>"));
				} else
					open.setText(Html.fromHtml("<font color='red' size='14'> Closed</font>"));
			}

		}

		if (open.getText().equals("")) {
			name.setPadding(10, 10, 10, 10);
		} else {
			name.setPadding(80, 10, 10, 10);
		}

		row.addView(name);
		row.addView(open);
		return row;
	}

	public void timeLeft() {
		hoursLeft = Integer.parseInt(timeLeft.substring(0, timeLeft.indexOf('.')));
		timeLeft = timeLeft + "0";
		timeLeft = timeLeft.substring(timeLeft.indexOf('.') + 1, timeLeft.indexOf('.') + 3);
		minutesLeft = Integer.parseInt(timeLeft);
		minutesLeft = (int) ((minutesLeft / 100.0) * 60.0);
	}

	public boolean open(String hours, int day, double curHour) {
		hours = hours.replace("Midnight-", "0am-");
		hours = hours.replace("Midnight", "12pm");
		hours = hours.replace("Noon", "12am");
		hours = hours.replace("24 Hours", "0am-12pm");
		hours = hours.replace("Closes ", "0am-");
		if (hours.contains("Opens")) {
			int indexOfOpens = hours.indexOf("Opens");
			int end = hours.indexOf("m", indexOfOpens);
			hours = hours.substring(0, end + 1) + "-12pm" + hours.substring(end + 1, hours.length());
			hours = hours.replace("Opens ", "");
		}
		hours = hours.replace(", ", ",");

		String[] temp = { hours };
		int start = openToday(day, temp);
		hours = temp[0];
		if (start == -1)
			return false;

		int colon = hours.indexOf(":", start);
		int end = hours.indexOf(" ", start);
		if (end == -1)
			end = hours.length();
		String times = hours.substring(colon + 1, end);
		String[] hourPeriods = times.split(",");

        for (String hourPeriod : hourPeriods) {
            if (checkPeriod(hourPeriod, curHour))
                return true;
        }
		return false;
	}

	private int openToday(int day, String[] hours) {
		if (day == Calendar.MONDAY) {
			if (hours[0].contains("M")) {
				return hours[0].indexOf("M");
			}
		} else if (day == Calendar.THURSDAY) {
			if (hours[0].contains("Th")) {
				return hours[0].indexOf("Th");
			}
		} else if (day == Calendar.TUESDAY) {
			hours[0] = hours[0].replace("Th", "");
			if (hours[0].contains("T")) {
				return hours[0].indexOf("T");
			}
		} else if (day == Calendar.WEDNESDAY) {
			if (hours[0].contains("W")) {
				return hours[0].indexOf("W");
			}
		} else if (day == Calendar.FRIDAY) {
			if (hours[0].contains("F")) {
				return hours[0].indexOf("F");
			}
		} else if (day == Calendar.SATURDAY) {
			if (hours[0].contains("Sa")) {
				return hours[0].indexOf("Sa");
			}
		} else if (day == Calendar.SUNDAY) {
			hours[0] = hours[0].replace("Sa", "");
			if (hours[0].contains("S")) {
				return hours[0].indexOf("S");
			}
		}
		return -1;
	}

	public boolean checkPeriod(String period, double curHour) {
		int dash = period.indexOf("-");
		String hour = period.substring(0, dash);
		double openTime = getHour(hour);

		hour = period.substring(dash + 1, period.length());
		double closeTime = getHour(hour);

		this.openTime = openTime;
		this.closeTime = closeTime;
		if (curHour >= openTime && curHour < closeTime) {
			return true;
		}
		return false;
	}

	public double getHour(String hour) {
		int time;
		double min = 0;
		if (hour.contains(":30")) {
			hour = hour.replace(":30", "");
			min = 0.5;
		}
		if (hour.contains("am")) {
			hour = hour.replace("am", "");
			time = Integer.parseInt(hour);
		} else {
			hour = hour.replace("pm", "");
			time = Integer.parseInt(hour) + 12;
		}
		return time + min;
	}
}
