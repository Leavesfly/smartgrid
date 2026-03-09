package io.leavesfly.smartgrid.user;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import io.leavesfly.smartgrid.retailer.PriceVector;

/**
 * 用户客户端线程。
 * 每个实例代表一个独立用户，负责与零售商服务器建立 Socket 连接，
 * 循环接收电价信号、计算最优用电响应并回传结果。
 *
 * 交互流程：连接服务器 → 接收 PriceVector → 计算 OneUserConsumVector → 发送响应 → 循环
 *
 * @see UserMaxSatisfaConsumVector 用户满意度算法
 * @see LogToTxtFile 日志记录工具
 */
public class UserThread implements Runnable {

	/** 用户唯一标识符 */
	private int userID;

	/**
	 * 初始化用户线程。
	 *
	 * @param userID 用户唯一标识符，范围 [0, UsersArgs.userNum)
	 */
	public UserThread(int userID) {
		this.userID = userID;
	}

	/**
	 * 线程主逻辑：建立连接 → 循环处理电价信号 → 关闭连接。
	 * 遇到结束信号（priceVector.isEnd()）时退出循环并清理资源。
	 */
	@Override
	public void run() {
		try (Socket socket = new Socket(UsersArgs.ip, UsersArgs.port)) {
			String connectionMsg = "User_" + userID + " socket:" + socket.getLocalPort() + " 已连接";
			System.out.println(connectionMsg);
			log(connectionMsg);

			ObjectInputStream objIn = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream());

			while (true) {
				// 接收零售商发送的电价向量
				PriceVector priceVector = (PriceVector) objIn.readObject();
				String receivedMsg = "User_" + userID + " 收到电价: " + priceVector;
				System.out.println(receivedMsg);
				log(receivedMsg);

				// 收到结束信号时退出
				if (priceVector.isEnd()) {
					log("结束信号: " + priceVector);
					LogToTxtFile.getWritelogtofile().flush();
					break;
				}

				// 计算最优用电向量并回传
				double[] consumVector = new double[UsersArgs.timeSlots];
				OneUserConsumVector oneUserConsumVector = new OneUserConsumVector(userID, consumVector);
				oneUserConsumVector = UserMaxSatisfaConsumVector
						.getConsumVectorByPriceVector(oneUserConsumVector, priceVector);
				objOut.writeObject(oneUserConsumVector);

				String responseMsg = "User_" + userID + " 发送用电响应: " + oneUserConsumVector;
				System.out.println(responseMsg);
				log(responseMsg);
				LogToTxtFile.getWritelogtofile().flush();
			}

			objOut.flush();
		} catch (Exception e) {
			System.err.println("用户线程 " + userID + " 发生异常:");
			e.printStackTrace();
		}
	}

	/** 向日志文件写入一行内容 */
	private void log(String message) {
		LogToTxtFile.getWritelogtofile().println(message);
	}

	/**
	 * 测试用户满意度算法的独立计算功能（不依赖网络连接）。
	 *
	 * @param args 命令行参数（未使用）
	 */
	public static void main(String[] args) {
		PriceVector priceVector = new PriceVector();
		System.out.println("测试电价向量: " + priceVector);

		double[] consumVector = new double[UsersArgs.timeSlots];
		OneUserConsumVector oneUserConsumVector = new OneUserConsumVector(0, consumVector);
		oneUserConsumVector = UserMaxSatisfaConsumVector
				.getConsumVectorByPriceVector(oneUserConsumVector, priceVector);

		System.out.println("计算结果: " + oneUserConsumVector);
	}

}
