# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# 先复制 pom.xml 下载依赖（利用 Docker layer 缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并打包
COPY src ./src
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=dev
ENV JWT_SECRET=change-in-production

ENTRYPOINT ["java", "-jar", "app.jar"]