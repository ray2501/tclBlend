public class JNI_example {
    public static native int magic();

    public static void main(String[] args) {
        System.loadLibrary("JNI_example");
        System.out.println("magic number is " + magic());
    }
}
