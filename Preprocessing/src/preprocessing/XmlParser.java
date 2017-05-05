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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;


public class XmlParser { 
	
	private String filename;
	
	public XmlParser(String filename) {
		this.filename = filename;
	}
	
	/**
	 * In this function the DOM of the XML file is obtained: if the file has errors
	 * in the XML structure an exception is thrown.
	 * If the DOM is correctly retrieved the DOM object is passed to the "parseVideo" method
	 * in order to perform a second parsing related to the file content.
	 * @return List of work to do containing versions and tiles desired
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public Worklist parseXml() throws ParserConfigurationException, SAXException, IOException  {
		File xmlFile = new File(filename);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		//Check if the XML is syntactically corrected
		Document doc = dBuilder.parse(xmlFile);
		Element videoElement = doc.getDocumentElement();
		videoElement.normalize();
		//Check if the XML is semantically corrected
		return parseVideo(videoElement);
	}
	
	/**
	 * Checks whether the root name has the proper tag value
	 * @param Video element to be parsed
	 * @throws SAXException
	 */
	private void parseRootNodeName(Element video) throws SAXException { 
		if(!(video.getNodeName().equals(K.videoTag))) { 
			throw new SAXException("The root XML tag must be '"+K.videoTag+"'!");
		}
	}
	
	/**
	 * Checks whether the video path has been specified just once
	 * @param Video element to be parsed
	 * @param Worklist in which the video path attribute will be set
	 * @throws SAXException
	 */
	private void parseVideoPath(Element video, Worklist worklist) throws SAXException { 
		NodeList paths = video.getElementsByTagName(K.videoPathTag);
		int pathLenght = paths.getLength();
		if(pathLenght==0) {
			//Attribute is mandatory
			throw new SAXException("The video path has not been specified in the '"+K.videoPathTag+"' tag!");
		} else if (pathLenght>1) {
			//Illegal length
			throw new SAXException("It is not possible to specify more than one path for a single video!");
		} else {
			//Add the path
			worklist.setVideoPath(paths.item(0).getTextContent());
		}
	}
	
	/**
	 * Checks whether the video width has been specified just once
	 * @param Video element to be parsed
	 * @param Worklist in which the video width attribute will be set
	 * @throws SAXException
	 */
	private void parseBigW(Element video, Worklist worklist) throws SAXException { 
		NodeList bigWs = video.getElementsByTagName(K.bigWTag);
		int bigWLenght = bigWs.getLength();
		if(bigWLenght==0) {
			//Attribute is mandatory
			throw new SAXException("The video W (big W) has not been specified in the '"+K.bigWTag+"' tag!");
		} else if (bigWLenght>1) {
			//Illegal length
			throw new SAXException("It is not possible to specify more than one W (big W) for a single video!");
		} else {
			//Add the width
			worklist.setBigW(Integer.parseInt(bigWs.item(0).getTextContent()));
		}
	}
	
	/**
	 * Checks whether the video height has been specified just once
	 * @param Video element to be parsed
	 * @param Worklist in which the video height attribute will be set
	 * @throws SAXException
	 */
	private void parseBigH(Element video, Worklist worklist) throws SAXException { 
		NodeList bigHs = video.getElementsByTagName(K.bigHTag);
		int bigHLenght = bigHs.getLength();
		if(bigHLenght==0) {
			//Attribute is mandatory
			throw new SAXException("The video H (big H) has not been specified in the '"+K.bigHTag+"' tag!");
		} else if (bigHLenght>1) {
			//Illegal length
			throw new SAXException("It is not possible to specify more than one H (big H) for a single video!");
		} else {
			//Add the height
			worklist.setBigH(Integer.parseInt(bigHs.item(0).getTextContent()));
		}
	}
	
	/**
	 * Checks whether the output folder has been specified just once
	 * @param Video element to be parsed
	 * @param Worklist in which the output-folder attribute will be set
	 * @throws SAXException
	 */
	private void parseOutput(Element video, Worklist worklist) throws SAXException { 
		NodeList outputs = video.getElementsByTagName(K.outputTag);
		int outputLenght = outputs.getLength();
		if(outputLenght==0) {
			//Attribute is mandatory
			throw new SAXException("The output folder has not been specified in the '"+K.outputTag+"' tag!");
		} else if (outputLenght>1) {
			//Illegal length
			throw new SAXException("It is not possible to specify more than one output folder for a single video!");
		} else {
			//Add the output folder
			worklist.setOutput(outputs.item(0).getTextContent());
		}
	}
	
	/**
	 * Checks whether the segment duration has been specified
	 * also ensuring that it is specified just once.
	 * If the segment duration hasn't been specified a default value is set.
	 * @param Video element to be parsed
	 * @param Worklist in which the segment length attribute will be set
	 * @throws SAXException
	 */
	private void parseSegment(Element video, Worklist worklist) throws SAXException { 
		NodeList segments = video.getElementsByTagName(K.segmentTag);
		int segmentLenght = segments.getLength();
		if(segmentLenght==0) {
			//Setting the default value
			worklist.setSegment(K.segmentDefault);
		} else if (segmentLenght>1) {
			//Illegal length
			throw new SAXException("It is not possible to specify more than one segment length for a single video!");
		} else {
			//Add the segment length
			worklist.setSegment(Integer.parseInt(segments.item(0).getTextContent()));
		}
	}
	
	/**
	 * If specified, version are added to the worklist.
	 * A single version leads to a different encoding of the video.
	 * @param Video element to be parsed
	 * @param Worklist in which the versions will be saved
	 * @throws SAXException
	 */
	private void parseVersions(Element video, Worklist worklist) throws SAXException {
		NodeList versions = video.getElementsByTagName(K.versionTag);
		for (int i = 0; i < versions.getLength(); i++) {
			worklist.addVersion(parseVersion((Element)versions.item(i)));
		}
	}
	
	/**
	 * Checks if the version has a bitrate attribute specified.
	 * If the attribute is specified, checks if the bitrate is a positive value
	 * and it has been specified just once.
	 * @param Version whose bitrate has to be checked
	 * @return Bitrate value or -1 if bitrate has not been specified
	 * @throws SAXException
	 */
	private int parseBitRate(Element version) throws SAXException {
		NodeList bitrates = version.getElementsByTagName(K.versionBitrateTag);
		int bitrateLength = bitrates.getLength();
		if (bitrateLength==1) {
			int bitrate = Integer.parseInt(bitrates.item(0).getTextContent());
			if(bitrate<1) {
				//Illegal value
				throw new SAXException("The bitrate value must be positive!");
			} else {
				//Add the value
				return bitrate;
			}
		} else if(bitrateLength>1) {
			//Illegal length
			throw new SAXException("It's possible to specify a single bitrate value for a single version!");
		} else {
			//Bitrate not specified
			return -1;
		}
	}
	
	/**
	 * Checks if the version has a width attribute specified.
	 * If the attribute is specified, checks if the width is a positive value
	 * and it has been specified just once.
	 * @param Version whose width has to be checked
	 * @return Width value or -1 if width has not been specified
	 * @throws SAXException
	 */
	private int parseWidth(Element version) throws SAXException {
		NodeList widths = version.getElementsByTagName(K.versionWidthTag);
		int widthLength = widths.getLength();
		if (widthLength==1) {
			int width = Integer.parseInt(widths.item(0).getTextContent());
			if(width<1) {
				//Illegal width
				throw new SAXException("The width value must be positive!");
			} else {
				//Add the width
				return width;
			}
		} else if(widthLength>1) {
			//Illegal length
			throw new SAXException("It's possible to specify a single width value for a single version!");
		} else {
			//Width not specified
			return -1;
		}
	}
	
	/**
	 * Checks if the version has a height attribute specified.
	 * If the attribute is specified, checks if the height is a positive value
	 * and it has been specified just once.
	 * @param Version whose height has to be checked
	 * @return Height value or -1 if height has not been specified
	 * @throws SAXException
	 */
	private int parseHeight(Element version) throws SAXException {
		NodeList heights = version.getElementsByTagName(K.versionHeightTag);
		int heightLength = heights.getLength();
		if (heightLength==1) {
			int height = Integer.parseInt(heights.item(0).getTextContent());
			if(height<1) {
				//Illegal height
				throw new SAXException("The height value must be positive!");
			} else {
				//Add the value
				return height;
			}
		} else if(heightLength>1) {
			//Illegal length
			throw new SAXException("It's possible to specify a single height value for a single version!");
		} else {
			//Height not specified
			return -1;
		}
	}
	
	/**
	 * Checks the correctness of the version: in particular at least an attribute must be set
	 * and the width and the height must be set at the same time.
	 * @param Version to be parsed
	 * @return Version with all the attribute set
	 * @throws SAXException
	 */
	private Version parseVersion(Element version) throws SAXException {

		//Parse bit-rate
		int bitrate = parseBitRate(version);
		
		//Parse width
		int width = parseWidth(version);
		
		//Parse height
		int height = parseHeight(version);
		
		//No parameters specified
		if((width == -1) && (height == -1) && (bitrate == -1))
			throw new SAXException("The version in missing parameters!");
		//Width specified and Height not specified or viceversa
		else if ((width!=-1)&&(height==-1)||(width==-1)&&(height!=-1))
			throw new SAXException("Width and Height must be either both specified or not specified!");
		else
			return new Version(width, height, bitrate);
	}
	
	/**
	 * Checks if a default tiling scheme has been specified.
	 * If yes, checks if it has been specified just once
	 * and fills the working list with all the tiles. 
	 * @param Video element to be parsed
	 * @param Worklist in which the tiles will be added
	 * @return true if a default tiling scheme has been specified, false otherwise
	 * @throws SAXException
	 */
	private boolean parseDefaultTiling(Element video, Worklist worklist) throws SAXException {
		NodeList defaultTilings = video.getElementsByTagName(K.tileDefaultTag);
		int defaultTilingLenght = defaultTilings.getLength();
		if(defaultTilingLenght==1) {
			String defaultTilingName = defaultTilings.item(0).getTextContent();
			if(K.defaultTiling.containsKey(defaultTilingName)) {
				String[] defaultTiles = K.defaultTiling.get(defaultTilingName);
				//Setting the default tiling
				for(int i=0; i<defaultTiles.length; i++) {
					worklist.addTile(new Tile(defaultTiles[i])); 
				}
				return true;
			} else {
				//Default tiling key not available
				throw new SAXException("Wrong default tiling specification");
			}	
			
		} else if (defaultTilingLenght>1) {
			//Illegal length
			throw new SAXException("It is not possible to specify more than one default tiling for a single video!");
		} else {
			//No defalut tiling
			return false;
		}
	}
	
	/**
	 * Parses a tile element checking that x,y,h,w and the percentage for tiling have been specified.
	 * If a default tiling scheme has been already specified the method throws an exception.
	 * @param Video element to be parsed
	 * @param Worklist in which the tiles will be added
	 * @param isDefaultTiling used to check if a default tiling has been already specified
	 * @throws SAXException
	 */
	private void parseTiling(Element video, Worklist worklist, boolean isDefaultTiling) throws SAXException {
		NodeList tilings = video.getElementsByTagName(K.tileTag);
		int tilingLenght = tilings.getLength();
		if(tilingLenght>0) {
			if(isDefaultTiling) {
				//Default tiling has been already specified: why also manual tiling?
				throw new SAXException("The default tiling is already specified: it is not possible to define other tiles");
			} else {
				//Add tile after parsing
				for(int i=0; i<tilingLenght; i++) {
					worklist.addTile(parseTile((Element)tilings.item(i))); 
				}
			}		
		}
	}
	
	/**
	 * Checks if all the attribute related to the tile have been specified in the correct way.
	 * @param Tile to be parsed
	 * @return Tile object
	 * @throws SAXException
	 */
		
	private Tile parseTile(Element tile) throws SAXException {	
		//Initializing variables
		float tileStartXPerc;
		float tileStartYPerc;
		float tileEndXPerc;
		float tileEndYPerc;
		int tileX;
		int tileY;
		int tileW;
		int tileH;
		
		//Parsing Tile start X percentage
		NodeList tileStartXPercs = tile.getElementsByTagName(K.tileStartXPercTag);
		if(tileStartXPercs.getLength()!=1) {
			//Illegal length
			throw new SAXException("It's mandatory to specify a single start X percentage!");
		} else {
			tileStartXPerc = Float.parseFloat(tileStartXPercs.item(0).getTextContent());
			if(tileStartXPerc<0 || tileStartXPerc>1)
				//Illegal value
				throw new SAXException("The start X percentage value must range between 0 and 1!");
		}
		
		//Parsing Tile start Y percentage
		NodeList tileStartYPercs = tile.getElementsByTagName(K.tileStartYPercTag);
		if(tileStartYPercs.getLength()!=1) {
			//Illegal length
			throw new SAXException("It's mandatory to specify a single start Y percentage!");
		} else {
			tileStartYPerc = Float.parseFloat(tileStartYPercs.item(0).getTextContent());
			if(tileStartYPerc<0 || tileStartYPerc>1)
				//Illegal value
				throw new SAXException("The start Y percentage value must range between 0 and 1!");
		}
		
		//Parsing Tile end X percentage
		NodeList tileEndXPercs = tile.getElementsByTagName(K.tileEndXPercTag);
		if(tileEndXPercs.getLength()!=1) {
			//Illegal length
			throw new SAXException("It's mandatory to specify a single end X percentage!");
		} else {
			tileEndXPerc = Float.parseFloat(tileEndXPercs.item(0).getTextContent());
			if(tileEndXPerc<0 || tileEndXPerc>1)
				//Illegal value
				throw new SAXException("The end X percentage value must range between 0 and 1!");
		}
		
		//Parsing Tile end Y percentage
		NodeList tileEndYPercs = tile.getElementsByTagName(K.tileEndYPercTag);
		if(tileEndYPercs.getLength()!=1) {
			//Illegal length
			throw new SAXException("It's mandatory to specify a single end Y percentage!");
		} else {
			tileEndYPerc = Float.parseFloat(tileEndYPercs.item(0).getTextContent());
			if(tileEndYPerc<0 || tileEndYPerc>1)
				//Illegal value
				throw new SAXException("The start Y percentage value must range between 0 and 1!");
		}
		
		//Parsing Tile x
		NodeList tileXs = tile.getElementsByTagName(K.tileXTag);
		if(tileXs.getLength()!=1) {
			//Illegal length
			throw new SAXException("It's mandatory to specify a single x!");
		} else {
			tileX = Integer.parseInt(tileXs.item(0).getTextContent());
			if(tileX<0)
				//Illegal value
				throw new SAXException("The x value must be greater than 0!");
		}
		
		//Parsing Tile y
		NodeList tileYs = tile.getElementsByTagName(K.tileYTag);
		if(tileYs.getLength()!=1) {
			//Illegal length
			throw new SAXException("It's mandatory to specify a single y!");
		} else {
			tileY = Integer.parseInt(tileYs.item(0).getTextContent());
			if(tileY<0)
				//Illegal value
				throw new SAXException("The y value must be greater than 0!");
		}
		
		//Parsing Tile w
		NodeList tileWs = tile.getElementsByTagName(K.tileWTag);
		if(tileWs.getLength()!=1) {
			//Illegal length
			throw new SAXException("It's mandatory to specify a single w!");
		} else {
			tileW = Integer.parseInt(tileWs.item(0).getTextContent());
			if(tileW<0)
				//Illegal value
				throw new SAXException("The w value must be greater than 0!");
		}
		
		//Parsing Tile h
		NodeList tileHs = tile.getElementsByTagName(K.tileHTag);
		if(tileHs.getLength()!=1) {
			//Illegal length
			throw new SAXException("It's mandatory to specify a single h!");
		} else {
			tileH = Integer.parseInt(tileHs.item(0).getTextContent());
			if(tileH<0)
				//Illegal value
				throw new SAXException("The h value must be greater than 0!");
		}
		return new Tile(tileX, tileY, tileW, tileH, tileStartXPerc, tileStartYPerc, tileEndXPerc,tileEndYPerc);
	}	
	
	/**
	 * In this method the content of the XML file is checked in order to ensure
	 * that all the mandatory parameters are present and all of them are correctly inserted. 
	 * @param Video element to be parsed
	 * @return List of work to do containing versions and tiles desired
	 * @throws SAXException
	 */
	private Worklist parseVideo(Element video) throws SAXException {
		Worklist worklist = new Worklist();
		
		//STEP 1 - Checking if the root node has the right tag
		parseRootNodeName(video);
		
		//STEP 2 - Checking if the video path is defined
		parseVideoPath(video,worklist);
		
		//STEP 3 - Checking if the W (big W) is defined
		parseBigW(video,worklist);
		
		//STEP 4 - Checking if the H (big H) is defined
		parseBigH(video,worklist);
		
		//STEP 5 - Checking if the output folder is defined
		parseOutput(video,worklist);
		
		//STEP 6 - Checking if the segment length is defined
		parseSegment(video,worklist);
		
		//STEP 7 - Checking the versions
		parseVersions(video,worklist);
		
		//STEP 8 - Checking default tiling
		boolean isDefaultTiling = false;
		isDefaultTiling = parseDefaultTiling(video,worklist);
		
		//STEP 9 - Checking tiling
		parseTiling(video,worklist,isDefaultTiling);
		
		return worklist;
	}
}