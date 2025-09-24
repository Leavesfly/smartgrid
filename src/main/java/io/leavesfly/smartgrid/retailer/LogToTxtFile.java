package io.leavesfly.smartgrid.retailer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志输出工具类
 * 用于将系统运行日志输出到指定的文本文件中
 * 支持线程安全的日志记录操作
 * 
 * @author SmartGrid Team
 * @version 1.0
 */
public class RetailerLogger {
    
    /** 文件输出流 */
    private static FileOutputStream fileOutputStream;
    
    /** 打印写入器 */
    private static final PrintWriter logWriter;
    
    /** 日期时间格式化器 */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // 静态初始化块，初始化日志文件写入器
    static {
        PrintWriter tempWriter = null;
        try {
            fileOutputStream = new FileOutputStream(new File(RetailerConfigConstants.LOG_FILE_PATH));
            tempWriter = new PrintWriter(fileOutputStream);
        } catch (FileNotFoundException e) {
            System.err.println("日志文件创建失败: " + e.getMessage());
            e.printStackTrace();
            // 如果文件创建失败，使用控制台输出作为备选
            tempWriter = new PrintWriter(System.out);
        }
        logWriter = tempWriter;
    }
    
    /**
     * 获取日志写入器
     * 返回线程安全的日志写入器实例
     * 
     * @return 日志写入器实例
     */
    public static synchronized PrintWriter getLogWriter() {
        return logWriter;
    }
    
    /**
     * 记录带时间戳的日志信息
     * 
     * @param message 要记录的日志信息
     */
    public static synchronized void logWithTimestamp(String message) {
        String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        logWriter.println("[" + timestamp + "] " + message);
        logWriter.flush();
    }
    
    /**
     * 记录信息日志
     * 
     * @param message 要记录的信息
     */
    public static synchronized void logInfo(String message) {
        logWithTimestamp("[INFO] " + message);
    }
    
    /**
     * 记录错误日志
     * 
     * @param message 错误信息
     */
    public static synchronized void logError(String message) {
        logWithTimestamp("[ERROR] " + message);
    }
    
    /**
     * 记录错误日志并包含异常信息
     * 
     * @param message 错误信息
     * @param throwable 异常对象
     */
    public static synchronized void logError(String message, Throwable throwable) {
        logError(message + ": " + throwable.getMessage());
        throwable.printStackTrace(logWriter);
        logWriter.flush();
    }
    
    /**
     * 关闭日志写入器和文件输出流
     * 应用程序退出时调用，确保资源正确释放
     */
    public static synchronized void close() {
        if (logWriter != null) {
            logWriter.close();
        }
        if (fileOutputStream != null) {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                System.err.println("关闭日志文件输出流失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 测试方法
     * 用于验证日志功能是否正常工作
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        RetailerLogger.logInfo("系统启动测试");
        RetailerLogger.logError("测试错误日志");
        RetailerLogger.close();
    }
}
