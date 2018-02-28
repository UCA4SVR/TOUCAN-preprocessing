This java script executes a conversion from a regular 360 video (not yet SRD-described) into a DASH-SRD one.   
1. It uses FFMPEG tool to firstly transcode the video at the desired resolutions and bitrates for adaptive streaming purposes.    
2. Then, always using FFMPEG, each video obtained in the previous step, is cropped to obtain tiles     
3. Finally, using MP4Box, a segmentation of the video is performed and a manifest file is created according to the DASH-SRD standard

Created by:

Universite Nice Sophia Antipolis (Universite Cote d'Azur) and CNRS  
Laboratoire d'Informatique, Signaux et Systèmes de Sophia Antipolis (I3S)

Contributors:

Savino DAMBRA  
Giuseppe SAMELA  
Lucile SASSATELLI  
Romaric PIGHETTI  
Ramon APARICIO-PARDO  
Anne-Marie PINNA-DERY

References:

[1] TOUCAN-Preprocessing https://github.com/UCA4SVR/TOUCAN-VR     
[2] S. Dambra, G. Samela, L. Sassatelli, R. Pighetti, R. Aparicio-Pardo, A. Pinna-Déry "Film Editing: New Levers to Improve VR Streaming", ACM Multimedia Systems Conference (MMSys), Amsterdam, The Netherlands, June 2018.

# Launching
The script can be launched from the shell as follow:   
```sh
user@machine:~/$ > java preprocessing.Preprocess [XML_file_1 XML_file_2 ... XML_file_N]
```

## [XML_file_N]
It is the absolute path of the XML file containing all the information to process a single video.
The XML file scructure is described below.

```XML
<?xml version="1.0"?>
<video>
<path>/home/user/Desktop/input.mp4</path>
<version>
    <width>1920</width>
    <height>1080</height>
</version>
<version>
    <width>720</width>
    <height>576</height>
    <bitrate>64</bitrate>
</version>
<version>
    <bitrate>64</bitrate>
</version>
<W>2</W>
<H>2</H>
<tile>
    <startXPerc>0</startXPerc>
    <startYPerc>0</startYPerc>
    <endXPerc>-0.5</endXPerc>
    <endYPerc>0.5</endYPerc>
    <x>0</x>
    <y>0</y>
    <w>1</w>
    <h>1</h>
</tile>
<segment>1500</segment>
<output>/home/user/Desktop/test</output>
</video>
```

* path    
**MANDATORY AND UNIQUE**        
> It is the absolute path including the filename of the video to be processed.
* version     
**NOT MANDATORY AND NOT UNIQUE** 
> Each version tag represents a different quality of the video to be included in the manifest. If no version is specified, the next phase (tiling one) will consider the original video.
Each version can be specified in three different ways:     
Specifying width and height (bitrate will be kept the same as in the original video)   
```XML
<version>
    <width>1920</width>
    <height>1080</height>
</version>
```
> Specifying width and height and bitrate    
```XML
<version>
    <width>720</width>
    <height>576</height>
    <bitrate>64</bitrate>
</version>
```
> Specifying only the bitrate (width and height will be kept the same as in the original video)    
```XML
<version>
<bitrate>64</bitrate>
</version>
```    
* W    
**MANDATORY AND UNIQUE**        
> Big W in the tiling grid scheme
* H    
**MANDATORY AND UNIQUE**        
> Big H in the tiling grid scheme
* tile    
**NOT MANDATORY AND NOT UNIQUE**        
> Each tile tag represents a different tile to be cropped from the original video. If tile tag is absent, contents obtained at the previous step are included in the manifest file and no supplemental property is added. Tile tag must be built with the following internal structure:    
1. x: x position in the tiling grid    
2. y: y position in the tiling grid    
3. w: width in the tiling grid    
4. h: height in the tiling grid       
5. startXPerc: LEFT UPPER corner START point WIDTH of the desired tile      
6. startYPerc: LEFT UPPER corner START point HEIGHT of the desired tile       
7. endXPerc: RIGHT BOTTOM corner END point WIDTH of the desired tile      
8. endYPerc: RIGHT BOTTOM corner END point HEIGHT of the desired tile     
      
Dealing with different resolutions, the last four values must be expressed as percentage of the video width or height using a scale between 0 and 1. Decimal values must be expressed with the dot and not using the comma (e.g., 0.5).    
* defaultTiling    
**NOT MANDATORY AND UNIQUE**           
> If the user wants to use a default tiling scheme, it can be done specifying it in this tag. If this tag is specified, an error is raised if also tile tags are present. There are two default tiling scheme:    
1. twoByTwo    
2. threeByThree        
> The result of the first one is the following:
<img src="http://i.imgur.com/4lMHkf7.jpg" alt="Tiling">     

* segment    
**NOT MANDATORY AND UNIQUE**        
> Segment length in milliseconds for the video division. If absent, the default value is set to 500 milliseconds.
* output    
**MANDATORY AND UNIQUE**        
> Output folder used by the script. If the folder doesn't exist the script creates it. Inside the specified output folder the script creates other two folders: temp and dashSRD. The first one is used as temporary directory while in the second one the DASH SRD described video is stored together with the manifest file and the initialization ones.

# Sample 
## Launching
```sh
user@machine:~/$ > java preprocessing.Preprocess /home/user/Desktop/video.xml
```
## video.xml
In the following example, a correct XML file is shown. The output will be a video with two different qualities and a 2x2 tiling scheme 

```XML
<?xml version="1.0"?>
<video>
<path>/home/user/Desktop/input.mp4</path>
<version>
    <width>1920</width>
    <height>1080</height>
</version>
<version>
    <width>720</width>
    <height>576</height>
    <bitrate>64</bitrate>
</version>
<W>2</W>
<H>2</H>
<defaultTiling>twoByTwo</defaultTiling>
<segment>1500</segment>
<output>/home/user/Desktop/test</output>
</video>
```

## First phase outputs
* /home/user/Desktop/test/temp/test-1920x1080.mp4
* /home/user/Desktop/test/temp/test-64K-720x576.mp4
* /home/user/Desktop/test/temp/test-audio.mp4

## Second phase outputs
* /home/user/Desktop/test/temp/test-1920x1080-0011.mp4
* /home/user/Desktop/test/temp/test-1920x1080-1011.mp4
* /home/user/Desktop/test/temp/test-1920x1080-0111.mp4
* /home/user/Desktop/test/temp/test-1920x1080-1111.mp4
* /home/user/Desktop/test/temp/test-64K-720x576-0011.mp4
* /home/user/Desktop/test/temp/test-64K-720x576-1011.mp4
* /home/user/Desktop/test/temp/test-64K-720x576-0111.mp4
* /home/user/Desktop/test/temp/test-64K-720x576-1111.mp4

## Third phase outputs    
* /home/user/Desktop/test/dashSRD/manifest.mpd
* M4s files and init files in the same folder
