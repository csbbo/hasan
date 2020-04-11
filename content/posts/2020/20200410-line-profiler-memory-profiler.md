---
title: "Python性能分析"
date: 2020-04-10T14:12:45+08:00
categories: ["Python"]
toc: true
---

对代码优化的前提是需要了解性能瓶颈在什么地方，程序运行的主要时间和内存是消耗在哪里。line_profiler和memory_profiler就能够很好的帮我们定位到时间和内存都消耗在哪里了。

<!--more-->

### line_profiler

line_profiler是一个用于逐行分析函数时间消耗的模块。

安装

```python
pip install line_profiler
```

封装了一个类装饰器
```python
class line_profile:
    """
    put @line_profile on function
    :param follow 显示调用的函数具体时间消耗
    """
    def __init__(self, follow=None):
        self.follow = follow or []
    
    def __call__(self, func):
        @functools.wraps(func)
        def prof_func(*args, **kw):
            lp = LineProfiler()
            list(map(lambda x: lp.add_function(x), self.follow))
            lp_wrapper = lp(func)
            result = lp_wrapper(*args, **kw)
            lp.print_stats()
            return result
        return prof_func
```

使用

```python
# example.py
def do_other_stuff(numbers):
    s = sum(numbers)

@line_profile([do_other_stuff])
def do_stuff(numbers):
    do_other_stuff(numbers)
    s = sum(numbers)
    l = [numbers[i]/43 for i in range(len(numbers))]
    m = ['hello'+str(numbers[i]) for i in range(len(numbers))]
```

```python
python example.py
```

运行结果
![](/assets/2020/0410/line_profile.png)
<!-- <img src="/assets/2020/0410/line_profile.png" style="margin-left:0px;width:70%"> -->
### memory_profiler

[memory_profiler](https://github.com/pythonprofilers/memory_profiler)是一个基于[psutil](https://pypi.org/project/psutil/)用于监视进程内存消耗的python模块，并且能够逐行分析程序的内存消耗。

安装

```python
pip install -U memory_profiler
```

使用

```python
# example.py
@profile(precision=4)
def do_stuff(numbers):
    do_other_stuff(numbers)
    s = sum(numbers)
    l = [numbers[i]/43 for i in range(len(numbers))]
    m = ['hello'+str(numbers[i]) for i in range(len(numbers))]
```

```python
python -m memory_profiler example.py
```

或者直接使用以下方式

```python
from memory_profiler import profile

@profile
def my_func():
    a = [1] * (10 ** 6)
    b = [2] * (2 * 10 ** 7)
    del b
    return a
```

运行结果
![](/assets/2020/0410/memory_profile.png)