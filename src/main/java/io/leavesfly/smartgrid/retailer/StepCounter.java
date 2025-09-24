package io.leavesfly.smartgrid.retailer;

/**
 * 步骤计数器类
 * 用于在多线程环境中跟踪用户连接数量，确保所有用户都已连接后再开始算法执行
 * 主要用于同步SAPC算法与用户连接的时序
 * 
 * @author SmartGrid Team
 * @version 1.0
 */
public class StepCounter {
    
    /** 当前步骤计数 */
    private int stepCount = 0;
    
    /**
     * 获取当前步骤计数
     * 
     * @return 当前步骤计数值
     */
    public int getStepCount() {
        return stepCount;
    }
    
    /**
     * 设置步骤计数值
     * 
     * @param stepCount 要设置的步骤计数值
     */
    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }
    
    /**
     * 增加步骤计数
     * 当有新用户连接时调用此方法
     */
    public void incrementStep() {
        stepCount++;
    }
    
    /**
     * 重置步骤计数为0
     * 用于重新开始计数
     */
    public void resetStepCount() {
        stepCount = 0;
    }
}
