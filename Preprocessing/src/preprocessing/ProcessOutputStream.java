/*
 * Copyright 2017 Laboratoire I3S, CNRS, Université côte d'azur
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package preprocessing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProcessOutputStream extends Thread {
	InputStream inputStream;
	
	ProcessOutputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	public void run() {
		try {
	    	InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	    	BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	    	String line=null;
	    	while ((line = bufferedReader.readLine()) != null) {
	        	System.out.println(line);
	    	}
		} catch (IOException ioe) {
	    	ioe.printStackTrace();  
	    }
	}
}