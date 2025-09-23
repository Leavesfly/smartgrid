package io.leavesfly.smartgrid.core.model;

import io.leavesfly.smartgrid.core.config.SmartGridConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

/**
 * PriceVector 单元测试类
 * 测试电价向量类的各项功能和数据完整性
 * 
 * @author SmartGrid Team
 * @version 2.0
 */
@DisplayName("PriceVector 电价向量测试")
class PriceVectorTest {

    private PriceVector priceVector;
    private float[] testPrices;

    @BeforeEach
    void setUp() {
        testPrices = new float[]{0.8f, 1.0f, 1.2f, 0.6f};
        priceVector = new PriceVector(testPrices);
    }

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        PriceVector defaultVector = new PriceVector();
        
        assertThat(defaultVector.getTimeSlots()).isEqualTo(SmartGridConfig.TIME_SLOTS);
        assertThat(defaultVector.isValid()).isTrue();
        
        // 验证所有价格都在有效范围内
        float[] prices = defaultVector.getPricesCopy();
        for (float price : prices) {
            assertThat(price)
                .isBetween(SmartGridConfig.MIN_PRICE, SmartGridConfig.MAX_PRICE);
        }
    }

    @Test
    @DisplayName("测试价格数组构造函数")
    void testArrayConstructor() {
        assertThat(priceVector.getTimeSlots()).isEqualTo(SmartGridConfig.TIME_SLOTS);
        assertThat(priceVector.isValid()).isTrue();
        
        float[] resultPrices = priceVector.getPricesCopy();
        assertThat(resultPrices).containsExactly(testPrices);
    }

    @Test
    @DisplayName("测试复制构造函数")
    void testCopyConstructor() {
        PriceVector copiedVector = new PriceVector(priceVector);
        
        assertThat(copiedVector.getPricesCopy()).containsExactly(testPrices);
        assertThat(copiedVector.isAlgorithmEnded()).isEqualTo(priceVector.isAlgorithmEnded());
        assertThat(copiedVector.isNewPrice()).isEqualTo(priceVector.isNewPrice());
        
        // 确保是深拷贝
        copiedVector.setPriceByPosition(0, 0.9f);
        assertThat(priceVector.getPriceByPosition(0)).isEqualTo(0.8f);
    }

    @Test
    @DisplayName("测试null参数的构造函数异常")
    void testConstructorWithNullParameters() {
        assertThatThrownBy(() -> new PriceVector((PriceVector) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("复制源价格向量不能为null");
        
        assertThatThrownBy(() -> new PriceVector((float[]) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("价格数组不能为null");
    }

    @Test
    @DisplayName("测试无效长度的价格数组")
    void testInvalidArrayLength() {
        float[] invalidArray = new float[]{0.8f, 1.0f};  // 长度不正确
        
        assertThatThrownBy(() -> new PriceVector(invalidArray))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("价格数组长度必须为 " + SmartGridConfig.TIME_SLOTS);
    }

    @Test
    @DisplayName("测试无效价格值的数组")
    void testInvalidPriceValues() {
        float[] invalidPrices = new float[]{0.8f, 2.0f, 1.2f, 0.6f};  // 2.0f超出范围
        
        assertThatThrownBy(() -> new PriceVector(invalidPrices))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("价格数组包含无效值");
    }

    @Test
    @DisplayName("测试根据位置获取价格")
    void testGetPriceByPosition() {
        for (int i = 0; i < testPrices.length; i++) {
            assertThat(priceVector.getPriceByPosition(i)).isEqualTo(testPrices[i]);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 4, 5, 10})
    @DisplayName("测试无效位置获取价格")
    void testGetPriceByInvalidPosition(int invalidPosition) {
        assertThatThrownBy(() -> priceVector.getPriceByPosition(invalidPosition))
            .isInstanceOf(IndexOutOfBoundsException.class)
            .hasMessageContaining("位置索引超出范围");
    }

    @Test
    @DisplayName("测试根据位置设置价格")
    void testSetPriceByPosition() {
        float newPrice = 0.7f;
        priceVector.setPriceByPosition(0, newPrice);
        
        assertThat(priceVector.getPriceByPosition(0)).isEqualTo(newPrice);
    }

    @ParameterizedTest
    @ValueSource(floats = {0.4f, 1.6f, Float.NaN, Float.POSITIVE_INFINITY})
    @DisplayName("测试设置无效价格")
    void testSetInvalidPrice(float invalidPrice) {
        assertThatThrownBy(() -> priceVector.setPriceByPosition(0, invalidPrice))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("价格无效");
    }

    @Test
    @DisplayName("测试获取价格副本")
    void testGetPricesCopy() {
        float[] pricesCopy = priceVector.getPricesCopy();
        
        assertThat(pricesCopy).containsExactly(testPrices);
        
        // 修改副本不应影响原始数据
        pricesCopy[0] = 0.9f;
        assertThat(priceVector.getPriceByPosition(0)).isEqualTo(0.8f);
    }

    @Test
    @DisplayName("测试创建新价格向量")
    void testCreateNewPriceVector() {
        int position = 1;
        float newPrice = 0.9f;
        
        PriceVectorInterface newVector = priceVector.createNewPriceVector(position, newPrice);
        
        assertThat(newVector).isNotSameAs(priceVector);
        assertThat(newVector.getPriceByPosition(position)).isEqualTo(newPrice);
        assertThat(((PriceVector) newVector).isNewPrice()).isTrue();
        
        // 验证其他位置价格不变
        for (int i = 0; i < SmartGridConfig.TIME_SLOTS; i++) {
            if (i != position) {
                assertThat(newVector.getPriceByPosition(i))
                    .isEqualTo(priceVector.getPriceByPosition(i));
            }
        }
    }

    @Test
    @DisplayName("测试价格向量有效性验证")
    void testIsValid() {
        assertThat(priceVector.isValid()).isTrue();
        
        // 创建无效价格向量
        PriceVector invalidVector = new PriceVector();
        invalidVector.getPrices()[0] = Float.NaN;  // 使用deprecated方法进行测试
        
        assertThat(invalidVector.isValid()).isFalse();
    }

    @Test
    @DisplayName("测试从其他向量复制")
    void testCopyFrom() {
        PriceVector sourceVector = new PriceVector();
        float[] sourcePrices = sourceVector.getPricesCopy();
        
        priceVector.copyFrom(sourceVector);
        
        assertThat(priceVector.getPricesCopy()).containsExactly(sourcePrices);
    }

    @Test
    @DisplayName("测试从null向量复制")
    void testCopyFromNull() {
        assertThatThrownBy(() -> priceVector.copyFrom(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("源价格向量不能为null");
    }

    @Test
    @DisplayName("测试生成随机价格")
    void testGenerateRandomPrice() {
        for (int i = 0; i < 100; i++) {  // 多次测试以确保随机性
            float randomPrice = PriceVector.generateRandomPrice();
            assertThat(randomPrice)
                .isBetween(SmartGridConfig.MIN_PRICE, SmartGridConfig.MAX_PRICE);
        }
    }

    @Test
    @DisplayName("测试算法结束标志")
    void testAlgorithmEndedFlag() {
        assertThat(priceVector.isAlgorithmEnded()).isFalse();
        
        priceVector.setAlgorithmEnded(true);
        assertThat(priceVector.isAlgorithmEnded()).isTrue();
    }

    @Test
    @DisplayName("测试新价格标志")
    void testNewPriceFlag() {
        assertThat(priceVector.isNewPrice()).isFalse();
        
        priceVector.setNewPrice(true);
        assertThat(priceVector.isNewPrice()).isTrue();
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        String result = priceVector.toString();
        
        assertThat(result)
            .startsWith("prices:(")
            .endsWith(")")
            .contains("0.800")
            .contains("1.000")
            .contains("1.200")
            .contains("0.600");
    }

    @Test
    @DisplayName("测试equals方法")
    void testEquals() {
        PriceVector equalVector = new PriceVector(testPrices);
        PriceVector differentVector = new PriceVector(new float[]{0.5f, 0.6f, 0.7f, 0.8f});
        
        assertThat(priceVector).isEqualTo(equalVector);
        assertThat(priceVector).isNotEqualTo(differentVector);
        assertThat(priceVector).isNotEqualTo(null);
        assertThat(priceVector).isNotEqualTo("not a price vector");
    }

    @Test
    @DisplayName("测试hashCode方法")
    void testHashCode() {
        PriceVector equalVector = new PriceVector(testPrices);
        PriceVector differentVector = new PriceVector(new float[]{0.5f, 0.6f, 0.7f, 0.8f});
        
        assertThat(priceVector.hashCode()).isEqualTo(equalVector.hashCode());
        assertThat(priceVector.hashCode()).isNotEqualTo(differentVector.hashCode());
    }

    @Test
    @DisplayName("测试已弃用的方法兼容性")
    @SuppressWarnings("deprecation")
    void testDeprecatedMethods() {
        // 测试getOneRandomPrice
        float randomPrice = PriceVector.getOneRandomPrice();
        assertThat(randomPrice).isBetween(SmartGridConfig.MIN_PRICE, SmartGridConfig.MAX_PRICE);
        
        // 测试getTimeSolts
        assertThat(PriceVector.getTimeSolts()).isEqualTo(SmartGridConfig.TIME_SLOTS);
        
        // 测试isEnd和setEnd
        assertThat(priceVector.isEnd()).isFalse();
        priceVector.setEnd(true);
        assertThat(priceVector.isEnd()).isTrue();
        
        // 测试privceVectorGiven
        PriceVector targetVector = new PriceVector();
        priceVector.privceVectorGiven(targetVector, priceVector);
        assertThat(targetVector.getPricesCopy()).containsExactly(testPrices);
        
        // 测试getNewPriceVector
        PriceVector newVector = new PriceVector();
        PriceVector result = priceVector.getNewPriceVector(0, 0.9f, newVector);
        assertThat(result.getPriceByPosition(0)).isEqualTo(0.9f);
        assertThat(result).isSameAs(newVector);
    }

    @Test
    @DisplayName("测试边界价格值")
    void testBoundaryPrices() {
        float[] boundaryPrices = new float[]{
            SmartGridConfig.MIN_PRICE, 
            SmartGridConfig.MAX_PRICE, 
            SmartGridConfig.MIN_PRICE, 
            SmartGridConfig.MAX_PRICE
        };
        
        PriceVector boundaryVector = new PriceVector(boundaryPrices);
        assertThat(boundaryVector.isValid()).isTrue();
        assertThat(boundaryVector.getPricesCopy()).containsExactly(boundaryPrices);
    }

    @Test
    @DisplayName("测试线程安全性的基本要求")
    void testBasicThreadSafety() {
        // 测试多个线程同时读取不会发生异常
        Runnable readTask = () -> {
            for (int i = 0; i < 100; i++) {
                priceVector.getPriceByPosition(i % SmartGridConfig.TIME_SLOTS);
                priceVector.getPricesCopy();
                priceVector.isValid();
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