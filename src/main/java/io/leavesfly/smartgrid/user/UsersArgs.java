package io.leavesfly.smartgrid.user;

/**
 * 用户系统配置参数类
 * 
 * <p>该类集中定义了智能电网系统中所有用户的配置参数和行为模型。
 * 作为系统的核心配置中心，它定义了用户的基本属性、电器特性、
 * 满意度模型和网络连接参数等。</p>
 * 
 * <p>数据模型设计：</p>
 * <ul>
 *   <li><strong>A类电器</strong>：不可调节电器，用电量固定（如冰箱、照明）</li>
 *   <li><strong>B类电器</strong>：可调节电器，根据电价动态调整（如空调、热水器）</li>
 *   <li><strong>用户满意度</strong>：反映用户对不同时段使用电器的偏好程度</li>
 * </ul>
 * 
 * <p>系统参数说明：</p>
 * <ul>
 *   <li>支持多用户并发访问</li>
 *   <li>时段化电价机制</li>
 *   <li>个性化用户行为模型</li>
 *   <li>负载均衡和安全限制</li>
 * </ul>
 * 
 * @author SmartGrid System
 * @version 1.0
 * @see UserThread 用户线程实现
 * @see UserMaxSatisfaConsumVector 用户满意度算法
 */

public class UsersArgs {
	
	// ================================
	// 网络连接配置参数
	// ================================
	
	/** 零售商服务器IP地址，默认为本地地址 */
	public final static String ip = "127.0.0.1";
	
	/** 零售商服务器端口号 */
	public final static int port = 1234;
	
	// ================================
	// 系统基本配置参数
	// ================================
	
	/** 系统时段数量，将一天分为4个时段进行分时电价 */
	public final static int timeSlots = 4;
	
	/** 系统用户总数，当前配置为2个用户 */
	public final static int userNum = 2;
	
	/** A类电器数量（不可调节电器） */
	public final static int A_applianceNum = 4;
	
	/** B类电器数量（可调节电器） */
	public final static int B_applianceNum = 4;
	
	/** 用户日志文件存储路径 */
	public final static String usersLogFile = "E://UsersLog.txt";

	// ================================
	// 用户个性化配置数据
	// ================================

	/** 
	 * 用户最大负载限制数组
	 * 
	 * <p>定义每个用户在任意时段的最大允许用电量。该限制用于：</p>
	 * <ul>
	 *   <li>防止电路过载和安全问题</li>
	 *   <li>约束用户的最大用电需求</li>
	 *   <li>保证系统的稳定运行</li>
	 * </ul>
	 * 
	 * <p>数组索引：userMax[userID] = 该用户的最大负载限制</p>
	 */
	public final static int[] userMax = new int[userNum];
	
	/** 
	 * A类电器用电量矩阵（不可调节电器）
	 * 
	 * <p>A类电器代表用户日常生活中不可关闭或调节的电器，如冰箱、照明等。
	 * 它们的用电量固定不变，不受电价影响。</p>
	 * 
	 * <p>数据结构：</p>
	 * <ul>
	 *   <li>第一维：用户ID（范围：0 ~ userNum-1）</li>
	 *   <li>第二维：时段索引（范围：0 ~ timeSlots-1）</li>
	 * </ul>
	 * 
	 * <p>访问方式：A_applianceConsum[userID][time_h] = 该用户在该时段的A类电器总用电量</p>
	 */
	public final static int[][] A_applianceConsum = new int[userNum][A_applianceNum];
	
	/** 
	 * B类电器最大用电量矩阵（可调节电器）
	 * 
	 * <p>B类电器代表用户可以根据电价和个人偏好调节的电器，如空调、热水器等。
	 * 此矩阵定义了每个B类电器的物理使用上限。</p>
	 * 
	 * <p>数据结构：</p>
	 * <ul>
	 *   <li>第一维：用户ID（范围：0 ~ userNum-1）</li>
	 *   <li>第二维：B类电器索引（范围：0 ~ B_applianceNum-1）</li>
	 * </ul>
	 * 
	 * <p>访问方式：B_applianceConsumMax[userID][applianceNum] = 该用户该电器的最大用电量</p>
	 */
	public final static int[][] B_applianceConsumMax = new int[userNum][B_applianceNum];
	
	/** 
	 * 用户B类电器满意度三维数组
	 * 
	 * <p>该数组是用户行为模型的核心，定义了每个用户对每个B类电器在不同时段的使用满意度。
	 * 满意度数值越高，表明用户在该时段使用该电器的意愿越强。</p>
	 * 
	 * <p>满意度在算法中的作用：</p>
	 * <ul>
	 *   <li>作为B类电器用电量计算公式的分母项</li>
	 *   <li>满意度越高，在相同电价下用电量越大</li>
	 *   <li>与电价共同决定用户的用电策略</li>
	 * </ul>
	 * 
	 * <p>数据结构：</p>
	 * <ul>
	 *   <li>第一维：用户ID（范围：0 ~ userNum-1）</li>
	 *   <li>第二维：B类电器索引（范围：0 ~ B_applianceNum-1）</li>
	 *   <li>第三维：时段索引（范围：0 ~ timeSlots-1）</li>
	 * </ul>
	 * 
	 * <p>访问方式：users_B_applianceSatisfa[userID][applianceNum][time_h] = 满意度数值</p>
	 */
	public final static int[][][] users_B_applianceSatisfa = new int[2][][];
	
	/** 用户0的B类电器满意度矩阵 */
	public final static int[][] user_0_B_applianceSatisfa = new int[B_applianceNum][timeSlots];
	
	/** 用户1的B类电器满意度矩阵 */
	public final static int[][] user_1_B_applianceSatisfa = new int[B_applianceNum][timeSlots];

	/**
	 * 静态初始化块
	 * 
	 * <p>在类加载时自动执行，初始化所有的用户配置数据。
	 * 这些数据定义了两个模拟用户的完整行为特征。</p>
	 * 
	 * <p>初始化内容包括：</p>
	 * <ol>
	 *   <li>用户最大负载限制</li>
	 *   <li>A类电器各时段固定用电量</li>
	 *   <li>B类电器最大用电量限制</li>
	 *   <li>用户满意度矩阵关联</li>
	 *   <li>各用户各电器各时段的满意度数值</li>
	 * </ol>
	 * 
	 * <p>数据说明：</p>
	 * <ul>
	 *   <li><strong>用户0</strong>：最大负载10，低电价敏感的理性用户</li>
	 *   <li><strong>用户1</strong>：最大负载12，高满意度需求的用户</li>
	 * </ul>
	 */
	static {
		// 1. 初始化用户最大负载限制
		userMax[0] = 10;  // 用户0的最大负载：10单位
		userMax[1] = 12;  // 用户1的最大负载：12单位

		// 2. 初始化A类电器在各时段的固定用电量
		// 用户0的A类电器用电量: [时段0, 时段1, 时段2, 时段3]
		A_applianceConsum[0] = new int[] { 1, 2, 3, 1 };
		// 用户1的A类电器用电量: [时段0, 时段1, 时段2, 时段3]
		A_applianceConsum[1] = new int[] { 1, 3, 3, 1 };

		// 3. 初始化B类电器的最大用电量限制
		// 用户0的B类电器最大用电量: [电器0, 电器1, 电器2, 电器3]
		B_applianceConsumMax[0] = new int[] { 2, 3, 4, 4 };
		// 用户1的B类电器最大用电量: [电器0, 电器1, 电器2, 电器3]
		B_applianceConsumMax[1] = new int[] { 2, 3, 1, 3 };

		// 4. 关联用户满意度矩阵
		users_B_applianceSatisfa[0] = user_0_B_applianceSatisfa;
		users_B_applianceSatisfa[1] = user_1_B_applianceSatisfa;

		// 5. 初始化用户0的B类电器满意度数据
		// 每行代表一个B类电器，每列代表一个时段的满意度
		user_0_B_applianceSatisfa[0] = new int[] { 2, 4, 5, 3 };  // B类电器0
		user_0_B_applianceSatisfa[1] = new int[] { 1, 3, 6, 3 };  // B类电器1
		user_0_B_applianceSatisfa[2] = new int[] { 2, 5, 3, 4 };  // B类电器2
		user_0_B_applianceSatisfa[3] = new int[] { 4, 1, 4, 3 };  // B类电器3

		// 6. 初始化用户1的B类电器满意度数据
		// 每行代表一个B类电器，每列代表一个时段的满意度
		user_1_B_applianceSatisfa[0] = new int[] { 2, 2, 5, 3 };  // B类电器0
		user_1_B_applianceSatisfa[1] = new int[] { 1, 6, 1, 3 };  // B类电器1
		user_1_B_applianceSatisfa[2] = new int[] { 2, 3, 5, 3 };  // B类电器2
		user_1_B_applianceSatisfa[3] = new int[] { 2, 1, 2, 4 };  // B类电器3

	}

}
