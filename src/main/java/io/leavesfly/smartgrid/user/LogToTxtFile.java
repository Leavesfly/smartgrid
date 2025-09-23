package io.leavesfly.smartgrid.user;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * 用户日志文件写入工具类
 * 
 * <p>该类采用单例模式，提供线程安全的日志写入功能。
 * 所有用户线程的运行状态、电价接收情况、用电响应等信息都会通过此类记录到指定的日志文件中。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>初始化日志文件输出流</li>
 *   <li>提供线程安全的日志写入器</li>
 *   <li>支持多个用户线程并发写入日志</li>
 * </ul>
 * 
 * <p>使用方式：</p>
 * <pre>
 * LogToTxtFile.getWritelogtofile().println("日志内容");
 * LogToTxtFile.getWritelogtofile().flush();
 * </pre>
 * 
 * @author SmartGrid System
 * @version 1.0
 * @see UsersArgs#usersLogFile 日志文件路径配置
 */
public class LogToTxtFile {
	
	/** 文件输出流，用于写入日志文件 */
	private static FileOutputStream fileOut;
	
	/** 日志写入器，线程安全的PrintWriter实例 */
	private final static PrintWriter writeLogToFile;
	
	/**
	 * 静态初始化块
	 * 
	 * <p>在类加载时自动执行，完成以下初始化工作：</p>
	 * <ol>
	 *   <li>根据UsersArgs.usersLogFile配置创建文件输出流</li>
	 *   <li>基于文件输出流创建PrintWriter实例</li>
	 *   <li>处理文件创建可能出现的异常</li>
	 * </ol>
	 */
	static {
		try {
			// 根据配置文件路径创建日志文件输出流
			fileOut = new FileOutputStream(new File(UsersArgs.usersLogFile));
			
		} catch (FileNotFoundException e) {
			// 日志文件创建失败时打印异常信息
			System.err.println("用户日志文件创建失败: " + UsersArgs.usersLogFile);
			e.printStackTrace();
		}
		// 创建基于文件输出流的PrintWriter实例
		writeLogToFile = new PrintWriter(fileOut);
	}
	
	/**
	 * 获取日志写入器实例
	 * 
	 * <p>该方法使用synchronized关键字保证线程安全，
	 * 多个用户线程可以安全地并发调用此方法获取日志写入器。</p>
	 * 
	 * <p>注意事项：</p>
	 * <ul>
	 *   <li>写入日志后需要调用flush()方法确保内容及时写入文件</li>
	 *   <li>所有用户线程共享同一个PrintWriter实例</li>
	 *   <li>该方法是线程安全的，可以在多线程环境中安全使用</li>
	 * </ul>
	 * 
	 * @return PrintWriter 日志写入器实例
	 */
	public synchronized static PrintWriter getWritelogtofile() {
		return writeLogToFile;
	}

	/**
	 * 测试方法
	 * 
	 * <p>用于验证日志写入功能是否正常工作。
	 * 在实际部署中，此方法主要用于调试和测试目的。</p>
	 * 
	 * @param args 命令行参数（未使用）
	 */
	public static void main(String[] args) {
		// 写入测试日志
		LogToTxtFile.writeLogToFile.println("hello");
		// 刷新缓冲区，确保内容写入文件
		LogToTxtFile.writeLogToFile.flush();
	}
	
}
