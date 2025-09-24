package io.leavesfly.smartgrid.retailer;

import io.leavesfly.smartgrid.user.OneUserConsumVector;
import io.leavesfly.smartgrid.user.UserMaxSatisfaConsumVector;
import io.leavesfly.smartgrid.user.UsersArgs;

/**
 * SAPC（Simulated Annealing Price Control）价格控制算法类
 * 实现智能电网中零售商的动态定价算法
 * 采用模拟退火算法找到最优价格策略，最大化零售商利润
 * 
 * 算法核心原理：
 * 1. 初始化随机价格向量
 * 2. 在每个温度下，随机扰动价格向量
 * 3. 根据利润改善情况决定是否接受新价格
 * 4. 逐渐降低温度，直至收敛
 * 
 * @author SmartGrid Team
 * @version 1.0
 */
public final class SAPC_Algorithm {
    
    /**
     * 模拟退火价格优化算法主方法
     * 在多线程环境中与用户进行交互，逐步优化价格策略
     * 
     * @param retailer 零售商核心对象，包含共享状态和同步机制
     * @throws Exception 算法执行过程中的异常
     */
    public static void simulatedAnnealingAglorith(Retailer retailer) throws Exception {
        
        RetailerLogger.logInfo("SAPC算法开始执行");
        
        // 等待所有用户连接完成
        synchronized (retailer.getStepCounter()) {
            retailer.getStepCounter().wait();
        }
        
        RetailerLogger.logInfo("所有用户已连接，SAPC算法开始初始化");
        
        // 初始化价格向量并进行第一轮交互
        retailer.getNewPriceVector().copyPriceVector(
            retailer.getNewPriceVector(), 
            retailer.getCurrentPriceVector()
        );
        
        Thread.sleep(1500);
        synchronized (retailer.getNewPriceVector()) {
            retailer.getNewPriceVector().notifyAll();
        }
        
        synchronized (retailer.getCurrentConsumption()) {
            retailer.getCurrentConsumption().wait();
        }
        retailer.setCurrentRetailerProfit(retailer.getNewRetailerProfit());
        
        RetailerLogger.logInfo("初始利润: " + retailer.getCurrentRetailerProfit());
        
        // 执行主算法循环
        while (RetailerConfigConstants.INITIAL_TEMPERATURE > RetailerConfigConstants.END_TEMPERATURE) {
            RetailerLogger.logInfo(
                "========================== 第 " + RetailerConfigConstants.CURRENT_ROUND + 
                " 轮迭代 =========================="
            );
            
            int position = 0;
            for (position = 0; position < retailer.getCurrentPriceVector().getPriceArray().length; position++) {
                
                float randomPrice = PriceVector.generateRandomPrice();
                retailer.setNewPriceVector(retailer.getCurrentPriceVector()
                        .createModifiedPriceVector(position, randomPrice,
                                retailer.getNewPriceVector()));
                
                RetailerLogger.logInfo("当前价格" + retailer.getNewPriceVector().toString());
                
                Thread.sleep(1500);
                synchronized (retailer.getNewPriceVector()) {
                    retailer.getNewPriceVector().notifyAll();
                }
                synchronized (retailer.getCurrentConsumption()) {
                    retailer.getCurrentConsumption().wait();
                }
                
                RetailerLogger.logInfo("系统总消耗:" + retailer.getNewConsumption());
                RetailerLogger.logInfo("新利润:" + retailer.getNewRetailerProfit());
                RetailerLogger.logInfo("------------------------------");
                
                if (retailer.getNewRetailerProfit() > retailer.getCurrentRetailerProfit()) {
                    retailer.getCurrentPriceVector().copyPriceVector(
                            retailer.getCurrentPriceVector(),
                            retailer.getNewPriceVector());
                    retailer.setCurrentRetailerProfit(retailer.getNewRetailerProfit());
                } else {
                    if ((float) Math.random() < (float) (Math.exp((retailer
                            .getNewRetailerProfit() - retailer
                            .getCurrentRetailerProfit())
                            / RetailerConfigConstants.INITIAL_TEMPERATURE))) {
                        retailer.getCurrentPriceVector().copyPriceVector(
                                retailer.getCurrentPriceVector(),
                                retailer.getNewPriceVector());
                        retailer.setCurrentRetailerProfit(retailer.getNewRetailerProfit());
                    }
                }
            }

            RetailerConfigConstants.CURRENT_ROUND++;
            RetailerConfigConstants.INITIAL_TEMPERATURE = (float) (RetailerConfigConstants.INITIAL_TEMPERATURE / Math
                    .log(RetailerConfigConstants.CURRENT_ROUND));
        }

        // 完成算法并发送最终结果
        retailer.getNewPriceVector().copyPriceVector(
                retailer.getNewPriceVector(), retailer.getCurrentPriceVector());

        synchronized (retailer.getNewPriceVector()) {
            retailer.getNewPriceVector().setAlgorithmEnded(true);
            retailer.getNewPriceVector().notifyAll();
        }

        RetailerLogger.logInfo("最终价格: " + retailer.getCurrentPriceVector().toString());
        RetailerLogger.logInfo("最终利润: " + retailer.getCurrentRetailerProfit());
    }

    public static void sapcAglorith() {
        int k = 1;
        float T = (float) Math.exp(-1);
        final float E = (float) Math.exp(-5);
        PriceVector priceVectorNow = new PriceVector();
        PriceVector priceVectorNew = new PriceVector();
        OneUserConsumVector userTimeConsumNow = SAPC_Algorithm
                .getUserTimeConsumByPrice(priceVectorNow);
        float profitNow = RetailerProfitAlgorithm.getRetialProfit(
                new ConsumEleByTime(userTimeConsumNow.getConsumVector()),
                priceVectorNow);
        System.out.println("T:" + T);
        System.out.println("E:" + E);
        System.out.println(priceVectorNow.toString());
        System.out.println("profitNow:" + profitNow);

        while (T > E) {
            int position = 0;
            for (position = 0; position < priceVectorNow.getPrices().length; position++) {


                float randomPrice = PriceVector.getOneRandomPrice();
                PriceVector PriceVetcorNew = priceVectorNow.getNewPriceVector(
                        position, randomPrice, priceVectorNew);


                OneUserConsumVector userTimeConsumNew = SAPC_Algorithm
                        .getUserTimeConsumByPrice(PriceVetcorNew);

                float profitNew = RetailerProfitAlgorithm
                        .getRetialProfit(
                                new ConsumEleByTime(userTimeConsumNew
                                        .getConsumVector()), PriceVetcorNew);


                if (profitNew > profitNow) {
                    priceVectorNow.privceVectorGiven(priceVectorNow,
                            PriceVetcorNew);
                    profitNow = profitNew;
                } else {
                    if ((float) Math.random() < (float) (Math
                            .exp((profitNew - profitNow) / T))) {
                        priceVectorNow.privceVectorGiven(priceVectorNow,
                                PriceVetcorNew);
                        profitNow = profitNew;
                    }
                }


                LogToTxtFile.getWritelogtofile().println(PriceVetcorNew.toString());

                LogToTxtFile.getWritelogtofile().println("profitNew:" + profitNew);
            }
            k++;
            T = (float) (T / Math.log(k));


            LogToTxtFile.getWritelogtofile().println("======================"
                    + k
                    + "_Round========================");
        }

        LogToTxtFile.getWritelogtofile().println("The right :" + priceVectorNow);
        LogToTxtFile.getWritelogtofile().flush();
        LogToTxtFile.getWritelogtofile().close();
    }

    public static OneUserConsumVector getUserTimeConsumByPrice(
            PriceVector priceVector) {
        int[] consumVector = new int[UsersArgs.timeSlots];
        OneUserConsumVector oneUserConsumVector = new OneUserConsumVector(0,
                consumVector);

        oneUserConsumVector = UserMaxSatisfaConsumVector
                .getConsumVectorByPriceVector(oneUserConsumVector, priceVector);
        System.out.println(oneUserConsumVector.toString());
        return oneUserConsumVector;
    }


    public static void main(String[] args) {
        SAPC_Algorithm.sapcAglorith();
    }
}
