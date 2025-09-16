package io.leavesfly.smartgrid.retailer;

import java.util.ArrayList;
import java.util.List;

import io.leavesfly.smartgrid.user.OneUserConsumVector;
import io.leavesfly.smartgrid.user.UsersArgs;


public class Retailer {
	

	private AddStepNum addStepNum;

	private PriceVector priceVector;
	private PriceVector priceVectorNew;
	private ConsumEleByTime consumByTimeNow;
	private float retailerProfitNow;
	private ConsumEleByTime consumByTimeNew;
	private float retailerProfitNew;
	private List<OneUserConsumVector> userConsumVectList;

	public Retailer() {

		addStepNum = new AddStepNum();

		priceVector = new PriceVector();
		priceVectorNew = new PriceVector();
		consumByTimeNow = new ConsumEleByTime();
		consumByTimeNew = new ConsumEleByTime();
		userConsumVectList = new ArrayList<OneUserConsumVector>();
	}

	public AddStepNum getAddStepNum() {
		return addStepNum;
	}

	public float getRetailerProfitNow() {
		return retailerProfitNow;
	}


	public void setRetailerProfitNow(float retailerProfitNow) {
		this.retailerProfitNow = retailerProfitNow;
	}

	public float getRetailerProfitNew() {
		return retailerProfitNew;
	}

	public void setRetailerProfitNew(float retailerProfitNew) {
		this.retailerProfitNew = retailerProfitNew;
	}

	public PriceVector getPriceVector() {
		return priceVector;
	}

	public List<OneUserConsumVector> getUserConsumList() {
		return userConsumVectList;
	}

	public ConsumEleByTime getConsumByTimeNow() {
		return consumByTimeNow;
	}

	public PriceVector getPriceVectorNew() {
		return priceVectorNew;
	}

	public void setPriceVectorNew(PriceVector priceVectorNew) {
		this.priceVectorNew = priceVectorNew;
	}

	public void setConsumByTimeNow(ConsumEleByTime consumByTimeNow) {
		this.consumByTimeNow = consumByTimeNow;
	}

	public ConsumEleByTime getConsumByTimeNew() {
		return consumByTimeNew;
	}

	public void setConsumByTimeNew(ConsumEleByTime consumByTimeNew) {
		this.consumByTimeNew = consumByTimeNew;
	}

	public static void fillConsumVectorByTime(ConsumEleByTime consumByTime,
			List<OneUserConsumVector> userConsumVectList) {
		if (userConsumVectList.size() != UsersArgs.userNum) {
			System.out.println(userConsumVectList.size());
			System.out.println("fillConsumVectorByTime");
		}
		for (int i = 0; i < consumByTime.getConsumByTimeVector().length; i++) {
			int consumInTime = 0;
			for (OneUserConsumVector oneUser : userConsumVectList) {
				consumInTime += oneUser.getConsumVector()[i];
			}
			consumByTime.getConsumByTimeVector()[i] = consumInTime;

		}
	}

}
