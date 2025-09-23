package io.leavesfly.smartgrid.core.config;

/**
 * 智能电网系统全局配置类
 * 统一管理系统中的所有配置参数，提高代码的可维护性和可配置性
 * 
 * @author SmartGrid Team
 * @version 2.0
 */
public final class SmartGridConfig {
    
    // =============== 时间相关配置 ===============
    /**
     * 时间槽数量 - 系统将一天分为4个时间段
     */
    public static final int TIME_SLOTS = 4;
    
    // =============== 网络通信配置 ===============
    /**
     * 服务器IP地址
     */
    public static final String SERVER_IP = "127.0.0.1";
    
    /**
     * 服务器监听端口
     */
    public static final int SERVER_PORT = 1234;
    
    // =============== 用户配置 ===============
    /**
     * 系统中的用户数量
     */
    public static final int USER_COUNT = 2;
    
    /**
     * A类电器数量（不可调节电器）
     */
    public static final int A_APPLIANCE_COUNT = 4;
    
    /**
     * B类电器数量（可调节电器）
     */
    public static final int B_APPLIANCE_COUNT = 4;
    
    // =============== 价格配置 ===============
    /**
     * 最低电价
     */
    public static final float MIN_PRICE = 0.5f;
    
    /**
     * 最高电价
     */
    public static final float MAX_PRICE = 1.5f;
    
    // =============== 算法参数配置 ===============
    /**
     * 模拟退火算法初始温度
     */
    public static float INITIAL_TEMPERATURE = (float) Math.exp(-1);
    
    /**
     * 算法终止温度
     */
    public static final float END_TEMPERATURE = (float) Math.exp(-5);
    
    /**
     * 当前算法轮次
     */
    public static int CURRENT_ROUND = 1;
    
    // =============== 利润计算参数 ===============
    /**
     * 利润计算系数a
     */
    public static final float PROFIT_COEFFICIENT_A = 0.005f;
    
    /**
     * 利润计算系数b
     */
    public static final float PROFIT_COEFFICIENT_B = 0.001f;
    
    /**
     * 权重系数w
     */
    public static final int WEIGHT_COEFFICIENT = 1;
    
    // =============== 日志配置 ===============
    /**
     * 零售商日志文件路径
     */
    public static final String RETAILER_LOG_FILE = "logs/retailer.log";
    
    /**
     * 用户日志文件路径
     */
    public static final String USER_LOG_FILE = "logs/users.log";
    
    // =============== 用户电器配置 ===============
    /**
     * 用户最大用电量限制
     */
    public static final int[] USER_MAX_CONSUMPTION = {10, 12};
    
    /**
     * A类电器用电量配置（固定）
     */
    public static final int[][] A_APPLIANCE_CONSUMPTION = {
        {1, 2, 3, 1},  // 用户0的A类电器用电量
        {1, 3, 3, 1}   // 用户1的A类电器用电量
    };
    
    /**
     * B类电器最大用电量配置
     */
    public static final int[][] B_APPLIANCE_MAX_CONSUMPTION = {
        {2, 3, 4, 4},  // 用户0的B类电器最大用电量
        {2, 3, 1, 3}   // 用户1的B类电器最大用电量
    };
    
    /**
     * 用户0的B类电器满意度矩阵
     */
    public static final int[][] USER_0_B_APPLIANCE_SATISFACTION = {
        {2, 4, 5, 3},  // B类电器0在各时间段的满意度
        {1, 3, 6, 3},  // B类电器1在各时间段的满意度
        {2, 5, 3, 4},  // B类电器2在各时间段的满意度
        {4, 1, 4, 3}   // B类电器3在各时间段的满意度
    };
    
    /**
     * 用户1的B类电器满意度矩阵
     */
    public static final int[][] USER_1_B_APPLIANCE_SATISFACTION = {
        {2, 2, 5, 3},  // B类电器0在各时间段的满意度
        {1, 6, 1, 3},  // B类电器1在各时间段的满意度
        {2, 3, 5, 3},  // B类电器2在各时间段的满意度
        {2, 1, 2, 4}   // B类电器3在各时间段的满意度
    };
    
    /**
     * 所有用户的B类电器满意度配置
     */
    public static final int[][][] ALL_USERS_B_APPLIANCE_SATISFACTION = {
        USER_0_B_APPLIANCE_SATISFACTION,
        USER_1_B_APPLIANCE_SATISFACTION
    };
    
    /**
     * 私有构造函数，防止实例化
     */
    private SmartGridConfig() {
        throw new AssertionError("配置类不允许实例化");
    }
    
    /**
     * 验证配置参数的有效性
     * @return 如果所有配置参数都有效则返回true
     */
    public static boolean validateConfig() {
        // 验证时间槽配置
        if (TIME_SLOTS <= 0) {
            return false;
        }
        
        // 验证用户数量配置
        if (USER_COUNT <= 0) {
            return false;
        }
        
        // 验证价格范围配置
        if (MIN_PRICE >= MAX_PRICE || MIN_PRICE <= 0) {
            return false;
        }
        
        // 验证电器数量配置
        if (A_APPLIANCE_COUNT <= 0 || B_APPLIANCE_COUNT <= 0) {
            return false;
        }
        
        // 验证数组长度一致性
        if (USER_MAX_CONSUMPTION.length != USER_COUNT ||
            A_APPLIANCE_CONSUMPTION.length != USER_COUNT ||
            B_APPLIANCE_MAX_CONSUMPTION.length != USER_COUNT ||
            ALL_USERS_B_APPLIANCE_SATISFACTION.length != USER_COUNT) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取指定用户的B类电器满意度矩阵
     * @param userId 用户ID
     * @return 该用户的B类电器满意度矩阵
     * @throws IllegalArgumentException 如果用户ID无效
     */
    public static int[][] getUserBApplianceSatisfaction(int userId) {
        if (userId < 0 || userId >= USER_COUNT) {
            throw new IllegalArgumentException("无效的用户ID: " + userId);
        }
        return ALL_USERS_B_APPLIANCE_SATISFACTION[userId];
    }
    
    /**
     * 获取指定用户的A类电器用电量配置
     * @param userId 用户ID
     * @return 该用户的A类电器用电量数组
     * @throws IllegalArgumentException 如果用户ID无效
     */
    public static int[] getUserAApplianceConsumption(int userId) {
        if (userId < 0 || userId >= USER_COUNT) {
            throw new IllegalArgumentException("无效的用户ID: " + userId);
        }
        return A_APPLIANCE_CONSUMPTION[userId];
    }
    
    /**
     * 获取指定用户的B类电器最大用电量配置
     * @param userId 用户ID
     * @return 该用户的B类电器最大用电量数组
     * @throws IllegalArgumentException 如果用户ID无效
     */
    public static int[] getUserBApplianceMaxConsumption(int userId) {
        if (userId < 0 || userId >= USER_COUNT) {
            throw new IllegalArgumentException("无效的用户ID: " + userId);
        }
        return B_APPLIANCE_MAX_CONSUMPTION[userId];
    }
    
    /**
     * 获取指定用户的最大用电量限制
     * @param userId 用户ID
     * @return 该用户的最大用电量限制
     * @throws IllegalArgumentException 如果用户ID无效
     */
    public static int getUserMaxConsumption(int userId) {
        if (userId < 0 || userId >= USER_COUNT) {
            throw new IllegalArgumentException("无效的用户ID: " + userId);
        }
        return USER_MAX_CONSUMPTION[userId];
    }
}