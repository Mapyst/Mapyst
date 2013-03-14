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

import java.util.ArrayList;

public class InterpretedInfo {
	
	//Constants
	public static final boolean START = true;
	public static final boolean END = false;
	
	//Values passed into Interpret
	private String originalInput;
	public boolean type;
	
	//Variables that Interpret uses and modifies
	private String input;
	public int buildingIndex;
	public int floorIndex;
	
	//The suggestions are the result of Interpret
	//	if there are 0 then interpret failed to find any matches
	//	if there are 1 then interpret found an exact match
	//	if there are more than 1 then interpret found multiple possible matches
	private ArrayList<InterpretResult> suggestions;
	
	public InterpretedInfo() {
		suggestions = new ArrayList<InterpretResult>();
		buildingIndex = -1;
		floorIndex = -1;
	}
	
	/* Used for recursive construction of interpreted infos */
	public InterpretedInfo(InterpretedInfo info, String updatedInput, int buildingIndex, int floorIndex) {
		this.input = updatedInput;
		this.originalInput = info.originalInput;
		this.type = info.type;
		this.buildingIndex = buildingIndex;
		this.floorIndex = floorIndex;
		suggestions = new ArrayList<InterpretResult>();
	}
	
	/* Used for recursive construction of interpreted infos */
	public InterpretedInfo(InterpretedInfo info) {
		this.originalInput = info.originalInput;
		this.type = info.type;
		suggestions = new ArrayList<InterpretResult>();
	}
	
	public String getInput() {
		return input;
	}
	
	public String getOriginalInput() {
		return originalInput;
	}
	
	public void setInput(String input) {
		this.input = input;
		if (originalInput == null)
			this.originalInput = input;
	}
	
	public boolean showDialog() {
		if (suggestions.size() > 1)
			return true;
		else
			return false;
	}
	
	public boolean successful() {
		if (suggestions.size() == 1)
			return true;
		else
			return false;
	}
	
	//should only be called if the info is successful
	public InterpretResult getResult() {
		if (suggestions == null || suggestions.size() == 0)
			return null;
		else
			return suggestions.get(0);
	}
	
	public ArrayList<InterpretResult> getAllResults() {
		//System.out.println(suggestions);
		if (suggestions == null || suggestions.size() == 0)
			return null;
		else
			return suggestions;
	}
	
	public void addSuggestion(InterpretResult addition) {
		boolean duplicateText = false;
		for (int i = 0; i < suggestions.size(); i++) {
			if (suggestions.get(i).getText().equals(addition.getText())) {
				suggestions.get(i).addWaypoints(addition.getWaypoints());
				duplicateText = true;
				//System.out.println("duplicate");
				break;
			}
		}
		if (!duplicateText)
			suggestions.add(addition);
	}
	
	public void addSuggestions(ArrayList<InterpretResult> additions) {
		suggestions.addAll(additions);
	}
	
	public ArrayList<InterpretResult> getSuggestions() {
		return suggestions;
	}
}