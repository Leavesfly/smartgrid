package io.leavesfly.smartgrid.user;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import io.leavesfly.smartgrid.retailer.PriceVector;

public class UserThread implements Runnable {
	private int userID;

	public UserThread(int userID) {
		this.userID = userID;
	}

	@Override
	public void run() {
		try {// �������Ӳ�����������������������
			Socket socket = new Socket(UsersArgs.ip, UsersArgs.port);
			System.out.println("User_" + userID + "�ڵ�ǰ��socket��"
					+ socket.getLocalPort() + "��ȡ���ӳɹ�...");
			LogToTxtFile.getWritelogtofile().println("User_" + userID + "�ڵ�ǰ��socket��"
					+ socket.getLocalPort() + "��ȡ���ӳɹ�...");

			ObjectInputStream objIn = new ObjectInputStream(
					socket.getInputStream());
			ObjectOutputStream objOut = new ObjectOutputStream(
					socket.getOutputStream());
			while (true) {
			
				PriceVector priceVector = (PriceVector) objIn.readObject();

				System.out.println("User_" + userID + "�ɹ���ȡ���㲥�ļ۸�"
						+ priceVector.toString());
				LogToTxtFile.getWritelogtofile().println("User_" + userID + "�ɹ���ȡ���㲥�ļ۸�"
						+ priceVector.toString());
				
				if (priceVector.isEnd()) {
					System.out.println( "�����̸��������ռ۸��ǣ�"
							+ priceVector.toString());
					LogToTxtFile.getWritelogtofile().println("�����̸��������ռ۸��ǣ�"
							+ priceVector.toString());
					LogToTxtFile.getWritelogtofile().flush();
					break;
				}
				int[] consumVector = new int[UsersArgs.timeSlots];
				OneUserConsumVector oneUserConsumVector = new OneUserConsumVector(
						userID, consumVector);
				oneUserConsumVector = UserMaxSatisfaConsumVector
						.getConsumVectorByPriceVector(oneUserConsumVector,
								priceVector);
				objOut.writeObject(oneUserConsumVector);
				System.out.println("User_" + userID + "�ڸü۸��¼ƻ��õ�����"
						+ oneUserConsumVector.toString());
				LogToTxtFile.getWritelogtofile().println("User_" + userID + "�ڸü۸��¼ƻ��õ�����"
						+ oneUserConsumVector.toString());
				LogToTxtFile.getWritelogtofile().flush();
			}
			objOut.flush();
			objIn.close();
			objOut.close();
			socket.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		PriceVector priceVector = new PriceVector();
		System.out.println(priceVector.toString());
		int[] consumVector = new int[UsersArgs.timeSlots];
		OneUserConsumVector oneUserConsumVector = new OneUserConsumVector(0,
				consumVector);
		oneUserConsumVector = UserMaxSatisfaConsumVector
				.getConsumVectorByPriceVector(oneUserConsumVector, priceVector);
		System.out.println(oneUserConsumVector.toString());
	}

}
