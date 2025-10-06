# 智能电网需求响应管理系统

[![Java](https://img.shields.io/badge/Java-8+-blue.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-green.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📖 项目概述

智能电网需求响应管理系统是一个基于模拟退火算法（SAPC）的电力价格优化系统。该系统通过动态定价机制和用户需求响应，实现电网负载均衡和零售商利润最大化的双重目标。

### ✨ 核心特性

- 🔄 **动态定价机制**：基于模拟退火算法的实时电价优化
- 📊 **需求响应管理**：智能用户用电行为分析和引导
- 🚀 **多线程架构**：高并发处理用户请求和价格计算
- 📈 **利润优化算法**：零售商收益最大化策略
- 🔍 **实时监控系统**：全方位的系统状态监控和日志记录
- ⚖️ **负载均衡**：电网峰谷用电平衡优化

## 🏗️ 系统架构

系统采用客户端-服务器架构，分为零售商模块和用户模块两大核心部分：

```
┌─────────────────────────────────────────────────────────────┐
│                    智能电网管理系统                          │
├─────────────────────┬───────────────────────────────────────┤
│    零售商模块        │              用户模块                │
│                     │                                       │
│  ┌─────────────┐    │    ┌─────────────┐ ┌─────────────┐   │
│  │RetailerServer│◄───┼───►│UserThread   │ │UserThread   │   │
│  └─────────────┘    │    └─────────────┘ └─────────────┘   │
│         │           │            │              │          │
│  ┌─────────────┐    │    ┌─────────────┐ ┌─────────────┐   │
│  │ Retailer    │    │    │    Users    │ │    Users    │   │
│  └─────────────┘    │    └─────────────┘ └─────────────┘   │
│         │           │                                       │
│  ┌─────────────┐    │                                       │
│  │SAPC_Algorithm│   │                                       │
│  └─────────────┘    │                                       │
└─────────────────────┴───────────────────────────────────────┘
```

### 🔧 核心组件

#### 零售商模块
- **RetailerServer**: 服务器端主控制器，管理客户端连接
- **Retailer**: 零售商核心业务逻辑处理
- **SAPC_Algorithm**: 模拟退火价格控制算法实现
- **RetailerProfitCalculator**: 利润计算和优化引擎

#### 用户模块
- **Users**: 用户实体和用电行为模拟
- **UserThread**: 用户端通信线程处理
- **UserConsumptionVector**: 用户用电向量模型
- **UserMaxSatisfaConsumVector**: 用户满意度最大化模型

## 🚀 快速开始

### 📋 环境要求

- **Java**: 8 或更高版本
- **Maven**: 3.6 或更高版本
- **操作系统**: Windows / macOS / Linux

### 🛠️ 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/your-repo/smartgrid.git
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
mvn clean package
```

### ▶️ 运行示例

#### 启动完整演示程序
```bash
java -cp target/classes io.leavesfly.smartgrid.example.SmartGridRefactoredDemo
```

#### 分别启动零售商和用户
```bash
# 启动零售商服务器
java -cp target/classes io.leavesfly.smartgrid.retailer.RetailerServer

# 启动用户客户端（另开终端）
java -cp target/classes io.leavesfly.smartgrid.user.Users
```

## 📊 核心算法

### 🌡️ 模拟退火算法（SAPC）

系统采用模拟退火算法进行电价优化，通过以下步骤实现：

```
1. 初始化 → 2. 生成新解 → 3. 计算目标函数 → 4. Metropolis准则判断 → 5. 更新当前解 → 6. 降温 → 回到步骤2
```

**算法参数配置：**
- 初始温度：`T = e^(-1) ≈ 0.368`
- 终止温度：`E = e^(-5) ≈ 0.007`
- 降温策略：`T = T / ln(k)`

### 💰 利润计算模型

零售商利润计算采用二次函数模型：

```
Profit = Σ(price[i] × consumption[i] - cost_function(consumption[i]))
```

其中成本函数考虑了购电成本和系统运营成本。

## ⚙️ 配置说明

### 零售商配置参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `timeSlots` | int | 4 | 时段数量 |
| `ListenPort` | int | 1234 | 服务监听端口 |
| `T` | float | e⁻¹ | 模拟退火初始温度 |
| `E` | float | e⁻⁵ | 模拟退火终止温度 |
| `a` | float | 0.005 | 利润计算二次项系数 |
| `b` | float | 0.001 | 利润计算三次项系数 |

### 用户配置参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `userNum` | int | 2 | 用户数量 |
| `ip` | String | 127.0.0.1 | 服务器IP地址 |
| `port` | int | 1234 | 服务器端口 |
| `A_applianceNum` | int | 4 | A类电器数量（不可调节） |
| `B_applianceNum` | int | 4 | B类电器数量（可调节） |

## 🧪 测试

### 运行所有测试
```bash
mvn test
```

### 运行特定测试类
```bash
mvn test -Dtest=RetailerProfitCalculatorTest
```

### 查看测试覆盖率
```bash
mvn test jacoco:report
```

测试报告将生成在 `target/site/jacoco/index.html`

## 📝 开发指南

### 代码结构

```
src/
├── main/java/io/leavesfly/smartgrid/
│   ├── core/                    # 核心组件
│   │   ├── algorithm/           # 算法实现
│   │   ├── config/             # 配置管理
│   │   └── model/              # 数据模型
│   ├── retailer/               # 零售商模块
│   ├── user/                   # 用户模块
│   ├── util/                   # 工具类
│   └── example/                # 示例程序
└── test/                       # 测试代码
```

### 编码规范

- 使用中文注释说明核心逻辑
- 遵循 Java 命名规范
- 单元测试覆盖率不低于 80%
- 重要方法必须包含 JavaDoc 文档

### 提交代码

1. 确保所有测试通过
2. 更新相关文档
3. 遵循 Git 提交信息规范
4. 创建 Pull Request

## 📈 性能优化

### 系统性能指标

- **并发用户支持**: 100+ 用户同时在线
- **算法收敛时间**: < 10秒（4时段，2用户）
- **内存使用**: < 256MB
- **CPU使用率**: < 30%

### 优化建议

1. **算法优化**: 调整初始温度和降温策略
2. **网络优化**: 使用连接池管理客户端连接
3. **内存优化**: 合理设置JVM堆大小
4. **日志优化**: 配置异步日志写入

## 📚 文档资源

- [系统架构设计](docs/architecture.md)
- [算法详细说明](docs/algorithm.md)
- [API文档](docs/api.md)
- [部署指南](docs/deployment.md)
- [常见问题](docs/faq.md)

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 许可证

本项目基于 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 👥 作者

- **山泽** - *项目维护者* 

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者和研究人员。

## 📞 联系我们

如有问题或建议，请通过以下方式联系：

- 📧 邮箱: [your-email@example.com]
- 🐛 问题反馈: [GitHub Issues](https://github.com/your-repo/smartgrid/issues)
- 💬 讨论区: [GitHub Discussions](https://github.com/your-repo/smartgrid/discussions)

---

⭐ 如果这个项目对你有帮助，请给我们一个 Star！