---
title: "Asyncio"
date: 2020-11-03T11:32:28+08:00
categories: ["Python"]
tags: ["python"]
toc: true
---

子程序，或者称为函数，在所有语言中都是层级调用的。协程看上去也是子程序，但执行过程中，在子程序内部可中断，然后转而执行别的子程序，在适当的时候再返回来接着执行。在一个子程序中中断，去执行其他子程序，不是函数调用，有点类似CPU的中断。

<!--more-->

### 协程与任务

python下协程通过async/await语法进行声明
```python
import asyncio

async def asyncio_test_func():
  print('hello')
  await asyncio.sleep(1)
  print('world')

asyncio.run(asyncio_test_func())
```

> 直接调用一个协程并不会将其加入执行日程

要运行一个协程，asyncio提供了三种主要机制

+ asyncio.run() 函数运行最高层级入口函数，参见上面的例子
+ 等待一个协程，在await关键字后面
+ asyncio.create_task() 函数并发运行多个作为asyncio任务的多个协程

> 可以理解成层级关系，asyncio.run()运行一个协程，该协程里通过await关键字运行等待一个“子协程”，而“子协程”也可以通过asyncio.create_task()打包成一个asyncio任务交由await执行

```python
import asyncio
import time

async def say_after(delay, what):
    await asyncio.sleep(delay)
    print(what)

async def main():
    task1 = asyncio.create_task(
        say_after(3, 'hello'))

    task2 = asyncio.create_task(
        say_after(2, 'world'))

    print(f"started at {time.strftime('%X')}")

    await task1
    await task2

asyncio.run(main())
```

### 可等待对象

如果一个对象可以在 await 语句中使用，那么它就是 可等待 对象。
可等待对象有三种主要类型: **协程**、**任务**、**Future**

+ 协程：定义形式为 async def 的函数称为协程函数，调用协程函数所返回的对象称为协程对象
+ 任务：任务被用来设置日程以便并发执行协程。当一个协程通过 asyncio.create_task() 等函数被打包为一个 任务，该协程将自动排入日程准备立即运行
+ Future：是一种特殊的 低层级 可等待对象，表示一个异步操作的 最终结果。当一个 Future 对象 被等待，这意味着协程将保持等待直到该 Future 对象在其他地方操作完毕。在 asyncio 中需要 Future 对象以便允许通过 async/await 使用基于回调的代码。一个很好的返回对象的低层级函数的示例是 loop.run_in_executor()

### 补充说明

+ **运行 asyncio 程序 asyncio.run(coro, \*, debug=False)**

此函数会运行传入的协程，负责管理 asyncio 事件循环，终结异步生成器，并关闭线程池。当有其他 asyncio 事件循环在同一线程中运行时，此函数不能被调用。如果 debug 为 True，事件循环将以调试模式运行。此函数总是会创建一个新的事件循环并在结束时关闭之。它应当被用作 asyncio 程序的主入口点，理想情况下应当只被调用一次。

+ **创建任务 asyncio.create_task(coro, \*, name=None)**

将 coro 协程 打包为一个 Task 排入日程准备执行。返回 Task 对象。

name 不为 None，它将使用 Task.set_name() 来设为任务的名称。

该任务会在 get_running_loop() 返回的循环中执行，如果当前线程没有在运行的循环则会引发 RuntimeError。

+ **休眠 asyncio.sleep(delay, result=None, \*, loop=None)**

阻塞 delay 指定的秒数。
如果指定了 result，则当协程完成时将其返回给调用者。
sleep() 总是会挂起当前任务，以允许其他任务运行。

+ **屏蔽取消操作asyncio.shield(aw, \*, loop=None)**

保护一个 可等待对象 防止其被 取消。

如果 aw 是一个协程，它将自动作为任务加入日程。
```python
res = await shield(something())
```
相当于:
```python
res = await something()
```
不同之处 在于如果包含它的协程被取消，在 something() 中运行的任务不会被取消。从 something() 的角度看来，取消操作并没有发生。然而其调用者已被取消，因此 "await" 表达式仍然会引发 CancelledError。

如果通过其他方式取消 something() (例如在其内部操作) 则 shield() 也会取消。

如果希望完全忽略取消操作 (不推荐) 则 shield() 函数需要配合一个 try/except 代码段，如下所示:
```python
try:
    res = await shield(something())
except CancelledError:
    res = None
```

### 事件循环

事件循环是每个 asyncio 应用的核心。 事件循环会运行异步任务和回调，执行网络 IO 操作，以及运行子进程。

**asyncio.get_running_loop()**

返回当前 OS 线程中正在运行的事件循环。

如果没有正在运行的事件循环则会引发 RuntimeError。 此函数只能由协程或回调来调用。

**asyncio.get_event_loop()**

获取当前事件循环，如果不存在，该线程为主线程，set_event_loop()没有有被调用，asyncio将会创建一个新的事件循环并且设置为当前线程的事件循环

**loop.run_until_complete(future)**

运行直到 future ( Future 的实例 ) 被完成。

如果参数是 coroutine object ，将被隐式调度为 asyncio.Task 来运行。

返回 Future 的结果 或者引发相关异常

### 协程优势

1. 执行效率。因为子程序切换不是线程切换，而是由程序自身控制，因此，没有线程切换的开销，和多线程比，线程数量越多，协程的性能优势就越明显。

2. 不需要多线程的锁机制，因为只有一个线程，也不存在同时写变量冲突，在协程中控制共享资源不加锁，只需要判断状态就好了，所以执行效率比多线程高很多。

> 因为协程是一个线程执行，那怎么利用多核CPU呢？最简单的方法是**多进程+协程**，既充分利用多核，又充分发挥协程的高效率，可获得极高的性能。