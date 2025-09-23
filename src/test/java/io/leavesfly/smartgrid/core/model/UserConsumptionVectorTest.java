package io.leavesfly.smartgrid.core.model;

import io.leavesfly.smartgrid.core.config.SmartGridConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

/**
 * UserConsumptionVector 单元测试类
 * 测试用户用电消耗向量类的各项功能和数据完整性
 * 
 * @author SmartGrid Team
 * @version 2.0
 */
@DisplayName("UserConsumptionVector 用户消耗向量测试")
class UserConsumptionVectorTest {

    private UserConsumptionVector consumptionVector;
    private int[] testConsumptions;
    private static final int TEST_USER_ID = 0;

    @BeforeEach
    void setUp() {
        testConsumptions = new int[]{2, 3, 1, 2};
        consumptionVector = new UserConsumptionVector(TEST_USER_ID, testConsumptions);
    }

    @Test
    @DisplayName("测试带消耗数组的构造函数")
    void testConstructorWithConsumptions() {
        assertThat(consumptionVector.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(consumptionVector.getConsumptionsCopy()).containsExactly(testConsumptions);
        assertThat(consumptionVector.getTotalConsumption()).isEqualTo(8);
        assertThat(consumptionVector.isValid()).isTrue();
    }

    @Test
    @DisplayName("测试零初始化构造函数")
    void testConstructorWithZeroInitialization() {
        UserConsumptionVector zeroVector = new UserConsumptionVector(TEST_USER_ID);
        
        assertThat(zeroVector.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(zeroVector.getTotalConsumption()).isZero();
        assertThat(zeroVector.isValid()).isTrue();
        
        for (int i = 0; i < SmartGridConfig.TIME_SLOTS; i++) {
            assertThat(zeroVector.getConsumptionByTimeSlot(i)).isZero();
        }
    }

    @Test
    @DisplayName("测试复制构造函数")
    void testCopyConstructor() {
        UserConsumptionVector copiedVector = new UserConsumptionVector(consumptionVector);
        
        assertThat(copiedVector.getUserId()).isEqualTo(consumptionVector.getUserId());
        assertThat(copiedVector.getConsumptionsCopy()).containsExactly(testConsumptions);
        assertThat(copiedVector.getTotalConsumption()).isEqualTo(consumptionVector.getTotalConsumption());
        
        // 确保是深拷贝
        copiedVector.setConsumptionByTimeSlot(0, 5);
        assertThat(consumptionVector.getConsumptionByTimeSlot(0)).isEqualTo(2);
    }

    @Test
    @DisplayName("测试null参数的复制构造函数")
    void testCopyConstructorWithNull() {
        assertThatThrownBy(() -> new UserConsumptionVector((UserConsumptionVector) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("复制源不能为null");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 2, 3, 10})
    @DisplayName("测试无效用户ID的构造函数")
    void testConstructorWithInvalidUserId(int invalidUserId) {
        assertThatThrownBy(() -> new UserConsumptionVector(invalidUserId, testConsumptions))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("用户ID超出范围");
    }

    @Test
    @DisplayName("测试null消耗数组的构造函数")
    void testConstructorWithNullConsumptions() {
        assertThatThrownBy(() -> new UserConsumptionVector(TEST_USER_ID, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("消耗数组不能为null");
    }

    @Test
    @DisplayName("测试无效长度消耗数组的构造函数")
    void testConstructorWithInvalidLengthConsumptions() {
        int[] invalidArray = new int[]{1, 2};  // 长度不正确
        
        assertThatThrownBy(() -> new UserConsumptionVector(TEST_USER_ID, invalidArray))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("消耗数组长度必须为 " + SmartGridConfig.TIME_SLOTS);
    }

    @Test
    @DisplayName("测试负数消耗值的构造函数")
    void testConstructorWithNegativeConsumptions() {
        int[] negativeArray = new int[]{1, -2, 3, 4};
        
        assertThatThrownBy(() -> new UserConsumptionVector(TEST_USER_ID, negativeArray))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("用电消耗不能为负数: -2");
    }

    @Test
    @DisplayName("测试根据时间段获取消耗")
    void testGetConsumptionByTimeSlot() {
        for (int i = 0; i < testConsumptions.length; i++) {
            assertThat(consumptionVector.getConsumptionByTimeSlot(i))
                .isEqualTo(testConsumptions[i]);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 4, 5, 10})
    @DisplayName("测试无效时间段获取消耗")
    void testGetConsumptionByInvalidTimeSlot(int invalidTimeSlot) {
        assertThatThrownBy(() -> consumptionVector.getConsumptionByTimeSlot(invalidTimeSlot))
            .isInstanceOf(IndexOutOfBoundsException.class)
            .hasMessageContaining("时间段索引超出范围");
    }

    @Test
    @DisplayName("测试根据时间段设置消耗")
    void testSetConsumptionByTimeSlot() {
        int newConsumption = 5;
        consumptionVector.setConsumptionByTimeSlot(0, newConsumption);
        
        assertThat(consumptionVector.getConsumptionByTimeSlot(0)).isEqualTo(newConsumption);
        assertThat(consumptionVector.getTotalConsumption()).isEqualTo(11); // 原来8，现在2->5，增加3
    }

    @Test
    @DisplayName("测试设置负数消耗")
    void testSetNegativeConsumption() {
        assertThatThrownBy(() -> consumptionVector.setConsumptionByTimeSlot(0, -1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("用电消耗不能为负数: -1");
    }

    @Test
    @DisplayName("测试获取消耗副本")
    void testGetConsumptionsCopy() {
        int[] consumptionsCopy = consumptionVector.getConsumptionsCopy();
        
        assertThat(consumptionsCopy).containsExactly(testConsumptions);
        
        // 修改副本不应影响原始数据
        consumptionsCopy[0] = 10;
        assertThat(consumptionVector.getConsumptionByTimeSlot(0)).isEqualTo(2);
    }

    @Test
    @DisplayName("测试总消耗计算")
    void testGetTotalConsumption() {
        assertThat(consumptionVector.getTotalConsumption()).isEqualTo(8);
        
        // 设置新值后重新计算
        consumptionVector.setConsumptionByTimeSlot(0, 0);
        assertThat(consumptionVector.getTotalConsumption()).isEqualTo(6);
    }

    @Test
    @DisplayName("测试数据有效性验证")
    void testIsValid() {
        assertThat(consumptionVector.isValid()).isTrue();
        
        // 测试总消耗超过限制的情况
        UserConsumptionVector overLimitVector = new UserConsumptionVector(TEST_USER_ID);
        int userMaxConsumption = SmartGridConfig.getUserMaxConsumption(TEST_USER_ID);
        overLimitVector.setConsumptionByTimeSlot(0, userMaxConsumption + 1);
        
        assertThat(overLimitVector.isValid()).isFalse();
    }

    @Test
    @DisplayName("测试重置功能")
    void testReset() {
        consumptionVector.reset();
        
        assertThat(consumptionVector.getTotalConsumption()).isZero();
        for (int i = 0; i < SmartGridConfig.TIME_SLOTS; i++) {
            assertThat(consumptionVector.getConsumptionByTimeSlot(i)).isZero();
        }
    }

    @Test
    @DisplayName("测试设置所有消耗")
    void testSetAllConsumptions() {
        int[] newConsumptions = new int[]{1, 1, 1, 1};
        consumptionVector.setAllConsumptions(newConsumptions);
        
        assertThat(consumptionVector.getConsumptionsCopy()).containsExactly(newConsumptions);
        assertThat(consumptionVector.getTotalConsumption()).isEqualTo(4);
    }

    @Test
    @DisplayName("测试消耗比例计算")
    void testGetConsumptionRatio() {
        // 总消耗为8，第0个时间段消耗为2
        double ratio = consumptionVector.getConsumptionRatio(0);
        assertThat(ratio).isEqualTo(2.0 / 8.0);
        
        // 总消耗为0时的比例
        UserConsumptionVector zeroVector = new UserConsumptionVector(TEST_USER_ID);
        assertThat(zeroVector.getConsumptionRatio(0)).isZero();
    }

    @Test
    @DisplayName("测试是否超过消耗限制")
    void testIsOverConsumptionLimit() {
        assertThat(consumptionVector.isOverConsumptionLimit()).isFalse();
        
        // 设置超过限制的消耗
        int userMaxConsumption = SmartGridConfig.getUserMaxConsumption(TEST_USER_ID);
        consumptionVector.setConsumptionByTimeSlot(0, userMaxConsumption);
        
        assertThat(consumptionVector.isOverConsumptionLimit()).isTrue();
    }

    @Test
    @DisplayName("测试剩余容量计算")
    void testGetRemainingCapacity() {
        int userMaxConsumption = SmartGridConfig.getUserMaxConsumption(TEST_USER_ID);
        int expectedRemaining = userMaxConsumption - consumptionVector.getTotalConsumption();
        
        assertThat(consumptionVector.getRemainingCapacity()).isEqualTo(expectedRemaining);
        
        // 测试超过限制时的剩余容量
        consumptionVector.setConsumptionByTimeSlot(0, userMaxConsumption);
        assertThat(consumptionVector.getRemainingCapacity()).isZero();
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        String result = consumptionVector.toString();
        
        assertThat(result)
            .contains("UserConsumptionVector")
            .contains("userId=" + TEST_USER_ID)
            .contains("consumptions=(2, 3, 1, 2)")
            .contains("total=8");
    }

    @Test
    @DisplayName("测试equals方法")
    void testEquals() {
        UserConsumptionVector equalVector = new UserConsumptionVector(TEST_USER_ID, testConsumptions);
        UserConsumptionVector differentVector = new UserConsumptionVector(TEST_USER_ID, new int[]{1, 1, 1, 1});
        UserConsumptionVector differentUserVector = new UserConsumptionVector(1, testConsumptions);
        
        assertThat(consumptionVector).isEqualTo(equalVector);
        assertThat(consumptionVector).isNotEqualTo(differentVector);
        assertThat(consumptionVector).isNotEqualTo(differentUserVector);
        assertThat(consumptionVector).isNotEqualTo(null);
        assertThat(consumptionVector).isNotEqualTo("not a consumption vector");
    }

    @Test
    @DisplayName("测试hashCode方法")
    void testHashCode() {
        UserConsumptionVector equalVector = new UserConsumptionVector(TEST_USER_ID, testConsumptions);
        UserConsumptionVector differentVector = new UserConsumptionVector(TEST_USER_ID, new int[]{1, 1, 1, 1});
        
        assertThat(consumptionVector.hashCode()).isEqualTo(equalVector.hashCode());
        assertThat(consumptionVector.hashCode()).isNotEqualTo(differentVector.hashCode());
    }

    @Test
    @DisplayName("测试已弃用的方法兼容性")
    @SuppressWarnings("deprecation")
    void testDeprecatedMethods() {
        // 测试getConsumVector
        int[] directAccess = consumptionVector.getConsumVector();
        assertThat(directAccess).containsExactly(testConsumptions);
        
        // 测试setConsumVector
        int[] newConsumptions = new int[]{1, 2, 3, 4};
        consumptionVector.setConsumVector(newConsumptions);
        assertThat(consumptionVector.getConsumptionsCopy()).containsExactly(newConsumptions);
        
        // 测试getUserID
        assertThat(consumptionVector.getUserID()).isEqualTo(TEST_USER_ID);
        
        // 测试setUserID（应该抛出异常）
        assertThatThrownBy(() -> consumptionVector.setUserID(1))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("用户ID不允许修改");
        
        // 测试getTimesolts
        assertThat(UserConsumptionVector.getTimesolts()).isEqualTo(SmartGridConfig.TIME_SLOTS);
        
        // 测试toOldFormatString
        String oldFormat = consumptionVector.toOldFormatString();
        assertThat(oldFormat)
            .startsWith("oneUserConsumVector:(")
            .endsWith(")")
            .contains("1,  2,  3,  4");
    }

    @Test
    @DisplayName("测试边界值处理")
    void testBoundaryValues() {
        // 测试最大消耗限制边界
        int userMaxConsumption = SmartGridConfig.getUserMaxConsumption(TEST_USER_ID);
        int[] maxConsumptions = new int[SmartGridConfig.TIME_SLOTS];
        maxConsumptions[0] = userMaxConsumption;  // 全部消耗在第一个时间段
        
        UserConsumptionVector maxVector = new UserConsumptionVector(TEST_USER_ID, maxConsumptions);
        assertThat(maxVector.isValid()).isTrue();
        assertThat(maxVector.isOverConsumptionLimit()).isFalse();
        assertThat(maxVector.getRemainingCapacity()).isZero();
    }

    @Test
    @DisplayName("测试多用户场景")
    void testMultipleUsers() {
        for (int userId = 0; userId < SmartGridConfig.USER_COUNT; userId++) {
            UserConsumptionVector userVector = new UserConsumptionVector(userId, testConsumptions);
            
            assertThat(userVector.getUserId()).isEqualTo(userId);
            assertThat(userVector.isValid()).isTrue();
            
            int expectedMaxConsumption = SmartGridConfig.getUserMaxConsumption(userId);
            assertThat(userVector.getRemainingCapacity())
                .isEqualTo(expectedMaxConsumption - userVector.getTotalConsumption());
        }
    }

    @Test
    @DisplayName("测试数据修改后的一致性")
    void testDataConsistencyAfterModification() {
        int originalTotal = consumptionVector.getTotalConsumption();
        int originalValue = consumptionVector.getConsumptionByTimeSlot(0);
        
        // 修改第一个时间段的消耗
        int newValue = originalValue + 3;
        consumptionVector.setConsumptionByTimeSlot(0, newValue);
        
        // 验证总消耗正确更新
        assertThat(consumptionVector.getTotalConsumption()).isEqualTo(originalTotal + 3);
        
        // 验证比例正确更新
        double newRatio = consumptionVector.getConsumptionRatio(0);
        double expectedRatio = (double) newValue / (originalTotal + 3);
        assertThat(newRatio).isEqualTo(expectedRatio);
    }

    @Test
    @DisplayName("测试线程安全性的基本要求")
    void testBasicThreadSafety() {
        // 测试多个线程同时读取不会发生异常
        Runnable readTask = () -> {
            for (int i = 0; i < 100; i++) {
                consumptionVector.getConsumptionByTimeSlot(i % SmartGridConfig.TIME_SLOTS);
                consumptionVector.getTotalConsumption();
                consumptionVector.getConsumptionsCopy();
                consumptionVector.isValid();
            }
        };
        
        Thread t1 = new Thread(readTask);
        Thread t2 = new Thread(readTask);
        
        assertThatCode(() -> {
            t1.start();
            t2.start();
            t1.join();
            t2.join();
        }).doesNotThrowAnyException();
    }
}