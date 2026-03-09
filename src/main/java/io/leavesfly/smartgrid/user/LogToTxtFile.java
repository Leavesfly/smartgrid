package io.leavesfly.smartgrid.user;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * 用户日志文件写入工具类（单例模式）。
 * 所有用户线程的运行状态、电价接收及用电响应信息均通过此类记录到指定日志文件中。
 *
 * 使用方式：
 *   LogToTxtFile.getWritelogtofile().println("日志内容");
 *   LogToTxtFile.getWritelogtofile().flush();
 *
 * @see UsersArgs#usersLogFile 日志文件路径配置
 */
public class LogToTxtFile {
	
	/** 文件输出流 */
	private static FileOutputStream fileOut;

	/** 共享的日志写入器实例 */
	private final static PrintWriter writeLogToFile;
	
	/** 类加载时初始化日志文件输出流及 PrintWriter 实例。 */
	static {
		try {
			fileOut = new FileOutputStream(new File(UsersArgs.usersLogFile));
		} catch (FileNotFoundException e) {
			System.err.println("用户日志文件创建失败: " + UsersArgs.usersLogFile);
			e.printStackTrace();
		}
		writeLogToFile = new PrintWriter(fileOut);
	}
	
	/**
	 * 获取线程安全的日志写入器。写入后需调用 flush() 确保内容及时落盘。
	 *
	 * @return 共享的 PrintWriter 实例
	 */
	public synchronized static PrintWriter getWritelogtofile() {
		return writeLogToFile;
	}

	/**
	 * 测试日志写入功能是否正常。
	 *
	 * @param args 命令行参数（未使用）
	 */
	public static void main(String[] args) {
		LogToTxtFile.writeLogToFile.println("hello");
		LogToTxtFile.writeLogToFile.flush();
	}
	
}
