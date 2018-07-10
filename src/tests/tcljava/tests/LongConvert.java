package tests;

public class LongConvert {
    private final static long val = -2398461842900206033L;

    public static boolean isMax(long num) {
	return (num == Long.MAX_VALUE);
    }

    public static boolean isMin(long num) {
	return (num == Long.MIN_VALUE);
    }

    public static boolean isVal(long num) {
	return (num == val);
    }
    
    public static boolean isVal(String numstr) {
	long num = Long.parseLong(numstr);
	return (num == val);
    }
}
