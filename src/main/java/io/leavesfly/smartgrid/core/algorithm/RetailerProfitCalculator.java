package io.leavesfly.smartgrid.core.algorithm;

import io.leavesfly.smartgrid.core.config.SmartGridConfig;
import io.leavesfly.smartgrid.core.model.PriceVectorInterface;

/**
 * 零售商利润计算器（重构版）
 * 基于电价向量和总用电消耗计算零售商的利润
 * 
 * 利润计算公式：
 * profit = Σ(consumption[i] * price[i]) - w * Σ(a * consumption[i]² + b * consumption[i]³)
 * 
 * 主要改进：
 * 1. 实现ProfitCalculatorInterface接口，提高可扩展性
 * 2. 增强参数验证和错误处理
 * 3. 提供详细的计算过程分解
 * 4. 改进方法命名和代码结构
 * 5. 添加计算结果的详细分析功能
 * 
 * @author SmartGrid Team
 * @version 2.0
 */
public class RetailerProfitCalculator implements ProfitCalculatorInterface {
    
    /** 利润计算系数a */
    private float coefficientA;
    
    /** 利润计算系数b */
    private float coefficientB;
    
    /** 权重系数w */
    private int weightCoefficient;
    
    /**
     * 默认构造函数
     * 使用配置文件中的默认参数
     */
    public RetailerProfitCalculator() {
        this.coefficientA = SmartGridConfig.PROFIT_COEFFICIENT_A;
        this.coefficientB = SmartGridConfig.PROFIT_COEFFICIENT_B;
        this.weightCoefficient = SmartGridConfig.WEIGHT_COEFFICIENT;
    }
    
    /**
     * 自定义参数构造函数
     * @param coefficientA 系数a
     * @param coefficientB 系数b
     * @param weightCoefficient 权重系数w
     * @throws IllegalArgumentException 如果参数无效
     */
    public RetailerProfitCalculator(float coefficientA, float coefficientB, int weightCoefficient) {
        setParameters(coefficientA, coefficientB, weightCoefficient);
    }
    
    @Override
    public float calculateProfit(PriceVectorInterface priceVector, int[] totalConsumption) {
        validateInputs(priceVector, totalConsumption);
        
        // 计算收益部分：Σ(consumption[i] * price[i])
        float revenue = calculateRevenue(priceVector, totalConsumption);
        
        // 计算成本部分：w * Σ(a * consumption[i]² + b * consumption[i]³)
        float cost = calculateCost(totalConsumption);
        
        // 利润 = 收益 - 成本
        return revenue - cost;
    }
    
    @Override
    public void setParameters(float coefficientA, float coefficientB, int weight) {
        if (coefficientA < 0) {
            throw new IllegalArgumentException("系数a不能为负数: " + coefficientA);
        }
        if (coefficientB < 0) {
            throw new IllegalArgumentException("系数b不能为负数: " + coefficientB);
        }
        if (weight < 0) {
            throw new IllegalArgumentException("权重系数不能为负数: " + weight);
        }
        
        this.coefficientA = coefficientA;
        this.coefficientB = coefficientB;
        this.weightCoefficient = weight;
    }
    
    @Override
    public boolean validateParameters() {
        return coefficientA >= 0 && coefficientB >= 0 && weightCoefficient >= 0;
    }
    
    /**
     * 计算收益部分
     * @param priceVector 价格向量
     * @param totalConsumption 总消耗数组
     * @return 收益值
     */
    public float calculateRevenue(PriceVectorInterface priceVector, int[] totalConsumption) {
        validateInputs(priceVector, totalConsumption);
        
        float revenue = 0.0f;
        for (int i = 0; i < totalConsumption.length; i++) {
            revenue += totalConsumption[i] * priceVector.getPriceByPosition(i);
        }
        
        return revenue;
    }
    
    /**
     * 计算成本部分
     * @param totalConsumption 总消耗数组
     * @return 成本值
     */
    public float calculateCost(int[] totalConsumption) {
        if (totalConsumption == null) {
            throw new IllegalArgumentException("总消耗数组不能为null");
        }
        if (totalConsumption.length != SmartGridConfig.TIME_SLOTS) {
            throw new IllegalArgumentException(
                "总消耗数组长度必须为 " + SmartGridConfig.TIME_SLOTS);
        }
        
        float costSum = 0.0f;
        for (int consumption : totalConsumption) {
            if (consumption < 0) {
                throw new IllegalArgumentException("用电消耗不能为负数: " + consumption);
            }
            
            // 计算二次项和三次项
            float quadraticTerm = coefficientA * (float) Math.pow(consumption, 2);
            float cubicTerm = coefficientB * (float) Math.pow(consumption, 3);
            
            costSum += quadraticTerm + cubicTerm;
        }
        
        return weightCoefficient * costSum;
    }
    
    /**
     * 获取详细的利润计算分析
     * @param priceVector 价格向量
     * @param totalConsumption 总消耗数组
     * @return 利润计算分析对象
     */
    public ProfitAnalysis getDetailedProfitAnalysis(PriceVectorInterface priceVector, int[] totalConsumption) {
        validateInputs(priceVector, totalConsumption);
        
        float revenue = calculateRevenue(priceVector, totalConsumption);
        float cost = calculateCost(totalConsumption);
        float profit = revenue - cost;
        
        // 计算各时间段的收益贡献
        float[] revenueByTimeSlot = new float[SmartGridConfig.TIME_SLOTS];
        for (int i = 0; i < SmartGridConfig.TIME_SLOTS; i++) {
            revenueByTimeSlot[i] = totalConsumption[i] * priceVector.getPriceByPosition(i);
        }
        
        // 计算各时间段的成本贡献
        float[] costByTimeSlot = new float[SmartGridConfig.TIME_SLOTS];
        for (int i = 0; i < SmartGridConfig.TIME_SLOTS; i++) {
            int consumption = totalConsumption[i];
            float quadraticTerm = coefficientA * (float) Math.pow(consumption, 2);
            float cubicTerm = coefficientB * (float) Math.pow(consumption, 3);
            costByTimeSlot[i] = weightCoefficient * (quadraticTerm + cubicTerm);
        }
        
        return new ProfitAnalysis(profit, revenue, cost, revenueByTimeSlot, costByTimeSlot);
    }
    
    /**
     * 验证输入参数的有效性
     * @param priceVector 价格向量
     * @param totalConsumption 总消耗数组
     * @throws IllegalArgumentException 如果参数无效
     */
    private void validateInputs(PriceVectorInterface priceVector, int[] totalConsumption) {
        if (priceVector == null) {
            throw new IllegalArgumentException("价格向量不能为null");
        }
        if (totalConsumption == null) {
            throw new IllegalArgumentException("总消耗数组不能为null");
        }
        if (totalConsumption.length != priceVector.getTimeSlots()) {
            throw new IllegalArgumentException(
                "总消耗数组长度必须与价格向量时间槽数量一致");
        }
        if (!priceVector.isValid()) {
            throw new IllegalArgumentException("价格向量无效");
        }
        
        for (int consumption : totalConsumption) {
            if (consumption < 0) {
                throw new IllegalArgumentException("用电消耗不能为负数: " + consumption);
            }
        }
    }
    
    // ============== Getter方法 ==============
    
    public float getCoefficientA() {
        return coefficientA;
    }
    
    public float getCoefficientB() {
        return coefficientB;
    }
    
    public int getWeightCoefficient() {
        return weightCoefficient;
    }
    
    // ============== 兼容性方法（用于与旧代码兼容） ==============
    
    /**
     * 兼容旧代码的静态方法
     * @param consumByTime 系统消耗聚合对象（旧版本）
     * @param priceVector 价格向量（旧版本）
     * @return 利润值
     * @deprecated 请使用实例方法 {@link #calculateProfit(PriceVectorInterface, int[])}
     */
    @Deprecated
    public static float getRetialProfit(Object consumByTime, Object priceVector) {
        // 这里需要根据旧代码的具体类型进行适配
        // 暂时抛出异常提示使用新方法
        throw new UnsupportedOperationException(
            "已弃用的方法，请使用 RetailerProfitCalculator 实例的 calculateProfit 方法");
    }
    
    /**
     * 利润计算分析结果类
     * 包含详细的利润计算分解信息
     */
    public static class ProfitAnalysis {
        private final float totalProfit;
        private final float totalRevenue;
        private final float totalCost;
        private final float[] revenueByTimeSlot;
        private final float[] costByTimeSlot;
        
        public ProfitAnalysis(float totalProfit, float totalRevenue, float totalCost,
                            float[] revenueByTimeSlot, float[] costByTimeSlot) {
            this.totalProfit = totalProfit;
            this.totalRevenue = totalRevenue;
            this.totalCost = totalCost;
            this.revenueByTimeSlot = revenueByTimeSlot.clone();
            this.costByTimeSlot = costByTimeSlot.clone();
        }
        
        public float getTotalProfit() { return totalProfit; }
        public float getTotalRevenue() { return totalRevenue; }
        public float getTotalCost() { return totalCost; }
        public float[] getRevenueByTimeSlot() { return revenueByTimeSlot.clone(); }
        public float[] getCostByTimeSlot() { return costByTimeSlot.clone(); }
        
        public float getProfitMargin() {
            return totalRevenue == 0 ? 0 : totalProfit / totalRevenue;
        }
        
        public int getMostProfitableTimeSlot() {
            float maxProfit = revenueByTimeSlot[0] - costByTimeSlot[0];
            int maxIndex = 0;
            
            for (int i = 1; i < revenueByTimeSlot.length; i++) {
                float profit = revenueByTimeSlot[i] - costByTimeSlot[i];
                if (profit > maxProfit) {
                    maxProfit = profit;
                    maxIndex = i;
                }
            }
            
            return maxIndex;
        }
        
        @Override
        public String toString() {
            return String.format(
                "ProfitAnalysis{利润=%.3f, 收益=%.3f, 成本=%.3f, 利润率=%.3f}",
                totalProfit, totalRevenue, totalCost, getProfitMargin());
        }
    }
}