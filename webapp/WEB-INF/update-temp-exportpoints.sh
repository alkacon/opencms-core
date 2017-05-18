#!/bin/bash
WEBAPP="$1" 
TEMPDIR="$WEBAPP/WEB-INF/exportpoint-temp"
TARGET_DIR="$WEBAPP"
DUMMY_MARKER="--OCMS-TEMP-EXPORTPOINT-DELETED-FILE-MARKER--"

# Transfers changes from the temporary export point folder to the real export point folders. 
# If a file in the source directory starts with the delete marker string, then the corresponding
# file in the target directory is deleted, otherwise the file will simply be copied to the target directory.
# The directory structure relative to the source folder is preserved. 
# Afterwards, the files in the source folder are removed

if [ -z "$1" ] ; then
	echo "Usage: update-temp-exportpoints.sh webapp_folder"
	exit 1
fi 

TODELETE=""

if [ ! -d "$TEMPDIR" ] ; then 
    echo "Source directory '$TEMPDIR' not found."
    exit 0
fi

if [ ! -d "$TARGET_DIR" ] ; then
    echo "Target directory '$TARGET_DIR' not found." 
    exit 1
fi 

TARGET_DIR=$(readlink --canonicalize "$TARGET_DIR")
cd "$TEMPDIR" 
for F in $(find . -type f ) ; do
    TARGET_PATH="$TARGET_DIR/$F" 
    OK_TO_DELETE=0
    FILEHEADER=$(head -q --bytes=${#DUMMY_MARKER} "$F")
    if [ "$FILEHEADER" = "$DUMMY_MARKER" ] ; then # check if file content starts with dummy marker
        if [ -e "$TARGET_PATH" ] ; then
            echo "Deleting $TARGET_PATH" 
            rm -f --preserve-root "$TARGET_PATH" && OK_TO_DELETE=1 
        fi
    else
        PARENT=$(dirname "$TARGET_PATH") 
        mkdir -p "$PARENT" 
        echo "Copying $F to $PARENT"
        cp "$F" "$PARENT" && OK_TO_DELETE=1 
    fi
    [ $OK_TO_DELETE = 1 ] && rm "$F" # File has been handled successfully, so make sure to delete it so it won't be handled the next time the script is run  
done



