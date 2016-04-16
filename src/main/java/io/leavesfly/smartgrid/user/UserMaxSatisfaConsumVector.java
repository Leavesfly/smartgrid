package io.leavesfly.smartgrid.user;

import io.leavesfly.smartgrid.retailer.PriceVector;

public class UserMaxSatisfaConsumVector {
	
	public static OneUserConsumVector getConsumVectorByPriceVector(
			OneUserConsumVector oneUserConsumVector, PriceVector priceVector) {

		for (int time_h = 0; time_h < UsersArgs.timeSlots; time_h++) {

			int temp = 0;
			float applianceSum = 0f;
			for (int applianceNum = 0; applianceNum < UsersArgs.B_applianceNum; applianceNum++) {
				applianceSum += UserMaxSatisfaConsumVector
						.getOneB_applianceConsumOneUser(priceVector,
								oneUserConsumVector.getUserID(), time_h,
								applianceNum);
			}
			int oneTimeConsumOneUser = (int) applianceSum
					+ UsersArgs.A_applianceConsum[oneUserConsumVector
							.getUserID()][time_h];
			
			if (oneTimeConsumOneUser > UsersArgs.userMax[oneUserConsumVector
					.getUserID()]) {
				temp = UsersArgs.userMax[oneUserConsumVector.getUserID()];
			} else {
				temp = oneTimeConsumOneUser;
			}

			oneUserConsumVector.getConsumVector()[time_h] = temp;
		}

		return oneUserConsumVector;
	}

	public static float getOneB_applianceConsumOneUser(PriceVector priceVector,
			int userID, int time_h, int applianceNum) {
		float temp = 0f;
		temp = (float) (1.5 * (time_h + 1))
				/ (float) (UsersArgs.users_B_applianceSatisfa[userID][applianceNum][time_h] + (priceVector
						.getPriceByPosition(time_h)));
		if (temp > UsersArgs.B_applianceConsumMax[userID][applianceNum]) {
			return (float) UsersArgs.B_applianceConsumMax[userID][applianceNum];
		}
		return temp;

	}
}
