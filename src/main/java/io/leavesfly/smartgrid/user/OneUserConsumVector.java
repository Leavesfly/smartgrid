package io.leavesfly.smartgrid.user;

import java.io.Serializable;

/**
 * 单个用户用电向量模型。
 * 封装用户 ID 及其在各时段的用电量数据，实现 Serializable 以支持 Socket 传输。
 *
 * 数据流：UserThread 接收电价 → 此对象经 UserMaxSatisfaConsumVector 计算 → 发送给零售商。
 *
 * @see UserMaxSatisfaConsumVector 用户满意度算法
 * @see UserThread 用户线程实现
 */
public class OneUserConsumVector implements Serializable {
	
	/** 序列化版本号 */
	private static final long serialVersionUID = 2500492976644903992L;

	/** 系统时段数量，对应论文 Example 2：H = {1,2,...,12} */
	public final static int timeSolts = 12;

	/** 用户唯一标识符 */
	private int userID;

	/** 各时段用电量（kWh），长度为 timeSolts，使用 double 以支持论文中 kWh 级浮点精度 */
	private double[] consumVector;
	
	/**
	 * 构造用户用电向量对象。
	 *
	 * @param userID       用户唯一标识符，范围 [0, UsersArgs.userNum)
	 * @param consumVector 初始用电向量，长度应为 timeSolts
	 */
	public OneUserConsumVector(int userID, double[] consumVector) {
		this.userID = userID;
		this.consumVector = consumVector;
	}

	/** @return 用户唯一标识符 */
	public int getUserID() {
		return userID;
	}

	/** @param userID 新的用户唯一标识符 */
	public void setUserID(int userID) {
		this.userID = userID;
	}

	/**
	 * 获取各时段用电向量。注意返回的是原始数组引用，修改会影响原对象。
	 *
	 * @return 用电向量数组（double[]）
	 */
	public double[] getConsumVector() {
		return consumVector;
	}

	/**
	 * 更新用电向量，通常由 UserMaxSatisfaConsumVector 在计算出最优方案后调用。
	 *
	 * @param consumVector 新的用电向量，长度应为 timeSolts
	 */
	public void setConsumVector(double[] consumVector) {
		this.consumVector = consumVector;
	}

	/** @return 系统时段数量常量 */
	public static int getTimesolts() {
		return timeSolts;
	}
	
	/**
	 * 返回格式化字符串，示例：oneUserConsumVector:(5.00, 8.12, 6.30, 4.00)
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("oneUserConsumVector:(");
		for (int i = 0; i < timeSolts; i++) {
			str.append(String.format("%.4f", consumVector[i]));
			if (i < timeSolts - 1) {
				str.append(", ");
			}
		}
		str.append(")");
		return str.toString();
	}
	
}
