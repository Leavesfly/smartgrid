package io.leavesfly.smartgrid.retailer;

public class ConsumEleByTime {
	private int[] consumByTimeVector;
	private boolean isFull;
	
	public ConsumEleByTime(){
		consumByTimeVector=new int[PriceVector.getTimeSolts()];
		isFull=false;
	}
	
	public ConsumEleByTime(int[] consumByTimeVector ){
		this.consumByTimeVector=consumByTimeVector;
		isFull=false;
	}

	public int[] getConsumByTimeVector() {
		return consumByTimeVector;
	}

	public void setConsumByTimeVector(int[] consumByTimeVector) {
		this.consumByTimeVector = consumByTimeVector;
	}

	public boolean isFull() {
		return isFull;
	}

	public void setFull(boolean isFull) {
		this.isFull = isFull;
	}
	public static void consumByTimeNewToConsumByTimeNow(
			ConsumEleByTime consumByTimeNow, ConsumEleByTime consumByTimeNew) {
		if (consumByTimeNew.getConsumByTimeVector().length != consumByTimeNow
				.getConsumByTimeVector().length) {

			return;
		}
		for (int i = 0; i < consumByTimeNew.getConsumByTimeVector().length; i++) {
			consumByTimeNow.getConsumByTimeVector()[i] = consumByTimeNew
					.getConsumByTimeVector()[i];
		}
	}
	
	
	public String toString() {
		String str = "(";
		for (int i = 0; i < RetailerInitArgs.timeSlots; i++) {
			if (i == RetailerInitArgs.timeSlots - 1) {
				str += consumByTimeVector[i];
			} else {
				str += consumByTimeVector[i] + ",  ";
			}
		}
		str += ")";
		return str;
	}
	

}
