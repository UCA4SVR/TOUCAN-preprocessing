# TOUCAN-preprocessing
Preprocessing programs/scripts to generates DASH-SRD encoded videos from regular 360° videos

# Launching
```sh
user@machine:~/$ > ./tiling.sh input_video_path bitrates_resolutions_csv_path tiles_csv_path output_file_name
```
# Bitrates/Resolution CSV file
Syntax allowed:   
64       
720,576   
1920,1080,128   
First line keep the resolution of the source video and transcode it with a bitrate of 64k.   
Second line keep the bitrate costant and change the resolution of the video to 720w x 576h   
Third line change both resolution and bitrate to 1920w x 1080h and 128k respectively.   

# Tiles CSV file
Syntax allowed:   
cord1,cord2,cord3,cord3   
cord1 LEFT UPPER corner START point WIDTH of the desired tile   
cord2 LEFT UPPER corner START point HEIGHT of the desired tile   
cord3 RIGHT BOTTOM corner END point WIDTH of the desired tile   
cord4 RIGHT BOTTOM corner END point HEIGHT of the desired tile

# Output file name
Specify the output path followed by the desired output prefix without extension
