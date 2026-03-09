package io.leavesfly.smartgrid.user;

import io.leavesfly.smartgrid.retailer.PriceVector;

import java.util.Arrays;

/**
 * 用户用电调度算法（对应论文 Section III）。
 *
 * 本类实现了论文中用户侧求解问题 P1 的三个核心算法：
 *
 * Lemma 1（B 类弹性电器闭式解）：
 * 对于效用函数 U(e) = -a*(e+b)^{-1}，有：
 * ê_h = clamp( sqrt(a/(p_h + η_h)) - b, 0, r_max )
 * 其中 a = a_ui_h，b = b_ui_h，r_max = B_rmax。
 *
 * Lemma 2（C 类半非弹性电器最优分配）：
 * 目标：在工作期 [alpha,beta] 内以最小费用消耗总电量 E。
 * 方法：将工作期内各时段按有效价格 (p_h + η_h) 升序排列，贪心分配（最便宜时段优先）。
 *
 * Algorithm 1（迭代次梯度法，论文公式 26）：
 * 通过对拉格朗日乘子 η_h 进行次梯度迭代，直至收敛：
 * η_h = max(0, η_h - ψ * (C_max - E_bg_h - Σê_B_h - Σê_C_h))
 * 收敛条件：连续两次迭代中各时段用电量变化的平方和 ≤ CONVERGENCE_THRESHOLD。
 *
 * @see CTypeApplianceParams C 类电器参数模型
 * @see UsersArgs 用户配置参数
 */
public class UserMaxSatisfaConsumVector {

    /** Algorithm 1 最大迭代次数 */
    private static final int MAX_ITER = 200;

    /** Algorithm 1 次梯度步长 ψ（论文中取较小正数以保证收敛） */
    private static final double STEP_SIZE = 0.005;

    /** Algorithm 1 收敛判据 ε */
    private static final double CONVERGENCE_THRESHOLD = 1e-6;

    // ========================================================================
    // 公共入口
    // ========================================================================

    /**
     * 根据电价向量计算用户最优用电向量（Problem P1 的近似最优解）。
     *
     * 流程：
     * 1. 运行 Algorithm 1，迭代更新拉格朗日乘子 η_h
     * 2. 在收敛后，根据最终 η_h 调用 Lemma 1 / Lemma 2 计算各电器用电量
     * 3. 叠加 A 类背景负载，得到每时段总用电量
     *
     * @param oneUserConsumVector 用户用电向量容器（含用户 ID，结果写入其 consumVector）
     * @param priceVector         零售商发布的电价向量
     * @return 填充了最优用电方案的 oneUserConsumVector
     */
    public static OneUserConsumVector getConsumVectorByPriceVector(
            OneUserConsumVector oneUserConsumVector, PriceVector priceVector) {

        int userID = oneUserConsumVector.getUserID();
        int H = UsersArgs.timeSlots;
        float[] p = priceVector.getPriceArray();

        // 拉格朗日乘子，初始化为小正值
        double[] eta = new double[H];
        Arrays.fill(eta, 0.1);

        // 记录上一轮各 B 类和 C 类电器用电量（用于收敛判断）
        double[][] prevEB = new double[UsersArgs.B_applianceNum][H];
        double[][] prevEC = new double[UsersArgs.C_applianceNum][H];

        // ── Algorithm 1 主循环 ──────────────────────────────────────────────
        for (int iter = 0; iter < MAX_ITER; iter++) {

            // Step 1: Lemma 1 → 计算所有 B 类电器当前时段用电量 ê_B[j][h]
            double[][] eB = computeBTypeConsumption(userID, p, eta);

            // Step 2: Lemma 2 → 计算所有 C 类电器当前调度用电量 ê_C[c][h]
            double[][] eC = computeCTypeConsumption(userID, p, eta);

            // Step 3: 计算每时段总 B+C 用电量（不含背景负载）
            double[] totalBC = new double[H];
            for (int h = 0; h < H; h++) {
                for (int j = 0; j < UsersArgs.B_applianceNum; j++) totalBC[h] += eB[j][h];
                for (int c = 0; c < UsersArgs.C_applianceNum; c++) totalBC[h] += eC[c][h];
            }

            // Step 4: 次梯度更新 η_h（公式 26）
            // slack_h = C_max - E_bg_h - Σê_B_h - Σê_C_h
            for (int h = 0; h < H; h++) {
                double slack = UsersArgs.userMax[userID]
                        - UsersArgs.backgroundConsumption[userID][h]
                        - totalBC[h];
                eta[h] = Math.max(0.0, eta[h] - STEP_SIZE * slack);
            }

            // Step 5: 收敛检查（公式 27 判据）
            if (iter > 0 && hasConverged(eB, eC, prevEB, prevEC, H)) {
                break;
            }

            // 保存当前值供下次收敛判断
            for (int j = 0; j < UsersArgs.B_applianceNum; j++)
                System.arraycopy(eB[j], 0, prevEB[j], 0, H);
            for (int c = 0; c < UsersArgs.C_applianceNum; c++)
                System.arraycopy(eC[c], 0, prevEC[c], 0, H);
        }

        // ── 用最终 η 计算最优用电量 ─────────────────────────────────────────
        double[][] finalEB = computeBTypeConsumption(userID, p, eta);
        double[][] finalEC = computeCTypeConsumption(userID, p, eta);

        double[] result = new double[H];
        for (int h = 0; h < H; h++) {
            result[h] += UsersArgs.backgroundConsumption[userID][h];   // A 类
            for (int j = 0; j < UsersArgs.B_applianceNum; j++) result[h] += finalEB[j][h];
            for (int c = 0; c < UsersArgs.C_applianceNum; c++) result[h] += finalEC[c][h];

            // 不超过用户最大负载 C_max（对应论文约束 (6)）
            result[h] = Math.min(result[h], UsersArgs.userMax[userID]);
        }

        oneUserConsumVector.setConsumVector(result);
        return oneUserConsumVector;
    }

    // ========================================================================
    // Lemma 1：B 类（弹性）电器闭式最优解
    // ========================================================================

    /**
     * 利用 Lemma 1 计算所有 B 类电器的最优用电量。
     *
     * 效用函数 U(e) = -a*(e+b)^{-1}，其导函数为 U'(e) = a/(e+b)²，
     * 反函数 U'^{-1}(x) = sqrt(a/x) - b。
     * 代入 Lemma 1 得到闭式解：
     * ê_h = clamp( sqrt(a_ui_h / (p_h + η_h)) - b_ui_h, 0, r_max )
     *
     * @param userID 用户 ID
     * @param p      当前价格数组
     * @param eta    拉格朗日乘子数组
     * @return ê_B[appliance][slot]，各 B 类电器在各时段的最优用电量
     */
    static double[][] computeBTypeConsumption(int userID, float[] p, double[] eta) {
        int H = UsersArgs.timeSlots;
        double[][] eB = new double[UsersArgs.B_applianceNum][H];

        for (int j = 0; j < UsersArgs.B_applianceNum; j++) {
            for (int h = 0; h < H; h++) {
                double effectivePrice = p[h] + eta[h];
                double a = UsersArgs.a_ui_h[userID][j][h];
                double b = UsersArgs.b_ui_h[userID][j][h];
                double rmax = UsersArgs.B_rmax[userID][j];

                // Lemma 1 闭式解
                double optimalE = Math.sqrt(a / effectivePrice) - b;
                eB[j][h] = Math.max(0.0, Math.min(optimalE, rmax));
            }
        }
        return eB;
    }

    // ========================================================================
    // Lemma 2：C 类（半非弹性）电器贪心最优分配
    // ========================================================================

    /**
     * 利用 Lemma 2 计算所有 C 类电器的最优用电分配。
     *
     * C 类电器在工作期 [alpha, beta] 内需消耗固定总量 E，目标是使费用 Σ(p_h+η_h)*e_h 最小。
     * 最优策略（Lemma 2）：将工作期内各时段按有效价格 (p_h+η_h) 升序排列，贪心分配 r_max
     * 到最便宜的时段，直到 E 用尽。
     *
     * @param userID 用户 ID
     * @param p      当前价格数组
     * @param eta    拉格朗日乘子数组
     * @return ê_C[appliance][slot]，各 C 类电器在各时段的最优用电量
     */
    static double[][] computeCTypeConsumption(int userID, float[] p, double[] eta) {
        int H = UsersArgs.timeSlots;
        double[][] eC = new double[UsersArgs.C_applianceNum][H];

        for (int c = 0; c < UsersArgs.C_applianceNum; c++) {
            CTypeApplianceParams appliance = UsersArgs.C_appliances[userID][c];
            int alpha = appliance.getAlpha();
            int beta  = appliance.getBeta();
            double E        = appliance.getTotalEnergy();
            double rmax     = appliance.getMaxPerSlot();

            // 收集工作期内各时段的有效价格，并排序（升序 = 越便宜越优先）
            int periodLen = beta - alpha + 1;
            Integer[] slots = new Integer[periodLen];
            for (int i = 0; i < periodLen; i++) slots[i] = alpha + i;
            Arrays.sort(slots, (s1, s2) -> {
                double price1 = p[s1] + eta[s1];
                double price2 = p[s2] + eta[s2];
                return Double.compare(price1, price2);
            });

            // 贪心分配：最便宜时段先分配 rmax，直到 E 耗尽
            double remaining = E;
            for (int s : slots) {
                if (remaining <= 0) break;
                double alloc = Math.min(remaining, rmax);
                eC[c][s] = alloc;
                remaining -= alloc;
            }
        }
        return eC;
    }

    // ========================================================================
    // 收敛判断（对应论文公式 27 的终止条件）
    // ========================================================================

    /**
     * 判断 Algorithm 1 是否已收敛。
     *
     * 收敛条件（论文公式 27 的简化判据）：
     * Σ_h [ Σ_B (ê_B^k - ê_B^{k-1})² + Σ_C (ê_C^k - ê_C^{k-1})² ] ≤ ε
     *
     * @param eB     当前 B 类用电量
     * @param eC     当前 C 类用电量
     * @param prevEB 上一轮 B 类用电量
     * @param prevEC 上一轮 C 类用电量
     * @param H      时段数
     * @return true 如果已收敛
     */
    private static boolean hasConverged(double[][] eB, double[][] eC,
                                        double[][] prevEB, double[][] prevEC, int H) {
        double sumSqDiff = 0.0;

        for (int j = 0; j < UsersArgs.B_applianceNum; j++) {
            for (int h = 0; h < H; h++) {
                double diff = eB[j][h] - prevEB[j][h];
                sumSqDiff += diff * diff;
            }
        }
        for (int c = 0; c < UsersArgs.C_applianceNum; c++) {
            for (int h = 0; h < H; h++) {
                double diff = eC[c][h] - prevEC[c][h];
                sumSqDiff += diff * diff;
            }
        }

        return sumSqDiff <= CONVERGENCE_THRESHOLD;
    }
}
