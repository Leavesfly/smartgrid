package io.leavesfly.smartgrid.retailer;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

/**
 * 零售商服务器类
 * 智能电网系统中零售商端的主服务器
 * 负责监听用户连接、创建处理线程和执行SAPC算法
 * 采用多线程模型，同时处理多个用户连接和价格优化
 * 
 * @author SmartGrid Team
 * @version 1.0
 */
public class RetailerServer {
    
    /**
     * 主方法 - 服务器入口点
     * 启动零售商服务器，初始化相关组件并开始监听用户连接
     * 
     * @param args 命令行参数
     * @throws Exception 服务器启动过程中可能抛出的异常
     */
    public static void main(String[] args) throws Exception {
        
        // 初始化服务器Socket并开始监听
        final ServerSocket serverSocket = initializeServerSocket();
        
        // 记录服务器启动日志
        RetailerLogger.logInfo("零售商服务器在端口 " + RetailerConfigConstants.LISTEN_PORT + " 启动成功");
        
        // 初始化零售商核心对象
        final Retailer retailer = new Retailer();
        
        // 启动客户端连接监听线程
        Thread clientListenerThread = createClientListenerThread(serverSocket, retailer);
        clientListenerThread.start();
        
        // 记录算法开始日志
        RetailerLogger.logInfo("开始执行SAPC价格优化算法");
        
        // 执行SAPC模拟退火价格优化算法
        SAPC_Algorithm.simulatedAnnealingAglorith(retailer);
        
        // 关闭资源
        closeResources(serverSocket);
    }
    
    /**
     * 初始化服务器Socket
     * 
     * @return 初始化完成的ServerSocket对象
     * @throws IOException Socket创建过程中的异常
     */
    private static ServerSocket initializeServerSocket() throws IOException {
        try {
            return new ServerSocket(RetailerConfigConstants.LISTEN_PORT);
        } catch (IOException e) {
            RetailerLogger.logError("服务器Socket创建失败", e);
            throw e;
        }
    }
    
    /**
     * 创建客户端连接监听线程
     * 该线程负责接受用户连接并为每个连接创建独立的处理线程
     * 
     * @param serverSocket 服务器Socket
     * @param retailer 零售商核心对象
     * @return 客户端监听线程
     */
    private static Thread createClientListenerThread(final ServerSocket serverSocket, final Retailer retailer) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    listenForClientConnections(serverSocket, retailer);
                } catch (Exception e) {
                    RetailerLogger.logError("客户端监听线程发生异常", e);
                }
            }
        });
    }
    
    /**
     * 监听客户端连接
     * 持续监听新的客户端连接，并为每个连接创建独立的处理线程
     * 
     * @param serverSocket 服务器Socket
     * @param retailer 零售商核心对象
     * @throws IOException 网络连接异常
     */
    private static void listenForClientConnections(ServerSocket serverSocket, Retailer retailer) throws IOException {
        while (true) {
            // 等待客户端连接
            Socket clientSocket = serverSocket.accept();
            
            // 为新连接创建并启动处理线程
            Thread clientHandlerThread = new Thread(new RetailerThread(clientSocket, retailer));
            clientHandlerThread.start();
            
            // 记录新连接日志
            RetailerLogger.logInfo("新用户连接已建立，客户端端口: " + clientSocket.getPort());
        }
    }
    
    /**
     * 关闭服务器资源
     * 在服务器关闭时清理相关资源
     * 
     * @param serverSocket 要关闭的服务器Socket
     */
    private static void closeResources(ServerSocket serverSocket) {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                RetailerLogger.logInfo("服务器Socket已关闭");
            }
        } catch (IOException e) {
            RetailerLogger.logError("关闭服务器Socket时发生异常", e);
        } finally {
            // 关闭日志记录器
            RetailerLogger.close();
        }
    }
}
