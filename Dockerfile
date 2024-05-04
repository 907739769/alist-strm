FROM eclipse-temurin:8u342-b07-jre-jammy
LABEL title="alist-strm"
LABEL description="将alist的视频文件生成媒体播放设备可播放的strm文件"
LABEL authors="JackDing"
COPY ./target/application.jar /application.jar
VOLUME /data
ENV TZ=Asia/Shanghai
ENV serverUrl=""
ENV serverToken=""
ENV scanPath=""
ENTRYPOINT ["java", "-jar", "--alist.server.url=$serverUrl --alist.server.token=$serverToken --alist.server.path=$scanPath","/application.jar"]
