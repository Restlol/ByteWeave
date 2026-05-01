# ByteWeave Agent (Learning Project)

> 一个基于 Java Instrumentation + ByteBuddy 的学习型项目 ，通过多个独立 Demo 演示 JVM 运行时插桩的核心能力  

---

## 限制：

❗仅用于学习和原理验证

- 每个功能以 demo 形式独立实现
- 未做完整工程化封装
- 未考虑高并发 / 安全 / 边界情况
- 主要用于理解 JVM 插桩机制

---
## 内容：
- Java Agent 启动机制（premain / attach）
- Instrumentation API（redefine / retransform）
- ByteBuddy 动态字节码增强
- Advice 插桩模型
- 方法入参 / 返回值 / 异常捕获
- 方法耗时统计实现
- 调用链入口识别（MethodCallRoot）
- 类动态增强 / reset 原理
