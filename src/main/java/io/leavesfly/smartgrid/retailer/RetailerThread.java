package io.leavesfly.smartgrid.retailer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.io.IOException;

import io.leavesfly.smartgrid.user.OneUserConsumVector;
import io.leavesfly.smartgrid.user.UsersArgs;

/**
 * 零售商线程类
 * 负责处理单个用户连接的线程，实现与用户的通信和数据交换
 * 包括发送价格信息、接收用户消耗数据、同步SAPC算法执行
 * 支持多线程并发操作和线程间同步
 * 
 * @author SmartGrid Team
 * @version 1.0
 */
public class RetailerThread implements Runnable {
    
    /** 客户端连接Socket */
    private Socket clientSocket;
    
    /** 零售商核心对象（共享状态） */
    private Retailer retailer;
    
    /**
     * 构造函数
     * 
     * @param clientSocket 客户端连接Socket
     * @param retailer 零售商核心对象
     */
    public RetailerThread(Socket clientSocket, Retailer retailer) {
        this.clientSocket = clientSocket;
        this.retailer = retailer;
    }
    
    /**
     * 线程执行方法
     * 实现与单个用户的完整通信流程
     */
    @Override
    public void run() {
        
        RetailerLogger.logInfo("用户线程启动，客户端端口: " + clientSocket.getPort());
        
        ObjectOutputStream objectOutputStream = null;
        ObjectInputStream objectInputStream = null;
        
        try {
            // 初始化输入输出流
            objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            
            // 通知SAPC算法有新用户连接
            notifyNewUserConnection();
            
            // 开始价格-消耗数据交换循环
            executePriceConsumptionLoop(objectOutputStream, objectInputStream);
            
        } catch (Exception e) {
            RetailerLogger.logError("用户线程执行过程中发生异常", e);
        } finally {
            // 清理资源
            cleanupResources(objectOutputStream, objectInputStream);
        }
    }
    
    /**
     * 通知SAPC算法有新用户连接
     * 增加用户连接计数，当所有用户都连接后通知算法开始
     */
    private void notifyNewUserConnection() {
        synchronized (retailer.getStepCounter()) {
            retailer.getStepCounter().incrementStep();
            
            // 当所有用户都已连接时，唤醒等待中的SAPC算法
            if (retailer.getStepCounter().getStepCount() == UsersArgs.userNum) {
                retailer.getStepCounter().notify();
                RetailerLogger.logInfo("所有用户已连接，通知SAPC算法开始");
            }
        }
    }
    
    /**
     * 执行价格-消耗数据交换循环
     * 在SAPC算法迭代过程中与用户进行数据交换
     * 
     * @param objectOutputStream 对象输出流
     * @param objectInputStream 对象输入流
     * @throws Exception 数据交换过程中的异常
     */
    private void executePriceConsumptionLoop(ObjectOutputStream objectOutputStream, 
                                           ObjectInputStream objectInputStream) throws Exception {
        
        while (true) {
            // 等待SAPC算法发送新价格
            PriceVector currentPrice = waitForNewPrice();
            
            // 检查算法是否结束
            if (currentPrice.isAlgorithmEnded()) {
                sendFinalPriceAndExit(objectOutputStream, currentPrice);
                break;
            }
            
            // 发送价格给用户
            sendPriceToUser(objectOutputStream, currentPrice);
            
            // 接收用户消耗数据
            OneUserConsumVector userConsumption = receiveUserConsumption(objectInputStream);
            
            // 处理用户消耗数据
            processUserConsumption(userConsumption);
        }
    }
    
    /**
     * 等待SAPC算法发送新价格
     * 
     * @return 新的价格向量
     * @throws InterruptedException 线程中断异常
     */
    private PriceVector waitForNewPrice() throws InterruptedException {
        synchronized (retailer.getNewPriceVector()) {
            retailer.getNewPriceVector().wait();
            return new PriceVector(retailer.getNewPriceVector());
        }
    }
    
    /**
     * 发送最终价格并退出
     * 
     * @param objectOutputStream 对象输出流
     * @param finalPrice 最终价格向量
     * @throws IOException 输出异常
     */
    private void sendFinalPriceAndExit(ObjectOutputStream objectOutputStream, 
                                     PriceVector finalPrice) throws IOException {
        objectOutputStream.writeObject(finalPrice);
        objectOutputStream.flush();
        RetailerLogger.logInfo("向用户发送最终价格，线程即将退出");
    }
    
    /**
     * 发送价格给用户
     * 
     * @param objectOutputStream 对象输出流
     * @param priceVector 要发送的价格向量
     * @throws IOException 输出异常
     */
    private void sendPriceToUser(ObjectOutputStream objectOutputStream, 
                               PriceVector priceVector) throws IOException {
        objectOutputStream.writeObject(priceVector);
        objectOutputStream.flush();
        RetailerLogger.logInfo("向用户发送价格: " + priceVector.toString());
    }
    
    /**
     * 接收用户消耗数据
     * 
     * @param objectInputStream 对象输入流
     * @return 用户消耗数据
     * @throws IOException 输入异常
     * @throws ClassNotFoundException 类不存在异常
     */
    private OneUserConsumVector receiveUserConsumption(ObjectInputStream objectInputStream) 
            throws IOException, ClassNotFoundException {
        OneUserConsumVector userConsumption = (OneUserConsumVector) objectInputStream.readObject();
        RetailerLogger.logInfo("接收到用户消耗数据: " + userConsumption.toString());
        return userConsumption;
    }
    
    /**
     * 处理用户消耗数据
     * 将用户消耗数据添加到全局列表，并在收集完所有用户数据后通知SAPC算法
     * 
     * @param userConsumption 用户消耗数据
     */
    private void processUserConsumption(OneUserConsumVector userConsumption) {
        synchronized (retailer.getUserConsumptionList()) {
            // 添加用户消耗数据到列表
            retailer.getUserConsumptionList().add(userConsumption);
            
            // 检查是否收集完所有用户的消耗数据
            if (retailer.getUserConsumptionList().size() == UsersArgs.userNum) {
                // 聚合所有用户的消耗数据
                Retailer.aggregateUserConsumption(
                    retailer.getNewConsumption(), 
                    retailer.getUserConsumptionList()
                );
                
                // 清空用户消耗列表以备下一轮使用
                retailer.getUserConsumptionList().clear();
                
                // 计算新的零售商利润
                float newProfit = RetailerProfitCalculator.calculateRetailerProfit(
                    retailer.getNewConsumption(), 
                    retailer.getNewPriceVector()
                );
                retailer.setNewRetailerProfit(newProfit);
                
                // 通知SAPC算法数据已准备完成
                synchronized (retailer.getCurrentConsumption()) {
                    retailer.getCurrentConsumption().notify();
                }
                
                RetailerLogger.logInfo("所有用户消耗数据已聚合完成，通知SAPC算法");
            }
        }
    }
    
    /**
     * 清理资源
     * 关闭输入输出流和Socket连接
     * 
     * @param objectOutputStream 对象输出流
     * @param objectInputStream 对象输入流
     */
    private void cleanupResources(ObjectOutputStream objectOutputStream, 
                                ObjectInputStream objectInputStream) {
        try {
            // 等待一段时间确保数据发送完成
            Thread.sleep(2000);
            
            // 关闭输入流
            if (objectInputStream != null) {
                objectInputStream.close();
            }
            
            // 关闭输出流
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
            
            // 关闭Socket连接
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            
            RetailerLogger.logInfo("用户线程资源清理完成");
            
        } catch (Exception e) {
            RetailerLogger.logError("清理线程资源时发生异常", e);
        }
    }
}