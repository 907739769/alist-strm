FROM eclipse-temurin:8u412-b08-jre-jammy

LABEL title="alist-strm"
LABEL description="将alist的视频文件生成媒体播放设备可播放的strm文件"
LABEL authors="JackDing"

# 安装必要工具 (gosu)
RUN apt-get update && apt-get install -y gosu && rm -rf /var/lib/apt/lists/*

# 设置默认的 PUID 和 PGID
ARG DEFAULT_PUID=1000
ARG DEFAULT_PGID=1000

# 创建一个新的用户和组
RUN groupadd -g ${DEFAULT_PGID} appgroup && \
    useradd -u ${DEFAULT_PUID} -g appgroup -m appuser

# 创建 /app 目录并复制 aliststrm.jar 到 /app
RUN mkdir -p /app
COPY ./target/application.jar /app/aliststrm.jar

# 创建 /data 和 /log 目录
RUN mkdir -p /data /log

# 修改文件和目录的权限
RUN chown -R appuser:appgroup /app /data /log

# 设置卷
VOLUME /data
VOLUME /log

# 环境变量
ENV TZ=Asia/Shanghai
ENV alistServerUrl=""
ENV alistServerToken=""
ENV alistScanPath=""
ENV isDownSub="0"
ENV slowMode=""
ENV encode="1"
ENV tgToken=""
ENV tgUserId=""
ENV JAVA_OPTS="-Xms32m -Xmx512m"
ENV srcDir=""
ENV dstDir=""
ENV replaceDir=""
ENV runAfterStartup="1"
ENV minFileSize="100"
ENV logLevel=""
ENV maxIdleConnections="5"
ENV refresh="1"
ENV scheduledCron="0 0 6,18 * * ?"

# 切换到非 root 用户
USER appuser

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -XX:+UseG1GC -XX:+OptimizeStringConcat -XX:+PrintGCDetails -Xloggc:/log/gc.log -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCApplicationConcurrentTime -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/log -jar /app/aliststrm.jar"]
