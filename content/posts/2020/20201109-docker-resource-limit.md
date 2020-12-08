---
title: "Docker资源限制"
date: 2020-11-09T14:39:20+08:00
categories: ["Docker"]
tags: ["Docker"]
toc: true
---

一个Docker宿主机上回运行若干个容器，每个容器都会消耗宿主机的CPU、内存和IO资源，对于KVM、VMWare等虚拟化技术，用户可以控制分配多少CPU、内存资源给每个虚拟机。Docker也提供了类似的机制避免某个容器因占用过多的资源从而影响其他容器乃至宿主机的性能

<!--more-->

### 内存限制

```shell
docker run -it --name limit_ubuntu --memory 1g ubuntu:18.04
```

通过`docker stats`可以查看到容器的内存限制
> 单位(b, k, m, g)


### cpu限制

- 使用`htop`监视主机CPU, 安装
```shell
sudo apt install htop -y
```

- 运行容器并设置`--cpus=1.0`
```shell
sudo docker run -it --name limit_ubuntu --cpus=1.0 ubuntu:18.04
```

- 进入容器将cpu拉满
```shell
for i in `seq 1 $(cat /proc/cpuinfo |grep "physical id" |wc -l)`; do dd if=/dev/zero of=/dev/null & done
```

<img src="/assets/2020/1109/htop-1hostcpu-100load.png"/>

> 可以看到docker容器使用等同于1个cpu的能力去拉满时，由于主机有4cpu，所以每个cpu的负载为25%

### 进程限制

- 运行容器并设置`--pids-limit 1024`
```shell
docker run -it --name limit_ubuntu --pids-limit 1024 ubuntu:18.04
```

- fork炸弹
```shell
:(){ :|:& };:
```
> 通过`docker stats limit_ubuntu`可以看到容器中的进程数无法超过1024
### ulimit限制

- 运行容器并设置`--ulimit nofile=20480:40960 --ulimit nproc=1024:2048`
```shell
docker run -it --name limit_ubuntu --ulimit nofile=20480:40960 --ulimit nproc=1024:2048 ubuntu:18.04
```


- ulimit所有参数命令
```shell
"core":       RLIMIT_CORE,                                            
"cpu":        RLIMIT_CPU,                       
"data":       RLIMIT_DATA,                      
"fsize":      RLIMIT_FSIZE,
"locks":      RLIMIT_LOCKS,
"memlock":    RLIMIT_MEMLOCK,
"msgqueue":   RLIMIT_MSGQUEUE,
"nice":       RLIMIT_NICE,
"nofile":     RLIMIT_NOFILE,
"nproc":      RLIMIT_NPROC,
"rss":        RLIMIT_RSS,
"rtprio":     RLIMIT_RTPRIO,
"rttime":     RLIMIT_RTTIME,
"sigpending": RLIMIT_SIGPENDING,
"stack":      RLIMIT_STACK,
```

> soft:hard soft警告设定，超过则警告。hard严格设定，不能超过该值

更多操作请参考`docker run --help`

[参考]

[Docker SDK for Python](https://docker-py.readthedocs.io/en/stable/containers.html)  
[Placing limits on cpu usage in containers](https://fabianlee.org/2020/01/19/docker-placing-limits-on-cpu-usage-in-containers/)  
[Docker1.6新特性初体验](http://dockone.io/article/302)  
[Docker底层实现](https://yeasy.gitbook.io/docker_practice/underly)