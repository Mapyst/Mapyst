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

package com.mapyst.android;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.mapyst.FileHandlerInterface;

public class AndroidFileHandler implements FileHandlerInterface {

	private int campusId = -1;
	private Mapyst app;

	public AndroidFileHandler(Mapyst app) {
		this.app = app;
	}

	@Override
	public int getCampusId() {
		return campusId;
	}

	@Override
	public void setCampusId(int campusId) {
		this.campusId = campusId;
	}

	@Override
	public InputStream getInputStream(String path) throws IOException {
		InputStream inputStream = app.getAssets().open(path);
		return inputStream;
	}

	@Override
	public OutputStream getOutputStream(String path)
			throws FileNotFoundException {
		return null;
	}

	@Override
	public String readCampusFile() {
		return readFile(campusId + ".json");
	}

	@Override
	public String readFile(String fileName) {
		InputStreamReader inReader = null;
		try {
			inReader = new InputStreamReader(getInputStream(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(inReader);

		String text = "";
		try {
			String line = null;
			while ((line = br.readLine()) != null) {
				text += line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return text;
	}
}
