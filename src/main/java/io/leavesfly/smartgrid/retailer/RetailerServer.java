package io.leavesfly.smartgrid.retailer;

import java.net.ServerSocket;
import java.net.Socket;

public class RetailerServer {


	public static void main(String[] args) throws Exception {


		final ServerSocket listenServer = new ServerSocket(RetailerInitArgs.ListenPort);

		LogToTxtFile.getWritelogtofile().println("服务端口"+RetailerInitArgs.ListenPort+"启动...");
		final Retailer retailer = new Retailer();


		Thread thread = new Thread(new Runnable() {

			public void run() {
				try {
					while (true) {
						Socket socket = listenServer.accept();

						new Thread(new RetailerThread(socket, retailer))
								.start();

						LogToTxtFile.getWritelogtofile().println("启动一个线程...");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();

		LogToTxtFile.getWritelogtofile().println("开始计算...");
		SAPC_Algorithm.simulatedAnnealingAglorith(retailer);

	}

}
