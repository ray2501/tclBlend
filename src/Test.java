//This class is used as a simple test for the compiler and runtime

public class Test {

  //used to test java runtime and io subsystem during configure
  public static void main(String[] argv) {
    System.out.println("OK");
    System.exit(0);
  }

  //used to test javah utility for .h file generation
  private static native void foo();

  //used to test java invocation after install
  public static String isOK() {
    return "OK";
  }

}
