package io.leavesfly.smartgrid.user;


public class UsersArgs {
	public final static String ip = "127.0.0.1";
	public final static int timeSlots = 4;
	public final static int port = 1234;
	public final static int userNum = 2;
	public final static int A_applianceNum = 4;
	public final static int B_applianceNum = 4;
	public final static String usersLogFile = "E://UsersLog.txt";

	public final static int[] userMax = new int[userNum];
	public final static int[][] A_applianceConsum = new int[userNum][A_applianceNum];
	public final static int[][] B_applianceConsumMax = new int[userNum][B_applianceNum];
	public final static int[][][] users_B_applianceSatisfa = new int[2][][];
	public final static int[][] user_0_B_applianceSatisfa = new int[B_applianceNum][timeSlots];
	public final static int[][] user_1_B_applianceSatisfa = new int[B_applianceNum][timeSlots];

	static {
		userMax[0] = 10;
		userMax[1] = 12;

		A_applianceConsum[0] = new int[] { 1, 2, 3, 1 };
		A_applianceConsum[1] = new int[] { 1, 3, 3, 1 };

		B_applianceConsumMax[0] = new int[] { 2, 3, 4, 4 };
		B_applianceConsumMax[1] = new int[] { 2, 3, 1, 3 };

		users_B_applianceSatisfa[0] = user_0_B_applianceSatisfa;
		users_B_applianceSatisfa[1] = user_1_B_applianceSatisfa;

		user_0_B_applianceSatisfa[0] = new int[] { 2, 4, 5, 3 };
		user_0_B_applianceSatisfa[1] = new int[] { 1, 3, 6, 3 };
		user_0_B_applianceSatisfa[2] = new int[] { 2, 5, 3, 4 };
		user_0_B_applianceSatisfa[3] = new int[] { 4, 1, 4, 3 };

		user_1_B_applianceSatisfa[0] = new int[] { 2, 2, 5, 3 };
		user_1_B_applianceSatisfa[1] = new int[] { 1, 6, 1, 3 };
		user_1_B_applianceSatisfa[2] = new int[] { 2, 3, 5, 3 };
		user_1_B_applianceSatisfa[3] = new int[] { 2, 1, 2, 4 };

	}

}
