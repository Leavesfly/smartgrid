package io.leavesfly.smartgrid.user;


public class Users {

	public static void main(String[] args) throws Exception {
		for (int i = 0; i < UsersArgs.userNum; i++) {
			System.out.println("user_" + i + "启动...");
			Thread user = new Thread(new UserThread(i));
			user.start();
		}
	}
}
