package io.leavesfly.smartgrid.retailer;

import java.io.Serializable;

/**
 * 价格向量类
 * 表示电力在不同时间段的价格向量，用于动态定价策略
 * 支持序列化，可在网络中传输
 * 
 * @author SmartGrid Team
 * @version 1.0
 */
public class PriceVector implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = -5652678536888894383L;
    
    /** 时间段数量 */
    private static final int TIME_SLOTS = 4;
    
    /** 最低价格限制 */
    private static final float MIN_PRICE = 0.5f;
    
    /** 最高价格限制 */
    private static final float MAX_PRICE = 1.5f;
    
    /** 标识算法是否结束 */
    private boolean isAlgorithmEnded = false;
    
    /** 标识是否为新价格 */
    private boolean isNewPrice = false;
    
    /** 各时间段的价格数组 */
    private float[] priceArray;
    
    /**
     * 默认构造函数
     * 初始化一个随机价格的价格向量
     */
    public PriceVector() {
        this.priceArray = new float[TIME_SLOTS];
        double randomPrice = MIN_PRICE + Math.random() * (MAX_PRICE - MIN_PRICE);
        for (int i = 0; i < TIME_SLOTS; i++) {
            priceArray[i] = (float) randomPrice;
        }
    }
    
    /**
     * 复制构造函数
     * 从已有的PriceVector对象创建一个新的副本
     * 
     * @param priceVector 要复制的价格向量对象
     */
    public PriceVector(PriceVector priceVector) {
        this.priceArray = new float[TIME_SLOTS];
        this.isAlgorithmEnded = priceVector.isAlgorithmEnded;
        for (int i = 0; i < TIME_SLOTS; i++) {
            this.priceArray[i] = priceVector.getPriceArray()[i];
        }
    }
    
    /**
     * 带参数的构造函数
     * 使用指定的价格数组创建价格向量
     * 
     * @param priceArray 价格数组
     */
    public PriceVector(float[] priceArray) {
        this.priceArray = priceArray;
    }
    
    /**
     * 获取时间段数量
     * 
     * @return 时间段数量
     */
    public static int getTimeSlots() {
        return TIME_SLOTS;
    }
    
    /**
     * 获取价格数组
     * 
     * @return 价格数组
     */
    public float[] getPriceArray() {
        return priceArray;
    }
    
    /**
     * 检查是否为新价格
     * 
     * @return true 如果是新价格，否则返回 false
     */
    public boolean isNewPrice() {
        return isNewPrice;
    }
    
    /**
     * 设置新价格标识
     * 
     * @param isNewPrice 新价格标识
     */
    public void setNewPrice(boolean isNewPrice) {
        this.isNewPrice = isNewPrice;
    }
    
    /**
     * 设置指定位置的价格
     * 
     * @param position 位置索引
     * @param price 要设置的价格值
     */
    public void setPriceAtPosition(int position, float price) {
        if (position >= 0 && position < TIME_SLOTS) {
            priceArray[position] = price;
        }
    }
    
    /**
     * 创建一个新的价格向量，修改指定位置的价格
     * 用于SAPC算法中生成邻近解
     * 
     * @param position 要修改的位置
     * @param price 新的价格值
     * @param targetPriceVector 目标价格向量（用于存放结果）
     * @return 修改后的新价格向量
     */
    public PriceVector createModifiedPriceVector(int position, float price, PriceVector targetPriceVector) {
        copyPriceVector(targetPriceVector, this);
        targetPriceVector.setPriceAtPosition(position, price);
        return targetPriceVector;
    }
    
    /**
     * 获取指定位置的价格
     * 
     * @param position 位置索引
     * @return 指定位置的价格，如果位置无效则返回-1
     */
    public float getPriceAtPosition(int position) {
        if (position >= 0 && position < TIME_SLOTS) {
            return priceArray[position];
        }
        return -1f;
    }
    
    /**
     * 获取指定位置的价格（兼容性方法）
     * 
     * @param position 位置索引
     * @return 指定位置的价格，如果位置无效则返回-1
     * @deprecated 请使用 {@link #getPriceAtPosition(int)} 代替
     */
    @Deprecated
    public float getPriceByPosition(int position) {
        return getPriceAtPosition(position);
    }
    
    /**
     * 生成一个随机价格
     * 用于SAPC算法中生成随机邻近解
     * 
     * @return 在合理范围内的随机价格
     */
    public static float generateRandomPrice() {
        return (float) (MIN_PRICE + Math.random() * (MAX_PRICE - MIN_PRICE));
    }
    
    /**
     * 检查算法是否结束
     * 
     * @return true 如果算法结束，否则返回 false
     */
    public boolean isAlgorithmEnded() {
        return isAlgorithmEnded;
    }
    
    /**
     * 设置算法结束标识
     * 
     * @param isAlgorithmEnded 算法结束标识
     */
    public void setAlgorithmEnded(boolean isAlgorithmEnded) {
        this.isAlgorithmEnded = isAlgorithmEnded;
    }
    
    /**
     * 检查算法是否结束（兼容性方法）
     * 
     * @return true 如果算法结束，否则返回 false
     * @deprecated 请使用 {@link #isAlgorithmEnded()} 代替
     */
    @Deprecated
    public boolean isEnd() {
        return isAlgorithmEnded;
    }
    
    /**
     * 设置算法结束标识（兼容性方法）
     * 
     * @param isEnd 算法结束标识
     * @deprecated 请使用 {@link #setAlgorithmEnded(boolean)} 代替
     */
    @Deprecated
    public void setEnd(boolean isEnd) {
        this.isAlgorithmEnded = isEnd;
    }
    
    /**
     * 复制价格向量数据
     * 将源价格向量的数据复制到目标价格向量
     * 
     * @param target 目标价格向量
     * @param source 源价格向量
     */
    public void copyPriceVector(PriceVector target, PriceVector source) {
        for (int i = 0; i < source.getPriceArray().length; i++) {
            target.getPriceArray()[i] = source.getPriceArray()[i];
        }
    }
    
    /**
     * 返回价格向量的字符串表示
     * 
     * @return 格式化的价格向量字符串
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("价格向量:(");
        
        for (int i = 0; i < TIME_SLOTS; i++) {
            if (i == TIME_SLOTS - 1) {
                stringBuilder.append(priceArray[i]);
            } else {
                stringBuilder.append(priceArray[i]).append(", ");
            }
        }
        
        stringBuilder.append(")");
        return stringBuilder.toString();
    }
}
