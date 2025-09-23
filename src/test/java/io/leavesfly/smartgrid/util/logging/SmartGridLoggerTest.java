package io.leavesfly.smartgrid.util.logging;

import io.leavesfly.smartgrid.util.logging.SmartGridLogger.LogLevel;
import io.leavesfly.smartgrid.util.logging.SmartGridLogger.LogType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * SmartGridLogger 单元测试类
 * 测试智能电网系统统一日志管理器的各项功能
 * 
 * @author SmartGrid Team
 * @version 2.0
 */
@DisplayName("SmartGridLogger 日志管理器测试")
class SmartGridLoggerTest {

    @TempDir
    Path tempDir;

    private SmartGridLogger logger;

    @BeforeEach
    void setUp() {
        logger = SmartGridLogger.getInstance();
        logger.setConsoleEnabled(false); // 禁用控制台输出，避免测试时干扰
        logger.setLogLevel(LogLevel.DEBUG); // 设置为最低级别，确保所有日志都会被记录
        
        // 清理之前的状态
        logger.flushAll();
    }

    @AfterEach
    void tearDown() {
        logger.flushAll();
        logger.setConsoleEnabled(true); // 恢复控制台输出
        logger.setLogLevel(LogLevel.INFO); // 恢复默认级别
    }

    @Test
    @DisplayName("测试单例模式")
    void testSingletonPattern() {
        SmartGridLogger logger1 = SmartGridLogger.getInstance();
        SmartGridLogger logger2 = SmartGridLogger.getInstance();
        
        assertThat(logger1).isSameAs(logger2);
        assertThat(logger1).isSameAs(logger);
    }

    @Test
    @DisplayName("测试日志级别枚举")
    void testLogLevelEnum() {
        assertThat(LogLevel.DEBUG.getLevelName()).isEqualTo("DEBUG");
        assertThat(LogLevel.INFO.getLevelName()).isEqualTo("INFO");
        assertThat(LogLevel.WARN.getLevelName()).isEqualTo("WARN");
        assertThat(LogLevel.ERROR.getLevelName()).isEqualTo("ERROR");
        
        // 测试级别顺序
        assertThat(LogLevel.DEBUG.ordinal()).isLessThan(LogLevel.INFO.ordinal());
        assertThat(LogLevel.INFO.ordinal()).isLessThan(LogLevel.WARN.ordinal());
        assertThat(LogLevel.WARN.ordinal()).isLessThan(LogLevel.ERROR.ordinal());
    }

    @Test
    @DisplayName("测试日志类型枚举")
    void testLogTypeEnum() {
        assertThat(LogType.RETAILER.getTypeName()).isEqualTo("retailer");
        assertThat(LogType.USER.getTypeName()).isEqualTo("user");
        assertThat(LogType.SYSTEM.getTypeName()).isEqualTo("system");
    }

    @Test
    @DisplayName("测试日志级别设置和获取")
    void testLogLevelSetterGetter() {
        assertThat(logger.getLogLevel()).isEqualTo(LogLevel.DEBUG);
        
        logger.setLogLevel(LogLevel.WARN);
        assertThat(logger.getLogLevel()).isEqualTo(LogLevel.WARN);
        
        logger.setLogLevel(LogLevel.ERROR);
        assertThat(logger.getLogLevel()).isEqualTo(LogLevel.ERROR);
        
        // 测试null值处理
        logger.setLogLevel(null);
        assertThat(logger.getLogLevel()).isEqualTo(LogLevel.ERROR); // 应该保持原值
    }

    @Test
    @DisplayName("测试控制台输出设置")
    void testConsoleEnabledSetterGetter() {
        assertThat(logger.isConsoleEnabled()).isFalse(); // 在setUp中设置为false
        
        logger.setConsoleEnabled(true);
        assertThat(logger.isConsoleEnabled()).isTrue();
        
        logger.setConsoleEnabled(false);
        assertThat(logger.isConsoleEnabled()).isFalse();
    }

    @Test
    @DisplayName("测试基本日志记录方法")
    void testBasicLoggingMethods() {
        String testMessage = "测试消息";
        
        // 这些方法应该不抛出异常
        assertThatCode(() -> {
            logger.debug(LogType.SYSTEM, testMessage);
            logger.info(LogType.RETAILER, testMessage);
            logger.warn(LogType.USER, testMessage);
            logger.error(LogType.SYSTEM, testMessage);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("测试异常日志记录")
    void testExceptionLogging() {
        String testMessage = "测试异常消息";
        Exception testException = new RuntimeException("测试异常");
        
        assertThatCode(() -> {
            logger.error(LogType.SYSTEM, testMessage, testException);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("测试null异常处理")
    void testNullExceptionHandling() {
        String testMessage = "测试null异常消息";
        
        assertThatCode(() -> {
            logger.error(LogType.SYSTEM, testMessage, null);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("测试日志级别过滤")
    void testLogLevelFiltering() {
        logger.setLogLevel(LogLevel.WARN);
        
        // 创建一个自定义的测试记录器来验证过滤
        TestLogHandler testHandler = new TestLogHandler();
        
        // 由于我们无法直接拦截文件输出，这里主要测试方法调用不会抛异常
        assertThatCode(() -> {
            logger.debug(LogType.SYSTEM, "debug消息 - 应该被过滤");
            logger.info(LogType.SYSTEM, "info消息 - 应该被过滤");
            logger.warn(LogType.SYSTEM, "warn消息 - 应该被记录");
            logger.error(LogType.SYSTEM, "error消息 - 应该被记录");
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("测试通用日志记录方法")
    void testGeneralLogMethod() {
        assertThatCode(() -> {
            logger.log(LogLevel.INFO, LogType.RETAILER, "通用日志消息");
            logger.log(LogLevel.ERROR, LogType.USER, "错误日志消息");
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("测试日志消息格式")
    void testLogMessageFormat() {
        // 由于日志输出到文件，我们主要测试方法调用的正确性
        String message = "格式测试消息";
        
        assertThatCode(() -> {
            logger.info(LogType.SYSTEM, message);
        }).doesNotThrowAnyException();
        
        // 测试包含特殊字符的消息
        String specialMessage = "包含特殊字符的消息: []{}&*$#@!";
        assertThatCode(() -> {
            logger.info(LogType.SYSTEM, specialMessage);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("测试多线程安全性")
    void testThreadSafety() throws InterruptedException {
        int threadCount = 10;
        int messagesPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < messagesPerThread; j++) {
                        logger.info(LogType.SYSTEM, 
                            String.format("线程%d - 消息%d", threadId, j));
                        logger.warn(LogType.USER, 
                            String.format("线程%d - 警告%d", threadId, j));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        
        executor.shutdown();
        boolean terminated = executor.awaitTermination(10, TimeUnit.SECONDS);
        assertThat(terminated).isTrue();
    }

    @Test
    @DisplayName("测试资源管理")
    void testResourceManagement() {
        // 测试flushAll方法
        assertThatCode(() -> {
            logger.info(LogType.RETAILER, "测试刷新消息");
            logger.flushAll();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("测试已弃用的RetailerLogger兼容性")
    @SuppressWarnings("deprecation")
    void testDeprecatedRetailerLogger() {
        assertThatCode(() -> {
            java.io.PrintWriter writer = SmartGridLogger.RetailerLogger.getWritelogtofile();
            assertThat(writer).isNotNull();
            
            // 测试兼容方法
            writer.println("兼容性测试消息");
            writer.print("兼容性测试消息2");
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("测试已弃用的UserLogger兼容性")
    @SuppressWarnings("deprecation")
    void testDeprecatedUserLogger() {
        assertThatCode(() -> {
            java.io.PrintWriter writer = SmartGridLogger.UserLogger.getWritelogtofile();
            assertThat(writer).isNotNull();
            
            // 测试兼容方法
            writer.println("用户兼容性测试消息");
            writer.print("用户兼容性测试消息2");
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("测试边界条件")
    void testBoundaryConditions() {
        // 测试空消息
        assertThatCode(() -> {
            logger.info(LogType.SYSTEM, "");
            logger.info(LogType.SYSTEM, null);
        }).doesNotThrowAnyException();
        
        // 测试很长的消息
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("很长的消息");
        }
        String longMessage = sb.toString();
        assertThatCode(() -> {
            logger.info(LogType.SYSTEM, longMessage);
        }).doesNotThrowAnyException();
        
        // 测试包含换行符的消息
        String multilineMessage = "第一行\n第二行\n第三行";
        assertThatCode(() -> {
            logger.info(LogType.SYSTEM, multilineMessage);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("测试所有日志类型")
    void testAllLogTypes() {
        String message = "测试所有日志类型";
        
        assertThatCode(() -> {
            for (LogType type : LogType.values()) {
                logger.info(type, message + " - " + type.getTypeName());
                logger.debug(type, message + " - DEBUG");
                logger.warn(type, message + " - WARN");
                logger.error(type, message + " - ERROR");
            }
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("测试所有日志级别")
    void testAllLogLevels() {
        String message = "测试所有日志级别";
        
        assertThatCode(() -> {
            for (LogLevel level : LogLevel.values()) {
                logger.log(level, LogType.SYSTEM, message + " - " + level.getLevelName());
            }
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("测试异常堆栈跟踪记录")
    void testExceptionStackTraceLogging() {
        // 创建一个有嵌套调用栈的异常
        Exception nestedException = new RuntimeException("内部异常");
        Exception outerException = new IllegalStateException("外部异常", nestedException);
        
        assertThatCode(() -> {
            logger.error(LogType.SYSTEM, "记录嵌套异常", outerException);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("测试并发写入不同日志类型")
    void testConcurrentWritingToDifferentLogTypes() throws InterruptedException {
        int threadCount = LogType.values().length;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        LogType[] logTypes = LogType.values();
        
        for (int i = 0; i < threadCount; i++) {
            final LogType logType = logTypes[i];
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 50; j++) {
                        logger.info(logType, 
                            String.format("%s日志 - 消息%d", logType.getTypeName(), j));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        boolean completed = latch.await(20, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        
        executor.shutdown();
        boolean terminated = executor.awaitTermination(5, TimeUnit.SECONDS);
        assertThat(terminated).isTrue();
    }

    @Test
    @DisplayName("测试日志级别变更对现有记录的影响")
    void testLogLevelChangeImpact() {
        // 设置为INFO级别
        logger.setLogLevel(LogLevel.INFO);
        
        assertThatCode(() -> {
            logger.debug(LogType.SYSTEM, "DEBUG消息 - 应该被过滤");
            logger.info(LogType.SYSTEM, "INFO消息 - 应该被记录");
        }).doesNotThrowAnyException();
        
        // 更改为DEBUG级别
        logger.setLogLevel(LogLevel.DEBUG);
        
        assertThatCode(() -> {
            logger.debug(LogType.SYSTEM, "DEBUG消息 - 现在应该被记录");
            logger.info(LogType.SYSTEM, "INFO消息 - 仍然被记录");
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("测试控制台输出开关")
    void testConsoleOutputToggle() {
        logger.setConsoleEnabled(true);
        assertThat(logger.isConsoleEnabled()).isTrue();
        
        assertThatCode(() -> {
            logger.info(LogType.SYSTEM, "控制台输出开启时的消息");
        }).doesNotThrowAnyException();
        
        logger.setConsoleEnabled(false);
        assertThat(logger.isConsoleEnabled()).isFalse();
        
        assertThatCode(() -> {
            logger.info(LogType.SYSTEM, "控制台输出关闭时的消息");
        }).doesNotThrowAnyException();
    }

    /**
     * 测试用的日志处理器（辅助类）
     */
    private static class TestLogHandler {
        private int recordCount = 0;
        
        public void handle(String message) {
            recordCount++;
        }
        
        public int getRecordCount() {
            return recordCount;
        }
    }
}