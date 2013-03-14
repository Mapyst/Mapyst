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


var info;

function showSuggestions(newInfo) {
	info = newInfo;
	var suggestions = info.suggestions;

	//fills in suggestions popup window with a title indicating whether the suggestions are for the start or end
	//and fills it in with the list of suggestions
	var startOrEnd = "";
	if (newInfo.type == true) //true means that the input was the start input
		startOrEnd = "Start";
	else
		startOrEnd = "Destination";
	var html = "<li>" + startOrEnd + " Suggestions:</li>";
	for (var i = 0; i < suggestions.length; i++) {
		var s = info.suggestions[i];
		html += "<li>" + s.text + "</li>";
	}
	$("#suggestions-content").html(html);

	//sets the classes of the newly added html and adds an event handler for when a suggestion is clicked
	var items = $("#suggestions-content li");
	for (var j = 0; j < items.length; j++) {
		if (j == 0) {
			items[j].className = "suggestions_title";
		}
		else {
			items[j].className = "suggestion";
			items[j].onclick = clickedSuggestion;
		}
	}

	//positions and shows the suggestions window in the center of the document body
	var height = 300;
	var width = 300;
	$("#suggestions").css("top", Math.round((document.body.clientHeight / 2) - (height)) + "px");
	$("#suggestions").css("left", Math.round((document.body.clientWidth / 2) - (width / 2)) + "px");
	$("#suggestions").css("visibility", "visible");
}

//this event is fired when a suggestion is clicked
function clickedSuggestion(event) {
	//sets the appropriate text box's text to the selected suggestion's text
	var selectedText = event.target.innerHTML;
	if (info.type == true) { //true means that the input was the start input
		$("#header-inputs-start").val(selectedText);
	}
	else { //false means that the input was the end input
		$("#header-inputs-end").val(selectedText);
	}

	//hides the suggestions window
	$("#suggestions").css("visibility", "hidden");
}