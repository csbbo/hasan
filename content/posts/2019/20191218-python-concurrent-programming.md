---
title: "Python 并发编程"
date: 2019-12-18T19:28:13+08:00
categories: ["Python"]
tags: ["python"]
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


### 给关键部分加锁

多线程程序中的临界区通过加锁以避免竞争条件。Python中需要使用 `threading` 库中的 `Lock` 对象

```python
class SharedCounter:
    def __init__(self, initial_value = 0):
        self._value = initial_value
        self._value_lock = threading.Lock()

    def incr(self,delta=1):
        with self._value_lock:
             self._value += delta

    def decr(self,delta=1):
        with self._value_lock:
             self._value -= delta
```

> `Lock` 对象和 `with` 语句块一起使用可以保证互斥执行，就是每次只有一个线程可以执行 `with` `语句包含的代码块。with` 语句会在这个代码块执行前自动获取锁，在执行结束后自动释放锁。为了避免出现死锁的情况，使用锁机制的程序应该设定为每个线程一次只允许获取一个锁。

### 创建一个线程池

`concurrent.futures` 函数库有一个 `ThreadPoolExecutor` 类可以被用来完成这个任务。 下面是一个简单的TCP服务器，使用了一个线程池来响应客户端：

```python
from socket import AF_INET, SOCK_STREAM, socket
from concurrent.futures import ThreadPoolExecutor

def echo_client(sock, client_addr):
    '''
    Handle a client connection
    '''
    print('Got connection from', client_addr)
    while True:
        msg = sock.recv(65536)
        if not msg:
            break
        sock.sendall(msg)
    print('Client closed connection')
    sock.close()

def echo_server(addr):
    pool = ThreadPoolExecutor(128)
    sock = socket(AF_INET, SOCK_STREAM)
    sock.bind(addr)
    sock.listen(5)
    while True:
        client_sock, client_addr = sock.accept()
        pool.submit(echo_client, client_sock, client_addr)

echo_server(('',15000))
```

> 每个客户端请求过来后服务端会开一个线程去执行处理函数`echo_client`。如果你想手动创建你自己的线程池， 通常可以使用一个Queue来轻松实现。

### 简单的并行编程

`concurrent.futures` 库提供了一个 `ProcessPoolExecutor`类， 可被用来在一个单独的Python解释器中执行计算密集型函数。

```python
import gzip
import io
import glob
from concurrent import futures

def find_robots(filename):
    '''
    Find all of the hosts that access robots.txt in a single log file

    '''
    robots = set()
    with gzip.open(filename) as f:
        for line in io.TextIOWrapper(f,encoding='ascii'):
            fields = line.split()
            if fields[6] == '/robots.txt':
                robots.add(fields[0])
    return robots

def find_all_robots(logdir):
    '''
    Find all hosts across and entire sequence of files
    '''
    files = glob.glob(logdir+'/*.log.gz')
    all_robots = set()
    with futures.ProcessPoolExecutor() as pool:
        for robots in pool.map(find_robots, files):
            all_robots.update(robots)
    return all_robots

if __name__ == '__main__':
    robots = find_all_robots('logs')
    for ipaddr in robots:
        print(ipaddr)
```

> 提交到池中的工作必须被定义为一个函数。有两种方法去提交。 如果你想让一个列表推导或一个 `map()` 操作并行执行的话，可使用 `pool.map()`。

另外，你可以使用 `pool.submit()` 来手动的提交单个任务，如果你手动提交一个任务，结果是一个 `Future` 实例。 要获取最终结果，你需要调用它的 `result()` 方法。 它会阻塞进程直到结果被返回来。如果不想阻塞，你还可以使用一个回调函数，例如：

```python
def when_done(r):
    print('Got:', r.result())

with ProcessPoolExecutor() as pool:
     future_result = pool.submit(work, arg)
     future_result.add_done_callback(when_done)
```

> 回调函数接受一个 Future 实例，被用来获取最终的结果（比如通过调用它的result()方法）。 

### 定义一个Actor任务

actor模式是一种最古老的也是最简单的并行和分布式计算解决方案。 事实上，它天生的简单性是它如此受欢迎的重要原因之一。 简单来讲，一个actor就是一个并发执行的任务，只是简单的执行发送给它的消息任务。 响应这些消息时，它可能还会给其他actor发送更进一步的消息。 actor之间的通信是单向和异步的。因此，消息发送者不知道消息是什么时候被发送， 也不会接收到一个消息已被处理的回应或通知。


结合使用一个线程和一个队列可以很容易的定义actor，例如：
```python
from queue import Queue
from threading import Thread, Event

class ActorExit(Exception):
    pass

class Actor:
    def __init__(self):
        self._mailbox = Queue()

    def send(self, msg):
        self._mailbox.put(msg)
    
    def recv(self):
        msg = self._mailbox.get()
        if msg is ActorExit:
            raise ActorExit()
        return msg

    def close(self):
        self.send(ActorExit)

    def start(self):
        self._terminated = Event()
        t = Thread(target=self._bootstrap)

        t.daemon = True
        t.start()

    def _bootstrap(self):
        try:
            self.run()
        except ActorExit:
            pass
        finally:
            self._terminated.wait()

    def join(self):
        self._terminated.wait()

    def run(self):
        while True:
            msg = self.recv()

class PrintActor(Actor):
    def run(self):
        while True:
            msg = self.recv()
            print('Got:', msg)

p = PrintActor()
p.start()
p.send('Hello')
p.send('World')
p.close()
p.join()
```

> 这个例子中，你使用actor实例的 send() 方法发送消息给它们。 其机制是，这个方法会将消息放入一个队里中， 然后将其转交给处理被接受消息的一个内部线程。 close() 方法通过在队列中放入一个特殊的哨兵值（ActorExit）来关闭这个actor。 用户可以通过继承Actor并定义实现自己处理逻辑run()方法来定义新的actor。 ActorExit 异常的使用就是用户自定义代码可以在需要的时候来捕获终止请求 （异常被get()方法抛出并传播出去）。


### 实现消息发布/订阅模型

要实现发布/订阅的消息通信模式， 你通常要引入一个单独的“交换机”或“网关”对象作为所有消息的中介。 也就是说，不直接将消息从一个任务发送到另一个，而是将其发送给交换机， 然后由交换机将它发送给一个或多个被关联任务。下面是一个非常简单的交换机实现例子：

```python
from collections import defaultdict

class Exchange:
    def __init__(self):
        self._subscribers = set()

    def attach(self, task):
        self._subscribers.add(task)

    def detach(self, task):
        self._subscribers.remove(task)

    def send(self, msg):
        for subscriber in self._subscribers:
            subscriber.send(msg)

_exchanges = defaultdict(Exchange)

def get_exchange(name):
    return _exchanges[name]
```
一个交换机就是一个普通对象，负责维护一个活跃的订阅者集合，并为绑定、解绑和发送消息提供相应的方法。 每个交换机通过一个名称定位，`get_exchange()` 通过给定一个名称返回相应的 `Exchange` 实例。

下面是一个简单例子，演示了如何使用一个交换机:

```python
class Task:
    ...
    def send(self, msg):
        ...

task_a = Task()
task_b = Task()

# Example of getting an exchange
exc = get_exchange('name')

# Examples of subscribing tasks to it
exc.attach(task_a)
exc.attach(task_b)

# Example of sending messages
exc.send('msg1')
exc.send('msg2')

# Example of unsubscribing
exc.detach(task_a)
exc.detach(task_b)
```
> 尽管对于这个问题有很多的变种，不过万变不离其宗。 消息会被发送给一个交换机，然后交换机会将它们发送给被绑定的订阅者。

### 使用生成器代替线程

生成器（协程）替代系统线程来实现并发，首先要对生成器函数和 yield 语句有深刻理解。 yield 语句会让一个生成器挂起它的执行，这样就可以编写一个调度器， 将生成器当做某种“任务”并使用任务协作切换来替换它们的执行。 要演示这种思想，考虑下面两个使用简单的 yield 语句的生成器函数：

```python
def countdown(n):
    while n > 0:
        print('T-minus', n)
        yield
        n -= 1
    print('Blastoff!')

def countup(n):
    x = 0
    while x < n:
        print('Counting up', x)
        yield
        x += 1
```

这些函数在内部使用yield语句，下面是一个实现了简单任务调度器的代码：

```python

class TaskScheduler:
    def __init__(self):
        self._task_queue = deque()

    def new_task(self, task):
        '''
        Admit a newly started task to the scheduler

        '''
        self._task_queue.append(task)

    def run(self):
        '''
        Run until there are no more tasks
        '''
        while self._task_queue:
            task = self._task_queue.popleft()
            try:
                # Run until the next yield statement
                next(task)
                self._task_queue.append(task)
            except StopIteration:
                # Generator is no longer executing
                pass

# Example use
sched = TaskScheduler()
sched.new_task(countdown(10))
sched.new_task(countdown(5))
sched.new_task(countup(15))
sched.run()
```

未完待续...

[参考]

[python3-cookbook 并发编程](https://python3-cookbook.readthedocs.io/zh_CN/latest/chapters/p12_concurrency.html)  
