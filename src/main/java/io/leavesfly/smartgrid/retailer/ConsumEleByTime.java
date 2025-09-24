package io.leavesfly.smartgrid.retailer;

/**
 * 电力按时间消耗类
 * 表示系统在不同时间段的电力消耗量，用于计算价格优化和利润分析
 * 
 * @author SmartGrid Team
 * @version 1.0
 */
public class ElectricityConsumptionByTime {
    
    /** 按时间段分组的电力消耗向量 */
    private int[] consumptionByTimeVector;
    
    /** 标识数据是否已完整填充 */
    private boolean isDataComplete;
    
    /**
     * 默认构造函数
     * 初始化一个空的消耗向量，长度为系统配置的时间段数
     */
    public ElectricityConsumptionByTime() {
        this.consumptionByTimeVector = new int[PriceVector.getTimeSlots()];
        this.isDataComplete = false;
    }
    
    /**
     * 带参数的构造函数
     * 
     * @param consumptionByTimeVector 按时间段分组的消耗向量
     */
    public ElectricityConsumptionByTime(int[] consumptionByTimeVector) {
        this.consumptionByTimeVector = consumptionByTimeVector;
        this.isDataComplete = false;
    }
    
    /**
     * 获取按时间段分组的消耗向量
     * 
     * @return 消耗向量数组
     */
    public int[] getConsumptionByTimeVector() {
        return consumptionByTimeVector;
    }
    
    /**
     * 设置按时间段分组的消耗向量
     * 
     * @param consumptionByTimeVector 要设置的消耗向量数组
     */
    public void setConsumptionByTimeVector(int[] consumptionByTimeVector) {
        this.consumptionByTimeVector = consumptionByTimeVector;
    }
    
    /**
     * 检查数据是否已完整填充
     * 
     * @return true 如果数据完整，否则返回 false
     */
    public boolean isDataComplete() {
        return isDataComplete;
    }
    
    /**
     * 设置数据完整性标识
     * 
     * @param isDataComplete 数据完整性标识
     */
    public void setDataComplete(boolean isDataComplete) {
        this.isDataComplete = isDataComplete;
    }
    
    /**
     * 将新的消耗数据复制到当前消耗数据中
     * 这个静态方法用于在SAPC算法中更新消耗数据
     * 
     * @param currentConsumption 当前消耗数据对象
     * @param newConsumption 新的消耗数据对象
     */
    public static void copyConsumptionData(ElectricityConsumptionByTime currentConsumption, 
                                          ElectricityConsumptionByTime newConsumption) {
        if (newConsumption.getConsumptionByTimeVector().length != 
            currentConsumption.getConsumptionByTimeVector().length) {
            // 向量长度不匹配，不执行复制操作
            return;
        }
        
        for (int i = 0; i < newConsumption.getConsumptionByTimeVector().length; i++) {
            currentConsumption.getConsumptionByTimeVector()[i] = 
                newConsumption.getConsumptionByTimeVector()[i];
        }
    }
    
    /**
     * 返回消耗数据的字符串表示
     * 
     * @return 格式化的消耗数据字符串
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("(");
        
        for (int i = 0; i < RetailerConfigConstants.TIME_SLOTS; i++) {
            if (i == RetailerConfigConstants.TIME_SLOTS - 1) {
                stringBuilder.append(consumptionByTimeVector[i]);
            } else {
                stringBuilder.append(consumptionByTimeVector[i]).append(", ");
            }
        }
        
        stringBuilder.append(")");
        return stringBuilder.toString();
    }
}
