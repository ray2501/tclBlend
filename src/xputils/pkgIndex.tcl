# pkgIndex.tcl file for the iload package.
# This package defines the iload command, an
# improved version of the load command.
# Author: Mo Dejong

set package XpUtils
set version 0.1

set load_cmd "
        source \"[file join $dir xputils.tcl]\"
        source \"[file join $dir iload.tcl]\"
        package provide $package $version
"

#puts "$package load cmd is\"$load_cmd\""

package ifneeded $package $version $load_cmd

unset package version load_cmd

