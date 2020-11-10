---
title: "Docker资源限制"
date: 2020-11-09T14:39:20+08:00
categories: ["Docker"]
tags: ["Docker"]
toc: true
---

一个Docker宿主机上回运行若干个容器，每个容器都会消耗宿主机的CPU、内存和IO资源，对于KVM、VMWare等虚拟化技术，用户可以控制分配多少CPU、内存资源给每个虚拟机。Docker也提供了类似的机制避免某个容器因占用过多的资源从而影响其他容器乃至宿主机的性能

<!--more-->

### docker-stress

docker-stress是一个可以用于压力测试的Docker容器，可以生成对CPU、内存和磁盘I/O的压力

```shell
docker run --rm -it progrium/stress --cpu 2 --io 1 --vm 2 --vm-bytes 128M --timeout 10s
```

- `--vm` 工作线程数
- `--vm-bytes` 每个线程分配的内存
- `--cpu` cpu数量，与宿主机数量一样可以将cpu压满

### 内存限额

- \-m设置内存限额
- \--memory-swap 设置交换分区限额

```shell
docker run -m 200M --memory-swap=300M ubuntu
```

使用`progrium/stress`进行压力测试
```shell
docker run -it -m 200M --memory-swap=300M progrium/stress --vm 1 --vm-bytes 208M
```

如果分配的内存超过限额，stress线程报错误容器退出

### CPU限额

默认设置下，所有容器可以平等地使用CPU资源并且没有限制

Docker可以通过-c或-pu-shares设置容器使用CPU的权重。如果不指定，默认值为1024。
与内存限额不同，通过-c设置的cpu share 并不是CPU资源的绝对数量，而是一个相对的权重值。某个容器最终能分配到的CPU资源取决于它的cpu share占所有容器cpu share总和的比例。
换句话说:通过cpu share可以设置容器使用CPU的优先级。

```shell
docker run --name "cont_A" -c 1024 ubuntu docker run --name "cont_B" -c 512 ubuntu
```