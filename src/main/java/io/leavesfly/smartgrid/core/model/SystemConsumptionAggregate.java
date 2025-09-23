package io.leavesfly.smartgrid.core.model;

import io.leavesfly.smartgrid.core.config.SmartGridConfig;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 系统总用电消耗聚合类（重构版）
 * 用于聚合和管理系统中所有用户在各时间段的总用电消耗
 * 
 * 主要改进：
 * 1. 重命名为更具描述性的类名
 * 2. 增强数据封装性和输入验证
 * 3. 提供更丰富的数据聚合和分析功能
 * 4. 统一使用配置类管理常量
 * 5. 改进错误处理和异常管理
 * 
 * @author SmartGrid Team
 * @version 2.0
 */
public class SystemConsumptionAggregate {
    
    /** 各时间段的总用电消耗量 */
    private final int[] totalConsumptions;
    
    /** 数据是否完整填充的标志 */
    private boolean isDataComplete = false;
    
    /**
     * 默认构造函数
     * 创建一个空的系统消耗聚合对象
     */
    public SystemConsumptionAggregate() {
        this.totalConsumptions = new int[SmartGridConfig.TIME_SLOTS];
        // 数组默认初始化为0
    }
    
    /**
     * 使用指定数组构造系统消耗聚合对象
     * @param totalConsumptions 总消耗数组
     * @throws IllegalArgumentException 如果数组无效
     */
    public SystemConsumptionAggregate(int[] totalConsumptions) {
        validateConsumptionArray(totalConsumptions);
        
        this.totalConsumptions = new int[SmartGridConfig.TIME_SLOTS];
        System.arraycopy(totalConsumptions, 0, this.totalConsumptions, 0, SmartGridConfig.TIME_SLOTS);
        this.isDataComplete = true;
    }
    
    /**
     * 复制构造函数
     * @param other 要复制的系统消耗聚合对象
     * @throws IllegalArgumentException 如果输入参数为null
     */
    public SystemConsumptionAggregate(SystemConsumptionAggregate other) {
        if (other == null) {
            throw new IllegalArgumentException("复制源不能为null");
        }
        
        this.totalConsumptions = new int[SmartGridConfig.TIME_SLOTS];
        System.arraycopy(other.totalConsumptions, 0, this.totalConsumptions, 0, SmartGridConfig.TIME_SLOTS);
        this.isDataComplete = other.isDataComplete;
    }
    
    /**
     * 获取指定时间段的总用电消耗
     * @param timeSlot 时间段索引
     * @return 该时间段的总用电消耗
     * @throws IndexOutOfBoundsException 如果时间段索引无效
     */
    public int getConsumptionByTimeSlot(int timeSlot) {
        validateTimeSlot(timeSlot);
        return totalConsumptions[timeSlot];
    }
    
    /**
     * 设置指定时间段的总用电消耗
     * @param timeSlot 时间段索引
     * @param consumption 用电消耗量
     * @throws IndexOutOfBoundsException 如果时间段索引无效
     * @throws IllegalArgumentException 如果消耗值无效
     */
    public void setConsumptionByTimeSlot(int timeSlot, int consumption) {
        validateTimeSlot(timeSlot);
        validateConsumptionValue(consumption);
        totalConsumptions[timeSlot] = consumption;
    }
    
    /**
     * 获取总消耗数组的副本
     * @return 总消耗数组副本
     */
    public int[] getTotalConsumptionsCopy() {
        return Arrays.copyOf(totalConsumptions, totalConsumptions.length);
    }
    
    /**
     * 设置所有时间段的总用电消耗
     * @param newConsumptions 新的总消耗数组
     * @throws IllegalArgumentException 如果数组无效
     */
    public void setAllConsumptions(int[] newConsumptions) {
        validateConsumptionArray(newConsumptions);
        System.arraycopy(newConsumptions, 0, this.totalConsumptions, 0, SmartGridConfig.TIME_SLOTS);
        this.isDataComplete = true;
    }
    
    /**
     * 重置所有时间段的用电消耗为0
     */
    public void reset() {
        Arrays.fill(totalConsumptions, 0);
        this.isDataComplete = false;
    }
    
    /**
     * 聚合多个用户的用电消耗数据
     * @param userConsumptions 用户消耗向量列表
     * @throws IllegalArgumentException 如果输入参数无效
     */
    public void aggregateUserConsumptions(List<? extends ConsumptionVectorInterface> userConsumptions) {
        if (userConsumptions == null) {
            throw new IllegalArgumentException("用户消耗列表不能为null");
        }
        
        if (userConsumptions.size() != SmartGridConfig.USER_COUNT) {
            throw new IllegalArgumentException(
                "用户消耗列表大小不匹配，期望: " + SmartGridConfig.USER_COUNT + 
                "，实际: " + userConsumptions.size());
        }
        
        // 重置数据
        reset();
        
        // 聚合各用户的消耗数据
        for (int timeSlot = 0; timeSlot < SmartGridConfig.TIME_SLOTS; timeSlot++) {
            int totalConsumption = 0;
            for (ConsumptionVectorInterface userConsumption : userConsumptions) {
                if (userConsumption == null) {
                    throw new IllegalArgumentException("用户消耗向量不能为null");
                }
                totalConsumption += userConsumption.getConsumptionByTimeSlot(timeSlot);
            }
            totalConsumptions[timeSlot] = totalConsumption;
        }
        
        this.isDataComplete = true;
    }
    
    /**
     * 计算系统总用电量
     * @return 系统总用电量
     */
    public int calculateTotalSystemConsumption() {
        int total = 0;
        for (int consumption : totalConsumptions) {
            total += consumption;
        }
        return total;
    }
    
    /**
     * 获取峰值用电时段
     * @return 峰值用电时段的索引
     */
    public int getPeakConsumptionTimeSlot() {
        int maxConsumption = totalConsumptions[0];
        int peakTimeSlot = 0;
        
        for (int i = 1; i < totalConsumptions.length; i++) {
            if (totalConsumptions[i] > maxConsumption) {
                maxConsumption = totalConsumptions[i];
                peakTimeSlot = i;
            }
        }
        
        return peakTimeSlot;
    }
    
    /**
     * 获取低谷用电时段
     * @return 低谷用电时段的索引
     */
    public int getValleyConsumptionTimeSlot() {
        int minConsumption = totalConsumptions[0];
        int valleyTimeSlot = 0;
        
        for (int i = 1; i < totalConsumptions.length; i++) {
            if (totalConsumptions[i] < minConsumption) {
                minConsumption = totalConsumptions[i];
                valleyTimeSlot = i;
            }
        }
        
        return valleyTimeSlot;
    }
    
    /**
     * 计算负载均衡度（标准差）
     * @return 负载均衡度，值越小表示负载越均衡
     */
    public double calculateLoadBalanceMetric() {
        double mean = (double) calculateTotalSystemConsumption() / SmartGridConfig.TIME_SLOTS;
        double sumSquaredDifferences = 0.0;
        
        for (int consumption : totalConsumptions) {
            double difference = consumption - mean;
            sumSquaredDifferences += difference * difference;
        }
        
        return Math.sqrt(sumSquaredDifferences / SmartGridConfig.TIME_SLOTS);
    }
    
    /**
     * 获取指定时间段的用电量占总量的比例
     * @param timeSlot 时间段索引
     * @return 该时间段用电量占总量的比例
     */
    public double getConsumptionRatio(int timeSlot) {
        validateTimeSlot(timeSlot);
        int total = calculateTotalSystemConsumption();
        return total == 0 ? 0.0 : (double) totalConsumptions[timeSlot] / total;
    }
    
    /**
     * 检查数据是否完整
     * @return 如果数据完整返回true
     */
    public boolean isDataComplete() {
        return isDataComplete;
    }
    
    /**
     * 设置数据完整性标志
     * @param dataComplete 数据完整性标志
     */
    public void setDataComplete(boolean dataComplete) {
        this.isDataComplete = dataComplete;
    }
    
    /**
     * 验证消耗数组是否有效
     * @param consumptions 消耗数组
     * @throws IllegalArgumentException 如果数组无效
     */
    private void validateConsumptionArray(int[] consumptions) {
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
     * 验证单个消耗值是否有效
     * @param consumption 消耗值
     * @throws IllegalArgumentException 如果消耗值无效
     */
    private void validateConsumptionValue(int consumption) {
        if (consumption < 0) {
            throw new IllegalArgumentException("用电消耗不能为负数: " + consumption);
        }
    }
    
    /**
     * 复制另一个系统消耗聚合对象的数据
     * @param other 要复制的对象
     * @throws IllegalArgumentException 如果输入参数为null
     */
    public void copyFrom(SystemConsumptionAggregate other) {
        if (other == null) {
            throw new IllegalArgumentException("复制源不能为null");
        }
        
        System.arraycopy(other.totalConsumptions, 0, this.totalConsumptions, 0, SmartGridConfig.TIME_SLOTS);
        this.isDataComplete = other.isDataComplete;
    }
    
    // ============== 兼容性方法（用于与旧代码兼容） ==============
    
    /**
     * 兼容旧代码的方法：获取消耗数组
     * @deprecated 请使用 {@link #getTotalConsumptionsCopy()} 获取数组副本
     * @return 消耗数组的直接引用
     */
    @Deprecated
    public int[] getConsumByTimeVector() {
        return totalConsumptions;
    }
    
    /**
     * 兼容旧代码的方法：设置消耗数组
     * @param consumByTimeVector 消耗数组
     * @deprecated 请使用 {@link #setAllConsumptions(int[])}
     */
    @Deprecated
    public void setConsumByTimeVector(int[] consumByTimeVector) {
        setAllConsumptions(consumByTimeVector);
    }
    
    /**
     * 兼容旧代码的方法：检查是否填充完整
     * @deprecated 请使用 {@link #isDataComplete()}
     * @return 是否填充完整
     */
    @Deprecated
    public boolean isFull() {
        return isDataComplete;
    }
    
    /**
     * 兼容旧代码的方法：设置填充完整标志
     * @param isFull 填充完整标志
     * @deprecated 请使用 {@link #setDataComplete(boolean)}
     */
    @Deprecated
    public void setFull(boolean isFull) {
        this.isDataComplete = isFull;
    }
    
    /**
     * 兼容旧代码的静态方法：复制数据
     * @param target 目标对象
     * @param source 源对象
     * @deprecated 请使用实例方法 {@link #copyFrom(SystemConsumptionAggregate)}
     */
    @Deprecated
    public static void consumByTimeNewToConsumByTimeNow(
            SystemConsumptionAggregate target, SystemConsumptionAggregate source) {
        if (target == null || source == null) {
            throw new IllegalArgumentException("目标对象和源对象都不能为null");
        }
        target.copyFrom(source);
    }
    
    // ============== Object方法重写 ==============
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SystemConsumptionAggregate{");
        sb.append("consumptions=(");
        for (int i = 0; i < totalConsumptions.length; i++) {
            if (i == totalConsumptions.length - 1) {
                sb.append(totalConsumptions[i]);
            } else {
                sb.append(totalConsumptions[i]).append(", ");
            }
        }
        sb.append("), total=").append(calculateTotalSystemConsumption());
        sb.append(", complete=").append(isDataComplete);
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
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < totalConsumptions.length; i++) {
            if (i == totalConsumptions.length - 1) {
                sb.append(totalConsumptions[i]);
            } else {
                sb.append(totalConsumptions[i]).append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SystemConsumptionAggregate that = (SystemConsumptionAggregate) obj;
        return isDataComplete == that.isDataComplete && 
               Arrays.equals(totalConsumptions, that.totalConsumptions);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(totalConsumptions), isDataComplete);
    }
}