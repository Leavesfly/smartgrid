package io.leavesfly.smartgrid.retailer;

public class RetailerProfitAlgorithm {
	public static float getRetialProfit(ConsumEleByTime consumByTime ,PriceVector priceVector){
		
		final float a=RetailerInitArgs.a;
		final float b=RetailerInitArgs.b;
		final int w=RetailerInitArgs.w;
		
		if(consumByTime.getConsumByTimeVector().length !=PriceVector.getTimeSolts()){
			return -1f;
		}
		float profit=0f;
		for(int i=0;i<consumByTime.getConsumByTimeVector().length;i++){
			profit +=consumByTime.getConsumByTimeVector()[i]*priceVector.getPrices()[i];
		}
		float temp=0f;
		for(int j=0;j<consumByTime.getConsumByTimeVector().length;j++){
			temp +=a*(float)Math.pow(consumByTime.getConsumByTimeVector()[j], 2);
			temp +=b*(float)Math.pow(consumByTime.getConsumByTimeVector()[j], 3);
		}
		
		profit -=w*temp;
		return profit;
	}
	public static void main(String[] args){
		
		float[] priceVector=new float[]{0.5f,0.8f,1.0f,1.5f};
		PriceVector price=new PriceVector(priceVector);
		ConsumEleByTime consumByTime=new ConsumEleByTime();
		consumByTime.setConsumByTimeVector(new int[]{2,2,2,3});
		System.out.println(RetailerProfitAlgorithm.getRetialProfit(consumByTime, price));
	}
	
}
