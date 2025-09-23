package io.leavesfly.smartgrid.core.model;

import io.leavesfly.smartgrid.core.config.SmartGridConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * SystemConsumptionAggregate 单元测试类
 * 测试系统总用电消耗聚合类的各项功能和数据完整性
 * 
 * @author SmartGrid Team
 * @version 2.0
 */
@DisplayName("SystemConsumptionAggregate 系统消耗聚合测试")
class SystemConsumptionAggregateTest {

    private SystemConsumptionAggregate aggregate;
    private int[] testConsumptions;

    @BeforeEach
    void setUp() {
        testConsumptions = new int[]{10, 15, 8, 12};
        aggregate = new SystemConsumptionAggregate(testConsumptions);
    }

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        SystemConsumptionAggregate defaultAggregate = new SystemConsumptionAggregate();
        
        assertThat(defaultAggregate.calculateTotalSystemConsumption()).isZero();
        assertThat(defaultAggregate.isDataComplete()).isFalse();
        
        for (int i = 0; i < SmartGridConfig.TIME_SLOTS; i++) {
            assertThat(defaultAggregate.getConsumptionByTimeSlot(i)).isZero();
        }
    }

    @Test
    @DisplayName("测试数组构造函数")
    void testArrayConstructor() {
        assertThat(aggregate.getTotalConsumptionsCopy()).containsExactly(testConsumptions);
        assertThat(aggregate.calculateTotalSystemConsumption()).isEqualTo(45);
        assertThat(aggregate.isDataComplete()).isTrue();
    }

    @Test
    @DisplayName("测试复制构造函数")
    void testCopyConstructor() {
        SystemConsumptionAggregate copiedAggregate = new SystemConsumptionAggregate(aggregate);
        
        assertThat(copiedAggregate.getTotalConsumptionsCopy()).containsExactly(testConsumptions);
        assertThat(copiedAggregate.isDataComplete()).isEqualTo(aggregate.isDataComplete());
        
        // 确保是深拷贝
        copiedAggregate.setConsumptionByTimeSlot(0, 20);
        assertThat(aggregate.getConsumptionByTimeSlot(0)).isEqualTo(10);
    }

    @Test
    @DisplayName("测试null参数的构造函数异常")
    void testConstructorWithNullParameters() {
        assertThatThrownBy(() -> new SystemConsumptionAggregate((SystemConsumptionAggregate) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("复制源不能为null");
        
        assertThatThrownBy(() -> new SystemConsumptionAggregate((int[]) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("消耗数组不能为null");
    }

    @Test
    @DisplayName("测试无效长度的消耗数组")
    void testInvalidArrayLength() {
        int[] invalidArray = new int[]{10, 15};  // 长度不正确
        
        assertThatThrownBy(() -> new SystemConsumptionAggregate(invalidArray))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("消耗数组长度必须为 " + SmartGridConfig.TIME_SLOTS);
    }

    @Test
    @DisplayName("测试负数消耗值的数组")
    void testNegativeConsumptionValues() {
        int[] negativeArray = new int[]{10, -5, 8, 12};
        
        assertThatThrownBy(() -> new SystemConsumptionAggregate(negativeArray))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("用电消耗不能为负数: -5");
    }

    @Test
    @DisplayName("测试根据时间段获取消耗")
    void testGetConsumptionByTimeSlot() {
        for (int i = 0; i < testConsumptions.length; i++) {
            assertThat(aggregate.getConsumptionByTimeSlot(i)).isEqualTo(testConsumptions[i]);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 4, 5, 10})
    @DisplayName("测试无效时间段获取消耗")
    void testGetConsumptionByInvalidTimeSlot(int invalidTimeSlot) {
        assertThatThrownBy(() -> aggregate.getConsumptionByTimeSlot(invalidTimeSlot))
            .isInstanceOf(IndexOutOfBoundsException.class)
            .hasMessageContaining("时间段索引超出范围");
    }

    @Test
    @DisplayName("测试根据时间段设置消耗")
    void testSetConsumptionByTimeSlot() {
        int newConsumption = 25;
        aggregate.setConsumptionByTimeSlot(0, newConsumption);
        
        assertThat(aggregate.getConsumptionByTimeSlot(0)).isEqualTo(newConsumption);
        assertThat(aggregate.calculateTotalSystemConsumption()).isEqualTo(60); // 原来45，现在10->25，增加15
    }

    @Test
    @DisplayName("测试设置负数消耗")
    void testSetNegativeConsumption() {
        assertThatThrownBy(() -> aggregate.setConsumptionByTimeSlot(0, -1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("用电消耗不能为负数: -1");
    }

    @Test
    @DisplayName("测试获取总消耗副本")
    void testGetTotalConsumptionsCopy() {
        int[] consumptionsCopy = aggregate.getTotalConsumptionsCopy();
        
        assertThat(consumptionsCopy).containsExactly(testConsumptions);
        
        // 修改副本不应影响原始数据
        consumptionsCopy[0] = 100;
        assertThat(aggregate.getConsumptionByTimeSlot(0)).isEqualTo(10);
    }

    @Test
    @DisplayName("测试设置所有消耗")
    void testSetAllConsumptions() {
        int[] newConsumptions = new int[]{5, 5, 5, 5};
        aggregate.setAllConsumptions(newConsumptions);
        
        assertThat(aggregate.getTotalConsumptionsCopy()).containsExactly(newConsumptions);
        assertThat(aggregate.calculateTotalSystemConsumption()).isEqualTo(20);
        assertThat(aggregate.isDataComplete()).isTrue();
    }

    @Test
    @DisplayName("测试重置功能")
    void testReset() {
        aggregate.reset();
        
        assertThat(aggregate.calculateTotalSystemConsumption()).isZero();
        assertThat(aggregate.isDataComplete()).isFalse();
        
        for (int i = 0; i < SmartGridConfig.TIME_SLOTS; i++) {
            assertThat(aggregate.getConsumptionByTimeSlot(i)).isZero();
        }
    }

    @Test
    @DisplayName("测试聚合用户消耗数据")
    void testAggregateUserConsumptions() {
        List<ConsumptionVectorInterface> userConsumptions = new ArrayList<>();
        userConsumptions.add(new UserConsumptionVector(0, new int[]{2, 3, 1, 2}));
        userConsumptions.add(new UserConsumptionVector(1, new int[]{3, 2, 4, 1}));
        
        SystemConsumptionAggregate emptyAggregate = new SystemConsumptionAggregate();
        emptyAggregate.aggregateUserConsumptions(userConsumptions);
        
        assertThat(emptyAggregate.getTotalConsumptionsCopy()).containsExactly(5, 5, 5, 3);
        assertThat(emptyAggregate.calculateTotalSystemConsumption()).isEqualTo(18);
        assertThat(emptyAggregate.isDataComplete()).isTrue();
    }

    @Test
    @DisplayName("测试聚合null用户消耗列表")
    void testAggregateNullUserConsumptions() {
        assertThatThrownBy(() -> aggregate.aggregateUserConsumptions(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("用户消耗列表不能为null");
    }

    @Test
    @DisplayName("测试聚合错误大小的用户消耗列表")
    void testAggregateWrongSizeUserConsumptions() {
        List<ConsumptionVectorInterface> wrongSizeList = new ArrayList<>();
        wrongSizeList.add(new UserConsumptionVector(0, new int[]{2, 3, 1, 2}));
        // 只有一个用户，但期望两个
        
        assertThatThrownBy(() -> aggregate.aggregateUserConsumptions(wrongSizeList))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("用户消耗列表大小不匹配");
    }

    @Test
    @DisplayName("测试聚合包含null用户的消耗列表")
    void testAggregateUserConsumptionsWithNull() {
        List<ConsumptionVectorInterface> userConsumptions = new ArrayList<>();
        userConsumptions.add(new UserConsumptionVector(0, new int[]{2, 3, 1, 2}));
        userConsumptions.add(null);  // null用户
        
        assertThatThrownBy(() -> aggregate.aggregateUserConsumptions(userConsumptions))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("用户消耗向量不能为null");
    }

    @Test
    @DisplayName("测试计算系统总用电量")
    void testCalculateTotalSystemConsumption() {
        assertThat(aggregate.calculateTotalSystemConsumption()).isEqualTo(45);
        
        // 修改数据后重新计算
        aggregate.setConsumptionByTimeSlot(0, 0);
        assertThat(aggregate.calculateTotalSystemConsumption()).isEqualTo(35);
    }

    @Test
    @DisplayName("测试获取峰值用电时段")
    void testGetPeakConsumptionTimeSlot() {
        // testConsumptions = {10, 15, 8, 12}，峰值在索引1
        assertThat(aggregate.getPeakConsumptionTimeSlot()).isEqualTo(1);
        
        // 修改数据，峰值变为索引3
        aggregate.setConsumptionByTimeSlot(3, 20);
        assertThat(aggregate.getPeakConsumptionTimeSlot()).isEqualTo(3);
    }

    @Test
    @DisplayName("测试获取低谷用电时段")
    void testGetValleyConsumptionTimeSlot() {
        // testConsumptions = {10, 15, 8, 12}，低谷在索引2
        assertThat(aggregate.getValleyConsumptionTimeSlot()).isEqualTo(2);
        
        // 修改数据，低谷变为索引0
        aggregate.setConsumptionByTimeSlot(0, 5);
        assertThat(aggregate.getValleyConsumptionTimeSlot()).isEqualTo(0);
    }

    @Test
    @DisplayName("测试负载均衡度计算")
    void testCalculateLoadBalanceMetric() {
        // testConsumptions = {10, 15, 8, 12}
        // 平均值 = 45/4 = 11.25
        // 标准差计算：sqrt(((10-11.25)^2 + (15-11.25)^2 + (8-11.25)^2 + (12-11.25)^2) / 4)
        double expected = Math.sqrt((1.5625 + 14.0625 + 10.5625 + 0.5625) / 4);
        
        assertThat(aggregate.calculateLoadBalanceMetric()).isCloseTo(expected, offset(0.001));
        
        // 测试完全均匀的情况
        SystemConsumptionAggregate uniformAggregate = new SystemConsumptionAggregate(new int[]{10, 10, 10, 10});
        assertThat(uniformAggregate.calculateLoadBalanceMetric()).isZero();
    }

    @Test
    @DisplayName("测试消耗比例计算")
    void testGetConsumptionRatio() {
        // testConsumptions = {10, 15, 8, 12}，总计45
        assertThat(aggregate.getConsumptionRatio(0)).isCloseTo(10.0 / 45.0, offset(0.001));
        assertThat(aggregate.getConsumptionRatio(1)).isCloseTo(15.0 / 45.0, offset(0.001));
        assertThat(aggregate.getConsumptionRatio(2)).isCloseTo(8.0 / 45.0, offset(0.001));
        assertThat(aggregate.getConsumptionRatio(3)).isCloseTo(12.0 / 45.0, offset(0.001));
        
        // 测试总消耗为0的情况
        SystemConsumptionAggregate zeroAggregate = new SystemConsumptionAggregate();
        assertThat(zeroAggregate.getConsumptionRatio(0)).isZero();
    }

    @Test
    @DisplayName("测试数据完整性标志")
    void testDataCompleteFlag() {
        assertThat(aggregate.isDataComplete()).isTrue();
        
        aggregate.setDataComplete(false);
        assertThat(aggregate.isDataComplete()).isFalse();
        
        aggregate.setDataComplete(true);
        assertThat(aggregate.isDataComplete()).isTrue();
    }

    @Test
    @DisplayName("测试从其他对象复制")
    void testCopyFrom() {
        SystemConsumptionAggregate sourceAggregate = new SystemConsumptionAggregate(new int[]{1, 2, 3, 4});
        sourceAggregate.setDataComplete(false);
        
        aggregate.copyFrom(sourceAggregate);
        
        assertThat(aggregate.getTotalConsumptionsCopy()).containsExactly(1, 2, 3, 4);
        assertThat(aggregate.isDataComplete()).isFalse();
    }

    @Test
    @DisplayName("测试从null对象复制")
    void testCopyFromNull() {
        assertThatThrownBy(() -> aggregate.copyFrom(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("复制源不能为null");
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        String result = aggregate.toString();
        
        assertThat(result)
            .contains("SystemConsumptionAggregate")
            .contains("consumptions=(10, 15, 8, 12)")
            .contains("total=45")
            .contains("complete=true");
    }

    @Test
    @DisplayName("测试equals方法")
    void testEquals() {
        SystemConsumptionAggregate equalAggregate = new SystemConsumptionAggregate(testConsumptions);
        SystemConsumptionAggregate differentAggregate = new SystemConsumptionAggregate(new int[]{1, 2, 3, 4});
        SystemConsumptionAggregate differentFlagAggregate = new SystemConsumptionAggregate(testConsumptions);
        differentFlagAggregate.setDataComplete(false);
        
        assertThat(aggregate).isEqualTo(equalAggregate);
        assertThat(aggregate).isNotEqualTo(differentAggregate);
        assertThat(aggregate).isNotEqualTo(differentFlagAggregate);
        assertThat(aggregate).isNotEqualTo(null);
        assertThat(aggregate).isNotEqualTo("not an aggregate");
    }

    @Test
    @DisplayName("测试hashCode方法")
    void testHashCode() {
        SystemConsumptionAggregate equalAggregate = new SystemConsumptionAggregate(testConsumptions);
        SystemConsumptionAggregate differentAggregate = new SystemConsumptionAggregate(new int[]{1, 2, 3, 4});
        
        assertThat(aggregate.hashCode()).isEqualTo(equalAggregate.hashCode());
        assertThat(aggregate.hashCode()).isNotEqualTo(differentAggregate.hashCode());
    }

    @Test
    @DisplayName("测试已弃用的方法兼容性")
    @SuppressWarnings("deprecation")
    void testDeprecatedMethods() {
        // 测试getConsumByTimeVector
        int[] directAccess = aggregate.getConsumByTimeVector();
        assertThat(directAccess).containsExactly(testConsumptions);
        
        // 测试setConsumByTimeVector
        int[] newConsumptions = new int[]{1, 2, 3, 4};
        aggregate.setConsumByTimeVector(newConsumptions);
        assertThat(aggregate.getTotalConsumptionsCopy()).containsExactly(newConsumptions);
        
        // 测试isFull和setFull
        assertThat(aggregate.isFull()).isTrue();
        aggregate.setFull(false);
        assertThat(aggregate.isFull()).isFalse();
        
        // 测试静态复制方法
        SystemConsumptionAggregate targetAggregate = new SystemConsumptionAggregate();
        SystemConsumptionAggregate.consumByTimeNewToConsumByTimeNow(targetAggregate, aggregate);
        assertThat(targetAggregate.getTotalConsumptionsCopy()).containsExactly(newConsumptions);
        
        // 测试toOldFormatString
        String oldFormat = aggregate.toOldFormatString();
        assertThat(oldFormat)
            .startsWith("(")
            .endsWith(")")
            .contains("1, 2, 3, 4");
    }

    @Test
    @DisplayName("测试边界值处理")
    void testBoundaryValues() {
        // 测试全零数组
        SystemConsumptionAggregate zeroAggregate = new SystemConsumptionAggregate(new int[]{0, 0, 0, 0});
        assertThat(zeroAggregate.calculateTotalSystemConsumption()).isZero();
        assertThat(zeroAggregate.getPeakConsumptionTimeSlot()).isZero(); // 第一个元素
        assertThat(zeroAggregate.getValleyConsumptionTimeSlot()).isZero(); // 第一个元素
        assertThat(zeroAggregate.calculateLoadBalanceMetric()).isZero();
        
        // 测试单一峰值
        SystemConsumptionAggregate singlePeakAggregate = new SystemConsumptionAggregate(new int[]{0, 100, 0, 0});
        assertThat(singlePeakAggregate.getPeakConsumptionTimeSlot()).isEqualTo(1);
        assertThat(singlePeakAggregate.getValleyConsumptionTimeSlot()).isEqualTo(0); // 第一个零值
    }

    @Test
    @DisplayName("测试复杂聚合场景")
    void testComplexAggregationScenario() {
        // 创建多个用户的复杂消耗模式
        List<ConsumptionVectorInterface> userConsumptions = new ArrayList<>();
        
        // 用户0：早高峰用电
        userConsumptions.add(new UserConsumptionVector(0, new int[]{5, 2, 1, 2}));
        
        // 用户1：晚高峰用电
        userConsumptions.add(new UserConsumptionVector(1, new int[]{1, 2, 2, 3}));
        
        SystemConsumptionAggregate complexAggregate = new SystemConsumptionAggregate();
        complexAggregate.aggregateUserConsumptions(userConsumptions);
        
        // 验证聚合结果
        assertThat(complexAggregate.getTotalConsumptionsCopy()).containsExactly(6, 4, 3, 5);
        assertThat(complexAggregate.calculateTotalSystemConsumption()).isEqualTo(18);
        
        // 验证峰谷分析
        assertThat(complexAggregate.getPeakConsumptionTimeSlot()).isEqualTo(0); // 早高峰
        assertThat(complexAggregate.getValleyConsumptionTimeSlot()).isEqualTo(2); // 低谷
        
        // 验证负载均衡度
        double loadBalance = complexAggregate.calculateLoadBalanceMetric();
        assertThat(loadBalance).isPositive(); // 有负载不均衡
    }

    @Test
    @DisplayName("测试数据一致性维护")
    void testDataConsistencyMaintenance() {
        int originalTotal = aggregate.calculateTotalSystemConsumption();
        
        // 修改一个时间段的消耗
        int originalValue = aggregate.getConsumptionByTimeSlot(0);
        int newValue = originalValue + 10;
        aggregate.setConsumptionByTimeSlot(0, newValue);
        
        // 验证总量正确更新
        assertThat(aggregate.calculateTotalSystemConsumption()).isEqualTo(originalTotal + 10);
        
        // 验证比例正确更新
        double newRatio = aggregate.getConsumptionRatio(0);
        double expectedRatio = (double) newValue / (originalTotal + 10);
        assertThat(newRatio).isCloseTo(expectedRatio, offset(0.001));
        
        // 验证峰谷分析正确更新
        if (newValue > 15) { // 原来最大值是15
            assertThat(aggregate.getPeakConsumptionTimeSlot()).isEqualTo(0);
        }
    }
}