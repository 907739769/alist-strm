# alist-strm
 alist生成可播放strm视频文件

## docker部署 


```
部署前参数需要修改
alistServerUrl  alist地址 如http://192.168.1.2:5244
alistServerToken 可在alist后台获取
alistScanPath 需要生成strm文件的目录如http://192.168.1.2:5244/阿里云分享/电影 那就填入/阿里云分享/电影
```

docker CLI安装

```
docker run -d \
--name=alist-strm \
-e TZ=Asia/Shanghai \
-e alistServerUrl=http://192.168.1.2:5244 \
-e alistServerToken=xxx \
-e alistScanPath='/阿里云分享/电影' \
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
      alistServerUrl: http://192.168.1.2:5244
      alistServerToken: xxx
      alistScanPath: /阿里云分享/电影
    volumes:
      - /volume1/docker/alist-strm/data:/data
```
