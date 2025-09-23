package io.leavesfly.smartgrid.core.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

/**
 * SmartGridConfig 单元测试类
 * 测试智能电网系统配置类的各项功能和参数验证
 * 
 * @author SmartGrid Team
 * @version 2.0
 */
@DisplayName("SmartGridConfig 配置类测试")
class SmartGridConfigTest {

    @Test
    @DisplayName("验证基本配置常量")
    void testBasicConfigConstants() {
        // 验证时间相关配置
        assertThat(SmartGridConfig.TIME_SLOTS).isEqualTo(4);
        
        // 验证网络配置
        assertThat(SmartGridConfig.SERVER_IP).isEqualTo("127.0.0.1");
        assertThat(SmartGridConfig.SERVER_PORT).isEqualTo(1234);
        
        // 验证用户配置
        assertThat(SmartGridConfig.USER_COUNT).isEqualTo(2);
        assertThat(SmartGridConfig.A_APPLIANCE_COUNT).isEqualTo(4);
        assertThat(SmartGridConfig.B_APPLIANCE_COUNT).isEqualTo(4);
        
        // 验证价格配置
        assertThat(SmartGridConfig.MIN_PRICE).isEqualTo(0.5f);
        assertThat(SmartGridConfig.MAX_PRICE).isEqualTo(1.5f);
    }

    @Test
    @DisplayName("验证算法参数配置")
    void testAlgorithmParameters() {
        assertThat(SmartGridConfig.INITIAL_TEMPERATURE).isEqualTo((float) Math.exp(-1));
        assertThat(SmartGridConfig.END_TEMPERATURE).isEqualTo((float) Math.exp(-5));
        assertThat(SmartGridConfig.CURRENT_ROUND).isEqualTo(1);
        
        // 验证利润计算参数
        assertThat(SmartGridConfig.PROFIT_COEFFICIENT_A).isEqualTo(0.005f);
        assertThat(SmartGridConfig.PROFIT_COEFFICIENT_B).isEqualTo(0.001f);
        assertThat(SmartGridConfig.WEIGHT_COEFFICIENT).isEqualTo(1);
    }

    @Test
    @DisplayName("验证日志配置")
    void testLogConfiguration() {
        assertThat(SmartGridConfig.RETAILER_LOG_FILE).isEqualTo("logs/retailer.log");
        assertThat(SmartGridConfig.USER_LOG_FILE).isEqualTo("logs/users.log");
    }

    @Test
    @DisplayName("验证配置数组结构")
    void testConfigArrayStructure() {
        // 验证用户最大消耗配置
        assertThat(SmartGridConfig.USER_MAX_CONSUMPTION)
            .hasSize(SmartGridConfig.USER_COUNT)
            .containsExactly(10, 12);
        
        // 验证A类电器配置结构
        assertThat(SmartGridConfig.A_APPLIANCE_CONSUMPTION.length)
            .isEqualTo(SmartGridConfig.USER_COUNT);
        
        for (int[] userConfig : SmartGridConfig.A_APPLIANCE_CONSUMPTION) {
            assertThat(userConfig).hasSize(SmartGridConfig.TIME_SLOTS);
        }
        
        // 验证B类电器最大消耗配置结构
        assertThat(SmartGridConfig.B_APPLIANCE_MAX_CONSUMPTION.length)
            .isEqualTo(SmartGridConfig.USER_COUNT);
        
        for (int[] userConfig : SmartGridConfig.B_APPLIANCE_MAX_CONSUMPTION) {
            assertThat(userConfig).hasSize(SmartGridConfig.B_APPLIANCE_COUNT);
        }
        
        // 验证满意度矩阵结构
        assertThat(SmartGridConfig.ALL_USERS_B_APPLIANCE_SATISFACTION.length).isEqualTo(SmartGridConfig.USER_COUNT);
    }

    @Test
    @DisplayName("验证配置参数有效性检查")
    void testConfigValidation() {
        // 默认配置应该是有效的
        assertThat(SmartGridConfig.validateConfig()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("测试有效用户ID的B类电器满意度获取")
    void testGetUserBApplianceSatisfactionWithValidId(int userId) {
        int[][] satisfaction = SmartGridConfig.getUserBApplianceSatisfaction(userId);
        
        assertThat(satisfaction.length)
            .isEqualTo(SmartGridConfig.B_APPLIANCE_COUNT);
        
        // 验证每个B类电器都有对应的时间槽数量的满意度值
        for (int[] applianceRow : satisfaction) {
            assertThat(applianceRow).hasSize(SmartGridConfig.TIME_SLOTS);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 2, 3, 10})
    @DisplayName("测试无效用户ID的B类电器满意度获取")
    void testGetUserBApplianceSatisfactionWithInvalidId(int invalidUserId) {
        assertThatThrownBy(() -> SmartGridConfig.getUserBApplianceSatisfaction(invalidUserId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("无效的用户ID: " + invalidUserId);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("测试有效用户ID的A类电器消耗获取")
    void testGetUserAApplianceConsumptionWithValidId(int userId) {
        int[] consumption = SmartGridConfig.getUserAApplianceConsumption(userId);
        
        assertThat(consumption)
            .isNotNull()
            .hasSize(SmartGridConfig.TIME_SLOTS);
        
        // 验证所有消耗值都是正数
        for (int value : consumption) {
            assertThat(value).isPositive();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 2, 3, 10})
    @DisplayName("测试无效用户ID的A类电器消耗获取")
    void testGetUserAApplianceConsumptionWithInvalidId(int invalidUserId) {
        assertThatThrownBy(() -> SmartGridConfig.getUserAApplianceConsumption(invalidUserId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("无效的用户ID: " + invalidUserId);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("测试有效用户ID的B类电器最大消耗获取")
    void testGetUserBApplianceMaxConsumptionWithValidId(int userId) {
        int[] maxConsumption = SmartGridConfig.getUserBApplianceMaxConsumption(userId);
        
        assertThat(maxConsumption)
            .isNotNull()
            .hasSize(SmartGridConfig.B_APPLIANCE_COUNT);
        
        // 验证所有最大消耗值都是非负数
        for (int value : maxConsumption) {
            assertThat(value).isNotNegative();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 2, 3, 10})
    @DisplayName("测试无效用户ID的B类电器最大消耗获取")
    void testGetUserBApplianceMaxConsumptionWithInvalidId(int invalidUserId) {
        assertThatThrownBy(() -> SmartGridConfig.getUserBApplianceMaxConsumption(invalidUserId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("无效的用户ID: " + invalidUserId);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("测试有效用户ID的最大消耗限制获取")
    void testGetUserMaxConsumptionWithValidId(int userId) {
        int maxConsumption = SmartGridConfig.getUserMaxConsumption(userId);
        
        assertThat(maxConsumption).isPositive();
        
        // 验证返回值与配置数组一致
        assertThat(maxConsumption).isEqualTo(SmartGridConfig.USER_MAX_CONSUMPTION[userId]);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 2, 3, 10})
    @DisplayName("测试无效用户ID的最大消耗限制获取")
    void testGetUserMaxConsumptionWithInvalidId(int invalidUserId) {
        assertThatThrownBy(() -> SmartGridConfig.getUserMaxConsumption(invalidUserId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("无效的用户ID: " + invalidUserId);
    }

    @Test
    @DisplayName("测试配置类不可实例化")
    void testConfigClassCannotBeInstantiated() {
        // 尝试使用反射创建实例应该抛出异常
        assertThatThrownBy(() -> {
            java.lang.reflect.Constructor<SmartGridConfig> constructor = 
                SmartGridConfig.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        }).hasCauseInstanceOf(AssertionError.class)
          .hasRootCauseMessage("配置类不允许实例化");
    }

    @Test
    @DisplayName("验证具体配置数据的正确性")
    void testSpecificConfigurationData() {
        // 验证用户0的A类电器消耗
        assertThat(SmartGridConfig.A_APPLIANCE_CONSUMPTION[0])
            .containsExactly(1, 2, 3, 1);
        
        // 验证用户1的A类电器消耗
        assertThat(SmartGridConfig.A_APPLIANCE_CONSUMPTION[1])
            .containsExactly(1, 3, 3, 1);
        
        // 验证用户0的B类电器最大消耗
        assertThat(SmartGridConfig.B_APPLIANCE_MAX_CONSUMPTION[0])
            .containsExactly(2, 3, 4, 4);
        
        // 验证用户1的B类电器最大消耗
        assertThat(SmartGridConfig.B_APPLIANCE_MAX_CONSUMPTION[1])
            .containsExactly(2, 3, 1, 3);
    }

    @Test
    @DisplayName("验证满意度矩阵的具体数据")
    void testSatisfactionMatrixData() {
        // 验证用户0的满意度矩阵
        int[][] user0Satisfaction = SmartGridConfig.getUserBApplianceSatisfaction(0);
        assertThat(user0Satisfaction[0]).containsExactly(2, 4, 5, 3);
        assertThat(user0Satisfaction[1]).containsExactly(1, 3, 6, 3);
        assertThat(user0Satisfaction[2]).containsExactly(2, 5, 3, 4);
        assertThat(user0Satisfaction[3]).containsExactly(4, 1, 4, 3);
        
        // 验证用户1的满意度矩阵
        int[][] user1Satisfaction = SmartGridConfig.getUserBApplianceSatisfaction(1);
        assertThat(user1Satisfaction[0]).containsExactly(2, 2, 5, 3);
        assertThat(user1Satisfaction[1]).containsExactly(1, 6, 1, 3);
        assertThat(user1Satisfaction[2]).containsExactly(2, 3, 5, 3);
        assertThat(user1Satisfaction[3]).containsExactly(2, 1, 2, 4);
    }

    @Test
    @DisplayName("验证价格范围的合理性")
    void testPriceRangeValidity() {
        assertThat(SmartGridConfig.MIN_PRICE)
            .isLessThan(SmartGridConfig.MAX_PRICE)
            .isPositive();
        
        assertThat(SmartGridConfig.MAX_PRICE)
            .isGreaterThan(SmartGridConfig.MIN_PRICE);
    }
}