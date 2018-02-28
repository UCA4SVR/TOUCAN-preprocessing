/*
 * Copyright 2017 Université Nice Sophia Antipolis (member of Université Côte d'Azur), CNRS
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

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class Preprocess {
	public static void main(String[] args) {
		
		for(String arg : args) {
			Worklist worklist;
			
			try {
				//Building the parser object
				XmlParser xmlParser = new XmlParser(arg);
				
				//Starting process
				System.out.println("Process Started with file "+arg+"...");
				
				//Parsing the file
				System.out.print("Parsing the XML file...");
				worklist = xmlParser.parseXml();
				System.out.println(" completed!");
			
				//Preliminary actions
				System.out.print("Preliminary actions...");
				
				//Checking if the video file exists
				worklist.videoExists();
				
				//Checking if the output folder exists
				worklist.outputDirectory();
				
				//Acquiring video dimensions
				worklist.videoDimensions();
				System.out.println(" completed!");
				
				//PHASE 1 - Audio Extraction
				System.out.print("Start audio extraction:");
				worklist.extractAudio();
				System.out.println(" complete!");
				
				//PHASE 2 - Transcoding videos
				System.out.println("Start transcoding:");
				worklist.transcode();
				System.out.println("Transcoding complete!");
				
				//PHASE 3 - Cropping videos
				System.out.println("Start cropping:");
				worklist.tile();
				System.out.println("Cropping complete!");
				
				//PHASE 4 - Segmenting videos
				System.out.println("Start Segmenting:");
				worklist.segment();
				System.out.println("Segmenting complete!");
				
				System.out.println("Process completed with file "+args+"!");
				
			} catch (ParserConfigurationException | SAXException | InterruptedException | IOException e) {
				System.err.println(e.getMessage());
			}
		}
			
	}
}
