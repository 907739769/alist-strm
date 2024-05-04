# alist-strm
 alist生成可播放strm视频文件

## docker部署 


```
部署前参数需要修改
serverUrl  alist地址 如http://192.168.1.2:5244
serverToken 可在alist后台获取
scanPath 需要生成strm文件的目录如http://192.168.1.2:5244/阿里云分享/电影 那就填入/阿里云分享/电影
```

docker CLI安装

```
docker run -d \
--name=alist-strm \
-e TZ=Asia/Shanghai \
-e serverUrl=http://192.168.1.2:5244 \
-e serverToken=xxx \
-e scanPath=/阿里云分享/电影 \
-v /volume1/docker/alist-strm/config:/config \
-v /volume1/docker/alist-strm/data:/data \
jacksaoding/alist-strm:latest
```

docker compose安装

```
version: "3"
services:
  app:
    container_name: alist-strm
    image: 'jacksaoding/alist-strm:latest'
    network_mode: "host"
    environment:
      TZ: Asia/Shanghai
      serverUrl: http://192.168.1.2:5244
      serverToken: xxx
      scanPath: /阿里云分享/电影
    volumes:
      - /volume1/docker/alist-strm/data:/data
```
