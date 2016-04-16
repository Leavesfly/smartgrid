package io.leavesfly.smartgrid.user;

import java.io.Serializable;

public class OneUserConsumVector implements Serializable {
	
	private static final long serialVersionUID = 2500492976644903992L;
	public final static int timeSolts = 4;
	private int userID; 
	private int[] consumVector;
	
	public OneUserConsumVector(int userID,int[] consumVector){
		this.userID=userID;
		this.consumVector=consumVector;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public int[] getConsumVector() {
		return consumVector;
	}

	public void setConsumVector(int[] consumVector) {
		this.consumVector = consumVector;
	}

	public static int getTimesolts() {
		return timeSolts;
	}
	
	public String toString() {
		String str = "oneUserConsumVector:(";
		for (int i = 0; i < timeSolts; i++) {
			if (i == timeSolts - 1) {
				str += consumVector[i];
			} else {
				str += consumVector[i] + ",  ";
			}
		}
		str += ")";
		return str;
	}
	
	
	
}
