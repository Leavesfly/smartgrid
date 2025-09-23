package io.leavesfly.smartgrid.user;

import io.leavesfly.smartgrid.retailer.PriceVector;

/**
 * 用户满意度最大化用电策略计算器
 * 
 * <p>该类实现智能电网系统中的用户用电行为建模算法。
 * 根据实时电价向量，为用户计算出一个最优的用电量向量，
 * 目标是<strong>在满足用户用电需求和设备限制的前提下，最大化用户的用电满意度</strong>。</p>
 * 
 * <p>算法设计原理：</p>
 * <ul>
 *   <li><strong>A类电器</strong>：不可调节电器，用电量固定，由UsersArgs.A_applianceConsum定义</li>
 *   <li><strong>B类电器</strong>：可调节电器，根据电价和用户满意度动态调整用电量</li>
 *   <li><strong>负荷限制</strong>：总用电量不能超过用户设定的最大值</li>
 * </ul>
 * 
 * <p>B类电器用电量计算公式：</p>
 * <pre>
 * 用电量 = min(1.5 * (时段+1) / (满意度 + 电价), 最大允许用电量)
 * </pre>
 * 
 * <p>该公式体现了以下经济学原理：</p>
 * <ul>
 *   <li>用户在满意度高的时段更愿意用电</li>
 *   <li>高电价会抑制用电需求</li>
 *   <li>时段系数反映了时间的价值权重</li>
 * </ul>
 * 
 * <p>在系统交互中的作用：</p>
 * <ol>
 *   <li>零售商（SAPC_Algorithm）发送电价向量</li>
 *   <li>用户模型计算用电响应向量</li>
 *   <li>用电向量返回给零售商</li>
 *   <li>零售商根据响应计算利润并进行下一轮优化</li>
 * </ol>
 * 
 * @author SmartGrid System
 * @version 1.0
 * @see UsersArgs 用户配置参数
 * @see PriceVector 电价向量模型
 * @see OneUserConsumVector 用户用电向量模型
 */
public class UserMaxSatisfaConsumVector {
	
	/**
	 * 根据电价向量计算用户最优用电向量
	 * 
	 * <p>该方法是整个用户行为模型的入口点。它接收一个OneUserConsumVector对象和一个
	 * PriceVector对象作为输入，计算并返回更新后的用电向量。</p>
	 * 
	 * <p>算法流程：</p>
	 * <ol>
	 *   <li>遍历每个时段（time_h）</li>
	 *   <li>计算所有B类电器的总用电量</li>
	 *   <li>加上A类电器的固定用电量</li>
	 *   <li>检查是否超过用户最大负荷限制</li>
	 *   <li>更新用电向量对应时段的数值</li>
	 * </ol>
	 * 
	 * <p>使用示例：</p>
	 * <pre>
	 * // 创建用户用电向量
	 * int[] consumVector = new int[UsersArgs.timeSlots];
	 * OneUserConsumVector userVector = new OneUserConsumVector(0, consumVector);
	 * 
	 * // 计算最优用电方案
	 * userVector = UserMaxSatisfaConsumVector.getConsumVectorByPriceVector(userVector, priceVector);
	 * </pre>
	 * 
	 * @param oneUserConsumVector 用户用电向量对象，包含用户ID和初始用电数据
	 * @param priceVector 电价向量对象，包含各个时段的电价信息
	 * @return 更新后的用户用电向量对象，其consumVector已被计算为最优值
	 */
	public static OneUserConsumVector getConsumVectorByPriceVector(
			OneUserConsumVector oneUserConsumVector, PriceVector priceVector) {

		// 遍历每个时段，计算最优用电量
		for (int time_h = 0; time_h < UsersArgs.timeSlots; time_h++) {

			int finalConsumption = 0;  // 该时段的最终用电量
			float bApplianceSum = 0f;  // B类电器总用电量
			
			// 计算所有B类电器在当前时段的用电量
			for (int applianceNum = 0; applianceNum < UsersArgs.B_applianceNum; applianceNum++) {
				bApplianceSum += UserMaxSatisfaConsumVector
						.getOneB_applianceConsumOneUser(priceVector,
								oneUserConsumVector.getUserID(), time_h,
								applianceNum);
			}
			
			// 计算总用电量：B类电器用电量 + A类电器固定用电量
			int totalConsumption = (int) bApplianceSum
					+ UsersArgs.A_applianceConsum[oneUserConsumVector
							.getUserID()][time_h];
			
			// 检查是否超过用户设定的最大负荷限制
			if (totalConsumption > UsersArgs.userMax[oneUserConsumVector
					.getUserID()]) {
				// 超过限制时，将用电量设置为用户最大允许值
				finalConsumption = UsersArgs.userMax[oneUserConsumVector.getUserID()];
			} else {
				// 未超过限制时，使用计算出的实际值
				finalConsumption = totalConsumption;
			}

			// 更新用电向量对应时段的数值
			oneUserConsumVector.getConsumVector()[time_h] = finalConsumption;
		}

		return oneUserConsumVector;
	}

	/**
	 * 计算单个B类电器在特定时段的用电量
	 * 
	 * <p>该方法实现了B类电器的智能调节算法。通过综合考虑电价、用户满意度和时间因子，
	 * 计算出单个电器的最优用电量。</p>
	 * 
	 * <p>计算公式详解：</p>
	 * <ul>
	 *   <li><strong>1.5 * (time_h + 1)</strong>：时间因子，体现不同时段的基础需求差异</li>
	 *   <li><strong>满意度 + 电价</strong>：成本因子，满意度越高或电价越低，用电量越大</li>
	 *   <li><strong>min(..., 最大允许用电量)</strong>：物理限制，确保不超过电器额定功率</li>
	 * </ul>
	 * 
	 * <p>该算法的经济学意义：</p>
	 * <ul>
	 *   <li>价格弹性：电价上升时，用电需求下降</li>
	 *   <li>满意度驱动：用户在高满意度时段更愿意用电</li>
	 *   <li>时间价值：不同时段的用电需求具有不同的权重</li>
	 * </ul>
	 * 
	 * @param priceVector 电价向量对象，用于获取指定时段的电价
	 * @param userID 用户唯一标识符，用于查找用户的配置参数
	 * @param time_h 时段索引，范围为[0, UsersArgs.timeSlots)
	 * @param applianceNum B类电器索引，范围为[0, UsersArgs.B_applianceNum)
	 * @return 该B类电器在指定时段的最优用电量（浮点数）
	 */
	public static float getOneB_applianceConsumOneUser(PriceVector priceVector,
			int userID, int time_h, int applianceNum) {
		
		// 基于满意度和电价的用电量计算
		float calculatedConsumption = (float) (1.5 * (time_h + 1))
				/ (float) (UsersArgs.users_B_applianceSatisfa[userID][applianceNum][time_h] 
						+ (priceVector.getPriceByPosition(time_h)));
		
		// 检查是否超过该电器的最大允许用电量
		if (calculatedConsumption > UsersArgs.B_applianceConsumMax[userID][applianceNum]) {
			// 超过最大值时，返回最大允许值
			return (float) UsersArgs.B_applianceConsumMax[userID][applianceNum];
		}
		
		// 未超过最大值时，返回计算值
		return calculatedConsumption;
	}
}
