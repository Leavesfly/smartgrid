package io.leavesfly.smartgrid.user;

import java.util.Random;

/**
 * 用户系统配置参数（对齐论文 Example 2）。
 * 使用固定随机种子（seed=42）生成 100 个用户的参数。
 *
 * 参数分布：
 *   - 调度区间 H = {1,2,...,12}（时段数 timeSlots = 12）
 *   - 用户数 userNum = 100
 *   - 每用户 4 个 B 类（弹性）电器，效用函数 U = -a*(e+b)^{-1}
 *   - 每用户 2 个 C 类（半非弹性）电器
 *   - A 类（背景）负载固定，每时段 ~ U[1,2] kWh
 *   - 用户最大负载限制 ~ U[10,15] kWh/时段
 *
 * B 类电器参数：a_ui_h ~ U[10,20]，b_ui_h ~ U[2,5]，B_rmax ~ U[1,2]
 * C 类电器参数：totalEnergy ~ U[4,6]，maxPerSlot ~ U[1,2]，alpha/beta 随机
 *
 * @see UserMaxSatisfaConsumVector 用户用电调度算法
 */
public class UsersArgs {

    // --- 网络连接配置 ---

    /** 零售商服务器 IP 地址 */
    public static final String ip = "127.0.0.1";

    /** 零售商服务器端口号 */
    public static final int port = 1234;

    // --- 系统基本参数（对齐论文 Example 2） ---

    /** 调度时段数 H = {1,2,...,12} */
    public static final int timeSlots = 12;

    /** 用户总数 */
    public static final int userNum = 100;

    /** B 类（弹性）电器数量，每用户 4 个 */
    public static final int B_applianceNum = 4;

    /** C 类（半非弹性）电器数量，每用户 2 个 */
    public static final int C_applianceNum = 2;

    /** 用户日志文件路径 */
    public static final String usersLogFile = "UsersLog.txt";

    // --- B 类电器参数（弹性电器，效用函数 U = -a*(e+b)^{-1}） ---

    /**
     * B 类电器效用函数系数 a，维度 [user][appliance][slot]，~ U[10, 20]
     */
    public static final double[][][] a_ui_h = new double[userNum][B_applianceNum][timeSlots];

    /**
     * B 类电器效用函数系数 b，维度 [user][appliance][slot]，~ U[2, 5]
     */
    public static final double[][][] b_ui_h = new double[userNum][B_applianceNum][timeSlots];

    /**
     * B 类电器每时段最大消耗量 r_max (kWh)，维度 [user][appliance]，~ U[1, 2]
     */
    public static final double[][] B_rmax = new double[userNum][B_applianceNum];

    // --- A 类电器参数（背景负载，固定用电） ---

    /**
     * A 类（背景）电器每时段固定消耗量 (kWh)，维度 [user][slot]，~ U[1, 2]
     */
    public static final double[][] backgroundConsumption = new double[userNum][timeSlots];

    // --- 用户最大负载限制 ---

    /**
     * 每用户每时段最大允许总用电量 C_max (kWh)，维度 [user]，~ U[10, 15]
     */
    public static final double[] userMax = new double[userNum];

    // --- C 类电器参数（半非弹性电器） ---

    /**
     * C 类电器参数数组，维度 [user][C_applianceNum]。
     * 每个对象包含：totalEnergy~U[4,6]，maxPerSlot~U[1,2]，alpha/beta 随机
     */
    public static final CTypeApplianceParams[][] C_appliances =
            new CTypeApplianceParams[userNum][C_applianceNum];

    // --- 静态初始化块（固定随机种子，保证可重复性） ---

    static {
        Random rng = new Random(42);  // 固定种子，对应论文可重复仿真

        for (int u = 0; u < userNum; u++) {
            // 用户最大负载 C_max ~ U[10, 15]
            userMax[u] = 10.0 + rng.nextDouble() * 5.0;

            // A 类电器背景负载 ~ U[1, 2] per slot
            for (int h = 0; h < timeSlots; h++) {
                backgroundConsumption[u][h] = 1.0 + rng.nextDouble();
            }

            // B 类电器参数
            for (int j = 0; j < B_applianceNum; j++) {
                B_rmax[u][j] = 1.0 + rng.nextDouble();  // r_max ~ U[1, 2]
                for (int h = 0; h < timeSlots; h++) {
                    a_ui_h[u][j][h] = 10.0 + rng.nextDouble() * 10.0;  // ~ U[10, 20]
                    b_ui_h[u][j][h] = 2.0 + rng.nextDouble() * 3.0;   // ~ U[2, 5]
                }
            }

            // C 类电器参数
            for (int c = 0; c < C_applianceNum; c++) {
                double totalEnergy = 4.0 + rng.nextDouble() * 2.0;  // ~ U[4, 6]
                double maxPerSlot  = 1.0 + rng.nextDouble();        // ~ U[1, 2]
                int alpha = rng.nextInt(timeSlots);
                int beta  = alpha + rng.nextInt(timeSlots - alpha);

                // 确保工作期内能容纳 totalEnergy
                int periodLen = beta - alpha + 1;
                if (periodLen * maxPerSlot < totalEnergy) {
                    beta = Math.min(alpha + (int) Math.ceil(totalEnergy / maxPerSlot), timeSlots - 1);
                    totalEnergy = Math.min(totalEnergy, (beta - alpha + 1) * maxPerSlot);
                }

                C_appliances[u][c] = new CTypeApplianceParams(totalEnergy, maxPerSlot, alpha, beta);
            }
        }
    }
}
