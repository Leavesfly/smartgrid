package io.leavesfly.smartgrid.core.model;

import io.leavesfly.smartgrid.core.config.SmartGridConfig;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

/**
 * 电价向量类（重构版）
 * 表示不同时间段的电价信息，支持动态价格调整和优化
 * 
 * 主要改进：
 * 1. 实现PriceVectorInterface接口，提高可扩展性
 * 2. 增强数据封装性，防止外部直接修改内部数组
 * 3. 添加输入验证和异常处理
 * 4. 使用配置类统一管理常量
 * 5. 改进方法命名和代码结构
 * 
 * @author SmartGrid Team
 * @version 2.0
 */
public class PriceVector implements PriceVectorInterface, Serializable {
    
    private static final long serialVersionUID = -5652678536888894383L;
    
    /** 随机数生成器 */
    private static final Random RANDOM = new Random();
    
    /** 价格数组 */
    private final float[] prices;
    
    /** 算法结束标志 */
    private boolean isAlgorithmEnded = false;
    
    /** 是否为新价格标志 */
    private boolean isNewPrice = false;
    
    /**
     * 默认构造函数
     * 创建一个价格向量，所有时间段使用相同的随机初始价格
     */
    public PriceVector() {
        this.prices = new float[SmartGridConfig.TIME_SLOTS];
        initializeWithRandomPrice();
    }
    
    /**
     * 复制构造函数
     * @param other 要复制的价格向量
     * @throws IllegalArgumentException 如果输入参数为null
     */
    public PriceVector(PriceVector other) {
        if (other == null) {
            throw new IllegalArgumentException("复制源价格向量不能为null");
        }
        
        this.prices = new float[SmartGridConfig.TIME_SLOTS];
        System.arraycopy(other.prices, 0, this.prices, 0, SmartGridConfig.TIME_SLOTS);
        this.isAlgorithmEnded = other.isAlgorithmEnded;
        this.isNewPrice = other.isNewPrice;
    }
    
    /**
     * 使用指定价格数组构造价格向量
     * @param prices 价格数组
     * @throws IllegalArgumentException 如果价格数组无效
     */
    public PriceVector(float[] prices) {
        if (prices == null) {
            throw new IllegalArgumentException("价格数组不能为null");
        }
        if (prices.length != SmartGridConfig.TIME_SLOTS) {
            throw new IllegalArgumentException(
                "价格数组长度必须为 " + SmartGridConfig.TIME_SLOTS);
        }
        
        this.prices = new float[SmartGridConfig.TIME_SLOTS];
        System.arraycopy(prices, 0, this.prices, 0, SmartGridConfig.TIME_SLOTS);
        
        if (!isValid()) {
            throw new IllegalArgumentException("价格数组包含无效值");
        }
    }
    
    /**
     * 使用随机价格初始化价格向量
     */
    private void initializeWithRandomPrice() {
        float randomPrice = generateRandomPrice();
        Arrays.fill(prices, randomPrice);
    }
    
    @Override
    public float getPriceByPosition(int position) {
        validatePosition(position);
        return prices[position];
    }
    
    @Override
    public void setPriceByPosition(int position, float price) {
        validatePosition(position);
        validatePrice(price);
        prices[position] = price;
    }
    
    @Override
    public float[] getPricesCopy() {
        return Arrays.copyOf(prices, prices.length);
    }
    
    @Override
    public int getTimeSlots() {
        return SmartGridConfig.TIME_SLOTS;
    }
    
    @Override
    public PriceVectorInterface createNewPriceVector(int position, float newPrice) {
        validatePosition(position);
        validatePrice(newPrice);
        
        PriceVector newVector = new PriceVector(this);
        newVector.setPriceByPosition(position, newPrice);
        newVector.setNewPrice(true);
        
        return newVector;
    }
    
    @Override
    public boolean isValid() {
        if (prices == null || prices.length != SmartGridConfig.TIME_SLOTS) {
            return false;
        }
        
        for (float price : prices) {
            if (!isValidPrice(price)) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public void copyFrom(PriceVectorInterface other) {
        if (other == null) {
            throw new IllegalArgumentException("源价格向量不能为null");
        }
        if (other.getTimeSlots() != this.getTimeSlots()) {
            throw new IllegalArgumentException("时间槽数量不匹配");
        }
        
        float[] otherPrices = other.getPricesCopy();
        System.arraycopy(otherPrices, 0, this.prices, 0, this.prices.length);
    }
    
    /**
     * 生成随机价格
     * @return 在有效范围内的随机价格
     */
    public static float generateRandomPrice() {
        return SmartGridConfig.MIN_PRICE + 
               RANDOM.nextFloat() * (SmartGridConfig.MAX_PRICE - SmartGridConfig.MIN_PRICE);
    }
    
    /**
     * 验证价格是否在有效范围内
     * @param price 要验证的价格
     * @return 如果价格有效返回true
     */
    private static boolean isValidPrice(float price) {
        return !Float.isNaN(price) && 
               !Float.isInfinite(price) &&
               price >= SmartGridConfig.MIN_PRICE && 
               price <= SmartGridConfig.MAX_PRICE;
    }
    
    /**
     * 验证位置索引是否有效
     * @param position 位置索引
     * @throws IndexOutOfBoundsException 如果位置无效
     */
    private void validatePosition(int position) {
        if (position < 0 || position >= SmartGridConfig.TIME_SLOTS) {
            throw new IndexOutOfBoundsException(
                "位置索引超出范围: " + position + "，有效范围: [0, " + 
                (SmartGridConfig.TIME_SLOTS - 1) + "]");
        }
    }
    
    /**
     * 验证价格值是否有效
     * @param price 要验证的价格
     * @throws IllegalArgumentException 如果价格无效
     */
    private void validatePrice(float price) {
        if (!isValidPrice(price)) {
            throw new IllegalArgumentException(
                "价格无效: " + price + "，有效范围: [" + 
                SmartGridConfig.MIN_PRICE + ", " + SmartGridConfig.MAX_PRICE + "]");
        }
    }
    
    // ============== Getter和Setter方法 ==============
    
    /**
     * 检查算法是否已结束
     * @return 如果算法已结束返回true
     */
    public boolean isAlgorithmEnded() {
        return isAlgorithmEnded;
    }
    
    /**
     * 设置算法结束标志
     * @param algorithmEnded 算法结束标志
     */
    public void setAlgorithmEnded(boolean algorithmEnded) {
        this.isAlgorithmEnded = algorithmEnded;
    }
    
    /**
     * 检查是否为新价格
     * @return 如果是新价格返回true
     */
    public boolean isNewPrice() {
        return isNewPrice;
    }
    
    /**
     * 设置新价格标志
     * @param newPrice 新价格标志
     */
    public void setNewPrice(boolean newPrice) {
        this.isNewPrice = newPrice;
    }
    
    /**
     * 获取价格数组的直接引用（已弃用，请使用getPricesCopy）
     * @deprecated 请使用 {@link #getPricesCopy()} 获取价格数组副本
     * @return 价格数组的直接引用
     */
    @Deprecated
    public float[] getPrices() {
        return prices;
    }
    
    // ============== 兼容性方法（用于与旧代码兼容） ==============
    
    /**
     * 兼容旧代码的方法：将另一个价格向量的值复制到当前向量
     * @param target 目标价格向量
     * @param source 源价格向量
     * @deprecated 请使用实例方法 {@link #copyFrom(PriceVectorInterface)}
     */
    @Deprecated
    public void privceVectorGiven(PriceVector target, PriceVector source) {
        if (target == null || source == null) {
            throw new IllegalArgumentException("价格向量不能为null");
        }
        target.copyFrom(source);
    }
    
    /**
     * 兼容旧代码的方法：创建新价格向量
     * @param position 要修改的位置
     * @param price 新价格
     * @param targetVector 目标向量
     * @return 目标向量（修改后）
     * @deprecated 请使用实例方法 {@link #createNewPriceVector(int, float)}
     */
    @Deprecated
    public PriceVector getNewPriceVector(int position, float price, PriceVector targetVector) {
        if (targetVector == null) {
            throw new IllegalArgumentException("目标向量不能为null");
        }
        targetVector.copyFrom(this);
        targetVector.setPriceByPosition(position, price);
        return targetVector;
    }
    
    /**
     * 兼容旧代码的静态方法：生成随机价格
     * @return 随机价格
     * @deprecated 请使用静态方法 {@link #generateRandomPrice()}
     */
    @Deprecated
    public static float getOneRandomPrice() {
        return generateRandomPrice();
    }
    
    /**
     * 兼容旧代码的静态方法：获取时间槽数量
     * @return 时间槽数量
     * @deprecated 请使用实例方法 {@link #getTimeSlots()}
     */
    @Deprecated
    public static int getTimeSolts() {
        return SmartGridConfig.TIME_SLOTS;
    }
    
    /**
     * 兼容旧代码的方法：检查是否结束
     * @return 是否结束
     * @deprecated 请使用 {@link #isAlgorithmEnded()}
     */
    @Deprecated
    public boolean isEnd() {
        return isAlgorithmEnded;
    }
    
    /**
     * 兼容旧代码的方法：设置结束标志
     * @param isEnd 结束标志
     * @deprecated 请使用 {@link #setAlgorithmEnded(boolean)}
     */
    @Deprecated
    public void setEnd(boolean isEnd) {
        this.isAlgorithmEnded = isEnd;
    }
    
    // ============== Object方法重写 ==============
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("prices:(");
        for (int i = 0; i < prices.length; i++) {
            if (i == prices.length - 1) {
                sb.append(String.format("%.3f", prices[i]));
            } else {
                sb.append(String.format("%.3f, ", prices[i]));
            }
        }
        sb.append(")");
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        PriceVector that = (PriceVector) obj;
        return Arrays.equals(prices, that.prices) &&
               isAlgorithmEnded == that.isAlgorithmEnded &&
               isNewPrice == that.isNewPrice;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(prices), isAlgorithmEnded, isNewPrice);
    }
}