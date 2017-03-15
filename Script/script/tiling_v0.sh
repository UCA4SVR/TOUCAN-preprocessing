#!/bin/bash
#Defining some general purpose variables
RED='\033[0;31m'
INTEGERREGEX='^[0-9]+$'
STARTENC="Start encoding..."
ENDENC="Encoding complete!\n"
TEMPFILE="tempdata.csv"

#Removing temp data if exists
if [ -f "$TEMPFILE" ]; then
rm $TEMPFILE
fi

echo  "Starting process..."
#Checking whether the input video is provided
if [ -z "$1" ]; then
        #Video file path not provided
        echo -e "${RED}ERROR: No input video file supplied"
        exit 1
else
	#Video width and height extraction
	eval $(ffprobe -v error -of flat=s=_ -select_streams v:0 -show_entries stream=height,width $1)
	INPUTVIDEOWIDTH=${streams_stream_0_width}
	INPUTVIDEOHEIGHT=${streams_stream_0_height}
fi
echo -e "Input file <- ${1}!"
#Checking whether the output video name is provided
if [ -z "$4" ]; then
        #Output Video file name not provided
        echo -e "${RED}ERROR: No output video file name supplied"
        exit 1
fi
echo -e "Output file name <- ${4}!"

##SECTION 0 - VIDEO TRANSCODING AT MULTIPLE RESOLUTIONS/BITRATES
#Checking whether the file path is provided:
if [ -z "$2" ]; then
	#File path not provided
	echo -e "${RED}ERROR: No Bitrates/Resolutions file supplied"
	exit 1
else
	echo -e "Bitrates/Resolutions File <- ${2}!"
	#File path provided: check if the file exists
	if [ ! -f $2 ]; then
    		#File doesn't exist
		echo -e "${RED}File ${1} not found!"
		exit 1
	else
		#File exists. Reading it
		line=0
		cat $2 | while IFS=, read  first second third
		do
			#Checking whether parameters have a correct syntax
			if ! [[ $first =~ $INTEGERREGEX ]] ; then
                                echo -e "${RED}ERROR: Not integer value supplied at line $((line+1)) as first parameter "
				exit 1
                        fi
			echo -e "Found\tWidth\tHeight\tBitrate"
			if ! [[ $second =~ $INTEGERREGEX ]] ; then
				bitrate=$first
				echo -e "\tSrc\tSrc\t${bitrate}k"
				#Start encoding...
				echo -e "${STARTENC}"
				outputfilename=${4}_${bitrate}k.mp4
				ffmpeg -y -i $1 \
				-b:v ${bitrate}k -bufsize ${bitrate}k \
				-loglevel 16 -hide_banner \
				$outputfilename \
				< /dev/null \
				#End of encoding!
				echo -e "${ENDENC}"
				#Pushing video resolution in a file for the next step
				eval $(ffprobe -v error \
				-of flat=s=_ -select_streams v:0 \
				-show_entries stream=height,width $1) \
				echo ${outputfilename},${INPUTVIDEOWIDTH},${INPUTVIDEOHEIGHT}>>$TEMPFILE
			else
				width=$first
				height=$second
				if [[ $third =~ $INTEGERREGEX ]] ; then
					bitrate=$third
					echo -e "\t${width}\t${height}\t${bitrate}k"
					#Start encoding...
                                	echo -e "${STARTENC}"
					outputfilename=${4}_${bitrate}k_${width}x_${height}.mp4
					ffmpeg -y -i $1 \
					-vf scale=${width}:${height} \
					-b:v ${bitrate}k -bufsize ${bitrate}k \
					-loglevel 16 -hide_banner \
					$outputfilename \
					< /dev/null \
                                	#End of encoding!
                                	echo -e "${ENDENC}"
					#Pushing video resolution in a file for the next step
                               		echo ${outputfilename},${width},${height}>>$TEMPFILE
				else
					echo -e "\t${width}\t${height}\tSrc"
					#Start encoding...
					echo -e "${STARTENC}"
					outputfilename=${4}_${width}x_${height}.mp4
                                	ffmpeg -y -i $1 \
                                        -vf scale=${width}:${height} \
                                        -loglevel 16 -hide_banner \
                                        $outputfilename \
					< /dev/null \
                                	#End of encoding!
                                	echo -e "${ENDENC}"
					#Pushing video resolution in a file for the next step
                                        echo ${outputfilename},${width},${height}>>$TEMPFILE
				fi
                        fi
		line=$((line+1))
		done
	fi
fi