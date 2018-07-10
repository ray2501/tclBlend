#!/bin/sh

# This program should be run after making changes to
# files used an input to any of the gnu autotools

#aclocal
#autoheader
rm -rf autom4*.cache
autoconf
rm -rf autom4*.cache
#automake
