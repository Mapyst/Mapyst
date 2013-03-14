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


//a jquery object for each of the preferences buttons
var elevators, stairs, hand, inside, outside;

function initPrefs(urlVars) {
	//initializes global variables
	elevators = $("#header-prefs-elevators");
	stairs = $("#header-prefs-stairs");
	hand = $("#header-prefs-hand");
	inside = $("#header-prefs-inside");
	outside = $("#header-prefs-outside");

	//the down variable is used to keep track of whether a button is toggled (down) or not toggled
	//initializes all to not toggled
	elevators[0].down = false;
	stairs[0].down = false;
	hand[0].down = false;
	inside[0].down = false;
	outside[0].down = false;

	//sets up event handlers
	elevators.click(elevators, toggleEvent);
	stairs.click(stairs, toggleEvent);
	hand.click(hand, toggleEvent);
	inside.click(inside, toggleEvent);
	outside.click(outside, toggleEvent);

	//sets the buttons' toggle states based on the parameters in the url
	if (urlVars['prefs']) {
		var prefs = urlVars['prefs'].split(',');
		if (prefs[0] && prefs[0] == 'true')
			toggle(elevators);
		else if (prefs[1] && prefs[1] == 'true')
			toggle(stairs);
		if (prefs[2] && prefs[2] == 'true')
			toggle(hand);
		if (prefs[3] && prefs[3] == 'true')
			toggle(inside);
		else if (prefs[4] && prefs[4] == 'true')
			toggle(outside);
	}
 }

//returns a common separated string representing the current state of the preferences
function getPrefs() {
	return elevators[0].down + "," + stairs[0].down + "," + hand[0].down + "," + inside[0].down + "," + outside[0].down;
}

//the event that is fired when a button is clicked
function toggleEvent(event) {
	toggle(event.data);
}

//toggles a button
function toggle(button) {
	if (button[0].down) {
		button[0].down = false;
		button.css("background-position", "0px 0px");
	}
	else {
		button[0].down = true;
		button.css("background-position", "0px -51px");

		//toggles other buttons as necessary
		//only one of elevators, stairs, handicapped can be pressed at a time
		//only one of inside, outside can be pressed at a time
		if (button == elevators) {
			resetButton(stairs);
			resetButton(hand);
		}
		else if (button == stairs) {
			resetButton(elevators);
			resetButton(hand);
		}
		else if (button == hand) {
			resetButton(elevators);
			resetButton(stairs);
		}
		else if (button == inside) {
			resetButton(outside);
		}
		else if (button == outside) {
			resetButton(inside);
		}
	}
}

//makes a button not toggled if it is toggled
function resetButton(button) {
	if (button[0].down) {
		button[0].down = false;
		button.css("background-position", "0px 0px");
	}
}