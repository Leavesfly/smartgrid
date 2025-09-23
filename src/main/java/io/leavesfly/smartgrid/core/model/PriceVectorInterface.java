package io.leavesfly.smartgrid.core.model;

/**
 * 价格向量接口
 * 定义电价向量的核心操作方法
 * 
 * @author SmartGrid Team
 * @version 2.0
 */
public interface PriceVectorInterface {
    
    /**
     * 获取指定位置的价格
     * @param position 时间段位置
     * @return 该时间段的价格
     */
    float getPriceByPosition(int position);
    
    /**
     * 设置指定位置的价格
     * @param position 时间段位置
     * @param price 要设置的价格
     */
    void setPriceByPosition(int position, float price);
    
    /**
     * 获取价格数组的副本
     * @return 价格数组副本
     */
    float[] getPricesCopy();
    
    /**
     * 获取时间槽数量
     * @return 时间槽数量
     */
    int getTimeSlots();
    
    /**
     * 创建新的价格向量实例
     * @param position 要修改的位置
     * @param newPrice 新价格
     * @return 新的价格向量实例
     */
    PriceVectorInterface createNewPriceVector(int position, float newPrice);
    
    /**
     * 验证价格向量是否有效
     * @return 如果有效返回true
     */
    boolean isValid();
    
    /**
     * 复制另一个价格向量的值
     * @param other 要复制的价格向量
     */
    void copyFrom(PriceVectorInterface other);
}