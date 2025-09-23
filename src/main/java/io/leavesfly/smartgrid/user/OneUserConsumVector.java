package io.leavesfly.smartgrid.user;

import java.io.Serializable;

/**
 * 单个用户用电向量模型
 * 
 * <p>该类用于封装单个用户在多个时段的用电量数据。
 * 作为智能电网系统中用户与零售商之间数据交换的核心数据结构，
 * 承载着用户对电价信储的用电响应信息。</p>
 * 
 * <p>主要功能特点：</p>
 * <ul>
 *   <li>存储用户ID和对应的用电向量数据</li>
 *   <li>实现Serializable接口，支持网络传输</li>
 *   <li>提供完整的getter/setter方法</li>
 *   <li>支持友好的字符串显示格式</li>
 * </ul>
 * 
 * <p>在系统架构中的作用：</p>
 * <ol>
 *   <li>用户线程接收到电价向量后，创建此对象</li>
 *   <li>通过UserMaxSatisfaConsumVector算法计算最优用电方案</li>
 *   <li>将计算结果通过Socket发送给零售商服务器</li>
 *   <li>零售商聚合所有用户响应进行下一轮优化</li>
 * </ol>
 * 
 * @author SmartGrid System
 * @version 1.0
 * @see UserMaxSatisfaConsumVector 用户满意度算法
 * @see UserThread 用户线程实现
 */
public class OneUserConsumVector implements Serializable {
	
	/** 序列化版本号，用于网络传输时的版本兼容性 */
	private static final long serialVersionUID = 2500492976644903992L;
	
	/** 系统时段数量常量，当前设定为4个时段 */
	public final static int timeSolts = 4;
	
	/** 用户唯一标识符 */
	private int userID;
	
	/** 用户在各个时段的用电向量，数组长度为timeSolts */
	private int[] consumVector;
	
	/**
	 * 构造函数
	 * 
	 * <p>初始化一个用户用电向量对象。通常在以下场景中使用：</p>
	 * <ul>
	 *   <li>UserThread接收到电价向量后创建新实例</li>
	 *   <li>传递给UserMaxSatisfaConsumVector进行用电量计算</li>
	 *   <li>测试和调试过程中模拟用户数据</li>
	 * </ul>
	 * 
	 * @param userID 用户唯一标识符，应在[0, UsersArgs.userNum)范围内
	 * @param consumVector 初始用电向量数组，长度应为timeSolts
	 */
	public OneUserConsumVector(int userID, int[] consumVector) {
		this.userID = userID;
		this.consumVector = consumVector;
	}

	/**
	 * 获取用户ID
	 * 
	 * <p>用户ID是系统中用户的唯一标识符，用于：</p>
	 * <ul>
	 *   <li>在UsersArgs中查找用户的配置参数</li>
	 *   <li>区分不同用户的用电数据</li>
	 *   <li>日志记录和调试输出</li>
	 * </ul>
	 * 
	 * @return 用户唯一标识符
	 */
	public int getUserID() {
		return userID;
	}

	/**
	 * 设置用户ID
	 * 
	 * @param userID 新的用户唯一标识符
	 */
	public void setUserID(int userID) {
		this.userID = userID;
	}

	/**
	 * 获取用电向量数组
	 * 
	 * <p>返回用户在各个时段的用电量数据。数组索引对应时段：</p>
	 * <ul>
	 *   <li>consumVector[0] - 第1个时段的用电量</li>
	 *   <li>consumVector[1] - 第2个时段的用电量</li>
	 *   <li>...</li>
	 *   <li>consumVector[timeSolts-1] - 最后一个时段的用电量</li>
	 * </ul>
	 * 
	 * <p>注意：返回的是原始数组的引用，修改返回值会影响原对象。</p>
	 * 
	 * @return 用电向量数组
	 */
	public int[] getConsumVector() {
		return consumVector;
	}

	/**
	 * 设置用电向量数组
	 * 
	 * <p>更新用户的用电向量数据。通常由UserMaxSatisfaConsumVector算法调用，
	 * 在计算出最优用电方案后更新此数据。</p>
	 * 
	 * @param consumVector 新的用电向量数组，长度应为timeSolts
	 */
	public void setConsumVector(int[] consumVector) {
		this.consumVector = consumVector;
	}

	/**
	 * 获取系统时段数量
	 * 
	 * <p>返回系统配置的时段数量常量。该值在整个系统中保持一致，
	 * 用于初始化数组大小和循环边界判断。</p>
	 * 
	 * @return 系统时段数量
	 */
	public static int getTimesolts() {
		return timeSolts;
	}
	
	/**
	 * 转换为字符串表示
	 * 
	 * <p>生成友好的字符串表示形式，用于日志输出和调试显示。</p>
	 * 
	 * <p>输出格式示例：</p>
	 * <pre>
	 * oneUserConsumVector:(5, 8, 6, 4)
	 * </pre>
	 * 
	 * <p>该方法广泛用于：</p>
	 * <ul>
	 *   <li>UserThread中的日志记录</li>
	 *   <li>系统调试和性能分析</li>
	 *   <li>结果验证和数据展示</li>
	 * </ul>
	 * 
	 * @return 格式化的字符串表示
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("oneUserConsumVector:(");
		for (int i = 0; i < timeSolts; i++) {
			if (i == timeSolts - 1) {
				// 最后一个元素不加逗号
				str.append(consumVector[i]);
			} else {
				// 非最后元素加逗号和空格
				str.append(consumVector[i]).append(", ");
			}
		}
		str.append(")");
		return str.toString();
	}
	
}
