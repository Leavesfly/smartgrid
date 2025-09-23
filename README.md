# 智能电网需求响应管理系统 (SmartGrid) - 重构版 v2.0

## 项目简介

智能电网需求响应管理系统是一个基于Java的分布式系统，专注于电力零售商和用户之间的动态定价与需求响应管理。系统采用模拟退火算法（SAPC - Simulated Annealing Price Control）进行电价优化，实现电力系统的智能化管理和用户满意度最大化。

### 重构亮点 (v2.0)

本项目经过全面重构，相较于原版本具有以下显著改进：

#### 🚀 架构优化
- **分层架构**: 采用清晰的分层架构设计，分离关注点
- **接口抽象**: 定义核心接口，提高系统的可扩展性和可测试性
- **模块化设计**: 将系统划分为配置、模型、算法、服务、工具等模块

#### 🛡️ 代码质量提升
- **类型安全**: 增强类型检查和参数验证
- **异常处理**: 完善的异常处理机制和错误恢复
- **线程安全**: 改进多线程处理和同步机制
- **内存管理**: 优化资源管理和内存使用

#### 📋 功能增强
- **统一配置**: 集中式配置管理，提高可维护性
- **日志系统**: 专业的日志记录系统，支持多级别和多类型日志
- **数据验证**: 全面的数据有效性验证
- **性能监控**: 内置性能分析和监控功能

#### 📚 文档完善
- **中文注释**: 完整的中文代码注释和JavaDoc
- **使用指南**: 详细的使用说明和示例代码
- **架构文档**: 系统架构和设计模式说明

## 系统架构

### 整体架构图

```
智能电网系统架构 v2.0
├── 核心层 (Core)
│   ├── 配置管理 (config)
│   │   └── SmartGridConfig - 统一配置管理
│   ├── 数据模型 (model)
│   │   ├── PriceVector - 电价向量模型
│   │   ├── UserConsumptionVector - 用户消耗向量
│   │   └── SystemConsumptionAggregate - 系统消耗聚合
│   └── 算法层 (algorithm)
│       ├── OptimizationAlgorithmInterface - 优化算法接口
│       ├── ProfitCalculatorInterface - 利润计算接口
│       └── RetailerProfitCalculator - 零售商利润计算器
├── 服务层 (Service)
│   ├── 零售商服务 (retailer)
│   │   ├── RetailerServer - 零售商服务器
│   │   ├── RetailerThread - 零售商线程处理
│   │   └── SAPC算法服务
│   └── 用户服务 (user)
│       ├── UserThread - 用户线程处理
│       └── 用户满意度计算服务
└── 工具层 (Util)
    ├── 日志系统 (logging)
    │   └── SmartGridLogger - 统一日志管理器
    └── 通信管理 (communication)
        └── 网络通信工具
```

### 技术栈

- **语言**: Java 17+
- **构建工具**: Maven 3.6+
- **架构模式**: 分层架构 + 模块化设计
- **设计模式**: 单例模式、策略模式、观察者模式、工厂模式
- **并发处理**: 多线程 + 同步机制
- **网络通信**: Socket + 对象序列化

## 快速开始

### 环境要求

- Java 17 或更高版本
- Maven 3.6 或更高版本
- 操作系统: Windows/Linux/macOS

### 安装与编译

1. **克隆项目**
```bash
git clone <repository-url>
cd smartgrid
```

2. **编译项目**
```bash
mvn clean compile
```

3. **运行测试**
```bash
mvn test
```

4. **打包项目**
```bash
mvn package
```

### 运行演示程序

系统提供了完整的功能演示程序，展示重构后的各项功能：

```bash
mvn exec:java -Dexec.mainClass="io.leavesfly.smartgrid.example.SmartGridRefactoredDemo"
```

### 运行完整系统

1. **启动零售商服务器**
```bash
# 将在未来版本中提供完整的服务器启动命令
mvn exec:java -Dexec.mainClass="io.leavesfly.smartgrid.service.retailer.RetailerServer"
```

2. **启动用户客户端**
```bash
# 将在未来版本中提供完整的客户端启动命令
mvn exec:java -Dexec.mainClass="io.leavesfly.smartgrid.service.user.UserService"
```

## 核心功能

### 1. 动态电价管理

- **价格向量模型**: 支持多时段电价设置
- **价格验证**: 自动验证价格范围的合理性
- **价格历史**: 记录和追踪价格变化历史

```java
// 创建价格向量
PriceVector priceVector = new PriceVector();

// 设置特定时段价格
priceVector.setPriceByPosition(1, 1.2f);

// 创建新价格向量
PriceVector newPrice = priceVector.createNewPriceVector(2, 0.8f);
```

### 2. 用户需求响应

- **消耗向量管理**: 跟踪用户各时段用电情况
- **满意度计算**: 基于价格和需求计算用户满意度
- **用电限制**: 自动检查用户用电限制

```java
// 创建用户消耗向量
UserConsumptionVector userVector = new UserConsumptionVector(0, new int[]{3, 5, 2, 4});

// 检查是否超限
boolean isOverLimit = userVector.isOverConsumptionLimit();

// 获取剩余容量
int remaining = userVector.getRemainingCapacity();
```

### 3. 系统负载聚合

- **多用户聚合**: 自动聚合所有用户的用电数据
- **负载分析**: 提供峰谷分析和负载均衡度计算
- **实时监控**: 实时跟踪系统总负载

```java
// 创建系统聚合对象
SystemConsumptionAggregate aggregate = new SystemConsumptionAggregate();

// 聚合用户数据
aggregate.aggregateUserConsumptions(userConsumptions);

// 分析负载情况
int peakTime = aggregate.getPeakConsumptionTimeSlot();
double balance = aggregate.calculateLoadBalanceMetric();
```

### 4. 利润优化计算

- **利润模型**: 基于收益和成本的利润计算模型
- **详细分析**: 提供分时段的收益和成本分析
- **参数配置**: 支持灵活的算法参数配置

```java
// 创建利润计算器
RetailerProfitCalculator calculator = new RetailerProfitCalculator();

// 计算利润
float profit = calculator.calculateProfit(priceVector, totalConsumption);

// 获取详细分析
ProfitAnalysis analysis = calculator.getDetailedProfitAnalysis(priceVector, totalConsumption);
```

### 5. 智能日志系统

- **多级别日志**: 支持DEBUG、INFO、WARN、ERROR多个级别
- **多类型日志**: 区分零售商、用户、系统不同类型的日志
- **线程安全**: 支持多线程环境下的安全日志记录

```java
// 获取日志实例
SmartGridLogger logger = SmartGridLogger.getInstance();

// 记录不同类型日志
logger.info(LogType.RETAILER, "零售商服务启动");
logger.error(LogType.SYSTEM, "系统异常", exception);
```

## 配置说明

系统配置统一由 `SmartGridConfig` 类管理，主要配置项包括：

### 基本配置
```java
// 时间配置
TIME_SLOTS = 4              // 时间段数量

// 网络配置
SERVER_IP = "127.0.0.1"     // 服务器IP
SERVER_PORT = 1234          // 服务器端口

// 用户配置
USER_COUNT = 2              // 用户数量
```

### 算法参数
```java
// 价格范围
MIN_PRICE = 0.5f            // 最低电价
MAX_PRICE = 1.5f            // 最高电价

// 模拟退火参数
INITIAL_TEMPERATURE = Math.exp(-1)  // 初始温度
END_TEMPERATURE = Math.exp(-5)      // 终止温度
```

### 用户电器配置
```java
// A类电器（不可调节）
A_APPLIANCE_CONSUMPTION = {
    {1, 2, 3, 1},  // 用户0
    {1, 3, 3, 1}   // 用户1
}

// B类电器最大用电量
B_APPLIANCE_MAX_CONSUMPTION = {
    {2, 3, 4, 4},  // 用户0
    {2, 3, 1, 3}   // 用户1
}
```

## API文档

### 核心接口

#### PriceVectorInterface
电价向量管理接口

```java
public interface PriceVectorInterface {
    float getPriceByPosition(int position);
    void setPriceByPosition(int position, float price);
    float[] getPricesCopy();
    PriceVectorInterface createNewPriceVector(int position, float newPrice);
    boolean isValid();
}
```

#### ConsumptionVectorInterface
用电消耗向量管理接口

```java
public interface ConsumptionVectorInterface {
    int getUserId();
    int getConsumptionByTimeSlot(int timeSlot);
    void setConsumptionByTimeSlot(int timeSlot, int consumption);
    int[] getConsumptionsCopy();
    int getTotalConsumption();
    boolean isValid();
}
```

#### ProfitCalculatorInterface
利润计算接口

```java
public interface ProfitCalculatorInterface {
    float calculateProfit(PriceVectorInterface priceVector, int[] totalConsumption);
    void setParameters(float coefficientA, float coefficientB, int weight);
    boolean validateParameters();
}
```

### 使用示例

#### 基本价格管理
```java
// 创建价格向量
PriceVector price = new PriceVector();

// 修改价格
price.setPriceByPosition(0, 0.8f);
price.setPriceByPosition(1, 1.0f);
price.setPriceByPosition(2, 1.2f);
price.setPriceByPosition(3, 0.9f);

// 验证价格
if (price.isValid()) {
    System.out.println("价格设置有效: " + price);
}
```

#### 用户消耗管理
```java
// 创建用户消耗向量
UserConsumptionVector user = new UserConsumptionVector(0);

// 设置各时段用电量
user.setConsumptionByTimeSlot(0, 3);
user.setConsumptionByTimeSlot(1, 5);
user.setConsumptionByTimeSlot(2, 2);
user.setConsumptionByTimeSlot(3, 4);

// 检查用电情况
System.out.println("总用电量: " + user.getTotalConsumption());
System.out.println("是否超限: " + user.isOverConsumptionLimit());
```

#### 利润计算
```java
// 创建计算器
RetailerProfitCalculator calculator = new RetailerProfitCalculator();

// 准备数据
PriceVector prices = new PriceVector(new float[]{0.8f, 1.0f, 1.2f, 0.9f});
int[] consumption = {10, 15, 12, 8};

// 计算利润
float profit = calculator.calculateProfit(prices, consumption);
System.out.println("计算得出利润: " + profit);

// 获取详细分析
ProfitAnalysis analysis = calculator.getDetailedProfitAnalysis(prices, consumption);
System.out.println("利润分析: " + analysis);
```

## 性能优化

### 内存优化
- 使用对象池减少GC压力
- 及时释放不需要的资源
- 优化数组复制操作

### 并发优化
- 使用读写锁提高并发性能
- 优化同步块粒度
- 使用线程安全的数据结构

### 网络优化
- 优化序列化性能
- 使用连接池管理
- 实现心跳机制

## 故障排除

### 常见问题

1. **编译错误**
   - 确保Java版本为17+
   - 检查Maven配置是否正确
   - 清理并重新编译：`mvn clean compile`

2. **运行时异常**
   - 检查配置文件参数是否合理
   - 确认网络端口未被占用
   - 查看日志文件获取详细错误信息

3. **性能问题**
   - 调整JVM参数优化内存使用
   - 检查网络连接质量
   - 监控系统资源使用情况

### 日志分析

系统日志位于 `logs/` 目录下：
- `retailer.log`: 零售商服务日志
- `users.log`: 用户服务日志
- `system.log`: 系统级日志

## 贡献指南

我们欢迎社区贡献！请遵循以下步骤：

1. Fork项目仓库
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交修改 (`git commit -m 'Add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建Pull Request

### 代码规范

- 使用4空格缩进
- 遵循Java命名约定
- 添加完整的中文注释
- 编写单元测试
- 更新相关文档

### 测试要求

- 所有新功能必须包含单元测试
- 测试覆盖率不低于80%
- 集成测试通过
- 性能测试满足要求

## 版本历史

### v2.0.0 (2024-12-24)
- 🚀 全面重构系统架构
- 📦 模块化设计和分层架构
- 🛡️ 增强类型安全和异常处理
- 📋 统一配置管理和日志系统
- 📚 完善中文文档和注释
- ⚡ 性能优化和内存管理改进

### v1.0.0 (原版本)
- 基础的智能电网需求响应系统
- SAPC模拟退火算法实现
- 简单的客户端-服务器架构

## 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

## 联系方式

- 项目维护者: SmartGrid Team
- 邮箱: [待添加]
- 问题反馈: 请使用GitHub Issues
- 技术讨论: [待添加]

## 鸣谢

感谢所有为本项目作出贡献的开发者和研究人员。

---

**注意**: 本项目仍在积极开发中，某些功能可能尚未完全实现。我们欢迎社区反馈和贡献！