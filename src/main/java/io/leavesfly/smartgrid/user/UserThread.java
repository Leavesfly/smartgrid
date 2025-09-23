package io.leavesfly.smartgrid.user;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import io.leavesfly.smartgrid.retailer.PriceVector;

/**
 * 用户线程实现类
 * 
 * <p>该类实现智能电网系统中的用户端线程逻辑，每个用户线程代表一个独立的用户客户端。
 * 主要负责与零售商服务器建立连接、接收电价信号、计算用电响应并发送回服务器。</p>
 * 
 * <p>核心功能：</p>
 * <ul>
 *   <li>建立与零售商服务器的Socket连接</li>
 *   <li>循环接收服务器发送的电价向量</li>
 *   <li>调用用户满意度算法计算最优用电方案</li>
 *   <li>将用电响应向量发送回服务器</li>
 *   <li>记录运行日志和调试信息</li>
 * </ul>
 * 
 * <p>工作流程：</p>
 * <ol>
 *   <li>连接到零售商服务器</li>
 *   <li>接收PriceVector对象</li>
 *   <li>检查是否为结束信号</li>
 *   <li>计算用电响应向量</li>
 *   <li>发送OneUserConsumVector对象</li>
 *   <li>记录日志并继续下一轮</li>
 * </ol>
 * 
 * @author SmartGrid System
 * @version 1.0
 * @see UsersArgs 用户配置参数
 * @see UserMaxSatisfaConsumVector 用户满意度算法
 * @see LogToTxtFile 日志记录工具
 */

public class UserThread implements Runnable {
	
	/** 用户唯一标识符，用于区分不同的用户线程 */
	private int userID;

	/**
	 * 构造函数
	 * 
	 * <p>初始化用户线程，设置用户ID。该ID将用于：</p>
	 * <ul>
	 *   <li>在UsersArgs中查找用户的个性化配置参数</li>
	 *   <li>日志记录和调试输出中的用户标识</li>
	 *   <li>与零售商服务器的通信标识</li>
	 * </ul>
	 * 
	 * @param userID 用户唯一标识符，应在[0, UsersArgs.userNum)范围内
	 */
	public UserThread(int userID) {
		this.userID = userID;
	}

	/**
	 * 线程主执行方法
	 * 
	 * <p>实现用户线程的主要逻辑，包括网络连接建立、数据交换和日志记录。
	 * 该方法将持续运行直到接收到结束信号或发生异常。</p>
	 * 
	 * <p>主要步骤：</p>
	 * <ol>
	 *   <li>建立与零售商服务器的Socket连接</li>
	 *   <li>初始化对象输入输出流</li>
	 *   <li>进入主循环，处理电价信号</li>
	 *   <li>关闭连接和资源</li>
	 * </ol>
	 * 
	 * <p>异常处理：</p>
	 * <ul>
	 *   <li>网络连接异常</li>
	 *   <li>数据序列化/反序列化异常</li>
	 *   <li>I/O操作异常</li>
	 * </ul>
	 */
	@Override
	public void run() {
		try {
			// 步險1: 建立与零售商服务器的Socket连接
			Socket socket = new Socket(UsersArgs.ip, UsersArgs.port);
			
			// 记录连接成功信息
			String connectionMsg = "User_" + userID + "socket" + socket.getLocalPort() + "...";
			System.out.println(connectionMsg);
			LogToTxtFile.getWritelogtofile().println(connectionMsg);

			// 步險2: 初始化对象输入输出流
			ObjectInputStream objIn = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream());
			
			// 步險3: 主循环 - 处理电价信号并计算响应
			while (true) {
			
				// 3.1 接收零售商发送的电价向量
				PriceVector priceVector = (PriceVector) objIn.readObject();

				// 3.2 记录接收到的电价信息
				String receivedMsg = "User_" + userID + "userID" + priceVector.toString();
				System.out.println(receivedMsg);
				LogToTxtFile.getWritelogtofile().println(receivedMsg);
				
				// 3.3 检查是否为结束信号
				if (priceVector.isEnd()) {
					// 接收到结束信号，记录日志并退出循环
					String endMsg = "priceVector:" + priceVector.toString();
					System.out.println(endMsg);
					LogToTxtFile.getWritelogtofile().println(endMsg);
					LogToTxtFile.getWritelogtofile().flush();
					break;
				}
				// 3.4 创建用户用电向量对象
				int[] consumVector = new int[UsersArgs.timeSlots];
				OneUserConsumVector oneUserConsumVector = new OneUserConsumVector(
						userID, consumVector);
				
				// 3.5 调用用户满意度算法计算最优用电方案
				oneUserConsumVector = UserMaxSatisfaConsumVector
						.getConsumVectorByPriceVector(oneUserConsumVector, priceVector);
				
				// 3.6 将计算结果发送回零售商服务器
				objOut.writeObject(oneUserConsumVector);
				
				// 3.7 记录发送的用电响应信息
				String responseMsg = "User_" + userID + "userID" + oneUserConsumVector.toString();
				System.out.println(responseMsg);
				LogToTxtFile.getWritelogtofile().println(responseMsg);
				LogToTxtFile.getWritelogtofile().flush();
			}
			
			// 步險4: 清理资源并关闭连接
			objOut.flush();  // 刷新输出流，确保数据发送完成
			objIn.close();   // 关闭对象输入流
			objOut.close();  // 关闭对象输出流
			socket.close();  // 关闭Socket连接

		} catch (Exception e) {
			// 异常处理：记录异常信息并打印堆栈跟踪
			System.err.println("用户线程 " + userID + " 发生异常:");
			e.printStackTrace();
		}
	}

	/**
	 * 测试方法
	 * 
	 * <p>用于验证用户满意度算法的正确性。该方法不依赖网络连接，
	 * 直接测试算法的计算功能。</p>
	 * 
	 * <p>测试步骤：</p>
	 * <ol>
	 *   <li>创建默认的电价向量</li>
	 *   <li>创建空的用户用电向量</li>
	 *   <li>调用算法计算最优用电方案</li>
	 *   <li>输出计算结果</li>
	 * </ol>
	 * 
	 * @param args 命令行参数（未使用）
	 */
	public static void main(String[] args) {

		// 创建默认电价向量用于测试
		PriceVector priceVector = new PriceVector();
		System.out.println("测试电价向量: " + priceVector.toString());
		
		// 创建空的用户用电向量（用户ID=0）
		int[] consumVector = new int[UsersArgs.timeSlots];
		OneUserConsumVector oneUserConsumVector = new OneUserConsumVector(0, consumVector);
		
		// 调用算法计算最优用电方案
		oneUserConsumVector = UserMaxSatisfaConsumVector
				.getConsumVectorByPriceVector(oneUserConsumVector, priceVector);
		
		// 输出计算结果
		System.out.println("计算结果: " + oneUserConsumVector.toString());
	}

}
