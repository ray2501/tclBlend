/*
 * PackerLayout.java
 *
 * Copyright (c) 2000 Mo DeJong, Red Hat, Inc.
 * Copyright (c) 1998 Mo DeJong, U of MN
 * Copyright (c) 1997 Daeron Meyer, U of MN
 * Copyright (c) 1996 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 * 
 * RCS: @(#) $Id: PackerLayout.java,v 1.2 2000/03/15 04:30:35 mo Exp $
 *
 */


/**
 * PackerLayout is used to lay out widget components.
 *
 * This layout manager is easy to use, as well as memory and speed tuned.
 * The layout manager parses up command options like -fill x -expand true
 * and does the correct layout management based on Tk's pack layout.
 *
 */

import java.lang.*;
import java.awt.*;
import java.util.*;


public class PackerLayout implements LayoutManager
{

  private Hashtable component_table;
  private Component firstcomp, lastcomp;

  //this hashtable will do the mapping between
  //options and the actual option object

  private static Hashtable option_table;

  //this hashtable will do the mapping between
  //option arguments and the option that they
  //are valid for

  private static Hashtable value_table;

  //if the option_table hash returns a ref to the
  //INT_MAP object then we know that this option
  //just takes an integer

  //if the option and the value args for two strings
  //both refer to the same object then the option
  //pair is valid and can be added to the packtable

  
  //finally a lookup is done on the value_object_table
  //when we know that both the option and the value
  //are correct. This lookup gives us our Object ref
  //for the value in question (not used for int options)
  
  private static Hashtable value_object_table;

  // This is kind of wacky, but there does not seem to
  // be any way to "update" the options of a component
  // in a Java layout manager. To implement "update",
  // we just call add() again except that we set
  // the special ignore_next_remove flag. Then on
  // the next call to add(), removeLayoutComponent()
  // will be invoked but we will not actually remove
  // the component. When addLayoutComponent() is called
  // we can then update the component's pack record.

  private boolean ignore_next_remove = false;



  private static final String OPT_ANCHOR	= "-anchor";
  private static final String OPT_EXPAND	= "-expand";
  private static final String OPT_FILL		= "-fill";
  private static final String OPT_IPADX		= "-ipadx";
  private static final String OPT_IPADY		= "-ipady";
  private static final String OPT_PADX		= "-padx";
  private static final String OPT_PADY		= "-pady";
  private static final String OPT_SIDE		= "-side";
  
  private static final String ANCHOR_OPT_N	= "n";
  private static final String ANCHOR_OPT_NE	= "ne";
  private static final String ANCHOR_OPT_E	= "e";
  private static final String ANCHOR_OPT_SE	= "se";
  private static final String ANCHOR_OPT_S	= "s";
  private static final String ANCHOR_OPT_SW	= "sw";
  private static final String ANCHOR_OPT_W	= "w";
  private static final String ANCHOR_OPT_NW	= "nw";
  private static final String ANCHOR_OPT_C 	= "center";	// default value

  private static final Object ANCHOR_OBJ_N	= new Object();
  private static final Object ANCHOR_OBJ_NE	= new Object();
  private static final Object ANCHOR_OBJ_E	= new Object();
  private static final Object ANCHOR_OBJ_SE	= new Object();
  private static final Object ANCHOR_OBJ_S	= new Object();
  private static final Object ANCHOR_OBJ_SW	= new Object();
  private static final Object ANCHOR_OBJ_W	= new Object();
  private static final Object ANCHOR_OBJ_NW	= new Object();
  private static final Object ANCHOR_OBJ_C	= new Object();	// default value
  
  private static final String EXPAND_OPT_TRUE	= "true";
  private static final String EXPAND_OPT_FALSE	= "false";	// default value
  private static final String EXPAND_OPT_YES	= "1";
  private static final String EXPAND_OPT_NO	= "0";
  
  private static final Object EXPAND_OBJ_TRUE	= new Object();
  private static final Object EXPAND_OBJ_FALSE	= new Object();


  private static final String FILL_OPT_NONE	= "none";	// default value
  private static final String FILL_OPT_X	= "x";
  private static final String FILL_OPT_Y	= "y";
  private static final String FILL_OPT_BOTH	= "both";

  private static final Object FILL_OBJ_NONE	= new Object();
  private static final Object FILL_OBJ_X	= new Object();
  private static final Object FILL_OBJ_Y	= new Object();
  private static final Object FILL_OBJ_BOTH	= new Object();


  private static final String SIDE_OPT_TOP	= "top";	// default value
  private static final String SIDE_OPT_BOTTOM	= "bottom";
  private static final String SIDE_OPT_LEFT	= "left";
  private static final String SIDE_OPT_RIGHT	= "right";


  private static final Object SIDE_OBJ_TOP	= new Object();
  private static final Object SIDE_OBJ_BOTTOM	= new Object();
  private static final Object SIDE_OBJ_LEFT	= new Object();
  private static final Object SIDE_OBJ_RIGHT	= new Object();
  

  private static final Object DEFAULT_ANCHOR	= ANCHOR_OBJ_C;
  private static final Object DEFAULT_EXPAND	= EXPAND_OBJ_FALSE;
  private static final Object DEFAULT_FILL	= FILL_OBJ_NONE;
  private static final int DEFAULT_IPADX	= 0;
  private static final int DEFAULT_IPADY	= 0;
  private static final int DEFAULT_PADX		= 0;
  private static final int DEFAULT_PADY		= 0;
  private static final Object DEFAULT_SIDE	= SIDE_OBJ_TOP;


  
  //place holder for mapping int values to options
  private static final Object INT_MAP	        = new Object();


  //this exception is throws if the number that is passes in on
  //number options is less than zero
  private static final NumberFormatException NFE = new NumberFormatException();


  //this dimension will be returned from our class in calls to
  //minimumLayoutSize. It is allocated once here so that it does
  //not have to get allocated every time minimumLayoutSize is called

  private static final Dimension RetDimension = new Dimension(0,0);

  //this helper method is used to split up an input string
  //based on a single splitchar. It returns an array of strings.
  // split("one two  three", ' ') -> {"one", "two", "three"}

  private static String[] split(String in, char splitchar) {
    // first we copy the contents of the string into
    // an array for quick processing
    
    int i;
    
    // create an array that is as big as the input
    // str plus one for an extra split char
    
    int len = in.length();
    char[] str = new char[len + 1];
    in.getChars(0,len,str,0);
    str[len++] = splitchar;
    
    int wordstart = 0;
    
    // make a vector that will hold our elements
    Vector words = new Vector(3);

    //for (i=0; i < len; i++) {
    //  System.out.println(str[i] + " : " + i );
    //}
    
    for (i=0; i < len; i++) {      
      // compare this char to the split char
      // if they are the same the we need to
      // add the last word to the array
      
      if (str[i] == splitchar) {
	//System.out.println("split char found at " + i);
	
	if (wordstart <= (i-1)) {
	  words.addElement( new String(str, wordstart, i - wordstart) );
	}

	wordstart = i + 1;
	
      }
    }
    
    
    // create an array that is as big as the number
    // of elements in the vector and copy over and return
    
    String[] ret = new String[ words.size() ];
    words.copyInto(ret);  
    return ret;
  }
  


  //in the init block we create the entries for the
  //mapping of option to valid args array, we use the
  //static hash table 

  static {

    option_table = new Hashtable(17);
    value_table = new Hashtable(17);
    value_object_table = new Hashtable(17);


    //-anchor option

    //map option string to option object
    option_table.put(OPT_ANCHOR,OPT_ANCHOR);

    //map option args to the option object
    value_table.put(ANCHOR_OPT_N,OPT_ANCHOR);
    value_table.put(ANCHOR_OPT_NE,OPT_ANCHOR);
    value_table.put(ANCHOR_OPT_E,OPT_ANCHOR);
    value_table.put(ANCHOR_OPT_SE,OPT_ANCHOR);
    value_table.put(ANCHOR_OPT_S,OPT_ANCHOR);
    value_table.put(ANCHOR_OPT_SW,OPT_ANCHOR);
    value_table.put(ANCHOR_OPT_W,OPT_ANCHOR);
    value_table.put(ANCHOR_OPT_NW,OPT_ANCHOR);
    value_table.put(ANCHOR_OPT_C,OPT_ANCHOR);
    

    //map option value to option value object
    value_object_table.put(ANCHOR_OPT_N,ANCHOR_OBJ_N);
    value_object_table.put(ANCHOR_OPT_NE,ANCHOR_OBJ_NE);
    value_object_table.put(ANCHOR_OPT_E,ANCHOR_OBJ_NE);
    value_object_table.put(ANCHOR_OPT_SE,ANCHOR_OBJ_SE);
    value_object_table.put(ANCHOR_OPT_S,ANCHOR_OBJ_S);
    value_object_table.put(ANCHOR_OPT_SW,ANCHOR_OBJ_SW);
    value_object_table.put(ANCHOR_OPT_W,ANCHOR_OBJ_W);
    value_object_table.put(ANCHOR_OPT_NW,ANCHOR_OBJ_NW);
    value_object_table.put(ANCHOR_OPT_C,ANCHOR_OBJ_C);
    



    //-expand option
    
    //map option string to option object
    option_table.put(OPT_EXPAND,OPT_EXPAND);
    
    //map option args to the option object
    value_table.put(EXPAND_OPT_TRUE,OPT_EXPAND);
    value_table.put(EXPAND_OPT_FALSE,OPT_EXPAND);
    value_table.put(EXPAND_OPT_YES,OPT_EXPAND);
    value_table.put(EXPAND_OPT_NO,OPT_EXPAND);
    
    
    //map option value to option object
    value_object_table.put(EXPAND_OPT_TRUE,EXPAND_OBJ_TRUE);
    value_object_table.put(EXPAND_OPT_FALSE,EXPAND_OBJ_FALSE);
    value_object_table.put(EXPAND_OPT_YES,EXPAND_OBJ_TRUE);
    value_object_table.put(EXPAND_OPT_NO,EXPAND_OBJ_FALSE);
    




    //-fill option
    
    //map option string to option object
    option_table.put(OPT_FILL,OPT_FILL);
    
    //map option args to the option object
    value_table.put(FILL_OPT_NONE,OPT_FILL);
    value_table.put(FILL_OPT_X,OPT_FILL);
    value_table.put(FILL_OPT_Y,OPT_FILL);
    value_table.put(FILL_OPT_BOTH,OPT_FILL);
    
    
    //map option value to option object
    value_object_table.put(FILL_OPT_NONE,FILL_OBJ_NONE);
    value_object_table.put(FILL_OPT_X,FILL_OBJ_X);
    value_object_table.put(FILL_OPT_Y,FILL_OBJ_Y);
    value_object_table.put(FILL_OPT_BOTH,FILL_OBJ_BOTH);




    //-ipadx and -ipady options
    
    //map from option string to the
    //INT_MAP place holder for options that
    //take integer arguments
    
    option_table.put(OPT_IPADX,INT_MAP);
    option_table.put(OPT_IPADY,INT_MAP);
    

    //map option string to the option object
    value_table.put(OPT_IPADX,OPT_IPADX);
    value_table.put(OPT_IPADY,OPT_IPADY);



    //-padx and -pady options
    //same as above option mappings

    option_table.put(OPT_PADX,INT_MAP);
    option_table.put(OPT_PADY,INT_MAP);
    
    value_table.put(OPT_PADX,OPT_PADX);
    value_table.put(OPT_PADY,OPT_PADY);




    
    //-side option
    
    //map option string to option object
    option_table.put(OPT_SIDE,OPT_SIDE);
    
    //map option args to the option object
    value_table.put(SIDE_OPT_TOP,OPT_SIDE);
    value_table.put(SIDE_OPT_BOTTOM,OPT_SIDE);
    value_table.put(SIDE_OPT_LEFT,OPT_SIDE);
    value_table.put(SIDE_OPT_RIGHT,OPT_SIDE);    

    //map option value to option object
    value_object_table.put(SIDE_OPT_TOP,SIDE_OBJ_TOP);
    value_object_table.put(SIDE_OPT_BOTTOM,SIDE_OBJ_BOTTOM);
    value_object_table.put(SIDE_OPT_LEFT,SIDE_OBJ_LEFT);
    value_object_table.put(SIDE_OPT_RIGHT,SIDE_OBJ_RIGHT);
    

  }






  /**
   * Constructs a new Packer Layout.
   */
  public PackerLayout() {    
    component_table = new Hashtable();
    firstcomp = null;
    lastcomp = null;
      
  }
  




  /**
   * Adds the specified component to the layout.
   * @param name information about attachments
   * @param comp the the component to be added
   */
  public void addLayoutComponent(String spec, Component comp)
  {
    int i , max;
    
    if (comp == null)
      return;

    boolean new_record = false;

    PackRecord pr = (PackRecord) component_table.get(comp);
    if (pr == null) {
	pr = new PackRecord();
	new_record = true;
	System.out.println("adding new record");
    }

    //System.out.println("input str is \"" + spec + "\"");    
    String[] args = split(spec,' ');
    

    //in the default case we will not add an element
    //to the hash table, only if an argument is given will
    //an element be added to the hash table
    
    if ((args.length % 2) != 0) {
      throw new PackingException("Fatal error in PackerLayout string spec, must have even number of arguments");
    }
    
    

    max = args.length;

    Object option, value;
    
    for (i=0 ; i < max ; i+=2) {
      

      /*
      System.out.println("trying to match the pair");
      System.out.println(args[i]);
      System.out.println(args[i+1]);
      */
      

      
      //first match the input string option
      option = option_table.get( args[i] );

      
      //see if the option returned is the INT_MAP
      //placeholder for options that take an int
      
      if (option == INT_MAP) {
	
	//if true then we know that the value must be
	//a non negative integer
	
	int num;
	
	try {

	  num = Integer.parseInt( args[i+1] );
	  	  
	  if (num < 0) {
	    throw NFE;
	  }
	  	  
	} catch (NumberFormatException e) {
	  throw new PackingException("error : value of the " + args[i]
				     + " option must be a non negative integer");
	}

	
	//this is an int option so we need to find out which
	//record member we will assign the int to

	value = value_table.get( args[i] );;


	//temp check for fatal case
	
	if (value == null) {

	  String str = "null value object for int parser on option \"" +
			     args[i] + "\" : \"" + args[i+1] + "\"";

	  throw new RuntimeException(str);

	  /*
	  System.out.println( str );
	  System.exit(-1);
	  */
	}


	if ( value == OPT_PADX ) {
	  pr.padx = num;
	}
	else if ( value == OPT_PADY ) {
	  pr.pady = num;
	}
	else if ( value == OPT_IPADX ) {
	  pr.ipadx = num;
	}
	else if ( value == OPT_IPADX ) {
	  pr.ipady = num;
	} else {
	  throw new RuntimeException("fatal : bad branch");
	}

      }

      else if (option == null) {
	//this is an error becuase the option
	//that was just given does not match
	//any of the predefined option types

	// unknown or ambiguous option "-foll": must be -after, -anchor, -before, -expand, -fill, -in, -ipadx, -ipady, -padx, -pady, or -side

	throw new PackingException("error : option \"" + args[i]
				   + "\" does not match any of " +
				   OPT_ANCHOR + " " +
				   OPT_EXPAND + " " +
				   OPT_FILL   + " " +
				   OPT_IPADX  + " " +
				   OPT_IPADY  + " " +
				   OPT_PADX   + " " +
				   OPT_PADY   + " " +
				   OPT_SIDE);
	
      } else if (option != value_table.get( args[i+1] ) ) {
	//in this case the given value for the option
	//did not match one of the possible option values
	
	throw new PackingException("error : option \"" + args[i]
				   + "\" can not take the value \""
                                   + args[i+1] + "\"");

      }
      else {

	//if no other conditions are true then we
	//must have matched both the option and the value
	//so we can add the option string and map it to
	//the value object in the value_object_table

	value = value_object_table.get(args[i+1]);


	//temp check
	
	if (value == null) {

	  String str = "null value object for option parser on option \"" +
			     args[i] + "\" : \"" + args[i+1] + "\"";

	  throw new RuntimeException(str);

	}


	//now we need to find out which field in our pack record
	//we need to assign this object to

	if ( option == OPT_ANCHOR ) {
	  pr.anchor = value;
	}
	else if ( option == OPT_EXPAND ) {
	  pr.expand = value;
	}
	else if ( option == OPT_FILL ) {
	  pr.fill = value;
	}
	else if ( option == OPT_SIDE ) {
	  pr.side = value;
	} else {
	  throw new RuntimeException("fatal : bad branch");
	}

      }
      
    }
    
    // If we created a new record, add a mapping from the component
    // to the pack record to our component_table

    if (new_record) {
	component_table.put(comp, pr);

	System.out.println("put pack record in table");

	// now add the component to the display list

	if (firstcomp == null) {
	    firstcomp = comp;
	    lastcomp = comp;
	} else {
	    PackRecord lpr = (PackRecord) component_table.get(lastcomp);
	    lpr.next = comp;
	    lpr.prev = lastcomp;
	    lastcomp = comp;
	}
    }

  }



  
  /**
   * Removes the specified component from the layout.
   * @param comp the component to remove
   */
  public void removeLayoutComponent(Component comp) {

    // Wacky "update" workaround, see above for comments
    if (ignore_next_remove) {
	ignore_next_remove = false;
	return;
    }

    PackRecord pr = (PackRecord) component_table.get(comp);
    Component prev = pr.prev, next = pr.next;
    
    if (prev == null) { // we are removing the first component
      firstcomp = next;
    } else {           //remove next pointer from prev component
      pr = (PackRecord) component_table.get(prev);
      pr.next = next;
    }

    if (next == null) { // we are removing the last component
      lastcomp = prev;
    } else {            //remove prev pointer from next component
      pr = (PackRecord) component_table.get(next);      
      pr.prev = prev;
    }

    //remove this component from the component table
    System.out.println("removing pack record from table");
    component_table.remove(comp);    
    return;

  }







  /**
   * Returns the preferred dimensions for this layout given the
   * components in the specified target container.
   * @param target the component which needs to be laid out
   * @see Container
   * @see #minimumSize
   */
  public Dimension preferredLayoutSize(Container target) {
    
    Dimension dim = minimumLayoutSize(target);
    Dimension cdim = target.getSize();
    
    if (cdim.width < dim.width)
      cdim.width = dim.width;
    if (cdim.height < dim.height)
      cdim.height = dim.height;
    return cdim;
    
  }
  
  





  /**
   * Returns the minimum dimensions needed to layout the
   * components contained in the specified target container.
   * @param target the component which needs to be laid out 
   * @see #preferredSize
   */
  public Dimension minimumLayoutSize(Container target) {

    int i;
    Insets insets = target.getInsets();
    int dim_width = 0, dim_height = 0;
    Dimension d;

    Dimension dmax = RetDimension;
    dmax.width = 0;
    dmax.height = 0;

    int nmembers = target.getComponentCount();
    PackRecord pr;

    for (i = 0; i < nmembers; i++) {
      
      Component m = target.getComponent(i);
      
      d = m.getMinimumSize();
      
      pr = (PackRecord) component_table.get(m);
      
      if (pr == null) {
	throw new RuntimeException("null PackRecord");
	//break;
      }

      d.width += (pr.padx * 2) + pr.ipadx + dim_width;
      d.height += (pr.pady * 2) + pr.ipady + dim_height;
	  
      if (pr.side == SIDE_OBJ_TOP || pr.side == SIDE_OBJ_BOTTOM) {
	    
	if (d.width > dmax.width)
	  dmax.width = d.width;
	
	dim_height = d.height;
	
      } else {
	
	if (d.height > dmax.height)
	  dmax.height = d.height;
	    
	dim_width = d.width;
      }
      
    }
    
    if (dim_width > dmax.width)
      dmax.width = dim_width;
    
    if (dim_height > dmax.height)
      dmax.height = dim_height;
    
    dmax.width += (insets.left + insets.right);
    dmax.height += (insets.top + insets.bottom);
    

    /*
      System.out.println("Insets: " + insets.left + " " +
			 insets.right + " " +
			 insets.top + " " +
			 insets.bottom);
      System.out.println("Container minimum size: " +
			 dmax.width + "x" +
			 dmax.height);
    */
    
    return dmax;
  }







  /**
   * Lays out the container. This method will actually reshape the
   * components in target in order to satisfy the constraints.
   * @param target the specified component being laid out.
   * @see Container
   */
  
  
  public void layoutContainer(Container target) {
    Insets insets = target.getInsets();
    Dimension dim = target.getSize();
    int cavityX = 0, cavityY = 0;
    int cavityWidth = dim.width - (insets.left + insets.right);
    int cavityHeight = dim.height - (insets.top + insets.bottom);
    int frameX, frameY, frameWidth, frameHeight;
    int width, height, x, y;
    Component current = firstcomp;
    
    
    /*
    System.out.println("Laying out container at size: " +
			 cavityWidth + "x" +
			 cavityHeight);
    */


    PackRecord pr;    
    int padx, pady;
    Object anchor;
    boolean fillx, filly;
    Dimension prefsize;


    while (current != null) {
      
      pr = (PackRecord) component_table.get(current);
            
      padx = pr.padx * 2;
      pady = pr.pady * 2;
      anchor = pr.anchor;


      //figure out the fill x and fill y
      
      //if the object is fill both or fill x then
      //fillx is true else fillx is false
      
      fillx	= (pr.fill == FILL_OBJ_X) || (pr.fill == FILL_OBJ_BOTH);
      
      //same for filly
      
      filly	= (pr.fill == FILL_OBJ_Y) || (pr.fill == FILL_OBJ_BOTH);



      current.doLayout();
      
      
      if (pr.side == SIDE_OBJ_TOP || pr.side == SIDE_OBJ_BOTTOM) {
	
	frameWidth = cavityWidth;
	frameHeight = current.getPreferredSize().height + pady + pr.ipady;
	
	if (pr.expand == EXPAND_OBJ_TRUE)
	  frameHeight += YExpansion(current, cavityHeight);
	
	cavityHeight -= frameHeight;
	
	if (cavityHeight < 0) {
	  frameHeight += cavityHeight;
	  cavityHeight = 0;
	}
	
	frameX = cavityX;
	
	if (pr.side == SIDE_OBJ_TOP) {
	  frameY = cavityY;
	  cavityY += frameHeight;
	} else {
	  frameY = cavityY + cavityHeight;
	}
	
      } else {
	frameHeight = cavityHeight;
	frameWidth = current.getPreferredSize().width + padx + pr.ipadx;
	
	if (pr.expand == EXPAND_OBJ_TRUE)
	  frameWidth += XExpansion(current, cavityWidth);
	
	cavityWidth -= frameWidth;
	if (cavityWidth < 0) {
	  frameWidth += cavityWidth;
	  cavityWidth = 0;
	}
	frameY = cavityY;
	if (pr.side == SIDE_OBJ_LEFT) {
	  frameX = cavityX;
	  cavityX += frameWidth;
	} else {
	  frameX = cavityX + cavityWidth;
	}
      }
      
      // Now that we have the frame size find out the actual component size
      
      prefsize = current.getPreferredSize();

      width = prefsize.width + pr.ipadx;
      
      if (fillx || (width > (frameWidth - padx)))
	width = frameWidth - padx;
      
      height = prefsize.height + pr.ipady;
      
      if (filly || (height > (frameHeight - pady)))
	height = frameHeight - pady;
      
      
      padx /= 2;
      pady /= 2;
      
      
      if (anchor == ANCHOR_OBJ_N) {
	x = frameX + (frameWidth - width)/2;
	y = frameY + pady;
      }
      else if (anchor == ANCHOR_OBJ_NE) {
	x = frameX + frameWidth - width - padx;
	y = frameY + pady;
      }
      else if (anchor == ANCHOR_OBJ_E) {
	x = frameX + frameWidth - width - padx;
	y = frameY + (frameHeight - height)/2;
      }
      else if (anchor == ANCHOR_OBJ_SE) {
	x = frameX + frameWidth - width - padx;
	y = frameY + frameHeight - height - pady;
      }
      else if (anchor == ANCHOR_OBJ_S) {
	x = frameX + (frameWidth - width)/2;
	y = frameY + frameHeight - height - pady;
      }
      else if (anchor == ANCHOR_OBJ_SW) {
	x = frameX + padx;
	y = frameY + frameHeight - height - pady;
      }
      else if (anchor == ANCHOR_OBJ_W) {
	x = frameX + padx;
	y = frameY + (frameHeight - height)/2;
	}
      else if (anchor == ANCHOR_OBJ_NW) {
	x = frameX + padx;
	y = frameY + pady;
	}
      else if (anchor == ANCHOR_OBJ_C) {
	x = frameX + (frameWidth - width)/2;
	y = frameY + (frameHeight - height)/2;
      } else {
	throw new RuntimeException("no match for ANCHOR type");
      }

      //System.out.println("Component size: " + width + "x" + height);

      current.setBounds(insets.left + x, y + insets.top, width, height);
      current = pr.next;
    }
  }




  private int XExpansion(Component current, int cavityWidth) {
    
    PackRecord pr = (PackRecord) component_table.get(current);
    int numExpand, minExpand, curExpand;
    int childWidth;
    
    minExpand = cavityWidth;
    numExpand = 0;
    

    for ( ; current != null; current = pr.next) {
      pr = (PackRecord) component_table.get(current);
      
      childWidth = current.getPreferredSize().width +
	(pr.padx * 2) + pr.ipadx;
      

      if (pr.side == SIDE_OBJ_TOP || pr.side == SIDE_OBJ_BOTTOM) {
	
	curExpand = (cavityWidth - childWidth) / numExpand;
	
	if (curExpand < minExpand)
	  minExpand = curExpand;
	
      } else {
	
	cavityWidth -= childWidth;
	
	if (pr.expand == EXPAND_OBJ_TRUE)
	  numExpand++;

      }  
    }
        
    curExpand = cavityWidth / numExpand;
    
    if (curExpand < minExpand)
      minExpand = curExpand;
    
    if (minExpand < 0)
      return 0;
    else
      return minExpand;
    
  }
  


  private int YExpansion(Component current, int cavityHeight) {
    
    PackRecord pr = (PackRecord) component_table.get(current);
    int numExpand, minExpand, curExpand;
    int childHeight;
    
    minExpand = cavityHeight;
    numExpand = 0;
    
    for ( ; current!=null ; current = pr.next ) {

      pr = (PackRecord) component_table.get(current);

      childHeight	= current.getPreferredSize().height +
	(pr.pady * 2) + pr.ipady;
      

      if (pr.side == SIDE_OBJ_LEFT || pr.side == SIDE_OBJ_RIGHT) {
	
	curExpand = (cavityHeight - childHeight)/numExpand;
	
	if (curExpand < minExpand)
	  minExpand = curExpand;
	
      } else {
	
	cavityHeight -= childHeight;
	
	if (pr.expand == EXPAND_OBJ_TRUE) {
	  numExpand++;
	  
	}
	
      }
      
    }
  
  
    curExpand = cavityHeight/numExpand;
    
    if (curExpand < minExpand)
      minExpand = curExpand;
    
    if (minExpand < 0)
      return 0;
    else
      return minExpand;
    
  }


  // This method will return the current packing properties
  // of a specific widget like the following format
  // -anchor center -expand 0 -fill none -ipadx 0 -ipady 0 -padx 0 -pady 0 -side top
  
  public String getComponentSettings(Component comp) {
    StringBuffer sb = new StringBuffer();

    PackRecord pr = (PackRecord) component_table.get(comp);

    if (pr == null)
	return null; // The widget is not managed by this PackerLayout

    // -anchor

    sb.append(OPT_ANCHOR);
    sb.append(' ');

    Object anchor = pr.anchor;

    if (anchor == ANCHOR_OBJ_N) {
	sb.append(ANCHOR_OPT_N);
    }
    else if (anchor == ANCHOR_OBJ_NE) {
	sb.append(ANCHOR_OPT_NE);
    }
    else if (anchor == ANCHOR_OBJ_E) {
	sb.append(ANCHOR_OPT_E);
    }
    else if (anchor == ANCHOR_OBJ_SE) {
	sb.append(ANCHOR_OPT_SE);
    }
    else if (anchor == ANCHOR_OBJ_S) {
	sb.append(ANCHOR_OPT_S);
    }
    else if (anchor == ANCHOR_OBJ_SW) {
	sb.append(ANCHOR_OPT_SW);
    }
    else if (anchor == ANCHOR_OBJ_W) {
	sb.append(ANCHOR_OPT_W);
    }
    else if (anchor == ANCHOR_OBJ_NW) {
	sb.append(ANCHOR_OPT_NW);
    }
    else if (anchor == ANCHOR_OBJ_C) {
	sb.append(ANCHOR_OPT_C);
    } else {
	throw new RuntimeException("no match for ANCHOR type");
    }

    sb.append(' ');

    // -expand

    sb.append(OPT_EXPAND);
    sb.append(' ');

    if (pr.expand == EXPAND_OBJ_TRUE) {
	sb.append(EXPAND_OPT_YES);
    }
    else if (pr.expand == EXPAND_OBJ_FALSE) {
	sb.append(EXPAND_OPT_NO);
    }
    else {
	throw new RuntimeException("no match for EXPAND type");
    }

    sb.append(' ');

    // -fill

    sb.append(OPT_FILL);
    sb.append(' ');

    if (pr.fill == FILL_OBJ_NONE) {
	sb.append(FILL_OPT_NONE);
    }
    else if (pr.fill == FILL_OBJ_X) {
	sb.append(FILL_OPT_X);
    }
    else if (pr.fill == FILL_OBJ_Y) {
	sb.append(FILL_OPT_Y);
    }
    else if (pr.fill == FILL_OBJ_BOTH) {
	sb.append(FILL_OPT_BOTH);
    }
    else {
	throw new RuntimeException("no match for FILL type");
    }

    sb.append(' ');
    

    // -ipadx -ipady -padx -pady

    sb.append(OPT_IPADX);
    sb.append(' ');
    sb.append(pr.ipadx);

    sb.append(' ');

    sb.append(OPT_IPADY);
    sb.append(' ');
    sb.append(pr.ipady);

    sb.append(' ');

    sb.append(OPT_PADX);
    sb.append(' ');
    sb.append(pr.padx);

    sb.append(' ');

    sb.append(OPT_PADY);
    sb.append(' ');
    sb.append(pr.pady);

    sb.append(' ');


    // -side

    sb.append(OPT_SIDE);
    sb.append(' ');

    if (pr.side == SIDE_OBJ_TOP) {
	sb.append(SIDE_OPT_TOP);
    }
    else if (pr.side == SIDE_OBJ_BOTTOM) {
	sb.append(SIDE_OPT_BOTTOM);
    }
    else if (pr.side == SIDE_OBJ_LEFT) {
	sb.append(SIDE_OPT_LEFT);
    }
    else if (pr.side == SIDE_OBJ_RIGHT) {
	sb.append(SIDE_OPT_RIGHT);
    } else {
	throw new RuntimeException("no match for SIDE type");
    }
    
    return sb.toString();
  }


  // "update" functionality workaround

  public void setIgnoreNextRemove(boolean bool) {
      ignore_next_remove = bool;
  }


  /**
   * Returns the String representation of this class...
   */

  public String toString() {
    return getClass().getName();    
  }



  //there is one of these records for each widget packed
  //into a window, it stores the pack info for that widget

  class PackRecord
  {
    Component prev = null;
    Component next = null;

    public int padx = DEFAULT_PADX;
    public int pady = DEFAULT_PADY;
    public int ipadx = DEFAULT_IPADX;
    public int ipady = DEFAULT_IPADY;

    public Object side = DEFAULT_SIDE;
    public Object expand = DEFAULT_EXPAND;
    public Object fill = DEFAULT_FILL;
    public Object anchor = DEFAULT_ANCHOR;
  }

}



//this exception is thrown if invalid arguments are passed
//to the packer layout

class PackingException extends RuntimeException
{    
  public PackingException(String desc) {
    super(desc);
  }
}

