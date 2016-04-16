package io.leavesfly.smartgrid.retailer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import io.leavesfly.smartgrid.user.OneUserConsumVector;
import io.leavesfly.smartgrid.user.UsersArgs;


public class RetailerThread implements Runnable {
	private Socket socket;
	private Retailer retailer;

	public RetailerThread(Socket socket, Retailer retailer) {
		this.socket = socket;
		this.retailer = retailer;
	}


	public void run() {

		LogToTxtFile.getWritelogtofile().println("启动" + socket.getPort());
		try {


			ObjectOutputStream objOut = new ObjectOutputStream(
					socket.getOutputStream());
			ObjectInputStream objIn = new ObjectInputStream(
					socket.getInputStream());

			synchronized (retailer.getAddStepNum()) {
				retailer.getAddStepNum().upStepNum();
				if (retailer.getAddStepNum().getStepNum() == UsersArgs.userNum) {
					retailer.getAddStepNum().notify();
				}
			}
			while (true) {

				synchronized (retailer.getPriceVectorNew()) {
					retailer.getPriceVectorNew().wait();

					if (retailer.getPriceVectorNew().isEnd()) {
						objOut.writeObject(new PriceVector(retailer
								.getPriceVectorNew()));
						objOut.flush();

						break;
					}
				}

				objOut.writeObject(new PriceVector(retailer.getPriceVectorNew()));


				OneUserConsumVector oneUserConsumVector = (OneUserConsumVector) objIn
						.readObject();

				synchronized (retailer.getUserConsumList()) {
					retailer.getUserConsumList().add(oneUserConsumVector);
					if (retailer.getUserConsumList().size() == UsersArgs.userNum) {

						Retailer.fillConsumVectorByTime(
								retailer.getConsumByTimeNew(),
								retailer.getUserConsumList());
						retailer.getUserConsumList().clear();

						retailer.setRetailerProfitNew(RetailerProfitAlgorithm
								.getRetialProfit(retailer.getConsumByTimeNew(),
										retailer.getPriceVectorNew()));
						synchronized (retailer.getConsumByTimeNow()) {
							retailer.getConsumByTimeNow().notify();
						}
					}

				}
			}

			Thread.sleep(2000);

			objIn.close();
			objOut.close();
			socket.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}