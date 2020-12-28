---
title: "Python Hapmap Object"
date: 2020-12-22T10:33:16+08:00
categories: ["Python"]
tags: ["Python"]
toc: true
draft: true
---


<!--more-->

### 作用域

python类创建的实例可以绑定任意属性，给实例绑定属性可以通过实例变量，或者通过self变量：
```python
class User():
    def __init__(self, username):
        self.username = username
user = User('bob')
user.password = '123456'
```
而类本身需要绑定属性，可以直接在class中定义：
```python
class User():
    name = 'User'

user = User()
user.name
```
> 属性归类所有，但所有实例都可以访问到

> 需要注意的是实例属性与类属性同名时，实例属性会将类属性屏蔽掉，但将实例属性删除掉后`del user.username`再访问到的就是类属性了

### 动态属性

在python中数据的属性，和处理属性的方法统称为属性。相对于C这种静态语言在类固定的情况下是无法为这个类和其对象添加属性的，而python就可以，这就是python的动态属性。

**影响属性处理方式的特殊属性**

- `__class__`对象所属类的引用，即`obj.__class__`与`type(obj)`作用相同
- `__dict__`一个映射，存储对象或类的可写属性
- `__slots__`类中可以定义该属性，限制实例可以有哪些属性，`__slots__`值是由字符串组成的元组

**处理属性的特殊方法**

内置的`getattr`、`hasattr`、`setattr`和`del`函数操作属性都会触发下述相应的特殊方法

- `__delattr__(self, name)`
- `__dir__(self)`
- `__getattr__(self, name)`
- `__getattribute__(self, name)`
- `__setattr__(self, name, value)`

除属性外，还可以创建特性(property)，使用存取方法修改数据属性。python内置的@property装饰器就是负责把一个方法变成属性调用的

```python
class Event(): 
    @property
    def venue(self):
    '''The Event attribute'''
        return self.__venue

    @venue.setter
    def venue(self,value):
        self.__venue = value

    @venue.deleter
    def venue(self,value):
        del self.__venue
```


<img src="/assets/2020/1222/python-hapmap.jpg" style="border: 1px solid #e0e0e0"/>