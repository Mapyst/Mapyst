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

package com.mapyst.route;

public class RoutePreferences {
	
	public final class Preferences {
		public static final int OUTSIDE = 1;
		public static final int INSIDE = 2;
		public static final int HANDICAPPED = 4;
		public static final int NO_ELEVATORS = 8;
	}
	
	public boolean elevators, stairs, hand, inside, outside;
	
	public RoutePreferences(boolean elevators, boolean stairs, boolean hand, boolean inside, boolean outside) {
		this.stairs = stairs;
		this.elevators = elevators;
		this.hand = hand;
		this.inside = inside;
		this.outside = outside;
	}
}
