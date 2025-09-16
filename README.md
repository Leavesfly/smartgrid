# 智能电网需求响应管理系统 (SmartGrid)

基于实时电价控制的智能电网需求响应管理系统

## 📖 项目简介

本项目是一个智能电网需求响应管理系统，通过实时电价控制机制来优化电力资源的使用，实现智能电网中的供需平衡。系统采用模拟退火算法(SAPC)来动态调整电价策略，引导用户合理用电，同时优化电力零售商的利润。

### 🎯 核心目标
- 通过动态电价机制引导用户合理用电
- 优化电力零售商的利润和用户的用电满意度
- 模拟和分析电力消费行为
- 实现智能电网中的需求响应管理

## 🏗️ 系统架构

### 主要组件

#### 1. 零售商模块 (`retailer`)
- **`RetailerServer`**: 零售商服务器，监听用户连接
- **`Retailer`**: 零售商核心类，管理价格向量和利润计算
- **`SAPC_Algorithm`**: 模拟退火算法实现，用于价格优化
- **`PriceVector`**: 价格向量类，管理时段电价
- **`RetailerThread`**: 零售商线程，处理用户请求
- **`RetailerProfitAlgorithm`**: 零售商利润计算算法
- **`LogToTxtFile`**: 日志记录工具

#### 2. 用户模块 (`user`)
- **`Users`**: 用户启动类，创建多个用户线程
- **`UserThread`**: 用户线程，与零售商通信
- **`OneUserConsumVector`**: 单用户消费向量
- **`UserMaxSatisfaConsumVector`**: 用户满意度最大化消费向量
- **`UsersArgs`**: 用户配置参数
- **`LogToTxtFile`**: 用户日志记录

### 系统工作流程
1. 零售商服务器启动，监听指定端口
2. 用户客户端连接到零售商服务器
3. 零售商使用SAPC算法计算最优价格向量
4. 价格向量广播给所有用户
5. 用户根据价格向量计算最优消费策略
6. 用户将消费向量返回给零售商
7. 零售商收集所有用户消费数据，计算总利润
8. 重复步骤3-7，直到算法收敛

## 🛠️ 技术栈

- **语言**: Java
- **构建工具**: Maven
- **测试框架**: JUnit 4.5
- **通信机制**: Socket编程
- **算法**: 模拟退火算法 (Simulated Annealing)
- **并发处理**: 多线程

## 📋 系统要求

- Java 8 或更高版本
- Maven 3.x
- 操作系统: Windows/Linux/macOS

## 🚀 快速开始

### 1. 克隆项目
```bash
git clone <repository-url>
cd smartgrid
```

### 2. 编译项目
```bash
mvn clean package
```

### 3. 运行系统

#### 启动零售商服务器
```bash
java -cp target/classes io.leavesfly.smartgrid.retailer.RetailerServer
```

#### 启动用户客户端（在新的终端窗口中）
```bash
java -cp target/classes io.leavesfly.smartgrid.user.Users
```

## ⚙️ 配置说明

### 零售商配置 (`RetailerInitArgs.java`)
- `timeSlots`: 时段数量 (默认: 4)
- `ListenPort`: 服务监听端口 (默认: 1234)
- `T`: 模拟退火初始温度
- `E`: 模拟退火终止温度
- `logFile`: 日志文件路径
- `a`, `b`, `w`: 算法参数

### 用户配置 (`UsersArgs.java`)
- `userNum`: 用户数量 (默认: 2)
- `timeSlots`: 时段数量 (默认: 4)
- `ip`: 服务器IP地址 (默认: 127.0.0.1)
- `port`: 服务器端口 (默认: 1234)
- `A_applianceNum`: A类电器数量 (默认: 4)
- `B_applianceNum`: B类电器数量 (默认: 4)

## 📊 算法说明

### SAPC算法 (Simulated Annealing Price Control)
系统采用模拟退火算法来寻找最优电价策略：

1. **初始化**: 生成随机价格向量
2. **迭代优化**: 
   - 在当前价格向量的邻域内生成新的价格向量
   - 计算新价格向量下的零售商利润
   - 根据Metropolis准则决定是否接受新解
3. **温度降低**: 随着迭代进行，逐渐降低接受劣解的概率
4. **收敛**: 当温度足够低时算法终止

### 用户行为建模
- **A类电器**: 不可调节电器，固定消费
- **B类电器**: 可调节电器，根据价格和满意度优化消费
- **满意度函数**: 用户对不同时段用电的满意度评分

## 📝 日志系统

系统提供详细的日志记录功能：
- **零售商日志**: 记录价格调整过程、利润变化等
- **用户日志**: 记录用户连接状态、消费决策等
- **日志文件**: 默认保存在E盘根目录

## 🧪 测试

运行单元测试：
```bash
mvn test
```

## 📁 项目结构
```
src/main/java/io/leavesfly/smartgrid/
├── retailer/          # 零售商模块
│   ├── RetailerServer.java       # 零售商服务器
│   ├── Retailer.java             # 零售商核心类
│   ├── SAPC_Algorithm.java       # 模拟退火算法
│   ├── PriceVector.java          # 价格向量
│   ├── RetailerThread.java       # 零售商线程
│   ├── RetailerInitArgs.java     # 零售商配置
│   ├── RetailerProfitAlgorithm.java  # 利润计算
│   ├── ConsumEleByTime.java      # 时段消费
│   ├── AddStepNum.java           # 步数计数
│   └── LogToTxtFile.java         # 日志工具
└── user/              # 用户模块
    ├── Users.java                 # 用户启动类
    ├── UserThread.java           # 用户线程
    ├── OneUserConsumVector.java  # 用户消费向量
    ├── UserMaxSatisfaConsumVector.java  # 满意度最大化
    ├── UsersArgs.java            # 用户配置
    └── LogToTxtFile.java         # 用户日志
```

## 🤝 贡献指南

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系方式

如有问题或建议，请通过以下方式联系：
- 提交 Issue
- 发送邮件至 [your-email@example.com]

## 🔮 未来计划

- [ ] 添加图形化用户界面
- [ ] 支持更多优化算法
- [ ] 增加实时数据可视化
- [ ] 支持分布式部署
- [ ] 添加更多的用户行为模型
- [ ] 集成机器学习预测模型
