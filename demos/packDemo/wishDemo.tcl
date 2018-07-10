. configure -width 200 -height 200
pack propagate . false

button .b1 -text "Mid"
button .b2 -text "Bot"
button .b3 -text "Right"

pack .b1 -side right -fill y -expand false -anchor n
pack .b2 -side bottom -fill both -expand true -anchor s -padx 10 -pady 10
pack .b3 -anchor center
