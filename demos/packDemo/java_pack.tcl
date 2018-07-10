#this file defines the java_pack command that is used to pack
#java widgets while running in a tcl shell

proc java_pack { component dash_in container args } {

	set usage "usage : java_pack component -in container ?options?"

	if {$dash_in != "-in"} {
		error "error arg2 must be -in, $usage"
	}

	#make sure the component and parent arguments are really
	#java objects that derive from the proper classes

	if {! [java::instanceof $component java.awt.Component]} {
		error "invalid component \"$component\""
	}

	if {! [java::instanceof $container java.awt.Container]} {
		error "invalid container \"$container\""
	}


	#if there are no args then we pack using all the defaults
	set len [llength $args]
	
	#the number of args must be even
	if {($len %2) != 0} {
		error "uneven number of options error, $usage"
	}


	#puts "pack command is \"$args\""



	#puts "getting layout manager"


	#we will assume that the layout manager is packer for right now


	#now make sure the container object is using the PackerLayout
	#as its layout manager. If it is not then we need to remove
	#any widgets it is currently managing and reset the layout manager

	set lm [$container getLayout]

	#puts "got layout manager $lm"
	

	if {! [java::instanceof $lm PackerLayout]} {

		#puts "removingAll widgets from this container"

		$container removeAll

		#puts "setting new layout manager as PackerLayout"

		$container setLayout [java::new PackerLayout]
	} else {

		#puts "layout manager was PackerLayout"
	}



	#now we are sure it is using our packer layout manager so
	#we can just add it with the container add method


	#puts "now to call the add method with args \"$args\" \"$component\""

	$container add $args $component

	return
}
