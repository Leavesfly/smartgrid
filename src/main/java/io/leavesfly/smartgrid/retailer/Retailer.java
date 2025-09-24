package io.leavesfly.smartgrid.retailer;

import java.util.ArrayList;
import java.util.List;

import io.leavesfly.smartgrid.user.OneUserConsumVector;
import io.leavesfly.smartgrid.user.UsersArgs;

/**
 * 零售商核心类
 * 作为智能电网系统中零售商的核心数据管理对象
 * 负责管理价格向量、消耗数据、利润信息和用户连接状态
 * 支持多线程并发访问和数据同步
 * 
 * @author SmartGrid Team
 * @version 1.0
 */
public class Retailer {
    
    /** 步骤计数器，用于同步用户连接数量 */
    private StepCounter stepCounter;
    
    /** 当前价格向量 */
    private PriceVector currentPriceVector;
    
    /** 新的价格向量（用于SAPC算法中的价格迭代） */
    private PriceVector newPriceVector;
    
    /** 当前消耗数据 */
    private ElectricityConsumptionByTime currentConsumption;
    
    /** 当前零售商利润 */
    private float currentRetailerProfit;
    
    /** 新的消耗数据（用于SAPC算法中的数据迭代） */
    private ElectricityConsumptionByTime newConsumption;
    
    /** 新的零售商利润（用于SAPC算法中的利润迭代） */
    private float newRetailerProfit;
    
    /** 用户消耗向量列表，存储所有用户的消耗数据 */
    private List<OneUserConsumVector> userConsumptionList;
    
    /**
     * 默认构造函数
     * 初始化零售商对象的所有数据结构
     */
    public Retailer() {
        this.stepCounter = new StepCounter();
        this.currentPriceVector = new PriceVector();
        this.newPriceVector = new PriceVector();
        this.currentConsumption = new ElectricityConsumptionByTime();
        this.newConsumption = new ElectricityConsumptionByTime();
        this.userConsumptionList = new ArrayList<OneUserConsumVector>();
    }
    
    // =========================== Getter 方法 ===========================
    
    /**
     * 获取步骤计数器
     * 用于同步线程操作和统计用户连接数量
     * 
     * @return 步骤计数器对象
     */
    public StepCounter getStepCounter() {
        return stepCounter;
    }
    
    /**
     * 获取当前零售商利润
     * 
     * @return 当前利润值
     */
    public float getCurrentRetailerProfit() {
        return currentRetailerProfit;
    }
    
    /**
     * 获取新的零售商利润
     * 用于SAPC算法中的利润计算和比较
     * 
     * @return 新的利润值
     */
    public float getNewRetailerProfit() {
        return newRetailerProfit;
    }
    
    /**
     * 获取当前价格向量
     * 
     * @return 当前价格向量对象
     */
    public PriceVector getCurrentPriceVector() {
        return currentPriceVector;
    }
    
    /**
     * 获取用户消耗列表
     * 
     * @return 用户消耗向量列表
     */
    public List<OneUserConsumVector> getUserConsumptionList() {
        return userConsumptionList;
    }
    
    /**
     * 获取当前消耗数据
     * 
     * @return 当前消耗数据对象
     */
    public ElectricityConsumptionByTime getCurrentConsumption() {
        return currentConsumption;
    }
    
    /**
     * 获取新的价格向量
     * 用于SAPC算法中的价格迭代
     * 
     * @return 新的价格向量对象
     */
    public PriceVector getNewPriceVector() {
        return newPriceVector;
    }
    
    /**
     * 获取新的消耗数据
     * 用于SAPC算法中的消耗数据迭代
     * 
     * @return 新的消耗数据对象
     */
    public ElectricityConsumptionByTime getNewConsumption() {
        return newConsumption;
    }
    
    // =========================== Setter 方法 ===========================
    
    /**
     * 设置当前零售商利润
     * 
     * @param currentRetailerProfit 要设置的利润值
     */
    public void setCurrentRetailerProfit(float currentRetailerProfit) {
        this.currentRetailerProfit = currentRetailerProfit;
    }
    
    /**
     * 设置新的零售商利润
     * 
     * @param newRetailerProfit 要设置的新利润值
     */
    public void setNewRetailerProfit(float newRetailerProfit) {
        this.newRetailerProfit = newRetailerProfit;
    }
    
    /**
     * 设置新的价格向量
     * 
     * @param newPriceVector 要设置的新价格向量
     */
    public void setNewPriceVector(PriceVector newPriceVector) {
        this.newPriceVector = newPriceVector;
    }
    
    /**
     * 设置当前消耗数据
     * 
     * @param currentConsumption 要设置的消耗数据
     */
    public void setCurrentConsumption(ElectricityConsumptionByTime currentConsumption) {
        this.currentConsumption = currentConsumption;
    }
    
    /**
     * 设置新的消耗数据
     * 
     * @param newConsumption 要设置的新消耗数据
     */
    public void setNewConsumption(ElectricityConsumptionByTime newConsumption) {
        this.newConsumption = newConsumption;
    }
    
    // =========================== 业务方法 ===========================
    
    /**
     * 根据用户消耗向量列表填充按时间的消耗数据
     * 这个静态方法用于聚合所有用户的消耗数据，计算系统总消耗
     * 
     * @param consumptionByTime 要填充的消耗数据对象
     * @param userConsumptionList 用户消耗向量列表
     */
    public static void aggregateUserConsumption(ElectricityConsumptionByTime consumptionByTime,
                                              List<OneUserConsumVector> userConsumptionList) {
        
        // 数据验证：检查用户数量是否匹配
        if (userConsumptionList.size() != UsersArgs.userNum) {
            RetailerLogger.logError("用户数量不匹配: 实际=" + userConsumptionList.size() + 
                                   ", 期望=" + UsersArgs.userNum);
            return;
        }
        
        // 聚合所有用户在各时间段的消耗数据
        for (int timeSlot = 0; timeSlot < consumptionByTime.getConsumptionByTimeVector().length; timeSlot++) {
            int totalConsumptionInTimeSlot = 0;
            
            // 累加所有用户在该时间段的消耗
            for (OneUserConsumVector userConsumption : userConsumptionList) {
                totalConsumptionInTimeSlot += userConsumption.getConsumVector()[timeSlot];
            }
            
            // 设置该时间段的总消耗
            consumptionByTime.getConsumptionByTimeVector()[timeSlot] = totalConsumptionInTimeSlot;
        }
        
        RetailerLogger.logInfo("用户消耗数据聚合完成: " + consumptionByTime.toString());
    }
}
