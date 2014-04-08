#!/bin/sh

set -e

SCRIPTDIR=$(dirname "$0")
APIDOCS_DIR="$SCRIPTDIR"/../target/site/apidocs
if [ ! -d "$APIDOCS_DIR" ] ; then
    echo "$APIDOCS_DIR not present"
    exit 1
fi
DEST_DIR=/nfs/mercury-04/u10/javadoc/bue-common-open

/usr/bin/find "$DEST_DIR" -mindepth 1 -delete
/bin/tar c -C "$APIDOCS_DIR" . | /bin/tar x -C "$DEST_DIR" .
/usr/bin/find "$DEST_DIR" -mindepth 1 -exec /bin/chmod ug+rw {} +
/usr/bin/find "$DEST_DIR" -mindepth 1 -type d -exec /bin/chmod ug+x {} +
