package io.leavesfly.smartgrid.retailer;

public class RetailerInitArgs {
	public final static int timeSlots=4;
	public final static int ListenPort = 1234;
	public static float T = (float) Math.exp(-1);
	public static final float E = (float) Math.exp(-5);
	public static int ROUND = 1;
	public final static String logFile = "E:\\RetailerLog.txt";

	public final static float a = 0.005f;
	public final static float b = 0.001f;
	public final static int w = 1;

}
