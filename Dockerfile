FROM eclipse-temurin:8u342-b07-jre-jammy
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
ENV slowMode=""
ENV encode="1"
ENV tgToken=""
ENV tgUserId=""
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS -XX:+OptimizeStringConcat -XX:+PrintGCDetails -Xloggc:/log/gc.log  -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/log /aliststrm.jar"]