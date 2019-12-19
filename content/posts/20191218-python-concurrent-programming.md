---
title: "Python 并发编程"
date: 2019-12-18T19:28:13+08:00
categories: ["Python"]
tags: ["nil"]
toc: true
---


对于并发编程, Python有多种长期支持的方法, 包括多线程, 调用子进程, 以及各种各样的关于生成器函数的技巧。
<!--more-->
### 启动与停止线程

`threading` 库可以在单独的线程中执行任何的在 Python 中可以调用的对象。你可以创建一个 Thread 对象并将你要执行的对象以 `target` 参数的形式提供给该对象。

```python
import time
from threading import Thread

def countdown(n):
    while n > 0:
        print('T-minus', n)
        n -= 1
        time.sleep(5)

t = Thread(target=countdown, args=(10,))
t.start()
```

> 创建好一个线程对象后需要调用 `start()` 方法才会去执行，python中的线程会在一个单独的系统级线程中执行由操作系统全权管理。`join()`方法可以将一个线程加入到当前线程并等待它终止。

Python解释器直到所有线程都终止前仍保持运行，对于需要长时间运行的线程或者是需要一直运行的后台任务，应该考虑后台线程。

```python
t = Thread(target=countdown, args=(10,), daemon=True)
t.start()
```

> 由于全局解释器锁（GIL）的原因，Python的进程被限制到同一时刻只允许一个线程执行,所以python的线程更适用于处理I/O和其他需要并发执行的阻塞操作（如等待I/O、网络、数据库操作），而不适用需要多处理器并行技术的计算密集型任务。


### 线程间通信

从一个线程向另一个线程发送数据最安全的方式可能就是使用 queue 库中的队列了。创建一个被多个线程共享的 Queue 对象，这些线程通过使用 put() 和 get() 操作来向队列中添加或者删除元素。

```python
from queue import Queue
from threading import Thread

_sentinel = object()

def producer(out_q):
    data = 0
    for i in range(10):
        data += 1
        out_q.put(data)
        print('producer put %s' % data)
    out_q.put(_sentinel)

def consumer1(in_q):
    while True:
        data = in_q.get()
        if data is _sentinel:
            in_q.put(_sentinel)
            break
        print('consumer1 get %s' % data)

def consumer2(in_q):
    while True:
        data = in_q.get()
        if data is _sentinel:
            in_q.put(_sentinel)
            break
        print('consumer2 get %s' % data)

q = Queue()
t1 = Thread(target=consumer1, args=(q,))
t2 = Thread(target=consumer2, args=(q,))
t3 = Thread(target=producer, args=(q,))
t1.start()
t2.start()
t3.start()
```
> `Queue` 对象已经包含了必要的锁，所以你可以通过它在多个线程间安全地共享数据。 当使用队列时，协调生产者和消费者的关闭问题可能会有一些麻烦。一个通用的解决方法是在队列中放置一个特殊的值，当消费者读到这个值的时候，终止执行。


未完待续...





[参考]

[python3-cookbook 并发编程](https://python3-cookbook.readthedocs.io/zh_CN/latest/chapters/p12_concurrency.html)  
