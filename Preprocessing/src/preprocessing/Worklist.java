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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class Worklist {
	private String videoPath;
	private int videoWidth;
	private int videoHeight;
	
	private ArrayList<Version> versions;
	private TranscodedVersion[] transcodedVersions;
	private String audio;
	
	private ArrayList<Tile> tiles;
	private String[] tiledVersions;
	
	private int segment;
	
	private String output;
	private String outputTemp;
	private String outputDashSRD;
	private String outputName;
	
	private int bigW;
	private int bigH;
	
	public Worklist() { 
		this.versions = new ArrayList<>();
		this.tiles = new ArrayList<>();
	}
	
	public void setVideoPath(String videoPath) {
		this.videoPath = videoPath;
	}
	
	public void setBigW(int bigW) { 
		this.bigW = bigW;
	}
	
	public void setBigH(int bigH) { 
		this.bigH = bigH;
	}
	
	
	public void addVersion(Version version) {
		versions.add(version);
	}
	
	public void addTile(Tile tile) {
		tiles.add(tile);
	}
	
	public void setOutput(String output) { 
		this.output = output;
	}
	
	public void setSegment(int segment) {
		this.segment = segment;
	}
	
	/**
	 * Check if the video file provided in input exists
	 * @throws FileNotFoundException
	 */
	public void videoExists() throws FileNotFoundException {
		File f = new File(videoPath);
		if(!f.exists() || f.isDirectory())
			throw new FileNotFoundException("The video file provided doesn't exist!");
	}
	
	/**
	 * Checks if the directory provided as output exists: if not creates it.
	 * This method also creates (if they don't exist) other directory:
	 * the first one is a temporary directory;
	 * the second one will contain the final outputs.
	 * @throws SecurityException
	 */
	public void outputDirectory() throws SecurityException {
		File root = new File(output);
		outputName = root.getName();
		outputTemp = output+"/temp";
		outputDashSRD = output+"/dashSRD";		
		File temp = new File(outputTemp);
		File dashSrd = new File(outputDashSRD);   
	    if (!root.exists()) {
	    	root.mkdir();
	    }
	    if (!temp.exists()) {
	    	temp.mkdir();
	    }   
	    if (!dashSrd.exists()) {
	    	dashSrd.mkdir();
	    }   
	}
	
	/**
	 * This method retrieves the video width and the video height and these parameters are set
	 * as internal variables.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void videoDimensions() throws IOException, InterruptedException {
		//TODO: check the buffer exception
		Runtime runTime = Runtime.getRuntime();
		
		//Preparing the commands to be launched
		//Width
		String widthCommand = "ffprobe -v error -of flat=s=_ -select_streams v:0 -show_entries stream=width "+videoPath;
		//Height
		String heightCommand = "ffprobe -v error -of flat=s=_ -select_streams v:0 -show_entries stream=height "+videoPath;		
		
		//Width
		Process widthProcess = runTime.exec(widthCommand);
		ProcessGetDimensions widthOutput = new ProcessGetDimensions(widthProcess.getInputStream());
		widthOutput.start();
		widthProcess.waitFor();
		//videoWidth = widthOutput.returnValue();
		widthProcess.destroy();
		
		//Height
		Process heightProcess = runTime.exec(heightCommand);	
		ProcessGetDimensions heightOutput = new ProcessGetDimensions(heightProcess.getInputStream());
		heightOutput.start();
		heightProcess.waitFor();
		videoHeight = heightOutput.returnValue();
		heightProcess.destroy();
		
	}
	
	/**
	 * Extracts the audio from the video and puts it in the temporary directory 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void extractAudio() throws IOException, InterruptedException {
		Runtime runTime = Runtime.getRuntime();
		Process worker;
		ProcessOutputStream outputStream;
		//Saving the audio filepath for the last phase
		audio = outputTemp+"/"+outputName+"-audio.mp4";
		String command = "ffmpeg -y -i "+videoPath+" -vn -loglevel 16 -hide_banner "+audio;
		worker = runTime.exec(command);	
		outputStream = new ProcessOutputStream(worker.getErrorStream());
		outputStream.start();
		worker.waitFor();
		worker.destroy();
		
	}
	
	/**
	 * For each version in the XML file an encoding process is launched.
	 * If no versions have been specified, the input filename is just saved to be used in the next phase 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void transcode() throws IOException, InterruptedException {
		Runtime runTime = Runtime.getRuntime();
		Process worker;
		ProcessOutputStream outputStream;
		String commandPrefix = "ffmpeg -y -i "+videoPath+ " -loglevel 16 -hide_banner -profile:v baseline";
		String commandPortion;
		String commandSuffix;
		String fileBasename;
		Version currentVersion;
		
		int versionNumber = versions.size();
		if(versionNumber>0) {
			//Versions have been specified: launching one encoding process for each version.
			transcodedVersions = new TranscodedVersion[versionNumber];
			for(int i=0; i<versionNumber; i++) {
				currentVersion = versions.get(i);
				System.out.print(currentVersion.generateLog()+"...");
				fileBasename = outputTemp+"/"+outputName+versions.get(i).generateFileSuffix();
				commandSuffix = fileBasename+".mp4";
				commandPortion = currentVersion.generateCommandPortion();
				worker = runTime.exec(commandPrefix+commandPortion+commandSuffix);	
				outputStream = new ProcessOutputStream(worker.getErrorStream());
				outputStream.start();
				worker.waitFor();
				worker.destroy();
				//Adding the current version for the next phase
				transcodedVersions[i] = currentVersion.generateTranscoded(fileBasename,videoWidth,videoHeight);
				System.out.println(" completed!");
			}
		} else {
			//Versions have not been specified. Just copying the original video filename for the next phase
			transcodedVersions = new TranscodedVersion[1];
			String videoPathNoExt = videoPath.replaceFirst("[.][^.]+$", "");
			transcodedVersions[0] = new TranscodedVersion(videoPathNoExt,videoWidth,videoHeight);
		}
		
	}
	
	/**
	 * For each tile specified in the XML file and for each video obtained in the previous phase,
	 * a process is created to obtain the cropped video.
	 * If no tiles have been specified, previous encoded filenames are saved
	 * in order to be used in the next phase.
	 * If no versions have been specified, the tiling is performed on the original video.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void tile() throws IOException, InterruptedException {
		Runtime runTime = Runtime.getRuntime();
		Process worker;
		ProcessOutputStream outputStream;
		String commandPrefix = "ffmpeg -y ";
		String commandPortion;
		String commandSuffix;
		String commandOutput;
		
		int tileNumber = tiles.size();
		//Checking if tiling is needed
		if(tileNumber>0) {
			//Tiling is needed
			tiledVersions = new String[tileNumber*transcodedVersions.length+1];
			//For each video
			for(int i=0; i<transcodedVersions.length;i++) {
				System.out.println("\tCropping the file "+transcodedVersions[i].filePath+".mp4");
				//For each tile
				for(int j=0; j<tileNumber; j++) {
					System.out.print(tiles.get(j).generateLog()+"...");
					commandSuffix = "-loglevel 16 -hide_banner -an -profile:v baseline ";
					commandOutput = outputTemp+"/"+transcodedVersions[i].fileName+tiles.get(j).generateFileSuffix()+".mp4";
					commandPortion = "-i "+transcodedVersions[i].filePath+".mp4 "+tiles.get(j).generateCommandPortion(transcodedVersions[i].width,transcodedVersions[i].height);
					worker = runTime.exec(commandPrefix+commandPortion+commandSuffix+commandOutput);	
					outputStream = new ProcessOutputStream(worker.getErrorStream());
					outputStream.start();
					worker.waitFor();
					worker.destroy();
					//Storing the current tile for the next phase 
					tiledVersions[(i*tileNumber)+j] = commandOutput
							+"#video:desc_as='<SupplementalProperty schemeIdUri=\"urn:mpeg:dash:srd:2014\""
							+ " value=\"0,"+tiles.get(j).generateSRDProperty()+","+bigW+","+bigH+"\"/>'";
							
					System.out.println(" completed!");
				}
				System.out.println("\t Work finished on the file "+transcodedVersions[i].filePath+".mp4");
			}
		tiledVersions[tiledVersions.length-1] = audio+"#audio";
		} else { 
			//No tiling is needed. Just copying the video paths and the audio for the last phase
			tiledVersions = new String[transcodedVersions.length+1];
			for(int i=0; i<transcodedVersions.length;i++) {
				tiledVersions[i] = transcodedVersions[i].filePath+".mp4#video";
			}
			tiledVersions[transcodedVersions.length] = audio+"#audio";
		}
	}
	
	/**
	 * All the versions and all the tiles are grouped to generate segments together with the MPD file.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void segment() throws IOException, InterruptedException {
		Runtime runTime = Runtime.getRuntime();
		Process worker;
		ProcessOutputStream outputStream;
		String command = "MP4Box -dash "+segment+" -segment-name %s_ -rap -out "+outputDashSRD+"/manifest.mpd ";
		
		//Putting each tile/version as input for MP4Box 
		for(int i=0; i<tiledVersions.length; i++) {
			command += tiledVersions[i]+" ";
		}
		
		//Creating a temporary script
		String tempDashFilename = outputTemp+"/dash.sh";   
		PrintStream writer = new PrintStream(new FileOutputStream(tempDashFilename));
		writer.println(command);
		writer.close();
		
		//Adding execution privileges 
		worker = runTime.exec("chmod 777 "+tempDashFilename);	 
		outputStream = new ProcessOutputStream(worker.getErrorStream());
		outputStream.start();
		worker.waitFor();
		worker.destroy();
		
		//Executing the script
		worker = runTime.exec(tempDashFilename);	 
		outputStream = new ProcessOutputStream(worker.getErrorStream());
		outputStream.start();
		worker.waitFor();
		worker.destroy();	
	}
}
