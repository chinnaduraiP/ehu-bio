#!/bin/bash

if [ $# -ne 1 ]; then
    echo "Usage: $0 <X.Y>"
    exit
fi

ZIP="PAnalyzer-v$1.zip"
UNZIP="PAnalyzer-v$1"
if [ ! -f "$ZIP" ]; then
    echo "$ZIP not found"
    exit
fi

SKEL="PAnalyzer-vX.Y-MacOSX"
DST="PAnalyzer-v$1-MacOSX"
if [ -f "$DST.zip" ]; then
    echo "$DST.zip already exists"
    exit
fi

cp -a "$SKEL" "$DST" &&
unzip "$ZIP" &&
cp -f "$UNZIP"/*.dll "$UNZIP"/*.exe "$DST"/PAnalyzer.app/Contents/MacOS &&
rm -rf "$UNZIP" &&
zip -r "$DST.zip" "$DST" &&
rm -rf "$DST"

if [ $? -ne 0 ]; then
    echo "Error creating $DST.zip"
else
    echo "$DST.zip created successfully!!"
fi
