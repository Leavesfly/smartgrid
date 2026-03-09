package io.leavesfly.smartgrid.retailer;

/**
 * 零售商配置常量类
 * 定义了智能电网零售商系统的所有关键配置参数
 * 包括网络配置、算法参数、日志配置等
 * 
 * @author SmartGrid Team
 * @version 1.0
 */
public final class RetailerConfigConstants {
    
    // =========================== 时间配置 ===========================
    
    /** 电力分时段数量，对应论文 Example 2：H = {1,2,...,12} */
    public static final int TIME_SLOTS = 12;
    
    // =========================== 网络配置 ===========================
    
    /** 零售商服务器监听端口号 */
    public static final int LISTEN_PORT = 1234;
    
    // =========================== SAPC算法参数 ===========================
    
    /** 模拟退火算法初始温度 T = e^(-1) */
    public static float INITIAL_TEMPERATURE = (float) Math.exp(-1);
    
    /** 模拟退火算法终止温度 E = e^(-5) */
    public static final float END_TEMPERATURE = (float) Math.exp(-5);
    
    /** 当前迭代轮数，用于算法温度调整 */
    public static int CURRENT_ROUND = 1;
    
    // =========================== 利润计算参数 ===========================
    
    /** 利润计算公式中的参数a，对应论文公式(12)，论文 Example 2：a = 10^-4 */
    public static final float PROFIT_PARAM_A = 1e-4f;
    
    /** 利润计算公式中的参数b，对应论文公式(12)，论文 Example 2：b = 2×10^-5 */
    public static final float PROFIT_PARAM_B = 2e-5f;
    
    /** 利润计算公式中的权重参数w */
    public static final int PROFIT_WEIGHT = 1;
    
    // =========================== 日志配置 ===========================
    
    /** 日志文件路径 */
    public static final String LOG_FILE_PATH = "E:\\RetailerLog.txt";
    
    // 私有构造函数，禁止实例化工具类
    private RetailerConfigConstants() {
        throw new UnsupportedOperationException("此类为工具类，不允许实例化");
    }
}
