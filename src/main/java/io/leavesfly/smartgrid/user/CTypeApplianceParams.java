package io.leavesfly.smartgrid.user;

import java.io.Serializable;

/**
 * C类电器（半非弹性电器）参数模型。
 *
 * <p>对应论文公式 (4)(5)：C 类电器在工作期 [alpha, beta] 内必须消耗固定总量
 * E_au，每时段消耗量受上限 r_max 约束，但可以灵活分配到工作期内的任意时段。</p>
 *
 * <p>典型代表：洗衣机、洗碗机、电动车充电桩等。</p>
 *
 * <p>论文参数范围（Example 2）：</p>
 * <ul>
 *   <li>totalEnergy ~ U[4, 6] kWh</li>
 *   <li>maxPerSlot  ~ U[1, 2] kWh/时段</li>
 *   <li>alpha, beta 随机选自调度区间 H</li>
 * </ul>
 *
 * @see UserMaxSatisfaConsumVector C类电器的最优分配算法（Lemma 2）
 */
public class CTypeApplianceParams implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 工作期内需消耗的总电量 E_au（kWh），对应论文公式 (4) */
    private double totalEnergy;

    /** 每时段最大允许消耗量 r_max（kWh），对应论文公式 (5) */
    private double maxPerSlot;

    /** 工作期开始时段索引（含），0-based */
    private int alpha;

    /** 工作期结束时段索引（含），0-based，满足 beta >= alpha */
    private int beta;

    /**
     * 构造函数。
     *
     * @param totalEnergy 工作期内总用电量（kWh）
     * @param maxPerSlot  每时段最大消耗（kWh）
     * @param alpha       工作期开始时段（0-based，含）
     * @param beta        工作期结束时段（0-based，含）
     */
    public CTypeApplianceParams(double totalEnergy, double maxPerSlot, int alpha, int beta) {
        this.totalEnergy = totalEnergy;
        this.maxPerSlot  = maxPerSlot;
        this.alpha       = alpha;
        this.beta        = beta;
    }

    public double getTotalEnergy() { return totalEnergy; }

    public double getMaxPerSlot() { return maxPerSlot; }

    public int getAlpha() { return alpha; }

    public int getBeta() { return beta; }

    @Override
    public String toString() {
        return String.format("C类电器[α=%d, β=%d, E=%.2f, rmax=%.2f]",
                alpha, beta, totalEnergy, maxPerSlot);
    }
}
