#Copyright 2017 Laboratoire I3S, CNRS, Université côte d'azur
#Author: Savino Dambra
#!/bin/bash
#Defining some general purpose variables
RED='\033[0;31m'
INTEGERREGEX='^[0-9]+$'
STARTENC="Start encoding..."
ENDENC="Encoding complete!\n"
ENCTEMPFILE="enctempdata.csv"
DASHTEMPFILE="dashtempfile.sh"

#Removing temp data if exists
if [ -f "$ENCTEMPFILE" ]; then
rm $ENCTEMPFILE
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

#Checking whether the number of second each segment is composed of is provided
if [ -z "$5" ]; then
        #Value not provided: keeping the default one
        slicing=5000
else
        slicing=$5 
fi
#Initializing the temp file for SECTION2
DIR=$(dirname "${4}")
OUT=$(basename "${4}")
mkdir $DIR/$OUT
echo -e "MP4Box -dash ${slicing} -segment-name %s_ -out ${DIR}/${OUT}/manifest.mpd \\" >> $DASHTEMPFILE

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
				outputfilename=${4}_${bitrate}k
				ffmpeg -y -i $1 \
				-b:v ${bitrate}k -bufsize ${bitrate}k \
				-loglevel 16 -hide_banner \
				${outputfilename}.mp4 \
				< /dev/null \
				#End of encoding!
				echo -e "${ENDENC}"
				#Pushing video resolution in a file for the next step
				eval $(ffprobe -v error \
				-of flat=s=_ -select_streams v:0 \
				-show_entries stream=height,width $1) \
				echo ${outputfilename},${INPUTVIDEOWIDTH},${INPUTVIDEOHEIGHT}>>$ENCTEMPFILE
			else
				width=$first
				height=$second
				if [[ $third =~ $INTEGERREGEX ]] ; then
					bitrate=$third
					echo -e "\t${width}\t${height}\t${bitrate}k"
					#Start encoding...
                                	echo -e "${STARTENC}"
					outputfilename=${4}_${bitrate}k_${width}x${height}
					ffmpeg -y -i $1 \
					-vf scale=${width}:${height} \
					-b:v ${bitrate}k -bufsize ${bitrate}k \
					-loglevel 16 -hide_banner \
					${outputfilename}.mp4 \
					< /dev/null \
                                	#End of encoding!
                                	echo -e "${ENDENC}"
					#Pushing video resolution in a file for the next step
                               		echo ${outputfilename},${width},${height}>>$ENCTEMPFILE
				else
					echo -e "\t${width}\t${height}\tSrc"
					#Start encoding...
					echo -e "${STARTENC}"
					outputfilename=${4}_${width}x${height}
                                	ffmpeg -y -i $1 \
                                        -vf scale=${width}:${height} \
                                        -loglevel 16 -hide_banner \
                                        ${outputfilename}.mp4 \
					< /dev/null \
                                	#End of encoding!
                                	echo -e "${ENDENC}"
					#Pushing video resolution in a file for the next step
                                        echo ${outputfilename},${width},${height}>>$ENCTEMPFILE
				fi
                        fi
		line=$((line+1))
		done
	fi
fi
##SECTION 1 - VIDEO TILING
#Checking whether the file path is provided:
if [ -z "$3" ]; then
        #File path not provided
        echo -e "${RED}ERROR: No tiling file supplied"
        exit 1
else
	echo -e "Tiling File <- ${3}!"
        #File path provided: check if the file exists
        if [ ! -f $3 ]; then
                #File doesn't exist
                echo -e "${RED}File ${3} not found!"
                exit 1
        else
                #File exists. Computing the tiling for each transcoded video
                cat $ENCTEMPFILE | while IFS=, read  first second third
                do
			echo -e "Current video: ${first}.mp4"
			line=0
			cat $3 | while IFS=, read  startwidth startheight endwidth endheight
			do
				#Checking if the coordinates are expressed as percentage between 0 and 1
				if (( \
				$(echo "$startwidth <= 1" | bc -l) &&  $(echo "$startwidth >= 0" | bc -l) \
				&& $(echo "$startheight <= 1" | bc -l) &&  $(echo "$startheight >= 0" | bc -l) \
				&& $(echo "$endwidth <= 1" | bc -l) &&  $(echo "$endwidth >= 0" | bc -l) \
				&& $(echo "$endheight <= 1" | bc -l) &&  $(echo "$endheight >= 0" | bc -l) \
				)); then
					startwpixel=$(echo "$second*$startwidth" |bc)
					starthpixel=$(echo "$third*$startheight" |bc)
					endwpixel=$(echo "$second*$endwidth" |bc)
					endhpixel=$(echo "$third*$endheight" |bc)
					areawidth=$(echo "$endwpixel-$startwpixel" |bc)
					areaheight=$(echo "$endhpixel-$starthpixel" |bc)
					#Start cropping
					echo -e "\tCropping from (${startwpixel},${starthpixel}) to (${endwpixel},${endhpixel}) with resolution (${areawidth},${areaheight})..."
					outputfilename="${first}_w-${areawidth},h-${areaheight}_sp-${startwpixel},${starthpixel}"
					ffmpeg -y -i $first.mp4 -filter:v "crop=${areawidth}:${areaheight}:${startwpixel}:${starthpixel}" \
					-loglevel 16 -hide_banner -c:a copy \
					$outputfilename.mp4 \
					< /dev/null \
					#Cropping complete
					echo -e "\tCropping complete"
					#Adding this tile to a file for the next section
					echo -e "${outputfilename}.mp4:desc_as='<SupplementalProperty schemeIdUri="urn:mpeg:dash:srd:2014" value="0,${startwidth},${startheight},${endwidth},${endheight},1,1"/>' \\" >> $DASHTEMPFILE
				else
					#Skip the line
					echo  -e "\tValues not between 0 and 1 at the line $((line+1)). Line has been skipped!"
				fi
			line=$((line+1))
			done
		done
	fi
fi
##SECTION 2 - DASH AND SRD
#Launching the script produced in the section 1
echo "Segmenting each tile and creating the DASH-SRD representation..."
./${DASHTEMPFILE}
echo "DASH-SRD representation created!"
#Removing temp files
rm $DASHTEMPFILE
#Process completed
echo "Overall process completed!"
