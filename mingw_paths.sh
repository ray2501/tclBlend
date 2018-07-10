#!/bin/sh

# This script will convert a Unix style list of paths
# into a Win32 native list of paths. This script
# depends on the mingw_path.sh script in this same dir.
#
# For example:

# /one:/home/joe:/c

# Could be converted to

# C:/msys/one;C:/msys/home/joe;C:/

THIS=$0
THIS_DIR=`dirname $THIS`
if [ "$THIS_DIR" = "." ]
then
   THIS_PREFIX=""
else
   THIS_PREFIX="$THIS_DIR/"
fi
#echo "THIS is \"$THIS\", THIS_DIR is \"$THIS_DIR\", THIS_PREFIX is \"$THIS_PREFIX\"" >&2

UPATHS=$1
WPATHS=""
#echo "INPUT is \"$UPATHS\"" >&2

prest=${UPATHS}
loop=1
while [ $loop = 1 ]
do
    pelem=${prest%%:*}
    prest=${prest#*:}
    #echo "pelem is \"$pelem\"" >&2
    #echo "prest is \"$prest\"" >&2
    if [ "$pelem" != "" ]
    then
        #echo "pelem \"$pelem\" is valid" >&2
        wpelem=$( ${THIS_PREFIX}mingw_path.sh "$pelem" )
        #echo "wpelem is \"$wpelem\"" >&2
        if [ "$WPATHS" = "" ]
        then
            WPATHS="$wpelem"
        else
            WPATHS="${WPATHS};${wpelem}"
        fi
    fi
    if [ "$prest" = "$pelem" ]
    then
        loop=0
    fi
done

#echo "OUTPUT IS \"$WPATHS\"" >&2
echo $WPATHS
exit 0

