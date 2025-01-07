FROM eclipse-temurin:8u412-b08-jre-jammy
LABEL title="alist-strm"
LABEL description="将alist的视频文件生成媒体播放设备可播放的strm文件"
LABEL authors="JackDing"
COPY ./target/application.jar /aliststrm.jar
VOLUME /data
VOLUME /log
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
ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS -XX:+UseG1GC -XX:+OptimizeStringConcat -XX:+PrintGCDetails -Xloggc:/log/gc.log -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCApplicationConcurrentTime -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/log /aliststrm.jar"]