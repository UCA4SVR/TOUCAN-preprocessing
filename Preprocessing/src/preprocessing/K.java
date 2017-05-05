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

import java.util.HashMap;

/**
 * Class of constants.
 * It contains useful parameters used to parse the XML file and to crop videos.
 */

public abstract class K {
	
	//XML tags
	public static final String videoTag = "video";
	
	public static final String videoPathTag = "path";
	
	public static final String bigWTag = "W";
	public static final String bigHTag = "H";
	
	public static final String versionTag = "version";
	public static final String versionWidthTag = "width";
	public static final String versionHeightTag = "height";
	public static final String versionBitrateTag = "bitrate";
	
	public static final String tileDefaultTag = "defaultTiling";
	
	public static final String tileTag = "tile";
	public static final String tileStartXPercTag = "startXPerc";
	public static final String tileStartYPercTag = "startYPerc";
	public static final String tileEndXPercTag = "endXPerc";
	public static final String tileEndYPercTag = "endYPerc";
	public static final String tileXTag = "x";
	public static final String tileYTag = "y";
	public static final String tileWTag = "w";
	public static final String tileHTag = "h";
	
	public static final String segmentTag = "segment";
	public static final int segmentDefault = 500;
	
	public static final String outputTag = "output";
	
	
	//Default objects for regular tiling
	//2x2
	public static final String[] twoByTwo = {
		"0,0,1,1,0,0,0.5,0.5",
		"1,0,1,1,0.5,0,1,0.5",
		"0,1,1,1,0,0.5,0.5,1",
		"1,1,1,1,0.5,0.5,1,1"
	};
	
	//3x3
	public static final String[] threeByThree = {
		"0,0,1,1,0,0,0.33,0.33",
		"1,0,1,1,0.33,0,0.66,0.33",
		"2,0,1,1,0.66,0,1,0.33",
		"0,1,1,1,0,0.33,0.33,0.66",
		"1,1,1,1,0.33,0.33,0.66,0.66",
		"2,1,1,1,0.66,0.33,1,0.66",
		"0,2,1,1,0,0.66,0.33,1",
		"1,2,1,1,0.33,0.66,0.66,1",
		"2,2,1,1,0.66,0.66,1,1"
	};
	
	public static final HashMap<String,String[]> defaultTiling = new HashMap<String,String[]>();
	
	static {
		defaultTiling.put("twoByTwo",twoByTwo);
		defaultTiling.put("threeByThree",threeByThree);
	}
}
