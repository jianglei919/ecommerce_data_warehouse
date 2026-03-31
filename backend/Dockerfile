FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 复制 pom.xml 并下载依赖
COPY pom.xml .
RUN mvn dependency:go-offline

# 复制源代码
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests -q

# 运行阶段
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# 复制 JAR 文件
COPY --from=builder /build/target/warehouse-backend-*.jar app.jar

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]

EXPOSE 8080
