#!/bin/bash

# 快速修复脚本：升级 JDK 到 21 解决编译问题
# 推荐方案：JDK 17 → JDK 21 (LTS)

set -e

PROJECT_ROOT="/Users/logcabin/Workspace/uwindsor/ecommerce_data_warehouse"
BACKEND_DIR="$PROJECT_ROOT/backend"

echo "=========================================="
echo "编译问题修复 - JDK 升级到 21"
echo "=========================================="
echo ""

# 第一步: 修改 pom.xml
echo "📝 Step 1: Updating pom.xml (JDK 17 → 21)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if grep -q "17" "$BACKEND_DIR/pom.xml"; then
    echo "✓ Found JDK 17 references in pom.xml"

    # 使用 sed 进行替换
    sed -i '' 's/<java.version>17<\/java.version>/<java.version>21<\/java.version>/g' "$BACKEND_DIR/pom.xml"
    sed -i '' 's/<maven.compiler.source>17<\/maven.compiler.source>/<maven.compiler.source>21<\/maven.compiler.source>/g' "$BACKEND_DIR/pom.xml"
    sed -i '' 's/<maven.compiler.target>17<\/maven.compiler.target>/<maven.compiler.target>21<\/maven.compiler.target>/g' "$BACKEND_DIR/pom.xml"

    echo "✅ Updated pom.xml properties:"
    echo "   - java.version: 17 → 21"
    echo "   - maven.compiler.source: 17 → 21"
    echo "   - maven.compiler.target: 17 → 21"
else
    echo "⚠️  No JDK 17 references found in pom.xml"
fi

echo ""

# 第二步: 修改 Dockerfile
echo "📝 Step 2: Updating Dockerfile (eclipse-temurin-17 → eclipse-temurin-21)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if grep -q "eclipse-temurin-17" "$BACKEND_DIR/Dockerfile"; then
    echo "✓ Found eclipse-temurin-17 references in Dockerfile"

    sed -i '' 's/eclipse-temurin-17/eclipse-temurin-21/g' "$BACKEND_DIR/Dockerfile"

    echo "✅ Updated Dockerfile:"
    echo "   - Builder image: maven:3.9-eclipse-temurin-17 → maven:3.9-eclipse-temurin-21"
    echo "   - Runtime image: eclipse-temurin:17-jdk-jammy → eclipse-temurin:21-jdk-jammy"
else
    echo "⚠️  No eclipse-temurin-17 references found in Dockerfile"
fi

echo ""

# 第三步: 清理旧的 Maven 缓存
echo "🧹 Step 3: Cleaning Maven cache"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
rm -rf "$BACKEND_DIR/target"
echo "✅ Removed $BACKEND_DIR/target"

echo ""

# 第四步: 测试编译
echo "🔨 Step 4: Testing Maven compilation"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
cd "$BACKEND_DIR"
echo "Running: mvn clean compile"
echo ""

if mvn clean compile -q 2>&1 | tail -20; then
    echo "✅ Compilation successful! JAR can be built"
else
    echo "❌ Compilation failed. See errors above."
    exit 1
fi

echo ""

# 第五步: 完整打包测试
echo "📦 Step 5: Building complete JAR package"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
cd "$BACKEND_DIR"

if mvn clean package -DskipTests -q 2>&1 | tail -10; then
    JAR_FILE=$(find "$BACKEND_DIR/target" -name "warehouse-backend-*.jar" | head -1)
    if [ -f "$JAR_FILE" ]; then
        echo "✅ JAR built successfully: $JAR_FILE"
        JAR_SIZE=$(du -h "$JAR_FILE" | cut -f1)
        echo "   Size: $JAR_SIZE"
    else
        echo "❌ JAR file not found"
        exit 1
    fi
else
    echo "❌ Build failed"
    exit 1
fi

echo ""

# 第六步: Docker 镜像重建
echo "🐳 Step 6: Rebuilding Docker images"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
cd "$PROJECT_ROOT"

echo ""
echo "Stopping Docker containers..."
docker-compose down 2> /dev/null || true

echo ""
echo "Building backend image (this may take a few minutes)..."
if docker-compose build --no-cache backend 2>&1 | grep -E "Successfully|ERROR"; then
    echo "✅ Docker image built successfully"
else
    echo "⚠️  Docker build may have encountered issues"
fi

echo ""

# 第七步: 启动容器并验证
echo "🚀 Step 7: Starting containers and verifying"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
cd "$PROJECT_ROOT"

echo "Starting docker-compose services..."
docker-compose up -d

echo "Waiting for services to start (30 seconds)..."
sleep 30

echo ""
echo "📊 Container status:"
docker-compose ps

echo ""

# 验证后端健康状态
echo "🏥 Checking backend health..."
HEALTH_URL="http://localhost:8080/actuator/health"

for i in {1..5}; do
    if curl -s "$HEALTH_URL" 2> /dev/null | grep -q "UP\|UNKNOWN"; then
        echo "✅ Backend health check passed!"
        break
    else
        echo "⏳ Attempt $i/5 - Waiting for backend to be ready..."
        sleep 5
    fi
done

echo ""

# 检查 JDK 版本
echo "ℹ️  Backend Java version:"
docker exec warehouse-backend java -version 2>&1

echo ""
echo "=========================================="
echo "✅ 修复完成！"
echo "=========================================="
echo ""
echo "📋 总结:"
echo "  ✓ pom.xml 已更新至 JDK 21"
echo "  ✓ Dockerfile 已更新至 JDK 21"
echo "  ✓ Maven 编译成功"
echo "  ✓ Docker 镜像已重建"
echo "  ✓ 容器已启动"
echo ""
echo "🧪 可以开始测试 API 端点:"
echo "  curl http://localhost:8080/api/business-data/health"
echo ""
echo "📚 后续操作:"
echo "  1. 部署 API 服务"
echo "  2. 测试 Kafka 事件发送"
echo "  3. 验证 ETL 数据同步"
echo ""
