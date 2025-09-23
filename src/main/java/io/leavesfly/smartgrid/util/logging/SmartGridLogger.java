package io.leavesfly.smartgrid.util.logging;

import io.leavesfly.smartgrid.core.config.SmartGridConfig;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 智能电网系统统一日志管理器（重构版）
 * 提供线程安全的日志记录功能，支持多个日志文件和不同日志级别
 * 
 * 主要改进：
 * 1. 线程安全的单例模式
 * 2. 支持多个日志文件管理
 * 3. 提供不同的日志级别
 * 4. 自动创建日志目录
 * 5. 资源自动管理和清理
 * 6. 异常处理和错误恢复
 * 
 * @author SmartGrid Team
 * @version 2.0
 */
public class SmartGridLogger {
    
    /** 日志级别枚举 */
    public enum LogLevel {
        DEBUG("DEBUG"),
        INFO("INFO"),
        WARN("WARN"),
        ERROR("ERROR");
        
        private final String levelName;
        
        LogLevel(String levelName) {
            this.levelName = levelName;
        }
        
        public String getLevelName() {
            return levelName;
        }
    }
    
    /** 日志类型枚举 */
    public enum LogType {
        RETAILER("retailer"),
        USER("user"),
        SYSTEM("system");
        
        private final String typeName;
        
        LogType(String typeName) {
            this.typeName = typeName;
        }
        
        public String getTypeName() {
            return typeName;
        }
    }
    
    /** 单例实例 */
    private static volatile SmartGridLogger instance;
    
    /** 日期格式化器 */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    /** PrintWriter缓存 */
    private final ConcurrentHashMap<LogType, PrintWriter> writerCache = new ConcurrentHashMap<>();
    
    /** 读写锁，保护writer缓存 */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    /** 当前日志级别 */
    private volatile LogLevel currentLogLevel = LogLevel.INFO;
    
    /** 是否启用控制台输出 */
    private volatile boolean consoleEnabled = true;
    
    /**
     * 私有构造函数，防止外部实例化
     */
    private SmartGridLogger() {
        // 确保日志目录存在
        createLogDirectoryIfNeeded();
        
        // 注册JVM关闭钩子，确保资源清理
        Runtime.getRuntime().addShutdownHook(new Thread(this::closeAllWriters));
    }
    
    /**
     * 获取单例实例
     * @return SmartGridLogger实例
     */
    public static SmartGridLogger getInstance() {
        if (instance == null) {
            synchronized (SmartGridLogger.class) {
                if (instance == null) {
                    instance = new SmartGridLogger();
                }
            }
        }
        return instance;
    }
    
    /**
     * 记录信息级别日志
     * @param logType 日志类型
     * @param message 日志消息
     */
    public void info(LogType logType, String message) {
        log(LogLevel.INFO, logType, message);
    }
    
    /**
     * 记录调试级别日志
     * @param logType 日志类型
     * @param message 日志消息
     */
    public void debug(LogType logType, String message) {
        log(LogLevel.DEBUG, logType, message);
    }
    
    /**
     * 记录警告级别日志
     * @param logType 日志类型
     * @param message 日志消息
     */
    public void warn(LogType logType, String message) {
        log(LogLevel.WARN, logType, message);
    }
    
    /**
     * 记录错误级别日志
     * @param logType 日志类型
     * @param message 日志消息
     */
    public void error(LogType logType, String message) {
        log(LogLevel.ERROR, logType, message);
    }
    
    /**
     * 记录异常信息
     * @param logType 日志类型
     * @param message 日志消息
     * @param throwable 异常对象
     */
    public void error(LogType logType, String message, Throwable throwable) {
        String fullMessage = message + "\n" + getStackTrace(throwable);
        log(LogLevel.ERROR, logType, fullMessage);
    }
    
    /**
     * 通用日志记录方法
     * @param level 日志级别
     * @param logType 日志类型
     * @param message 日志消息
     */
    public void log(LogLevel level, LogType logType, String message) {
        if (!shouldLog(level)) {
            return;
        }
        
        String formattedMessage = formatMessage(level, logType, message);
        
        // 输出到控制台
        if (consoleEnabled) {
            System.out.println(formattedMessage);
        }
        
        // 输出到文件
        writeToFile(logType, formattedMessage);
    }
    
    /**
     * 格式化日志消息
     * @param level 日志级别
     * @param logType 日志类型
     * @param message 原始消息
     * @return 格式化后的消息
     */
    private String formatMessage(LogLevel level, LogType logType, String message) {
        String timestamp = DATE_FORMAT.format(new Date());
        String threadName = Thread.currentThread().getName();
        
        return String.format("[%s] [%s] [%s] [%s] %s",
                timestamp, level.getLevelName(), logType.getTypeName().toUpperCase(), threadName, message);
    }
    
    /**
     * 判断是否应该记录该级别的日志
     * @param level 日志级别
     * @return 如果应该记录返回true
     */
    private boolean shouldLog(LogLevel level) {
        return level.ordinal() >= currentLogLevel.ordinal();
    }
    
    /**
     * 写入日志到文件
     * @param logType 日志类型
     * @param message 格式化后的消息
     */
    private void writeToFile(LogType logType, String message) {
        lock.readLock().lock();
        try {
            PrintWriter writer = getOrCreateWriter(logType);
            if (writer != null) {
                writer.println(message);
                writer.flush();
            }
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 获取或创建指定类型的PrintWriter
     * @param logType 日志类型
     * @return PrintWriter实例
     */
    private PrintWriter getOrCreateWriter(LogType logType) {
        PrintWriter writer = writerCache.get(logType);
        if (writer == null) {
            lock.writeLock().lock();
            try {
                // 双重检查
                writer = writerCache.get(logType);
                if (writer == null) {
                    writer = createWriter(logType);
                    if (writer != null) {
                        writerCache.put(logType, writer);
                    }
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
        return writer;
    }
    
    /**
     * 创建指定类型的PrintWriter
     * @param logType 日志类型
     * @return PrintWriter实例，如果创建失败返回null
     */
    private PrintWriter createWriter(LogType logType) {
        try {
            String logFilePath = getLogFilePath(logType);
            File logFile = new File(logFilePath);
            
            // 确保父目录存在
            File parentDir = logFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // 创建PrintWriter，启用自动刷新
            FileWriter fileWriter = new FileWriter(logFile, true);
            return new PrintWriter(fileWriter, true);
            
        } catch (IOException e) {
            System.err.println("创建日志文件失败: " + logType.getTypeName() + ", 错误: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取指定类型的日志文件路径
     * @param logType 日志类型
     * @return 日志文件路径
     */
    private String getLogFilePath(LogType logType) {
        switch (logType) {
            case RETAILER:
                return SmartGridConfig.RETAILER_LOG_FILE;
            case USER:
                return SmartGridConfig.USER_LOG_FILE;
            case SYSTEM:
                return "logs/system.log";
            default:
                return "logs/default.log";
        }
    }
    
    /**
     * 创建日志目录
     */
    private void createLogDirectoryIfNeeded() {
        File logDir = new File("logs");
        if (!logDir.exists()) {
            boolean created = logDir.mkdirs();
            if (!created) {
                System.err.println("警告: 无法创建日志目录: " + logDir.getAbsolutePath());
            }
        }
    }
    
    /**
     * 获取异常堆栈跟踪字符串
     * @param throwable 异常对象
     * @return 堆栈跟踪字符串
     */
    private String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }
    
    /**
     * 关闭所有PrintWriter
     */
    private void closeAllWriters() {
        lock.writeLock().lock();
        try {
            for (PrintWriter writer : writerCache.values()) {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            }
            writerCache.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 设置日志级别
     * @param level 新的日志级别
     */
    public void setLogLevel(LogLevel level) {
        if (level != null) {
            this.currentLogLevel = level;
        }
    }
    
    /**
     * 获取当前日志级别
     * @return 当前日志级别
     */
    public LogLevel getLogLevel() {
        return currentLogLevel;
    }
    
    /**
     * 设置是否启用控制台输出
     * @param enabled 是否启用
     */
    public void setConsoleEnabled(boolean enabled) {
        this.consoleEnabled = enabled;
    }
    
    /**
     * 检查控制台输出是否启用
     * @return 如果启用返回true
     */
    public boolean isConsoleEnabled() {
        return consoleEnabled;
    }
    
    /**
     * 刷新所有日志写入器
     */
    public void flushAll() {
        lock.readLock().lock();
        try {
            for (PrintWriter writer : writerCache.values()) {
                if (writer != null) {
                    writer.flush();
                }
            }
        } finally {
            lock.readLock().unlock();
        }
    }
    
    // ============== 兼容性方法（用于与旧代码兼容） ==============
    
    /**
     * 兼容旧代码的零售商日志记录器
     * @deprecated 请使用 SmartGridLogger.getInstance().info(LogType.RETAILER, message)
     */
    @Deprecated
    public static class RetailerLogger {
        private static PrintWriter writer;
        
        public static synchronized PrintWriter getWritelogtofile() {
            if (writer == null) {
                SmartGridLogger logger = SmartGridLogger.getInstance();
                writer = logger.getOrCreateWriter(LogType.RETAILER);
            }
            return new PrintWriter(System.out) {
                @Override
                public void println(String message) {
                    SmartGridLogger.getInstance().info(LogType.RETAILER, message);
                }
                
                @Override
                public void print(String message) {
                    SmartGridLogger.getInstance().info(LogType.RETAILER, message);
                }
            };
        }
    }
    
    /**
     * 兼容旧代码的用户日志记录器
     * @deprecated 请使用 SmartGridLogger.getInstance().info(LogType.USER, message)
     */
    @Deprecated
    public static class UserLogger {
        private static PrintWriter writer;
        
        public static synchronized PrintWriter getWritelogtofile() {
            if (writer == null) {
                SmartGridLogger logger = SmartGridLogger.getInstance();
                writer = logger.getOrCreateWriter(LogType.USER);
            }
            return new PrintWriter(System.out) {
                @Override
                public void println(String message) {
                    SmartGridLogger.getInstance().info(LogType.USER, message);
                }
                
                @Override
                public void print(String message) {
                    SmartGridLogger.getInstance().info(LogType.USER, message);
                }
            };
        }
    }
}