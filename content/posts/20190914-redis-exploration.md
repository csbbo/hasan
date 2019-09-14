---
title: "Redis初探"
date: 2019-09-14T14:26:52+08:00
tags: ["Redis"]
categories: ["数据库"]
toc: true
---

Redis数据类型丰富，支持数据磁盘持久化存储，支持主从同步，支持分片。由C语言编写性能非常高按官方的数据能达到100000QPS（query per second，每秒查询次数）

## Redis数据类型

+ String: 最基本的数据类型，二进制安全（能够包含任何类型数据）其实现依靠地城的动态字符串**sdshdr**
+ Hash: string组成的字典，适合存储对象
+ List: 列表，按string元素插入顺序排序
+ Set: string组成的无序集合，通过哈希表实现，不允许重复
+ Sorted Set: 通过分数来为集合中的成员从小到大排序
+ 用于计数的HyperLogLog，用于支持存储地理位置信息的Geo

<!--more-->
```c
struct sdshdr {
    //buf中已占用空间长度
    int len;

    // buf剩余可用空间长度
    int free;

    //数据空间
    char buf[];
}
```

> Redis单个操作的是原子性的

## 为什么Redis那么快

+ 完全基于内存，绝大部分的请求是纯粹的内存操作，执行效率高
+ 数据结构简单，存储结构就是键值对类似Hash Map查找的时间复杂度为O(1)
+ 采用单线程，单线程也能处理高并发请求，多核可启动多个实例
+ 使用多路I/O复用模型（select系统调用），非阻塞I/O

> 这里的单线程是指主线程是单线程，负责I/O事件的处理，请求业务处理，过期键处理，集群协调，被封装成周期性的任务由主线程周期性的处理，对于客户端所有的读写请求都由一个主线程串行的处理，避免了频繁的上下文切换和锁竞争

## 从海量的数据中查询出某一固定前缀的Key

keys指令带来的问题:

+ keys指令一次性返回所有匹配的key
+ 键的数量过大会使服务卡顿，对内存消耗和Redis服务都是一个隐患

scan指令可以无阻塞的取出指定模式的key列表:

+ 基于游标的迭代器，需要基于上一次查询的游标延续之前的迭代过程
+ 以0作为游标开始一次新的迭代，知道命令返回游标0完成一次遍历
+ 不保证每次执行都返回某个给定数量的元素，支持模糊查询
+ 一次返回元素数量不可控，只能大概率符合count参数

## Redis分布式锁的实现

分布式锁需要解决的问题：

+ 互斥性--同一时刻只能有一个客户端获取锁
+ 安全性--锁只能被持有该锁的客户端删除
+ 死锁--服务器宕机导致无法释放锁造成死锁
+ 容错--当部分节点宕机，客户端仍能够获取锁和释放锁

setnx实现分布式锁，但setnx不支持传入expire参数，需要使用expire指令设置过期时间释放锁。缺点是该操作不是原子性的，setnx完后服务器挂掉来不及expire释放锁会导致资源一直得不到释放

Redis >= 2.6.12版本开始可以通过set操作设置过期时间，指令格式如下：
SET key value [EX seconds] [PX milliseconds] [NX|XX]

+ EX seconds:设置过期时间为seconds秒
+ PX milliseconds:设置过期时间为milliseconds秒
+ NX: 只有键不存在时，才对键进行设置
+ XX: 只有键存在时，才对键进行设置
+ SET操作成功时返回OK，否则返回nil

> 当大量的key集中过期，由于清除key需要时间，会导致Redis出现卡顿现象，因此在设置key过期时间时候因给每个key加上随机值

## Redis异步队列

使用List作为队列，rpush生产消息，lpop消费消息

缺点：不等待有消息才去消费
解决方案：1.应用层sleep机制重试 2.blpop key timeout阻塞直到队列有消息或者超时

List作为队列当消息被pop后就没了，只能提供一个消费者消费

pub/sub:主题订阅模式实现一对多的异步队列，发送者(pub)发送消息，订阅者(sub)接收消息，订阅者可以订阅任意数量的频道

缺点：消息发布时无状态的，无法保证消息到达

## Redis持久化

**RDB(快照)持久化：保存某个时间点的全量数据快照**

主动触发RDB持久化:

+ SAVE:阻塞Redis的服务进程，直到RDB文件被创建完毕
+ BGSAVE:Fork出一个子进程来创建RDB文件，不阻塞服务器进程

自动触发RDB的情况:

+ 根据redis.conf配置里的SAVE m n定时触发，使用的是（bgsave）
+ 主从复制时，主节点自动触发
+ 执行Debug Reload
+ 执行Shutdown且没有开启AOF持久化

缺点:

+ 内存数据全量同步，数据量大导致大量的I/O影响性能
+ 可能会因为Redis挂掉而丢失从当前到最近一次快照之间的数据

BGSAVE原理:

BGSAVE会先检查是否存在AOF、RDB子进程正在运行，存在则返回错误，没有的话就触发持久化，进行系统调用fork()创建子进程，实现了Copy-on-Write

**AOF(Append-Only-File)持久化:保存写状态**

+ 记录下除查询以外的所有变更数据库状态的指令
+ 以append形式追加保存到AOF文件中(增量)

日志重写解决AOF文件不断增大问题

**RDB和AOF文件共存情况的恢复流程**(只需重新启动Redis服务)

首先检查是否存在AOF文件，存在则加载AOF文件，启动成功；当AOF不存在检查是否存在RDB文件，存在则加载RDB文件启动成功；都不存在启动失败。

**RDB和AOF优缺点**

RDB优点:全量数据快照，文件小，恢复快
RDB缺点:无法保存最近一次快照之后的数据
AOF优点:可读性高，适合保存增量数据，数据不易丢失
AOF缺点:文件体积大，恢复时间长

**RDB-AOF混合持久化**(Redis >= 4.0)

实际中AOF文件前半段是RDB格式的全量持久化数据，后半段是redis命令格式的增量数据

## Pipeline及主从同步

**Pipeline**:

+ Pipeline和Linux管道类似
+ Redis基于请求响应模型，当个请求需要一一应答
+ Pipeline批量执行指令能够节省多次IO往返时间
+ 有顺序依赖的指令还是建议分批发送

**主从同步**

全同步过程

+ Slave发送sync命令到Master
+ Master启动一个后台进程，将Redis中的数据快照保存到文件中
+ Master将保存数据快照期间接收到的写命令缓存起来
+ Master完成写文件操作后将该文件发送给Slave
+ 使用新的AOF文件替换掉旧的AOF文件
+ Master将这期间收集到的增量写命令发送给Slave端

> 完成同步后所有写操作在Master上进行，所有读操作在Slave上进行

增量同步过程

+ Master接收到用户的操作指令，判断是否要传播到Slave
+ 将操作记录追加到AOF文件
+ 将操作传播到其他Slave：1、对齐从库 2、往相应缓存写入指令
+ 将缓存中的数据发送给Slave

Redis Sentinel

解决主从同步Master宕机后的主从切换问题

## Redis集群

如何从海量数据中快速找到所需

+ 分片：按照某种规则去划分数据，分散存储在多个节点
+ 常规的按照哈希算法划分无法实现节点的动态增减

一致性哈希算法：对2^32取模，将哈希值空间组织成虚拟的圆环

将各个主机进行哈希变换到圆环上，数据的key也使用相同的哈希函数计算出哈细值，然后顺时针方向将数据存储到距离最近的主机上


## 附录
```redis
String:
set name "redis"
get name
set count 1
incr count

Hash:
hmset lilei name "lilei" age 22
hget lilei name

List:
lpush mylist aaa
lrange mylist 0 10

Set:
sadd myset abc
smembers myset

Sorted Set:
zadd myzset 1 abc
zrangebyscore myzset 0 10

dbsize
keys p*

scan cursor [MATCH pattern] [COUNT count]

setnx locknx test
expire locknx

set locktarget aaa ex 10 nx

rpush testlist aaa
lpop testlist

subscribe topic
publish topic "hello"

save
lastsave
bgsave
lastsave
```