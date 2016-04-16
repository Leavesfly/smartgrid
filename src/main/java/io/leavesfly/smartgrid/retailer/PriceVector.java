package io.leavesfly.smartgrid.retailer;

import java.io.Serializable;

public class PriceVector implements Serializable {

	private static final long serialVersionUID = -5652678536888894383L;
	private final static int timeSolts = 4;
	private final static float minPrice = 0.5f;
	private final static float maxPrice = 1.5f;
	private boolean isEnd = false;

	private boolean isNewPrice = false;
	private float[] prices;

	public PriceVector() {
		this.prices = new float[timeSolts];
		double randomPrice = minPrice + Math.random() * (maxPrice - minPrice);
		for (int i = 0; i < timeSolts; i++) {
			prices[i] = (float) randomPrice;
		}
	}
	
	public PriceVector(PriceVector priceVector){
		this.prices = new float[timeSolts];
		this.isEnd=priceVector.isEnd;
		for(int i=0;i<timeSolts;i++){
			this.prices[i]=priceVector.getPrices()[i];
		}
		
	}
	

	public PriceVector(float[] prices) {
		this.prices = prices;
	}

	public static final int getTimeSolts() {
		return timeSolts;
	}

	public float[] getPrices() {
		return prices;
	}

	public boolean isNewPrice() {
		return isNewPrice;
	}

	public void setNewPrice(boolean isNewPrice) {
		this.isNewPrice = isNewPrice;
	}

	public void setPriceByPosition(int position, float price) {

		prices[position] = price;
	}

	public PriceVector getNewPriceVector(int position, float price,
			PriceVector priceVectorNew) {
		privceVectorGiven(priceVectorNew, this);
		priceVectorNew.setPriceByPosition(position, price);
		return priceVectorNew;
	}

	public float getPriceByPosition(int position) {
		if (position >= 0 && position < timeSolts) {
			return prices[position];
		}
		return -1f;
	}

	public static float getOneRandomPrice() {
		return (float) (minPrice + Math.random() * (maxPrice - minPrice));
	}

	public boolean isEnd() {
		return isEnd;
	}

	public void privceVectorGiven(PriceVector priceVectorNow,
			PriceVector priceVectorNew) {
		for (int i = 0; i < priceVectorNew.getPrices().length; i++) {
			priceVectorNow.getPrices()[i] = priceVectorNew.getPrices()[i];
		}
	}

	public String toString() {
		String str = "prices:(";
		for (int i = 0; i < timeSolts; i++) {
			if (i == timeSolts - 1) {
				str += prices[i];
			} else {
				str += prices[i] + ",  ";
			}
		}
		str += ")";
		return str;
	}

	public void setEnd(boolean isEnd) {
		this.isEnd = isEnd;

	}

}
