#!/bin/sh
echo build start at $(date)
echo -------------------------------+++++++++++++++ starting, setup env +++++++++++++++-------------------------------
if [ ! -d "$(pwd)/build" ]; then
	:
else
	rm -rf $(pwd)/build
fi

if [ ! -d "$(pwd)/output" ]; then
	:
else
	rm -rf $(pwd)/output
fi

echo -------------------------------+++++++++++++++ update local properties +++++++++++++++-------------------------------
#android update project -n BaiduBridge -t "android-24" --path .

#echo -------------------------------+++++++++++++++ build debug, sign with debug key
#+++++++++++++++-------------------------------
#ant debug

echo -------------------------------+++++++++++++++ build unsigned +++++++++++++++-------------------------------
#ant release
./gradlew clean
./gradlew assembleRelease

echo -------------------------------+++++++++++++++ copy output +++++++++++++++-------------------------------

if [ ! -d "$(pwd)/output" ]; then
	mkdir $(pwd)/output
fi

cp $(pwd)/build/outputs/apk/facelogin-android-release.apk $(pwd)/output