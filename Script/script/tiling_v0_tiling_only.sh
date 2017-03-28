#Copyright 2017 Laboratoire I3S, CNRS, Université côte d'azur
#Author: Savino Dambra
#!/bin/bash
#Defining some general purpose variables
RED='\033[0;31m'
INTEGERREGEX='^[0-9]+$'
DASHTEMPFILE="dashtempfile.sh"

echo  "Starting process (just tiling)..."
#Checking whether the temp CSV file containing all bitrates and resolution is provided
if [ -z "$1" ]; then
        #CSV Video file not provided
        echo -e "${RED}ERROR: No video file name supplied"
        exit 1
else
	#File path provided: check if the file exists
        if [ ! -f $1 ]; then
                #File doesn't exist
                echo -e "${RED}File ${1} not found!"
                exit 1
	else
		ENCTEMPFILE=$1
	fi
fi

#Checking whether the output video name is provided
if [ -z "$3" ]; then
        #Output Video file name not provided
        echo -e "${RED}ERROR: No output video file name supplied"
        exit 1
fi
echo -e "Output file name <- ${3}!"

#Checking whether the number of second each segment is composed of is provided
if [ -z "$4" ]; then
        #Value not provided: keeping the default one
        slicing=5000
else
        slicing=$4 
fi
#Initializing the temp file for SECTION2
DIR=$(dirname "${3}")
OUT=$(basename "${3}")
#If the directory doesn't exist, creates it
if [ ! -d $DIR/$OUT ]; then
	#Directory doesn't exist. Create it
	mkdir $DIR/$OUT
fi
echo -e "MP4Box -dash ${slicing} -segment-name %s_ -out ${DIR}/${OUT}/manifest.mpd \\" >> $DASHTEMPFILE
#Making the script executable
chmod 777 $DASHTEMPFILE

##SECTION 1 - VIDEO TILING
#Checking whether the file path is provided:
if [ -z "$2" ]; then
        #File path not provided
        echo -e "${RED}ERROR: No tiling file supplied"
        exit 1
else
	echo -e "Tiling File <- ${2}!"
        #File path provided: check if the file exists
        if [ ! -f $2 ]; then
                #File doesn't exist
                echo -e "${RED}File ${2} not found!"
                exit 1
        else
                #File exists. Computing the tiling for each transcoded video
                cat $ENCTEMPFILE | while IFS=, read  first second third
                do
			echo -e "Current video: ${first}.mp4"
			#Checking if the video exists
			if [ ! -f ${first}.mp4 ]; then
                		#File doesn't exist
                		echo -e "File ${first}.mp4 not found! Skipping it..."
        		else
				line=0
				cat $2 | while IFS=, read  startwidth startheight endwidth endheight
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
						#Extracting directory and applying the new output name provided as input
						outputfilename="${DIR}/${OUT}_w-${areawidth},h-${areaheight}_sp-${startwpixel},${starthpixel}"
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
			fi
		done
	fi
fi
##SECTION 2 - DASH AND SRD
#Launching the script produced in the section 1
echo "Segmenting each tile and creating the DASH-SRD representation..."
./${DASHTEMPFILE}
exitCode=$?
if [[ $exitCode != 0 ]]; then
echo "DASH-SRD representation creation FAILED!"
else
#Process completed
echo "DASH-SRD representation created!"
echo "Overall process completed!"
fi
#Removing temp files
rm $DASHTEMPFILE
