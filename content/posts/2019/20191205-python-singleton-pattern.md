---
title: "Python单例模式实现"
date: 2019-12-05T13:46:02+08:00
categories: ["Python"]
tags: ["python"]
toc: true
---

单例是一种设计模式，使用该模式的类只会生成一个实例。单例模式保证了在程序不同位置都可以读取到同一对象实例，如果实例不存在则创建，已存在则返回这个实例。

<!--more-->

### 函数装饰器实现

实现代码:
```python
def singleson(cls):
    _instance = {}

    def wrapper(*args, **kw):
        if cls not in _instance:
            _instance[cls] = cls(*args, **kw)
        return _instance[cls]
    return wrapper

@singleson
class Cls():
    def __init__(self, x, y):
        self.x = x
        self.y = y

c1 = Cls(1,2)
c2 = Cls(3,4)
print(id(c1) == id(c2))
```
输出结果
```python
True
```
> 内存值相同说明是同一个对象,使用类地址作为键，实例对象作为值，检查如果实例存在则直接返回

### 类装饰器实现

实现代码:
```python
class Singleson():
    def __init__(self, cls):
        self._cls = cls
        self._instance = {}
    
    def __call__(self, *args, **kw):
        if self._cls not in self._instance:
            self._instance[self._cls] = self._cls(*args, **kw)
        return self._instance

@Singleson
class Cls():
    def __init__(self, x):
        self.x = x

c1 = Cls(1)
c2 = Cls(2)
print(id(c1) == id(c2))
```
输出结果
```python
True
```
> 类装饰器实现单例的原理和函数装饰器 实现的原理相似


### __new__关键字实现

代码实现:
```python
class Singleson():
    _instance = None
    def __new__(cls, *args, **kw):
        if cls._instance is None:
            cls._instance = object.__new__(cls, *args, **kw)
        return cls._instance

c1 = Singleson()
c2 = Singleson()
print(id(c1) == id(c2))
```
输出结果
```python
True
```

> 一个实例的创建过程，首先**元类(metaclass)**通过方法**__metaclass**创造**类(class)**,而**类(class)**通过方法**__new__**创造的**实例(instance)**,所以在类的创造过程中稍加控制便可以实现单例模式

### metaclass实现

type创造类的方法
```python
def func(self):
    print("do sth")

Cls = type("Cls", (), {"func": func})

c = Cls()
c.func
```

使用type创造了一个类出来是mataclass实现单例的基础
```python
class Singleton(type):
    _instances = {}
    def __call__(cls, *args, **kwargs):
        if cls not in cls._instances:
            cls._instances[cls] = super(Singleton, cls).__call__(*args, **kwargs)
        return cls._instances[cls]

class Cls(metaclass=Singleton):
    pass

cls1 = Cls()
cls2 = Cls()
print(id(cls1) == id(cls2))
```
输出结果
```python
True
```

> 这里将metaclass指向Singleton类，让Singleton中的type来创造新的Cls实例