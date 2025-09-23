package io.leavesfly.smartgrid.example;

import io.leavesfly.smartgrid.core.config.SmartGridConfig;
import io.leavesfly.smartgrid.core.model.PriceVector;
import io.leavesfly.smartgrid.core.model.UserConsumptionVector;
import io.leavesfly.smartgrid.core.model.SystemConsumptionAggregate;
import io.leavesfly.smartgrid.core.algorithm.RetailerProfitCalculator;
import io.leavesfly.smartgrid.util.logging.SmartGridLogger;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * 智能电网系统重构后的功能演示程序
 * 展示重构后各组件的使用方法和功能
 * 
 * @author SmartGrid Team
 * @version 2.0
 */
public class SmartGridRefactoredDemo {
    
    private static final SmartGridLogger logger = SmartGridLogger.getInstance();
    
    public static void main(String[] args) {
        logger.info(SmartGridLogger.LogType.SYSTEM, "=== 智能电网系统重构演示开始 ===");
        
        try {
            // 1. 配置验证演示
            demonstrateConfigValidation();
            
            // 2. 价格向量功能演示
            demonstratePriceVector();
            
            // 3. 用户消耗向量功能演示
            demonstrateUserConsumptionVector();
            
            // 4. 系统消耗聚合功能演示
            demonstrateSystemConsumptionAggregate();
            
            // 5. 利润计算功能演示
            demonstrateProfitCalculation();
            
            // 6. 日志系统功能演示
            demonstrateLoggingSystem();
            
            logger.info(SmartGridLogger.LogType.SYSTEM, "=== 智能电网系统重构演示完成 ===");
            
        } catch (Exception e) {
            logger.error(SmartGridLogger.LogType.SYSTEM, "演示程序执行失败", e);
        }
    }
    
    /**
     * 演示配置验证功能
     */
    private static void demonstrateConfigValidation() {
        logger.info(SmartGridLogger.LogType.SYSTEM, "1. 配置验证演示");
        
        boolean isValid = SmartGridConfig.validateConfig();
        logger.info(SmartGridLogger.LogType.SYSTEM, "配置验证结果: " + (isValid ? "通过" : "失败"));
        
        logger.info(SmartGridLogger.LogType.SYSTEM, "系统配置信息:");
        logger.info(SmartGridLogger.LogType.SYSTEM, String.format("  时间段数量: %d", SmartGridConfig.TIME_SLOTS));
        logger.info(SmartGridLogger.LogType.SYSTEM, String.format("  用户数量: %d", SmartGridConfig.USER_COUNT));
        logger.info(SmartGridLogger.LogType.SYSTEM, String.format("  价格范围: [%.2f, %.2f]", 
            SmartGridConfig.MIN_PRICE, SmartGridConfig.MAX_PRICE));
        
        System.out.println();
    }
    
    /**
     * 演示价格向量功能
     */
    private static void demonstratePriceVector() {
        logger.info(SmartGridLogger.LogType.SYSTEM, "2. 价格向量功能演示");
        
        // 创建价格向量
        PriceVector priceVector = new PriceVector();
        logger.info(SmartGridLogger.LogType.SYSTEM, "初始价格向量: " + priceVector.toString());
        
        // 修改特定时间段价格
        priceVector.setPriceByPosition(1, 1.2f);
        logger.info(SmartGridLogger.LogType.SYSTEM, "修改时间段1价格后: " + priceVector.toString());
        
        // 创建新价格向量
        PriceVector newPriceVector = (PriceVector) priceVector.createNewPriceVector(2, 0.8f);
        logger.info(SmartGridLogger.LogType.SYSTEM, "新价格向量: " + newPriceVector.toString());
        
        // 验证价格向量
        logger.info(SmartGridLogger.LogType.SYSTEM, "价格向量有效性: " + priceVector.isValid());
        
        System.out.println();
    }
    
    /**
     * 演示用户消耗向量功能
     */
    private static void demonstrateUserConsumptionVector() {
        logger.info(SmartGridLogger.LogType.SYSTEM, "3. 用户消耗向量功能演示");
        
        // 创建用户消耗向量
        int[] consumptions = {3, 5, 2, 4};
        UserConsumptionVector userVector = new UserConsumptionVector(0, consumptions);
        logger.info(SmartGridLogger.LogType.SYSTEM, "用户0消耗向量: " + userVector.toString());
        
        // 计算总消耗
        int totalConsumption = userVector.getTotalConsumption();
        logger.info(SmartGridLogger.LogType.SYSTEM, "总消耗量: " + totalConsumption);
        
        // 检查是否超限
        boolean isOverLimit = userVector.isOverConsumptionLimit();
        logger.info(SmartGridLogger.LogType.SYSTEM, "是否超过用电限制: " + isOverLimit);
        
        // 获取剩余容量
        int remainingCapacity = userVector.getRemainingCapacity();
        logger.info(SmartGridLogger.LogType.SYSTEM, "剩余用电容量: " + remainingCapacity);
        
        System.out.println();
    }
    
    /**
     * 演示系统消耗聚合功能
     */
    private static void demonstrateSystemConsumptionAggregate() {
        logger.info(SmartGridLogger.LogType.SYSTEM, "4. 系统消耗聚合功能演示");
        
        // 创建多个用户消耗向量
        List<UserConsumptionVector> userConsumptions = new ArrayList<>();
        userConsumptions.add(new UserConsumptionVector(0, new int[]{3, 4, 5, 2}));
        userConsumptions.add(new UserConsumptionVector(1, new int[]{2, 3, 4, 5}));
        
        // 创建系统消耗聚合
        SystemConsumptionAggregate systemAggregate = new SystemConsumptionAggregate();
        systemAggregate.aggregateUserConsumptions(userConsumptions);
        
        logger.info(SmartGridLogger.LogType.SYSTEM, "系统总消耗: " + systemAggregate.toString());
        
        // 分析峰谷用电
        int peakTimeSlot = systemAggregate.getPeakConsumptionTimeSlot();
        int valleyTimeSlot = systemAggregate.getValleyConsumptionTimeSlot();
        logger.info(SmartGridLogger.LogType.SYSTEM, String.format("峰值时段: %d, 低谷时段: %d", 
            peakTimeSlot, valleyTimeSlot));
        
        // 计算负载均衡度
        double loadBalance = systemAggregate.calculateLoadBalanceMetric();
        logger.info(SmartGridLogger.LogType.SYSTEM, String.format("负载均衡度: %.3f", loadBalance));
        
        System.out.println();
    }
    
    /**
     * 演示利润计算功能
     */
    private static void demonstrateProfitCalculation() {
        logger.info(SmartGridLogger.LogType.SYSTEM, "5. 利润计算功能演示");
        
        // 创建利润计算器
        RetailerProfitCalculator calculator = new RetailerProfitCalculator();
        
        // 创建价格向量和消耗数据
        PriceVector priceVector = new PriceVector(new float[]{0.8f, 1.0f, 1.2f, 0.9f});
        int[] totalConsumption = {5, 7, 9, 7};
        
        // 计算利润
        float profit = calculator.calculateProfit(priceVector, totalConsumption);
        logger.info(SmartGridLogger.LogType.SYSTEM, String.format("计算得出利润: %.3f", profit));
        
        // 获取详细分析
        RetailerProfitCalculator.ProfitAnalysis analysis = 
            calculator.getDetailedProfitAnalysis(priceVector, totalConsumption);
        logger.info(SmartGridLogger.LogType.SYSTEM, "详细利润分析: " + analysis.toString());
        
        logger.info(SmartGridLogger.LogType.SYSTEM, String.format("最盈利时段: %d", 
            analysis.getMostProfitableTimeSlot()));
        
        System.out.println();
    }
    
    /**
     * 演示日志系统功能
     */
    private static void demonstrateLoggingSystem() {
        logger.info(SmartGridLogger.LogType.SYSTEM, "6. 日志系统功能演示");
        
        // 测试不同级别的日志
        logger.debug(SmartGridLogger.LogType.RETAILER, "这是一条调试信息");
        logger.info(SmartGridLogger.LogType.RETAILER, "零售商服务启动成功");
        logger.warn(SmartGridLogger.LogType.USER, "用户连接异常，正在重试");
        logger.error(SmartGridLogger.LogType.SYSTEM, "系统出现错误");
        
        // 测试异常日志
        try {
            throw new RuntimeException("测试异常");
        } catch (Exception e) {
            logger.error(SmartGridLogger.LogType.SYSTEM, "捕获到测试异常", e);
        }
        
        // 设置不同日志级别
        logger.setLogLevel(SmartGridLogger.LogLevel.WARN);
        logger.info(SmartGridLogger.LogType.SYSTEM, "这条INFO日志不会显示");
        logger.warn(SmartGridLogger.LogType.SYSTEM, "这条WARN日志会显示");
        
        // 恢复INFO级别
        logger.setLogLevel(SmartGridLogger.LogLevel.INFO);
        
        // 刷新所有日志
        logger.flushAll();
        
        System.out.println();
    }
}