package io.leavesfly.smartgrid.core.algorithm;

import io.leavesfly.smartgrid.core.model.PriceVectorInterface;
import io.leavesfly.smartgrid.core.model.ConsumptionVectorInterface;

/**
 * 优化算法接口
 * 定义电价优化算法的核心方法
 * 
 * @author SmartGrid Team
 * @version 2.0
 */
public interface OptimizationAlgorithmInterface {
    
    /**
     * 执行优化算法
     * @param initialPrice 初始价格向量
     * @return 优化后的价格向量
     * @throws Exception 算法执行异常
     */
    PriceVectorInterface optimize(PriceVectorInterface initialPrice) throws Exception;
    
    /**
     * 设置算法参数
     * @param initialTemperature 初始温度
     * @param endTemperature 终止温度
     */
    void setParameters(float initialTemperature, float endTemperature);
    
    /**
     * 获取当前迭代轮次
     * @return 当前轮次
     */
    int getCurrentRound();
    
    /**
     * 检查算法是否收敛
     * @return 如果收敛返回true
     */
    boolean hasConverged();
    
    /**
     * 重置算法状态
     */
    void reset();
}