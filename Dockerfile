FROM eclipse-temurin:8u412-b08-jre-jammy

LABEL title="alist-strm"
LABEL description="将alist的视频文件生成媒体播放设备可播放的strm文件"
LABEL authors="JackDing"

# 安装必要工具 (gosu)
RUN apt-get update && apt-get install -y gosu && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 复制应用程序文件
COPY ./target/application.jar /app/aliststrm.jar
COPY --chmod=755 entrypoint.sh /entrypoint.sh

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
ENV PUID=0
ENV PGID=0
ENV UMASK=022

# 设置启动命令
ENTRYPOINT [ "/entrypoint.sh" ]

# 设置卷
VOLUME /data
VOLUME /log
