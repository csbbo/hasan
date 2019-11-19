---
title: "Docker新手入门"
date: 2019-11-19T14:43:10+08:00
categories: ["Docker", "新手入门"]
tags: ["Docker"]
toc: true
---

Docker 属于 Linux 容器的一种封装，提供简单易用的容器使用接口。它是目前最流行的 Linux 容器解决方案。

Docker 将应用程序与该程序的依赖，打包在一个文件里面。运行这个文件，就会生成一个虚拟容器。程序在这个虚拟容器里运行，就好像在真实的物理机上运行一样。有了 Docker，就不用担心环境问题。
<!--more-->
### Docker 的用途

+ （1）提供一次性的环境。比如，本地测试他人的软件、持续集成的时候提供单元测试和构建的环境。
+ （2）提供弹性的云服务。因为 Docker 容器可以随开随关，很适合动态扩容和缩容。
+ （3）组建微服务架构。通过多个容器，一台机器可以跑多个服务，因此在本机就可以模拟出微服务架构。

### Docker的安装

Docker CE 的安装请参考官方文档。

+ [Mac](https://docs.docker.com/docker-for-mac/install/)
+ [Windows](https://docs.docker.com/docker-for-windows/install/)
+ [Ubuntu](https://docs.docker.com/install/linux/docker-ce/ubuntu/)
+ [Debian](https://docs.docker.com/install/linux/docker-ce/debian/)
+ [CentOS](https://docs.docker.com/install/linux/docker-ce/centos/)
+ [Fedora](https://docs.docker.com/install/linux/docker-ce/fedora/)
+ [其他 Linux 发行版](https://docs.docker.com/install/linux/docker-ce/binaries/)

验证安装成功

```docker
docker version
docker info
```

Docker 需要用户具有 sudo 权限，为了避免每次命令都输入sudo，可以把用户加入 Docker 用户组

```docker
sudo usermod -aG docker $USER
```

命令行启动

```docker
service启动
sudo service docker start
systemctl启动
sudo systemctl start docker
```

### image文件

Docker 把应用程序及其依赖，打包在 image 文件里面。只有通过这个文件，才能生成 Docker 容器。image 文件可以看作是容器的模板。Docker 根据 image 文件生成容器的实例。同一个 image 文件，可以生成多个同时运行的容器实例。

列出所有image文件
```docker
docker image ls
```

删除image文件,强制删除加`-f`参数
```docker
docker image rm [imagename]
```

简单实例:

获取hello-world image
```docker
docker image pull hello-world
```
运行这个image
```docker
docker container run hello-world
```
> `docker container run`命令具有自动抓取 image 文件的功能。如果发现本地没有指定的 image 文件，就会从仓库自动抓取。因此，前面的`docker image pull`命令并不是必需的步骤。

删除所有image
```docker
docker image rm $(docker image ls -q)
```

### 容器文件

image 文件生成的容器实例，本身也是一个文件，称为容器文件。也就是说，一旦容器生成，就会同时存在两个文件： image 文件和容器文件。而且关闭容器并不会删除容器文件，只是容器停止运行而已。

列出正在运行的容器
```docker
docker container ls
```
列出所有容器
```docker
docker container ls --all
```

终止容器
```docker
docker container kill
```

容器启动、停止、重启
```docker
docker start [containername]
docker stop [containername]
docker restart [containername]
```

进入一个正在运行的docker容器
```docker
docker exec -it [containername] bash
```

查看 docker 容器的输出
```docker
docker container logs [containerID]
```
将文件复制到容器里
```docker
docker cp file [containername]:/path/to/file
```

> 上面的情况外面的file会替换掉容器里的file

可以利用shell组合使用,停止和删除所有容器
```docker
停止
docker stop $(docker container ls -q)
删除
docker container rm $(docker container ls -aq)
```

### Dockerfile制作image

Dockerfile 是一个文本文件，其内包含了一条条的 指令(Instruction)，每一条指令构建一层，各层叠加得到最后的镜像，因此每一条指令的内容，就是描述该层应当如何构建。

在项目的根目录下，新建一个.dockerfile文本文件
```docker
resources/
*.md
docker-compose.yml
Dockerfile
.git
```
上面的文件和路径将不会被打包进image

创建一个Dockerfile文件
```docker
FROM ubuntu:18.04

COPY requirements.txt /tmp/requirements.txt
RUN apt-get update &&\
    apt-get install -y gcc libpq-dev vim curl git unzip&&\
    pip3 install -r /tmp/requirements.txt &&\
    rm /tmp/requirements.txt &&\
    apt-get autoremove -y gcc git

ADD ./src/training_platform /web

WORKDIR /web

RUN ln -sfv /web/training_platform/settings.py.pro /web/training_platform/settings.py

CMD bash /web/run.sh
```

+ FROM 指定基础镜像，定制镜像就是就是在一个镜像的基础上对其修改，因此FROM是Dockerfile中必备的指令，并且是第一条指令
+ RUN 执行命令行命令，RUN有两种格式1.shell格式`RUN <command>`2.exec格式`RUN ["可执行文件","参数1","参数2"]`
+ COPY 复制文件同RUN一样有两种格式，COPY <源路径> <目标路径>,<源路径>可以是多个也可以是通配符,<目标路径>不存在时自动创建，可以使容器内绝对路径也可以是相对于工作目录的相对路径
+ ADD 在COPY的基础上增加了一些功能，比如 <源路径> 可以是一个 URL，如果 <源路径> 为一个 tar 压缩文件的话，压缩格式为 gzip, bzip2 以及 xz 的情况下，ADD 指令将会自动解压缩这个压缩文件到 <目标路径> 去。
+ WORK 指定工作目录
+ CMD 指定默认的容器主进程的启动命令，需要注意的是容器中的应用都应该以前台执行

创建image文件
```docker
docker build -t [imagename]:[tag] .
```

> `-t`指定image文件的名字，后面还可以用冒号指定标签，默认标签是`latest`,最后的点表示上下文路径

生成容器
```docker
docker container run -p 8000:3000 -it koa-demo:0.0.1 /bin/bash
```

+ -p参数：表示容器3000端口映射到本机的8000端口
+ /bin/bash：容器启动后内部第一个执行的命令

### Docker Compose

Compose 项目是 Docker 官方的开源项目，负责实现对 Docker 容器集群的快速编排。Compose 定位是 「定义和运行多个 Docker 容器的应用（Defining and running multi-container Docker applications）」

Compose 中有两个重要的概念：

+ 服务 (service)：一个应用的容器，实际上可以包括若干运行相同镜像的容器实例。
+ 项目 (project)：由一组关联的应用容器组成的一个完整业务单元，在 docker-compose.yml 文件中定义。

Compose 的默认管理对象是项目，通过子命令对项目中的一组容器进行便捷地生命周期管理。

#### 安装与卸载

Docker Desktop for Mac/Windows 自带 docker-compose 二进制文件，安装 Docker 之后可以直接使用。

Linux上直接下载编译好的二进制文件即可

```docker
sudo curl -L https://github.com/docker/compose/releases/download/1.24.1/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose
sudo curl -L https://github.com/docker/compose/releases/download/1.24.1/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose
```

Compose 项目由 Python 编写，可以用pip安装

```docker
sudo pip install -U docker-compose
```

bash补全命令

```docker
$ curl -L https://raw.githubusercontent.com/docker/compose/1.24.1/contrib/completion/bash/docker-compose > /etc/bash_completion.d/docker-compose
```

卸载

二进制包方式安装，删除二进制文件即可
```docker
sudo rm /usr/local/bin/docker-compose
```

通过pip安装则执行uninstall卸载
```docker
sudo pip uninstall docker-compose
```

#### Compose 模板文件

```docker
version: "3"
services:
  postgres:
    image: postgres:11.0-alpine
    container_name: tp_postgres
    restart: always
    ports:
      - "5432:5432/tcp"
    environment:
      POSTGRES_DB: trainingplatform
      POSTGRES_PASSWORD: "trainingplatform"
      POSTGRES_USER: trainingplatform
    volumes:
      - "./data:/var/lib/postgresql/data"
  server:
    image: tp_server:latest
    container_name: tp_server
    restart: always
    depends_on:
      - postgres
    volumes:
      - "./superuser:/web/superuser"
    healthcheck:
      test: "curl -fs http://127.0.0.1:8000/api/CSRFTokenAPI || exit 1"
      interval: 60s
      timeout: 10s
```

每个服务都必须通过 image 指令指定镜像或 build 指令（需要 Dockerfile）等来自动构建生成镜像。

如果使用 build 指令，在 Dockerfile 中设置的选项(例如：CMD, EXPOSE, VOLUME, ENV 等) 将会自动被获取，无需在 docker-compose.yml 中重复设置。


[参考]

[Docker 入门教程](https://www.ruanyifeng.com/blog/2018/02/docker-tutorial.html)  
[Docker -- 从入门到实践](https://yeasy.gitbooks.io/docker_practice/content/)