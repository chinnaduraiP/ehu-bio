#!/bin/bash

if [ $# -ne 1 ]; then
    echo "Usage: $0 <X.Y>"
    exit
fi
VER="$1"

ZIP="PAnalyzer.zip"
UNZIP="PAnalyzer"
if [ ! -f "$ZIP" ]; then
    echo "$ZIP not found"
    exit
fi

DIR="PAnalyzer-v$VER"
if [ -d "$DIR" ]; then
    echo "$DIR already exists"
    exit
fi
mkdir -p "$DIR"

create-macosx() {
    local SKEL="Skel/MacOSX"
    local DST="$DIR/PAnalyzer-v$VER-MacOSX"

    svn export "$SKEL" "$DST" &&
    cp -f "$UNZIP"/*.dll "$UNZIP"/*.exe "$DST"/PAnalyzer.app/Contents/MacOS &&
    zip -r "$DST.zip" "$DST" &&
    rm -rf "$DST"

    if [ $? -ne 0 ]; then
        echo "Error creating $DST.zip"
        false
    else
        echo "$DST.zip created successfully!!"
    fi
}

create-windows() {
    local SKEL="Skel/Windows"
    local DST="$DIR/PAnalyzer-v$VER-Windows"

    svn export "$SKEL" "$DST" &&
    cp -f "$UNZIP"/*.dll "$UNZIP"/*.exe "$DST" &&
    zip -r "$DST.zip" "$DST" &&
    rm -rf "$DST"

    if [ $? -ne 0 ]; then
        echo "Error creating $DST.zip"
        false
    else
        echo "$DST.zip created successfully!!"
    fi
}

create-linux() {
    local SKEL="Skel/Linux"
    local DST="$DIR/PAnalyzer-v$VER-Linux"

    svn export "$SKEL" "$DST" &&
    cp -f "$UNZIP"/*.dll "$UNZIP"/*.exe "$DST" &&
    tar -czf "$DST.tar.gz" "$DST" &&
    rm -rf "$DST"

    if [ $? -ne 0 ]; then
        echo "Error creating $DST.tar.gz"
        false
    else
        echo "$DST.tar.gz created successfully!!"
    fi
}

unzip "$ZIP" &&
create-macosx &&
create-windows &&
create-linux &&
rm -rf "$UNZIP"

if [ $? -ne 0 ]; then
    echo "Error creating packages"
else
   echo "Packages created successfully!!"
fi
