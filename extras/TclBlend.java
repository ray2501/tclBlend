import tcl.lang.*;

// use this class to load TclBlend into an existing Java program

public class TclBlend {

  public static void main(String[] argv) throws Exception {
	Interp i = new Interp();

        i.eval("set i -1");

        TclObject obj = i.getVar("i", 0);

        String val = obj.toString();

        System.out.println("val is \"" + val + "\"");

  }

}

