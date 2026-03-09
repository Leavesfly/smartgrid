package io.leavesfly.smartgrid.retailer;

import io.leavesfly.smartgrid.user.OneUserConsumVector;
import io.leavesfly.smartgrid.user.UserMaxSatisfaConsumVector;
import io.leavesfly.smartgrid.user.UsersArgs;

/**
 * SAPC（Simulated Annealing Price Control）价格控制算法。
 *
 * 采用模拟退火算法为智能电网零售商求解最优动态定价策略，最大化利润。
 * 算法流程：初始化随机价格向量 → 逐时段 round-robin 扰动 → 按 SA 准则接受/拒绝 → 降温直至收敛。
 */
public final class SAPC_Algorithm {

    /**
     * 多线程版 SAPC 算法主入口（对应论文 Algorithm 2）。
     *
     * 与 RetailerServer / UserThread 协同：每次发布新价格后等待所有用户响应，
     * 再根据聚合用电量和利润决定是否采纳新价格。
     *
     * @param retailer 零售商核心对象，持有共享状态与同步原语
     * @throws Exception 线程等待或 sleep 中断时抛出
     */
    public static void simulatedAnnealingAlgorithm(Retailer retailer) throws Exception {
        RetailerLogger.logInfo("SAPC算法开始执行");

        // 等待所有用户连接完成
        synchronized (retailer.getStepCounter()) {
            retailer.getStepCounter().wait();
        }
        RetailerLogger.logInfo("所有用户已连接，SAPC算法开始初始化");

        // 以当前价格向量为初始解，广播并收集第一轮用电量
        retailer.getNewPriceVector().copyPriceVector(
                retailer.getNewPriceVector(), retailer.getCurrentPriceVector());
        broadcastAndWait(retailer);
        retailer.setCurrentRetailerProfit(retailer.getNewRetailerProfit());
        RetailerLogger.logInfo("初始利润: " + retailer.getCurrentRetailerProfit());

        // SA 主循环：逐轮降温，直至温度低于终止阈值
        while (RetailerConfigConstants.INITIAL_TEMPERATURE > RetailerConfigConstants.END_TEMPERATURE) {
            RetailerLogger.logInfo("========================== 第 "
                    + RetailerConfigConstants.CURRENT_ROUND + " 轮迭代 ==========================");

            // 逐时段扰动价格（round-robin）
            for (int h = 0; h < retailer.getCurrentPriceVector().getPriceArray().length; h++) {
                float randomPrice = PriceVector.generateRandomPrice();
                retailer.setNewPriceVector(
                        retailer.getCurrentPriceVector().createModifiedPriceVector(
                                h, randomPrice, retailer.getNewPriceVector()));

                RetailerLogger.logInfo("当前价格" + retailer.getNewPriceVector().toString());
                broadcastAndWait(retailer);
                RetailerLogger.logInfo("系统总消耗:" + retailer.getNewConsumption());
                RetailerLogger.logInfo("新利润:" + retailer.getNewRetailerProfit());
                RetailerLogger.logInfo("------------------------------");

                // SA 接受准则：利润改善则直接接受，否则以 Metropolis 概率接受
                if (shouldAccept(retailer)) {
                    retailer.getCurrentPriceVector().copyPriceVector(
                            retailer.getCurrentPriceVector(), retailer.getNewPriceVector());
                    retailer.setCurrentRetailerProfit(retailer.getNewRetailerProfit());
                }
            }

            // 降温：T(k) = T(k-1) / ln(k)（论文公式）
            RetailerConfigConstants.CURRENT_ROUND++;
            RetailerConfigConstants.INITIAL_TEMPERATURE =
                    (float) (RetailerConfigConstants.INITIAL_TEMPERATURE
                            / Math.log(RetailerConfigConstants.CURRENT_ROUND));
        }

        // 广播最终价格并标记算法结束
        retailer.getNewPriceVector().copyPriceVector(
                retailer.getNewPriceVector(), retailer.getCurrentPriceVector());
        synchronized (retailer.getNewPriceVector()) {
            retailer.getNewPriceVector().setAlgorithmEnded(true);
            retailer.getNewPriceVector().notifyAll();
        }
        RetailerLogger.logInfo("最终价格: " + retailer.getCurrentPriceVector().toString());
        RetailerLogger.logInfo("最终利润: " + retailer.getCurrentRetailerProfit());
    }

    /**
     * 单用户离线版 SAPC 算法（用于单机调试）。
     */
    public static void sapcAlgorithm() {
        int k = 1;
        float T = (float) Math.exp(-1);
        final float E = (float) Math.exp(-5);
        PriceVector priceVectorNow = new PriceVector();
        PriceVector priceVectorNew = new PriceVector();
        OneUserConsumVector consumNow = getUserTimeConsumByPrice(priceVectorNow);
        float profitNow = RetailerProfitCalculator.calculateRetailerProfit(
                new ElectricityConsumptionByTime(consumNow.getConsumVector()), priceVectorNow);

        RetailerLogger.logInfo("初始温度 T=" + T + "  终止温度 E=" + E);
        RetailerLogger.logInfo(priceVectorNow.toString());
        RetailerLogger.logInfo("初始利润: " + profitNow);

        while (T > E) {
            for (int h = 0; h < priceVectorNow.getPriceArray().length; h++) {
                float randomPrice = PriceVector.generateRandomPrice();
                priceVectorNew = priceVectorNow.createModifiedPriceVector(h, randomPrice, priceVectorNew);

                OneUserConsumVector consumNew = getUserTimeConsumByPrice(priceVectorNew);
                float profitNew = RetailerProfitCalculator.calculateRetailerProfit(
                        new ElectricityConsumptionByTime(consumNew.getConsumVector()), priceVectorNew);

                float acceptProb = (float) Math.exp((profitNew - profitNow) / T);
                if (profitNew > profitNow || (float) Math.random() < acceptProb) {
                    priceVectorNow.copyPriceVector(priceVectorNow, priceVectorNew);
                    profitNow = profitNew;
                }

                RetailerLogger.logInfo(priceVectorNew.toString());
                RetailerLogger.logInfo("利润: " + profitNew);
            }
            k++;
            T = (float) (T / Math.log(k));
            RetailerLogger.logInfo("====== 第 " + k + " 轮 ======");
        }

        RetailerLogger.logInfo("最终价格: " + priceVectorNow);
    }

    /**
     * 根据给定价格向量计算单用户最优用电向量（用于离线调试）。
     *
     * @param priceVector 当前电价向量
     * @return 用户最优用电向量
     */
    public static OneUserConsumVector getUserTimeConsumByPrice(PriceVector priceVector) {
        double[] consumVector = new double[UsersArgs.timeSlots];
        OneUserConsumVector oneUserConsumVector = new OneUserConsumVector(0, consumVector);
        return UserMaxSatisfaConsumVector.getConsumVectorByPriceVector(oneUserConsumVector, priceVector);
    }

    public static void main(String[] args) {
        SAPC_Algorithm.sapcAlgorithm();
    }

    // ── 私有辅助方法 ─────────────────────────────────────────────────────────

    /**
     * 广播新价格并等待所有用户响应（sleep → notifyAll → wait）。
     */
    private static void broadcastAndWait(Retailer retailer) throws InterruptedException {
        Thread.sleep(1500);
        synchronized (retailer.getNewPriceVector()) {
            retailer.getNewPriceVector().notifyAll();
        }
        synchronized (retailer.getCurrentConsumption()) {
            retailer.getCurrentConsumption().wait();
        }
    }

    /**
     * SA 接受准则：利润改善时直接接受；否则以 Metropolis 概率接受。
     */
    private static boolean shouldAccept(Retailer retailer) {
        double delta = retailer.getNewRetailerProfit() - retailer.getCurrentRetailerProfit();
        if (delta > 0) return true;
        float acceptProb = (float) Math.exp(delta / RetailerConfigConstants.INITIAL_TEMPERATURE);
        return (float) Math.random() < acceptProb;
    }
}