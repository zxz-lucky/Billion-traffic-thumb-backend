**亿级流量点赞系统**

**项目介绍**
本项目是一个基于 Spring Boot 3 + Java 21 + MyBatis-Plus + TiDB + Redis + Pulsar + Docker + Nginx 的高并发点赞系统。该项目涵盖高并发、大流量、高性能、高可用、分布式架构、可观测性等核心技术，集成最新的技术栈和架构设计方案。

**项目三大阶段**
1.第一阶段 - 基础功能开发：使用 Spring Boot 3 + MyBatis-Plus 快速开发基础点赞功能。

<img width="748" alt="第一阶段" src="https://github.com/user-attachments/assets/ee90c88f-420a-48fb-ad48-9d2d5be0e1cd" />


2.第二阶段 - 性能优化和高并发支持（进阶）：采用多种方案优化缓存策略，同时引入 Pulsar 消息队列 和 TiDB 分布式关系型数据库，减少数据库压力，提升系统并发能力。

<img width="760" alt="第二阶段" src="https://github.com/user-attachments/assets/dc12bd60-e57c-4e6e-ab75-dc99922e3a58" />

实战消息队列解耦方案：

![解耦方案](https://github.com/user-attachments/assets/331a6769-898b-4b37-b908-8c80d8456867)

3.第三阶段 - 企业级高可用架构（高级）：介绍 DB/缓存/消息队列 多级降级方案，保证系统不崩溃，打造稳定的企业级高可用架构，支持大规模流量和容灾。

<img width="971" alt="第三阶段" src="https://github.com/user-attachments/assets/908f9f89-b826-4b9b-ad86-2ddbc1da9cba" />

基于 Prometheus + Grafana 实现系统的多维可视化监控和告警，实现可观测性：
<img width="1707" alt="监控" src="https://github.com/user-attachments/assets/6f17ac1b-1d4b-4072-88fc-7a2b8d4c28f3" />
