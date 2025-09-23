package io.leavesfly.smartgrid.core.model;

/**
 * 用电消耗向量接口
 * 定义用户用电消耗数据的核心操作方法
 * 
 * @author SmartGrid Team
 * @version 2.0
 */
public interface ConsumptionVectorInterface {
    
    /**
     * 获取用户ID
     * @return 用户ID
     */
    int getUserId();
    
    /**
     * 获取指定时间段的用电量
     * @param timeSlot 时间段
     * @return 该时间段的用电量
     */
    int getConsumptionByTimeSlot(int timeSlot);
    
    /**
     * 设置指定时间段的用电量
     * @param timeSlot 时间段
     * @param consumption 用电量
     */
    void setConsumptionByTimeSlot(int timeSlot, int consumption);
    
    /**
     * 获取用电量数组的副本
     * @return 用电量数组副本
     */
    int[] getConsumptionsCopy();
    
    /**
     * 获取总用电量
     * @return 总用电量
     */
    int getTotalConsumption();
    
    /**
     * 验证用电数据是否有效
     * @return 如果有效返回true
     */
    boolean isValid();
    
    /**
     * 重置所有时间段的用电量为0
     */
    void reset();
}