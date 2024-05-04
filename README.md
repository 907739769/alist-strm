# alist-strm
 alist生成可播放strm视频文件

## docker部署 

配置文件`application.properties`放到`/volume1/docker/alist-strm/config/`目录下

```
#alist地址 如http://192.168.1.2:5244
alist.server.url=
#alist地址 可在alist后台获取
alist.server.token=
#需要生成strm文件的目录如http://192.168.1.2:5244/阿里云分享/电影 那就填入/阿里云分享/电影
alist.server.path=
#生成strm文件的存放地址
output.dir=
```

docker CLI安装

```
docker run -d \
--name=alist-strm \
-e TZ=Asia/Shanghai \
-v /volume1/docker/alist-strm/config:/config \
-v /volume1/docker/alist-strm/data:/data \
--restart unless-stopped \
jacksaoding/alist-strm:latest
```

docker compose安装

```
version: "3"
services:
  app:
    container_name: alist-strm
    image: 'jacksaoding/alist-strm:latest'
    restart: unless-stopped
    network_mode: "host"
    environment:
      TZ: Asia/Shanghai
    volumes:
      - /volume1/docker/alist-strm/config:/config
      - /volume1/docker/alist-strm/data:/data
```
