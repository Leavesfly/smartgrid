package io.leavesfly.smartgrid.core.algorithm;

import io.leavesfly.smartgrid.core.config.SmartGridConfig;
import io.leavesfly.smartgrid.core.model.PriceVector;
import io.leavesfly.smartgrid.core.model.PriceVectorInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

/**
 * RetailerProfitCalculator 单元测试类
 * 测试零售商利润计算器的各项功能和计算准确性
 * 
 * @author SmartGrid Team
 * @version 2.0
 */
@DisplayName("RetailerProfitCalculator 利润计算器测试")
class RetailerProfitCalculatorTest {

    private RetailerProfitCalculator calculator;
    private PriceVectorInterface priceVector;
    private int[] totalConsumption;

    @BeforeEach
    void setUp() {
        calculator = new RetailerProfitCalculator();
        priceVector = new PriceVector(new float[]{0.8f, 1.0f, 1.2f, 0.6f});
        totalConsumption = new int[]{10, 15, 8, 12};
    }

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        assertThat(calculator.getCoefficientA()).isEqualTo(SmartGridConfig.PROFIT_COEFFICIENT_A);
        assertThat(calculator.getCoefficientB()).isEqualTo(SmartGridConfig.PROFIT_COEFFICIENT_B);
        assertThat(calculator.getWeightCoefficient()).isEqualTo(SmartGridConfig.WEIGHT_COEFFICIENT);
        assertThat(calculator.validateParameters()).isTrue();
    }

    @Test
    @DisplayName("测试自定义参数构造函数")
    void testCustomParametersConstructor() {
        float customA = 0.01f;
        float customB = 0.002f;
        int customWeight = 2;
        
        RetailerProfitCalculator customCalculator = 
            new RetailerProfitCalculator(customA, customB, customWeight);
        
        assertThat(customCalculator.getCoefficientA()).isEqualTo(customA);
        assertThat(customCalculator.getCoefficientB()).isEqualTo(customB);
        assertThat(customCalculator.getWeightCoefficient()).isEqualTo(customWeight);
        assertThat(customCalculator.validateParameters()).isTrue();
    }

    @Test
    @DisplayName("测试无效参数的构造函数")
    void testInvalidParametersConstructor() {
        assertThatThrownBy(() -> new RetailerProfitCalculator(-0.01f, 0.002f, 1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("系数a不能为负数: -0.01");
        
        assertThatThrownBy(() -> new RetailerProfitCalculator(0.01f, -0.002f, 1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("系数b不能为负数: -0.002");
        
        assertThatThrownBy(() -> new RetailerProfitCalculator(0.01f, 0.002f, -1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("权重系数不能为负数: -1");
    }

    @Test
    @DisplayName("测试利润计算")
    void testCalculateProfit() {
        // 手动计算期望值
        // 收益：10*0.8 + 15*1.0 + 8*1.2 + 12*0.6 = 8 + 15 + 9.6 + 7.2 = 39.8
        float expectedRevenue = 39.8f;
        
        // 成本：weight * Σ(a * consumption² + b * consumption³)
        // a = 0.005, b = 0.001, weight = 1
        // 10²*0.005 + 10³*0.001 = 0.5 + 1 = 1.5
        // 15²*0.005 + 15³*0.001 = 1.125 + 3.375 = 4.5
        // 8²*0.005 + 8³*0.001 = 0.32 + 0.512 = 0.832
        // 12²*0.005 + 12³*0.001 = 0.72 + 1.728 = 2.448
        float expectedCost = 1 * (1.5f + 4.5f + 0.832f + 2.448f);
        float expectedProfit = expectedRevenue - expectedCost;
        
        float actualProfit = calculator.calculateProfit(priceVector, totalConsumption);
        
        assertThat(actualProfit).isCloseTo(expectedProfit, offset(0.001f));
    }

    @Test
    @DisplayName("测试null价格向量的利润计算")
    void testCalculateProfitWithNullPriceVector() {
        assertThatThrownBy(() -> calculator.calculateProfit(null, totalConsumption))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("价格向量不能为null");
    }

    @Test
    @DisplayName("测试null总消耗的利润计算")
    void testCalculateProfitWithNullTotalConsumption() {
        assertThatThrownBy(() -> calculator.calculateProfit(priceVector, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("总消耗数组不能为null");
    }

    @Test
    @DisplayName("测试长度不匹配的总消耗数组")
    void testCalculateProfitWithMismatchedLength() {
        int[] invalidConsumption = new int[]{10, 15}; // 长度不匹配
        
        assertThatThrownBy(() -> calculator.calculateProfit(priceVector, invalidConsumption))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("总消耗数组长度必须与价格向量时间槽数量一致");
    }

    @Test
    @DisplayName("测试负数消耗的利润计算")
    void testCalculateProfitWithNegativeConsumption() {
        int[] negativeConsumption = new int[]{10, -15, 8, 12};
        
        assertThatThrownBy(() -> calculator.calculateProfit(priceVector, negativeConsumption))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("用电消耗不能为负数: -15");
    }

    @Test
    @DisplayName("测试收益计算")
    void testCalculateRevenue() {
        // 收益：10*0.8 + 15*1.0 + 8*1.2 + 12*0.6 = 39.8
        float expectedRevenue = 39.8f;
        float actualRevenue = calculator.calculateRevenue(priceVector, totalConsumption);
        
        assertThat(actualRevenue).isCloseTo(expectedRevenue, offset(0.001f));
    }

    @Test
    @DisplayName("测试成本计算")
    void testCalculateCost() {
        // 使用默认参数：a=0.005, b=0.001, weight=1
        // 成本计算见上面的手动计算
        float expectedCost = 1 * (1.5f + 4.5f + 0.832f + 2.448f);
        float actualCost = calculator.calculateCost(totalConsumption);
        
        assertThat(actualCost).isCloseTo(expectedCost, offset(0.001f));
    }

    @Test
    @DisplayName("测试null总消耗的成本计算")
    void testCalculateCostWithNull() {
        assertThatThrownBy(() -> calculator.calculateCost(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("总消耗数组不能为null");
    }

    @Test
    @DisplayName("测试无效长度的成本计算")
    void testCalculateCostWithInvalidLength() {
        int[] invalidArray = new int[]{10, 15}; // 长度不正确
        
        assertThatThrownBy(() -> calculator.calculateCost(invalidArray))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("总消耗数组长度必须为 " + SmartGridConfig.TIME_SLOTS);
    }

    @Test
    @DisplayName("测试设置参数")
    void testSetParameters() {
        float newA = 0.01f;
        float newB = 0.002f;
        int newWeight = 2;
        
        calculator.setParameters(newA, newB, newWeight);
        
        assertThat(calculator.getCoefficientA()).isEqualTo(newA);
        assertThat(calculator.getCoefficientB()).isEqualTo(newB);
        assertThat(calculator.getWeightCoefficient()).isEqualTo(newWeight);
    }

    @ParameterizedTest
    @ValueSource(floats = {-0.01f, -1.0f})
    @DisplayName("测试设置无效的系数a")
    void testSetInvalidCoefficientA(float invalidA) {
        assertThatThrownBy(() -> calculator.setParameters(invalidA, 0.001f, 1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("系数a不能为负数: " + invalidA);
    }

    @ParameterizedTest
    @ValueSource(floats = {-0.001f, -0.1f})
    @DisplayName("测试设置无效的系数b")
    void testSetInvalidCoefficientB(float invalidB) {
        assertThatThrownBy(() -> calculator.setParameters(0.005f, invalidB, 1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("系数b不能为负数: " + invalidB);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -10})
    @DisplayName("测试设置无效的权重系数")
    void testSetInvalidWeightCoefficient(int invalidWeight) {
        assertThatThrownBy(() -> calculator.setParameters(0.005f, 0.001f, invalidWeight))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("权重系数不能为负数: " + invalidWeight);
    }

    @Test
    @DisplayName("测试参数验证")
    void testValidateParameters() {
        assertThat(calculator.validateParameters()).isTrue();
        
        // 设置无效参数后应该失败，但由于setParameters会抛异常，这里测试边界情况
        calculator.setParameters(0.0f, 0.0f, 0);
        assertThat(calculator.validateParameters()).isTrue(); // 0是有效的
    }

    @Test
    @DisplayName("测试详细利润分析")
    void testGetDetailedProfitAnalysis() {
        RetailerProfitCalculator.ProfitAnalysis analysis = 
            calculator.getDetailedProfitAnalysis(priceVector, totalConsumption);
        
        assertThat(analysis).isNotNull();
        assertThat(analysis.getTotalRevenue()).isCloseTo(39.8f, offset(0.001f));
        assertThat(analysis.getTotalCost()).isPositive();
        assertThat(analysis.getTotalProfit()).isEqualTo(analysis.getTotalRevenue() - analysis.getTotalCost());
        
        // 验证分时段数据
        float[] revenueByTimeSlot = analysis.getRevenueByTimeSlot();
        assertThat(revenueByTimeSlot).hasSize(SmartGridConfig.TIME_SLOTS);
        assertThat(revenueByTimeSlot[0]).isCloseTo(8.0f, offset(0.001f)); // 10 * 0.8
        assertThat(revenueByTimeSlot[1]).isCloseTo(15.0f, offset(0.001f)); // 15 * 1.0
        
        float[] costByTimeSlot = analysis.getCostByTimeSlot();
        assertThat(costByTimeSlot).hasSize(SmartGridConfig.TIME_SLOTS);
        for (float cost : costByTimeSlot) {
            assertThat(cost).isNotNegative();
        }
    }

    @Test
    @DisplayName("测试利润分析类的功能")
    void testProfitAnalysisClass() {
        RetailerProfitCalculator.ProfitAnalysis analysis = 
            calculator.getDetailedProfitAnalysis(priceVector, totalConsumption);
        
        // 测试利润率计算
        float expectedMargin = analysis.getTotalProfit() / analysis.getTotalRevenue();
        assertThat(analysis.getProfitMargin()).isCloseTo(expectedMargin, offset(0.001f));
        
        // 测试最盈利时段
        int mostProfitableSlot = analysis.getMostProfitableTimeSlot();
        assertThat(mostProfitableSlot).isBetween(0, SmartGridConfig.TIME_SLOTS - 1);
        
        // 测试toString方法
        String analysisString = analysis.toString();
        assertThat(analysisString)
            .contains("ProfitAnalysis")
            .contains("利润=")
            .contains("收益=")
            .contains("成本=")
            .contains("利润率=");
    }

    @Test
    @DisplayName("测试零消耗情况")
    void testZeroConsumption() {
        int[] zeroConsumption = new int[]{0, 0, 0, 0};
        
        float profit = calculator.calculateProfit(priceVector, zeroConsumption);
        float revenue = calculator.calculateRevenue(priceVector, zeroConsumption);
        float cost = calculator.calculateCost(zeroConsumption);
        
        assertThat(revenue).isZero();
        assertThat(cost).isZero();
        assertThat(profit).isZero();
    }

    @Test
    @DisplayName("测试高消耗情况下的成本增长")
    void testHighConsumptionCostGrowth() {
        int[] lowConsumption = new int[]{1, 1, 1, 1};
        int[] highConsumption = new int[]{10, 10, 10, 10};
        
        float lowCost = calculator.calculateCost(lowConsumption);
        float highCost = calculator.calculateCost(highConsumption);
        
        // 由于包含三次项，成本应该非线性增长
        assertThat(highCost).isGreaterThan(lowCost * 100); // 10倍消耗，成本增长超过100倍
    }

    @Test
    @DisplayName("测试不同参数对利润的影响")
    void testParameterImpactOnProfit() {
        float baseProfit = calculator.calculateProfit(priceVector, totalConsumption);
        
        // 增大系数a，成本应该增加，利润减少
        RetailerProfitCalculator calculatorHighA = new RetailerProfitCalculator(0.01f, 0.001f, 1);
        float profitHighA = calculatorHighA.calculateProfit(priceVector, totalConsumption);
        assertThat(profitHighA).isLessThan(baseProfit);
        
        // 增大权重系数，成本应该增加，利润减少
        RetailerProfitCalculator calculatorHighWeight = new RetailerProfitCalculator(0.005f, 0.001f, 2);
        float profitHighWeight = calculatorHighWeight.calculateProfit(priceVector, totalConsumption);
        assertThat(profitHighWeight).isLessThan(baseProfit);
    }

    @Test
    @DisplayName("测试边界价格值的影响")
    void testBoundaryPriceImpact() {
        // 最低价格
        PriceVectorInterface minPriceVector = new PriceVector(new float[]{
            SmartGridConfig.MIN_PRICE, SmartGridConfig.MIN_PRICE, 
            SmartGridConfig.MIN_PRICE, SmartGridConfig.MIN_PRICE
        });
        
        // 最高价格
        PriceVectorInterface maxPriceVector = new PriceVector(new float[]{
            SmartGridConfig.MAX_PRICE, SmartGridConfig.MAX_PRICE, 
            SmartGridConfig.MAX_PRICE, SmartGridConfig.MAX_PRICE
        });
        
        float minProfit = calculator.calculateProfit(minPriceVector, totalConsumption);
        float maxProfit = calculator.calculateProfit(maxPriceVector, totalConsumption);
        
        // 价格更高时，收益更高，利润应该更高
        assertThat(maxProfit).isGreaterThan(minProfit);
    }

    @Test
    @DisplayName("测试已弃用的方法兼容性")
    @SuppressWarnings("deprecation")
    void testDeprecatedMethods() {
        // 测试已弃用的静态方法应该抛出异常
        assertThatThrownBy(() -> RetailerProfitCalculator.getRetialProfit(null, null))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("已弃用的方法");
    }

    @Test
    @DisplayName("测试利润分析的不可变性")
    void testProfitAnalysisImmutability() {
        RetailerProfitCalculator.ProfitAnalysis analysis = 
            calculator.getDetailedProfitAnalysis(priceVector, totalConsumption);
        
        float[] originalRevenue = analysis.getRevenueByTimeSlot();
        float[] originalCost = analysis.getCostByTimeSlot();
        
        // 修改返回的数组不应影响分析对象内部状态
        originalRevenue[0] = 999.0f;
        originalCost[0] = 999.0f;
        
        float[] newRevenue = analysis.getRevenueByTimeSlot();
        float[] newCost = analysis.getCostByTimeSlot();
        
        assertThat(newRevenue[0]).isNotEqualTo(999.0f);
        assertThat(newCost[0]).isNotEqualTo(999.0f);
    }

    @Test
    @DisplayName("测试复杂场景的利润计算")
    void testComplexScenarioProfit() {
        // 创建一个复杂的价格模式：峰谷价格
        PriceVectorInterface peakValleyPrices = new PriceVector(new float[]{
            1.5f,  // 早高峰
            0.5f,  // 低谷
            0.5f,  // 低谷
            1.4f   // 晚高峰
        });
        
        // 对应的高低消耗模式
        int[] peakValleyConsumption = new int[]{20, 5, 5, 18};
        
        RetailerProfitCalculator.ProfitAnalysis analysis = 
            calculator.getDetailedProfitAnalysis(peakValleyPrices, peakValleyConsumption);
        
        // 验证高价格高消耗时段是最盈利的
        int mostProfitable = analysis.getMostProfitableTimeSlot();
        assertThat(mostProfitable).isIn(0, 3); // 应该是高价格时段之一
        
        // 验证总体盈利性
        assertThat(analysis.getTotalProfit()).isPositive();
        assertThat(analysis.getProfitMargin()).isPositive();
    }

    @Test
    @DisplayName("测试数值精度和舍入")
    void testNumericalPrecision() {
        // 使用正常范围内的数值测试精度
        PriceVectorInterface precisionPrices = new PriceVector(new float[]{
            0.5f, 0.6f, 0.9f, 1.1f
        });
        int[] precisionConsumption = new int[]{3, 6, 9, 12};
        
        float profit = calculator.calculateProfit(precisionPrices, precisionConsumption);
        
        // 应该能正常计算，不会出现NaN或无穷大
        assertThat(profit).isFinite();
        assertThat(profit).isNotNaN();
    }
}