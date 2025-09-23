package io.leavesfly.smartgrid.core.algorithm;

import io.leavesfly.smartgrid.core.model.PriceVectorInterface;
import io.leavesfly.smartgrid.core.model.ConsumptionVectorInterface;

/**
 * 利润计算接口
 * 定义零售商利润计算的核心方法
 * 
 * @author SmartGrid Team
 * @version 2.0
 */
public interface ProfitCalculatorInterface {
    
    /**
     * 计算零售商利润
     * @param priceVector 价格向量
     * @param totalConsumption 总用电消耗数据
     * @return 计算得出的利润值
     */
    float calculateProfit(PriceVectorInterface priceVector, int[] totalConsumption);
    
    /**
     * 设置利润计算参数
     * @param coefficientA 参数a
     * @param coefficientB 参数b
     * @param weight 权重系数
     */
    void setParameters(float coefficientA, float coefficientB, int weight);
    
    /**
     * 验证计算参数是否有效
     * @return 如果参数有效返回true
     */
    boolean validateParameters();
}