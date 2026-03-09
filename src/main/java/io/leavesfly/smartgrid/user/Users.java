package io.leavesfly.smartgrid.user;

/**
 * 用户系统启动入口。
 * 批量创建并并发启动 UsersArgs.userNum 个用户线程，每个线程对应一个独立的用户客户端。
 * 运行前需确保零售商服务器（RetailerServer）已启动。
 *
 * @see UsersArgs 用户配置参数
 * @see UserThread 用户线程实现
 */
public class Users {

	/**
	 * 批量创建并启动用户线程，userID 从 0 开始依次分配。
	 *
	 * @param args 命令行参数（未使用）
	 * @throws Exception 线程启动过程中可能抛出的异常
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("智能电网用户系统启动，将创建 " + UsersArgs.userNum + " 个用户线程");

		for (int i = 0; i < UsersArgs.userNum; i++) {
			System.out.println("user_" + i + " 启动...");
			new Thread(new UserThread(i)).start();
		}

		System.out.println("所有用户线程已启动，正在连接服务器...");
	}
}
