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

import com.mapyst.route.LatLngPoint;

public class Building {
	public String[] names; //all the names by which the building can be referred to (including acronyms besides the acronym field)
	public String acronym;
	public int type; //type of building (see constants below)
	public Floor[] floors;
	public LatLngPoint location; //center of the building
	
	public static final int ACADEMIC = 0;
	public static final int RESIDENCE = 1;
	public static final int ATHLETIC = 2;
	public static final int OTHER = 3;
	public static final int OUTSIDE = 4;
}
