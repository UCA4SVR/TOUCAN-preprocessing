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

public class Version {
	
	private int bitrate; //Video bitrate (can be -1 if not specified)
	private int width; //Video width (can be -1 if not specified)
	private int height; //Video height (can be -1 if not specified)
	
	public Version(int width, int height, int bitrate) {
		this.width = width;
		this.height = height;
		this.bitrate = bitrate;
	}
	
	/**
	 * Generates the scale filter description according to which the encoding must be performed
	 * @return String containing the scale filter description to properly encode the video
	 */
	public String generateCommandPortion() { 
		String commandPortion = "";
		if(bitrate>0)
			commandPortion += " -b:v "+bitrate+"k -bufsize "+bitrate+"k ";
		if(width>0 && height>0)
			commandPortion += " -vf scale="+width+":"+height+" ";
		return commandPortion;
	}
	
	/**
	 * Creates a suffix to be added to the file according to the encoding version
	 * @return String with the suffix
	 */
	public String generateFileSuffix() { 
		String fileSuffix = "";
		if(bitrate>0)
			fileSuffix += "-"+bitrate+"K";
		if(width>0 && height>0)
			fileSuffix += "-"+width+"x"+height;
		return fileSuffix;
	}
	
	/**
	 * Creates a log message according to the encoding version
	 * @return String with the message related to the encoding version
	 */
	public String generateLog() { 
		String log = "\tVersion: ";
		if(bitrate>0)
			log += "Bitrate: "+bitrate+"K ";
		if(width>0 && height>0)
			log += "Resolution: "+width+"x"+height;
		return log;
	}
	
	/**
	 * Generate a TranscodedVersion object used for the tiling phase.
	 * @param fileBasename Name of the file without the extension
	 * @param originalWidth Video original width
	 * @param originalHeight Video original height
	 * @return TranscodedVersion object
	 */
	public TranscodedVersion generateTranscoded(String fileBasename, int originalWidth, int originalHeight) { 
		if(width>0 && height>0)
			return new TranscodedVersion(fileBasename,this.width,this.height);
		else
			return new TranscodedVersion(fileBasename,originalWidth,originalHeight);
	}
}
