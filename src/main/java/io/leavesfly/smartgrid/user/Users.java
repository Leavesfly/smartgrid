package io.leavesfly.smartgrid.user;

/**
 * 用户系统启动器
 * 
 * <p>该类负责启动所有的用户线程，是智能电网系统中用户端的入口程序。
 * 根据UsersArgs中的配置参数，创建和启动指定数量的用户线程，
 * 每个线程代表一个独立的用户客户端。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>批量创建用户线程</li>
 *   <li>为每个线程分配唯一的用户ID</li>
 *   <li>并发启动所有用户线程</li>
 *   <li>输出启动状态信息</li>
 * </ul>
 * 
 * <p>在系统架构中的作用：</p>
 * <ul>
 *   <li>与零售商服务器（RetailerServer）对应</li>
 *   <li>每个用户线程与一个零售商线程（RetailerThread）连接</li>
 *   <li>形成多对多的客户端-服务器通信架构</li>
 * </ul>
 * 
 * <p>使用方式：</p>
 * <pre>
 * // 在启动零售商服务器后运行
 * java io.leavesfly.smartgrid.user.Users
 * </pre>
 * 
 * @author SmartGrid System
 * @version 1.0
 * @see UsersArgs 用户配置参数
 * @see UserThread 用户线程实现
 */
public class Users {

	/**
	 * 程序入口方法
	 * 
	 * <p>批量创建和启动用户线程。线程数量由UsersArgs.userNum配置，
	 * 每个线程都将获得一个唯一的用户ID（从0开始）。</p>
	 * 
	 * <p>启动流程：</p>
	 * <ol>
	 *   <li>读取UsersArgs.userNum获取需要创建的线程数量</li>
	 *   <li>循环创建每个用户线程</li>
	 *   <li>为每个线程分配唯一的userID</li>
	 *   <li>输出启动状态信息</li>
	 *   <li>启动线程</li>
	 * </ol>
	 * 
	 * <p>注意事项：</p>
	 * <ul>
	 *   <li>在运行此程序之前，必须先启动零售商服务器</li>
	 *   <li>所有线程将同时运行，形成并发访问模式</li>
	 *   <li>程序不会等待线程结束，主线程立即退出</li>
	 * </ul>
	 * 
	 * @param args 命令行参数（未使用）
	 * @throws Exception 线程创建或启动过程中可能抛出的异常
	 */
	public static void main(String[] args) throws Exception {
		// 输出系统启动信息
		System.out.println("智能电网用户系统启动中...");
		System.out.println("将创建 " + UsersArgs.userNum + " 个用户线程");
		
		// 批量创建和启动用户线程
		for (int i = 0; i < UsersArgs.userNum; i++) {
			// 输出单个线程的启动信息
			System.out.println("user_" + i + " 启动...");
			
			// 创建新的用户线程实例
			Thread user = new Thread(new UserThread(i));
			
			// 启动线程（非阻塞）
			user.start();
		}
		
		// 输出全部线程启动完成信息
		System.out.println("所有用户线程已启动完成，正在连接服务器...");
	}
}
