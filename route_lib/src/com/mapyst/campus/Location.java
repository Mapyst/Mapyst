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

package com.mapyst.campus;

import com.mapyst.route.WaypointID;

public class Location {
	public String[] names; //all possible names the location goes by
	public WaypointID[] waypoints;
	public String description;
	
	//hours must be formatted properly, see examples:
	//example: MT:8:30am-5pm WF:8:30am-5pm Sa:10am-5pm
	//example: MTWThF:8am-8pm
	//example: MTWThF:6am-Midnight, Midnight-2am SaS:9am-Midnight, Midnight-2am
	//example: MTWTh:24 Hours F:Closes 9pm Sa:10am-5pm S:Opens Noon
	public String hours = "";
	
	//determines whether to cut a route into separate directions whether a route
	//passes through this location
	public boolean is_direction_end;
	
	
}
