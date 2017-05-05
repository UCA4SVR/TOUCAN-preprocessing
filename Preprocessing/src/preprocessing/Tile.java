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

public class Tile {
	
	private int x; //x position in the grid
	private int y; //y position in the grid
	private int w; //grid weight
	private int h; //grid height
	private float startXPerc; //x-Percentage of the original video where the tiling must begin
	private float startYPerc; //y-Percentage of the original video where the tiling must begin
	private float endXPerc; //x-Percentage of the original video where the tiling must end
	private float endYPerc; //y-Percentage of the original video where the tiling must end
	
	public Tile(int x, int y, int w, int h, float startXPerc, float startYPerc, float endXPerc, float endYPerc) { 
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.startXPerc = startXPerc;
		this.startYPerc = startYPerc;
		this.endXPerc = endXPerc;
		this.endYPerc = endYPerc;
	}
	
	//Used for default tiling: we are sure the provided string contains 8 values
	public Tile(String tileDescription) { 
		String[] pieces = tileDescription.split(",");
		this.x = Integer.parseInt(pieces[0]);
		this.y = Integer.parseInt(pieces[1]);
		this.w = Integer.parseInt(pieces[2]);
		this.h = Integer.parseInt(pieces[3]);
		this.startXPerc = Float.parseFloat(pieces[4]);
		this.startYPerc = Float.parseFloat(pieces[5]);
		this.endXPerc = Float.parseFloat(pieces[6]);
		this.endYPerc = Float.parseFloat(pieces[7]);
	}
	
	/**
	 * Generates the crop filter description according to which the tiling must be performed
	 * @param width of the video to be tiled
	 * @param height of the video to be tiled
	 * @return String containing the crop filter description to properly tile the video
	 */
	public String generateCommandPortion(int width, int height) {
		int startWpixel= (int) (width*startXPerc);
		int startHpixel= (int) (height*startYPerc);
		int endWpixel= (int) (width*endXPerc);
		int endHpixel= (int) (height*endYPerc);
		int areaWidth= endWpixel-startWpixel;
		int areaHeight= endHpixel-startHpixel;
		return " -filter:v crop="+areaWidth+":"+areaHeight+":"+startWpixel+":"+startHpixel+" ";
	}
	
	/**
	 * Creates a log message according to the tile position and size
	 * @return String with the message related to the tile position and size
	 */
	public String generateLog() { 
		return "\t\tTiling the tile with position ("+x+","+y+") and size ("+w+","+h+")";
	}
	
	/**
	 * Creates a suffix to be added to the file according to the tile position and size
	 * @return String with the suffix
	 */
	public String generateFileSuffix() { 
		return "-"+x+""+y+""+w+""+h;
	}
	
	/**
	 * Generates the SRD property of the tile according to the format x,y,w,h
	 * @return String with the SRD property
	 */
	public String generateSRDProperty() { 
		return x+","+y+","+w+","+h;
	}
}
