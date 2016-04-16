package io.leavesfly.smartgrid.retailer;

public class AddStepNum {
	private int stepNum = 0;

	public int getStepNum() {
		return stepNum;
	}

	public void setStepNum(int stepNum) {
		this.stepNum = stepNum;
	}

	public void upStepNum() {
		stepNum++;
	}
	public void resetStepNum(){
		stepNum=0;
	}
}
