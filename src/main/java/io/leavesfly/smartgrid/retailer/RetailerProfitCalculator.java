package io.leavesfly.smartgrid.retailer;

/**
 * 零售商利润计算算法类
 * 实现智能电网中零售商的利润计算逻辑
 * 利润计算公式：Profit = ∑(consumption[i] * price[i]) - w * ∑(a * consumption[i]^2 + b * consumption[i]^3)
 * 
 * @author SmartGrid Team
 * @version 1.0
 */
public final class RetailerProfitCalculator {
    
    /**
     * 计算零售商利润
     * 基于给定的消耗数据和价格向量计算零售商的利润
     * 
     * @param consumptionByTime 按时间段分组的电力消耗数据
     * @param priceVector 价格向量数据
     * @return 计算得到的零售商利润，如果输入数据不匹配则返回-1
     */
    public static float calculateRetailerProfit(ElectricityConsumptionByTime consumptionByTime, 
                                               PriceVector priceVector) {
        
        // 参数获取
        final float paramA = RetailerConfigConstants.PROFIT_PARAM_A;
        final float paramB = RetailerConfigConstants.PROFIT_PARAM_B;
        final int weight = RetailerConfigConstants.PROFIT_WEIGHT;
        
        // 数据验证：检查消耗数据和价格数据的长度是否匹配
        if (consumptionByTime.getConsumptionByTimeVector().length != PriceVector.getTimeSlots()) {
            return -1f;
        }
        
        // 计算收入：∑(consumption[i] * price[i])
        float revenue = calculateRevenue(consumptionByTime, priceVector);
        
        // 计算成本：w * ∑(a * consumption[i]^2 + b * consumption[i]^3)
        float cost = calculateCost(consumptionByTime, paramA, paramB, weight);
        
        // 计算利润 = 收入 - 成本
        return revenue - cost;
    }
    
    /**
     * 计算收入
     * 计算公式：∑(consumption[i] * price[i])
     * 
     * @param consumptionByTime 消耗数据
     * @param priceVector 价格向量
     * @return 收入值
     */
    private static float calculateRevenue(ElectricityConsumptionByTime consumptionByTime, 
                                        PriceVector priceVector) {
        float revenue = 0f;
        int[] consumption = consumptionByTime.getConsumptionByTimeVector();
        float[] prices = priceVector.getPriceArray();
        
        for (int i = 0; i < consumption.length; i++) {
            revenue += consumption[i] * prices[i];
        }
        
        return revenue;
    }
    
    /**
     * 计算成本
     * 计算公式：w * ∑(a * consumption[i]^2 + b * consumption[i]^3)
     * 
     * @param consumptionByTime 消耗数据
     * @param paramA 二次项系数
     * @param paramB 三次项系数
     * @param weight 权重系数
     * @return 成本值
     */
    private static float calculateCost(ElectricityConsumptionByTime consumptionByTime, 
                                     float paramA, float paramB, int weight) {
        float totalCost = 0f;
        int[] consumption = consumptionByTime.getConsumptionByTimeVector();
        
        for (int i = 0; i < consumption.length; i++) {
            // 计算 a * consumption[i]^2 + b * consumption[i]^3
            float quadraticCost = paramA * (float) Math.pow(consumption[i], 2);
            float cubicCost = paramB * (float) Math.pow(consumption[i], 3);
            totalCost += quadraticCost + cubicCost;
        }
        
        return weight * totalCost;
    }
    
    /**
     * 测试方法
     * 用于验证利润计算算法的正确性
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 创建测试数据
        float[] testPrices = {0.5f, 0.8f, 1.0f, 1.5f};
        PriceVector priceVector = new PriceVector(testPrices);
        
        ElectricityConsumptionByTime consumptionByTime = new ElectricityConsumptionByTime();
        consumptionByTime.setConsumptionByTimeVector(new int[]{2, 2, 2, 3});
        
        // 计算并输出结果
        float profit = RetailerProfitCalculator.calculateRetailerProfit(consumptionByTime, priceVector);
        System.out.println("计算得到的零售商利润: " + profit);
        
        // 记录日志
        RetailerLogger.logInfo("利润计算测试完成，结果: " + profit);
    }
    
    // 私有构造函数，禁止实例化工具类
    private RetailerProfitCalculator() {
        throw new UnsupportedOperationException("此类为工具类，不允许实例化");
    }
}
