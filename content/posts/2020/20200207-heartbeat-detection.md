---
title: "分布式系统心跳检查"
date: 2020-02-07T23:40:02+08:00
categories: ["Python"]
tags: ["分布式","心跳检查"]
toc: true
---

在分布式系统中经常使用心跳(Heartbeat)来检测Server的健康状况，传统的检测方法是设定一个超时时间T，只要在T之内没有接收到对方的心跳包便认为对方宕机，方法简单粗暴，但使用广泛。从理论上来说，心跳无法真正检测对方是否crash，主要困难在于无法真正区别对方是宕机还是“慢”。

<!--more-->

### 传统错误检测存在的缺陷

如上所述，在传统方式下，目标主机会每间隔t秒发起心跳，而接收方采用超时时间T(t<T)来判断目标是否宕机，接收方首先要非常清楚目标的心跳规律（周期为t的间隔）才能正确设定一个超时时间T，而T的选择依赖当前网络状况、目标主机的处理能力等很多不确定因素，因此在实际中往往会通过测试或估计的方式为T赋一个上限值。上限值设置过大，会导致判断“迟缓”，但会增大判断的正确性；过小，会提高判断效率，但会增加误判的可能性。但下面几种场景不能使用传统检测方法：

1. Gossip通信

    但在实际应用中，比如基于Gossip通信应用中，因为随机通信，两个Server之间并不存在有规律的心跳，因此很难找到一个适合的超时时间T，除非把T设置的非常大，但这样检测过程就会“迟缓”的无法忍受。

2. 网络负载动态变化

    还有一种情况是，随着网路负载的加大，Server心跳的接收时间可能会大于上限值T；但当网络压力减少时，心跳接收时间又会小于T，如果用一成不变的T来反映心跳状况，则会造成判断”迟缓“或误判。

3. 心跳检测与结果的分离

    并不是每个应用都只需要知道一个目标主机宕机与否的结果（true/false），即有很多应用需要自己解释心跳结果从而采取不同的处理动作。比如，如果目标主机3s内没有心跳，应用A解读为宕机并重试；而应用B则解读为目标”不活跃“，需要把任务委派到其他Server。

    也就是说，目标主机是否“宕机”应该由业务逻辑决定的，而不是简单的通过一个超时时间T决定，这就需要把心跳检测过程与对结果的解释相分离，从而为应用提供更好的灵活性。

### 对传统错误检测的改造

很多人已经意识到了传统检测的缺点，因此提出了各种解决方案，这些方案的重点都放在如何改造超时时间T上，基本原理是在运行时根据前几次心跳时间动态估计下一次的心跳时间。而估计的方法大多通过采用随机抽样寻找心跳时间与网络波动的之间的关系曲线，这些方法有效地弥补了传统检测方法的缺陷，但解决的最贴近实际的还是φ失败检测算法，下面着重介绍这种算法。

### 概率密度函数心跳检测算法

下面介绍两种基于概率密度函数的心跳检测算法。这两种算法都是采用了在一段时间内采集到得一些心跳间隔时间的样本来进行概率计算，类似于一个队列，有最新的心跳过来的时候会进入队列作为算法样本，样本数值为当前过来的心跳于上一次心跳的时间间隔，队列会有最大长度，满了就会把对早的样本踢出样本队列，所以队列窗口保存了最近N个数据样本。通过对样本队列中的样本进行概率计算，最终得出不会再有心跳的概率。

#### 基于正态分布的算法

像这样基于正态分布的失败检测算法也被称为φ失败检测算法，φ失败检测算法认为心跳时间间隔符合正态分布：

![](/assets/2020/0207/4ec2d5628535e5dd25ea56d576c6a7efcf1b62f2.jpg)

> 其中 **σ(sigma)** 是标准偏差， **μ(miu)** 代表样本队列的平均值，**x** 则代表随即变量，我们可以把 **x** 认为是需要进行概率计算的输入参数。那整个函数则表达了正态分布的几何函数图。函数图以样本数据为横坐标，纵坐标为正态分布的函数值。

而对此函数进行积分，则认为其结果是时间间隔下的心跳接收概率，其积分如下：

<!-- ![](/assets/2020/0207/8aa278c4eec28aad4de0efc9da5a0ca1.png) -->
<img src="/assets/2020/0207/8aa278c4eec28aad4de0efc9da5a0ca1.png" style="margin-left:0px">

所以，如果在[x,+∞)上积分则认为是时间间隔下心跳无法接收的概率：

<!-- ![](/assets/2020/0207/0_1308188128rKpN.gif) -->
<img src="/assets/2020/0207/0_1308188128rKpN.gif" style="margin-left:0px">

而φ采用下面公式计算：

<!-- ![](/assets/2020/0207/0_1308188613XtsX.gif) -->
<img src="/assets/2020/0207/0_1308188613XtsX.gif" style="margin-left:0px">

> 此时计算出的φ是一个数字，是从(tnow-Tlast)到+∞的积分，并取以10为底的对数并转换为正数，其含义是：假如在时间tnow判断目标宕机，则误判的可能性为P=G(tnow-Tlater)，即心跳在tnow之后到达的概率。

*

> 则，P越大说明误判的概率越大，因为0<P<1，所以P是一个小数,实际结果会是一个很小的小数 ，为表达方便取其对数log(P)，又因为0<P<1，故log(P)<0，因此定义φ=-log(P)，为一正数。

现在从概率P推导不同φ之间的关系：

+ P1、P2为不同时间点的概率，且P2 > P1
+ φ1、φ2为对于的φ值（对数取反）

*P2>P1* => *log(P2) > log(P1)* => *-log(P2) < -log(P1)* => *φ2 < φ1*

> 一般设定Φ为临界值，φ为根据当前时间计算出的值，如果φ>Φ，即误判的可能性小于设定的临界值，则认为目标主机已经宕机。

*

> 因为对数的关系，Φ=1，则认为误判的可能性不能超过10%，Φ=2，则为1%，Φ=3，则为0.1%...

*

> 而正态分布中的σ、μ参数需要根据当前的随机样本动态计算，因为随机样本随网络状况的动态变化，正态分布函数也就随之变化，从而心跳时间也发生变化，但我们只要设定一个误判率（比如Φ=3）则能保证正确的结果。这是传统的基于超时时间T无法做到的。

#### 基于指数分布的算法

上面提到的正态分布是一个[概率密度函数](https://zh.wikipedia.org/wiki/%E6%A9%9F%E7%8E%87%E5%AF%86%E5%BA%A6%E5%87%BD%E6%95%B8)，当然也可以构造自己的密度函数，但要满足很多性质，具体请参考：概率密度函数。在实际中，连续时间随机分布的概率密度函数一般采用指数分布函数，而不会采用正态分布，当然正态分布函数因为不可积，其积分也很难计算。

*指数分布的概率密度函数：*

<div style="display: flex; flex-direction: row; border:1px solid black">
<!-- ![](/assets/2020/0207/0_1308203009IGII.gif) -->
<img src="/assets/2020/0207/0_1308203009IGII.gif">
<!-- ![](/assets/2020/0207/0_1308203033cs8U.gif) -->
<img src="/assets/2020/0207/0_1308203033cs8U.gif">
</div>

*对应的累积函数：*

<div style="display: flex; flex-direction: row; border:1px solid black">
<!-- ![](/assets/2020/0207/0_1308203029z97X.gif) -->
<img src="/assets/2020/0207/0_1308203029z97X.gif">
<!-- ![](/assets/2020/0207/0_1308203037lN6F.gif) -->
<img src="/assets/2020/0207/0_1308203037lN6F.gif">
</div>


和上面的算法一样，同样采用滑动窗口采样样本数据的方式。x是随机变量，在这里代表当前时间和最后一次心跳到达时间的间隔。λ是率参数我们这里取值 1/样本平均数。

P_later(t) = 1 - F(t)， F(t)是累计分布函数，上正态分布中使用正太累积分布函数，在这里使用的是指数分布累积函数。最终应该计算
-log10(P_later(t))的值，也就是-log10(1-(1-e^(-λx)))。通过计算也就简化成 
φ(x) = (xλ) / ln(10)。

> 最终的计算结果为节点失效的一个误判概率值。值越大，误判的几率越小。Cassandra里面设置为8。

### Python实现

*server.py*

```python
#! /usr/bin/python3
# -*- coding:utf8 -*-

import socket
import time
import threading
import random
import math

Slave = {}
SLID_WINDOW_LENGTH = 8
HOST = '127.0.0.1'
PORT = 8888

class SlidWindow:
    def __init__(self, length):
        self.laster_time = 0
        self.queue = []
        self.length = length

    def add(self, now_time):
        if self.laster_time != 0:
            if self.is_full():
                self.queue.pop(0)
            self.queue.append(now_time - self.laster_time)

        self.laster_time = now_time

    def is_full(self):
        return len(self.queue) == self.length

    def is_empty(self):
        return not bool(self.queue)

    def get_mean(self):
        if self.is_empty():
            return 0
        return sum(self.queue) / len(self.queue)

    def get_laster_time(self):
        return self.laster_time

def randomUUID():
    u = [random.randint(0, 255) for dummy in range(0, 16)]
    return "-".join(["%02x" * 4, "%02x" * 2, "%02x" * 2, "%02x" * 2, "%02x" * 6]) % tuple(u)


def phi_accumulative_alg(variable, lambda_arg):
    return (variable / lambda_arg) / math.log(10) # 默认以e为底

def judge_threshold(error_rate):
    return -1 * math.log(error_rate, 10)

def check_hart(delay):
    global Slave
    while True:
        for uuid in list(Slave.keys()):
            if Slave[uuid].is_empty():
                continue
            if phi_accumulative_alg(time.time() - Slave[uuid].get_laster_time(), Slave[uuid].get_mean()) < judge_threshold(0.1):
                print('Slave %s 正常' % uuid)
            else:
                print('Slave %s 被判定宕机' % uuid)
                del Slave[uuid]
        time.sleep(delay)

def get_hart(sock, addr):
    global Slave
    # print('Accept new connection from %s:%s...' % addr)
    uuid = sock.recv(1024).decode()
    now_time = time.time()
    
    if uuid not in Slave:
        uuid = randomUUID()
        print('Slave %s 加入网络' % uuid)
        sock.sendall(uuid.encode())
        Slave[uuid] = SlidWindow(SLID_WINDOW_LENGTH)
        
    Slave[uuid].add(now_time)

    sock.close()


def server(host, port):
    global Slave
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.bind((host, port))
    s.listen(5)
    while True:
        sock, addr = s.accept()
        t = threading.Thread(target=get_hart, args=(sock, addr))
        t.start()


if __name__ == '__main__':
    t1 = threading.Thread(target=server, args=(HOST, PORT))
    t2 = threading.Thread(target=check_hart, args=(1,))
    t1.start()
    t2.start()
```

*client.py*

```python
#! /usr/bin/python3
# -*- coding:utf8 -*-

import socket
import time

HOST = '127.0.0.1'
PORT = 8888
UUID = b'0'

def send_hart(host, port, delay):
    global UUID
    while True:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            s.connect((host, port))
            s.sendall(UUID)

            recv_uuid = s.recv(1024)
            if bool(recv_uuid) and UUID != recv_uuid:
                UUID = recv_uuid
                print(UUID)
        except:
            print('connect fail!')
            break

        time.sleep(delay)
 
 
if __name__ == '__main__':
    try:
        send_hart(HOST, PORT, 1)
    except KeyboardInterrupt:
        print('Slave exit!')

```

[参考]

[分布式系统中的节点失效算法](https://www.cnblogs.com/haoxinyue/archive/2013/01/15/2861395.html)  
[φ累积失败检测算法](https://blog.csdn.net/chen77716/article/details/6541968)