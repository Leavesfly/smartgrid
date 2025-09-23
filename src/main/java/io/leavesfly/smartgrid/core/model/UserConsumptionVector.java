package io.leavesfly.smartgrid.core.model;

import io.leavesfly.smartgrid.core.config.SmartGridConfig;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * 用户用电消耗向量类（重构版）
 * 表示单个用户在各时间段的用电消耗情况
 * 
 * 主要改进：
 * 1. 实现ConsumptionVectorInterface接口，提高可扩展性
 * 2. 增强数据封装性和输入验证
 * 3. 统一使用配置类管理常量
 * 4. 添加数据有效性验证
 * 5. 改进方法命名和代码结构
 * 
 * @author SmartGrid Team
 * @version 2.0
 */
public class UserConsumptionVector implements ConsumptionVectorInterface, Serializable {
    
    private static final long serialVersionUID = 2500492976644903992L;
    
    /** 用户ID */
    private final int userId;
    
    /** 各时间段的用电消耗量 */
    private final int[] consumptions;
    
    /**
     * 构造函数
     * @param userId 用户ID
     * @param consumptions 用电消耗数组
     * @throws IllegalArgumentException 如果参数无效
     */
    public UserConsumptionVector(int userId, int[] consumptions) {
        validateUserId(userId);
        validateConsumptions(consumptions);
        
        this.userId = userId;
        this.consumptions = new int[SmartGridConfig.TIME_SLOTS];
        System.arraycopy(consumptions, 0, this.consumptions, 0, SmartGridConfig.TIME_SLOTS);
    }
    
    /**
     * 构造函数（使用零初始化）
     * @param userId 用户ID
     * @throws IllegalArgumentException 如果用户ID无效
     */
    public UserConsumptionVector(int userId) {
        validateUserId(userId);
        
        this.userId = userId;
        this.consumptions = new int[SmartGridConfig.TIME_SLOTS];
        // 数组默认初始化为0
    }
    
    /**
     * 复制构造函数
     * @param other 要复制的用户消耗向量
     * @throws IllegalArgumentException 如果输入参数为null
     */
    public UserConsumptionVector(UserConsumptionVector other) {
        if (other == null) {
            throw new IllegalArgumentException("复制源不能为null");
        }
        
        this.userId = other.userId;
        this.consumptions = new int[SmartGridConfig.TIME_SLOTS];
        System.arraycopy(other.consumptions, 0, this.consumptions, 0, SmartGridConfig.TIME_SLOTS);
    }
    
    @Override
    public int getUserId() {
        return userId;
    }
    
    @Override
    public int getConsumptionByTimeSlot(int timeSlot) {
        validateTimeSlot(timeSlot);
        return consumptions[timeSlot];
    }
    
    @Override
    public void setConsumptionByTimeSlot(int timeSlot, int consumption) {
        validateTimeSlot(timeSlot);
        validateConsumptionValue(consumption);
        consumptions[timeSlot] = consumption;
    }
    
    @Override
    public int[] getConsumptionsCopy() {
        return Arrays.copyOf(consumptions, consumptions.length);
    }
    
    @Override
    public int getTotalConsumption() {
        int total = 0;
        for (int consumption : consumptions) {
            total += consumption;
        }
        return total;
    }
    
    @Override
    public boolean isValid() {
        // 检查用户ID是否有效
        if (userId < 0 || userId >= SmartGridConfig.USER_COUNT) {
            return false;
        }
        
        // 检查消耗数组是否有效
        if (consumptions == null || consumptions.length != SmartGridConfig.TIME_SLOTS) {
            return false;
        }
        
        // 检查所有消耗值是否非负
        for (int consumption : consumptions) {
            if (consumption < 0) {
                return false;
            }
        }
        
        // 检查总消耗是否超过用户限制
        int userMaxConsumption = SmartGridConfig.getUserMaxConsumption(userId);
        return getTotalConsumption() <= userMaxConsumption;
    }
    
    @Override
    public void reset() {
        Arrays.fill(consumptions, 0);
    }
    
    /**
     * 设置所有时间段的用电消耗
     * @param newConsumptions 新的用电消耗数组
     * @throws IllegalArgumentException 如果数组无效
     */
    public void setAllConsumptions(int[] newConsumptions) {
        validateConsumptions(newConsumptions);
        System.arraycopy(newConsumptions, 0, this.consumptions, 0, SmartGridConfig.TIME_SLOTS);
    }
    
    /**
     * 获取指定时间段的用电量占总量的比例
     * @param timeSlot 时间段
     * @return 该时间段用电量占总量的比例
     */
    public double getConsumptionRatio(int timeSlot) {
        validateTimeSlot(timeSlot);
        int total = getTotalConsumption();
        return total == 0 ? 0.0 : (double) consumptions[timeSlot] / total;
    }
    
    /**
     * 检查用户是否超过最大用电限制
     * @return 如果超过限制返回true
     */
    public boolean isOverConsumptionLimit() {
        int userMaxConsumption = SmartGridConfig.getUserMaxConsumption(userId);
        return getTotalConsumption() > userMaxConsumption;
    }
    
    /**
     * 获取用户的剩余可用电量
     * @return 剩余可用电量
     */
    public int getRemainingCapacity() {
        int userMaxConsumption = SmartGridConfig.getUserMaxConsumption(userId);
        return Math.max(0, userMaxConsumption - getTotalConsumption());
    }
    
    /**
     * 验证用户ID是否有效
     * @param userId 用户ID
     * @throws IllegalArgumentException 如果用户ID无效
     */
    private void validateUserId(int userId) {
        if (userId < 0 || userId >= SmartGridConfig.USER_COUNT) {
            throw new IllegalArgumentException(
                "用户ID超出范围: " + userId + "，有效范围: [0, " + 
                (SmartGridConfig.USER_COUNT - 1) + "]");
        }
    }
    
    /**
     * 验证时间段索引是否有效
     * @param timeSlot 时间段索引
     * @throws IndexOutOfBoundsException 如果时间段索引无效
     */
    private void validateTimeSlot(int timeSlot) {
        if (timeSlot < 0 || timeSlot >= SmartGridConfig.TIME_SLOTS) {
            throw new IndexOutOfBoundsException(
                "时间段索引超出范围: " + timeSlot + "，有效范围: [0, " + 
                (SmartGridConfig.TIME_SLOTS - 1) + "]");
        }
    }
    
    /**
     * 验证消耗数组是否有效
     * @param consumptions 消耗数组
     * @throws IllegalArgumentException 如果数组无效
     */
    private void validateConsumptions(int[] consumptions) {
        if (consumptions == null) {
            throw new IllegalArgumentException("消耗数组不能为null");
        }
        if (consumptions.length != SmartGridConfig.TIME_SLOTS) {
            throw new IllegalArgumentException(
                "消耗数组长度必须为 " + SmartGridConfig.TIME_SLOTS);
        }
        for (int consumption : consumptions) {
            validateConsumptionValue(consumption);
        }
    }
    
    /**
     * 验证单个消耗值是否有效
     * @param consumption 消耗值
     * @throws IllegalArgumentException 如果消耗值无效
     */
    private void validateConsumptionValue(int consumption) {
        if (consumption < 0) {
            throw new IllegalArgumentException("用电消耗不能为负数: " + consumption);
        }
    }
    
    // ============== 兼容性方法（用于与旧代码兼容） ==============
    
    /**
     * 兼容旧代码的方法：获取消耗数组
     * @deprecated 请使用 {@link #getConsumptionsCopy()} 获取数组副本
     * @return 消耗数组的直接引用
     */
    @Deprecated
    public int[] getConsumVector() {
        return consumptions;
    }
    
    /**
     * 兼容旧代码的方法：设置消耗数组
     * @param consumVector 消耗数组
     * @deprecated 请使用 {@link #setAllConsumptions(int[])}
     */
    @Deprecated
    public void setConsumVector(int[] consumVector) {
        setAllConsumptions(consumVector);
    }
    
    /**
     * 兼容旧代码的方法：获取用户ID
     * @deprecated 请使用 {@link #getUserId()}
     * @return 用户ID
     */
    @Deprecated
    public int getUserID() {
        return userId;
    }
    
    /**
     * 兼容旧代码的方法：设置用户ID
     * @param userID 用户ID
     * @deprecated 用户ID在构造时设定，不允许修改
     */
    @Deprecated
    public void setUserID(int userID) {
        throw new UnsupportedOperationException("用户ID不允许修改");
    }
    
    /**
     * 兼容旧代码的静态方法：获取时间槽数量
     * @deprecated 请使用 {@link SmartGridConfig#TIME_SLOTS}
     * @return 时间槽数量
     */
    @Deprecated
    public static int getTimesolts() {
        return SmartGridConfig.TIME_SLOTS;
    }
    
    // ============== Object方法重写 ==============
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("UserConsumptionVector{");
        sb.append("userId=").append(userId);
        sb.append(", consumptions=(");
        for (int i = 0; i < consumptions.length; i++) {
            if (i == consumptions.length - 1) {
                sb.append(consumptions[i]);
            } else {
                sb.append(consumptions[i]).append(", ");
            }
        }
        sb.append("), total=").append(getTotalConsumption());
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * 兼容旧代码的toString格式
     * @deprecated 请使用标准的 {@link #toString()} 方法
     * @return 旧格式的字符串表示
     */
    @Deprecated
    public String toOldFormatString() {
        StringBuilder sb = new StringBuilder("oneUserConsumVector:(");
        for (int i = 0; i < consumptions.length; i++) {
            if (i == consumptions.length - 1) {
                sb.append(consumptions[i]);
            } else {
                sb.append(consumptions[i]).append(",  ");
            }
        }
        sb.append(")");
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        UserConsumptionVector that = (UserConsumptionVector) obj;
        return userId == that.userId && Arrays.equals(consumptions, that.consumptions);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, Arrays.hashCode(consumptions));
    }
}