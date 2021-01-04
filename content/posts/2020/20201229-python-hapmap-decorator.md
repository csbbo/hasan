---
title: "巧用装饰器"
date: 2020-12-29T15:00:10+08:00
categories: ["Python技能图谱"]
tags: ["Python"]
toc: true
---

函数与装饰器
<!--more-->

## 深拷贝与浅拷贝

- 直接赋值：其实就是对象的引用
- 浅拷贝(copy)：拷贝父对象，不会拷贝对象的内部的子对象
- 深拷贝(deepcopy)： copy 模块的 deepcopy 方法，完全拷贝了父对象及其子对象

首先来看一个例子:
```python
a = {1: [1,2,3]}
```

将a赋值给b:
```python
b = a
```
> 此时b和a指向的是同一个对象{1: [1,2,3]},所以无论是通过a或b来操作该对象另一方都可以看到

浅拷贝:
```python
b = a.copy()
```
> 此时b和a分别是两个不同的对象，但他们子对象还是指向一个相同的对象。所以父对象更改不会影响另一方，但子对象更改两边都是一样的

深拷贝:
```python
b = copy.deepcopy(a)
```
> 此时b完全拷贝了a的父对象及其子对象，两者已经是完全独立的了

### 数据类型

**字典**

字典是通过哈希表实现的，通过哈希函数将key转换成一个整型数字，然后用该数字对数组长度取余，key对应的value就存在取余后的数组下标空间里。取数据也是同理，到相应的数组下标里取数据。

**Python解释器内存分配和回收**

- 内存池
- 引用计数
- 标记清除
- 分代回收


### 函数

**内置函数**

[https://docs.python.org/zh-cn/3/library/functions.html](https://docs.python.org/zh-cn/3/library/functions.html)

**高阶函数**

map()、reduce()、filter()、sorted()

**装饰器**

```python
def log(func):
    def wrapper(*args, **kw):
        print('call %s():' % func.__name__)
        return func(*args, **kw)
    return wrapper

@log
def now():
    print('2015-3-25')
```

### 技能图谱
<img src="/assets/2020/1222/python-hapmap.jpg" style="border: 1px solid #e0e0e0"/>