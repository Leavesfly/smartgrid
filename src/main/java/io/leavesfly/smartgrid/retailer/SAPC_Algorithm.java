package io.leavesfly.smartgrid.retailer;

import io.leavesfly.smartgrid.user.OneUserConsumVector;
import io.leavesfly.smartgrid.user.UserMaxSatisfaConsumVector;
import io.leavesfly.smartgrid.user.UsersArgs;

public class SAPC_Algorithm {

    public static void simulatedAnnealingAglorith(Retailer retailer)
            throws Exception {

        LogToTxtFile.getWritelogtofile().println("@开始计算....");
        synchronized (retailer.getAddStepNum()) {

            retailer.getAddStepNum().wait();
        }

        LogToTxtFile.getWritelogtofile().println("@价格向量...");
        retailer.getPriceVectorNew().privceVectorGiven(
                retailer.getPriceVectorNew(), retailer.getPriceVector());

        Thread.sleep(1500);
        synchronized (retailer.getPriceVectorNew()) {

            retailer.getPriceVectorNew().notifyAll();
        }

        synchronized (retailer.getConsumByTimeNow()) {
            retailer.getConsumByTimeNow().wait();
        }
        retailer.setRetailerProfitNow(retailer.getRetailerProfitNew());

        LogToTxtFile.getWritelogtofile().println("现在价格" + retailer.getRetailerProfitNow());


        while (RetailerInitArgs.T > RetailerInitArgs.E) {
            System.out.println("=================================="
                    + RetailerInitArgs.ROUND + "_Round=============================");
            LogToTxtFile.getWritelogtofile().println("=================================="
                    + RetailerInitArgs.ROUND + "_Round=============================");
            int position = 0;
            for (position = 0; position < retailer.getPriceVector().getPrices().length; position++) {

                float randomPrice = PriceVector.getOneRandomPrice();
                retailer.setPriceVectorNew(retailer.getPriceVector()
                        .getNewPriceVector(position, randomPrice,
                                retailer.getPriceVectorNew()));

                LogToTxtFile.getWritelogtofile().println("当前价格"
                        + retailer.getPriceVectorNew().toString()
                        + "...");


                Thread.sleep(1500);
                synchronized (retailer.getPriceVectorNew()) {
                    retailer.getPriceVectorNew().notifyAll();
                }
                synchronized (retailer.getConsumByTimeNow()) {
                    retailer.getConsumByTimeNow().wait();
                }

                LogToTxtFile.getWritelogtofile().println("retailer:" + retailer.getConsumByTimeNew());
                LogToTxtFile.getWritelogtofile().println("getRetailerProfitNew:" + retailer.getRetailerProfitNew());
                LogToTxtFile.getWritelogtofile().println("------------------------------");

                if (retailer.getRetailerProfitNew() > retailer
                        .getRetailerProfitNow()) {

                    retailer.getPriceVector().privceVectorGiven(
                            retailer.getPriceVector(),
                            retailer.getPriceVectorNew());

                    retailer.setRetailerProfitNow(retailer
                            .getRetailerProfitNew());
                } else {

                    if ((float) Math.random() < (float) (Math.exp((retailer
                            .getRetailerProfitNew() - retailer
                            .getRetailerProfitNow())
                            / RetailerInitArgs.T))) {
                        retailer.getPriceVector().privceVectorGiven(
                                retailer.getPriceVector(),
                                retailer.getPriceVectorNew());
                        retailer.setRetailerProfitNow(retailer
                                .getRetailerProfitNew());
                    }
                }

            }

            RetailerInitArgs.ROUND++;
            RetailerInitArgs.T = (float) (RetailerInitArgs.T / Math
                    .log(RetailerInitArgs.ROUND));

        }


        retailer.getPriceVectorNew().privceVectorGiven(
                retailer.getPriceVectorNew(), retailer.getPriceVector());

        synchronized (retailer.getPriceVectorNew()) {
            retailer.getPriceVectorNew().setEnd(true);
            retailer.getPriceVectorNew().notifyAll();
        }

        LogToTxtFile.getWritelogtofile().println(retailer.getPriceVector().toString());
        LogToTxtFile.getWritelogtofile().println(retailer.getRetailerProfitNow());
        LogToTxtFile.getWritelogtofile().flush();
        LogToTxtFile.getWritelogtofile().close();

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
