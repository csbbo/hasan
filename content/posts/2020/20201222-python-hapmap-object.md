---
title: "Python面向对象编程"
date: 2020-12-22T10:33:16+08:00
categories: ["Python技能图谱"]
tags: ["Python"]
toc: true
---

面向对象中的属性、继承、设计模式等
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

### 多继承

**MRO 算法**

mor(method resolution order)，主要用于在多继承时判断所调用的属性的路径(来自哪个类)。没有深究据说mor算法在python2.3之前是基于深度优先搜索，但在python2.3之后使用的事C3算法,参考[Python多重继承mro](http://blog.sina.com.cn/s/blog_45ac0d0a01018488.html)

**Mixin 模式**

利用Python的多重继承，子类可以继承不同功能的Mixin类，按需动态组合使用。参考[通过Python理解Mixin概念](https://zhuanlan.zhihu.com/p/95857866)

定义和使用Mixin类应当遵循几个原则:

1. Mixin 实现的功能需要是通用的，并且是单一的，比如上例中两个 Mixin 类都适用于大部分子类，每个 Mixin 只实现一种功能，可按需继承。
2. Mixin 只用于拓展子类的功能，不能影响子类的主要功能，子类也不能依赖 Mixin。比如上例中 Person 继承不同的 Mixin 只是增加了一些功能，并不影响自身的主要功能。如果是依赖关系，则是真正的基类，不应该用 Mixin 命名。
3. Mixin 类自身不能进行实例化，仅用于被子类继承。

### 设计模式

**SOLID 设计原则**

在程序设计领域，SOLID指代了面向对象编程和面向对象设计的五个基本原则。当这些原则被一起应用时，他们使得一个程序员开发一个容易进行软件维护和拓展的系统变得更加可能。SOLID所包含的原则是通过引发编程者进行软件源代码的代码重构进行软件代码异味清扫，从而使得软件清晰可读以及可拓展时可以应用的指南。

- S 单一功能原则(Single Responsibility Principle), 认为对象应该仅具有一种单一功能的概念
- O 开闭原则(Open-Close Principle), 认为“软件体应该是对于扩展开放的，但对于修改封闭的”的概念
- L 里式替换原则(Liskov Substitution Principle), 认为“程序中的对象应该是可以在不改变程序正确性的前提下被它的子类所替换的”的概念
- I 接口隔离原则(Interface Segregation Principle), 认为“多个特定客户端接口要好于一个宽泛用途的接口”的概念
- D 依赖反转原则(Dependency Inversion Principle), 认为一个方法应该遵循”依赖于抽象而不是一个实例“的概念

**工厂模式**

工厂模式中可以通过一个简单的函数就可以创建不同的对象，工厂模式一般包含**工厂方法**和**抽象工厂**两种模式。

工厂方法(factory method)，工厂方法模式是指定义一个可以根据输入参数的不同返回不同对象的函数:

```python
class JsonParser:
    def parse(self, raw_data):
        return json.load(raw_data)

class XMLParser:
    def parse(self, raw_data):
        return xmldict(raw_data)

class new_parser(type, **kwargs):
    if type == 'json':
        return JsonParser()
    if type == 'xml':
        return XMLParser()

if __name__ == '__mian__':
    parser = new_parser('json')
    parser.parser(jsonstr)
```

抽象工厂(abstract factory), 一系列工厂方法组合在一起实现了一个抽象工厂，基于上面的例子
```python
class DBSaver:
    def save(self, obj, **kwargs):
        model = Model(**obj)
        model.save()


class FileSaver:
    def __init__(self, save_dir):
        self.save_dir = save_dir

    def save(self, obj, name):
        path = os.path.join(self.save_dir, name)
        with open(path, 'wb') as fp:
            data = json.dumps(obj)
            fp.write(data)


def new_saver(type, **kwargs):
    if type == 'db':
        return DBSaver()
    elif type == 'file':
        save_dir = kwargs['save_dir']
        return FileSaver(save_dir)


class FileHandler:
    def __init__(self, parse_type, save_type, **kwargs):
        self.parser = new_parser(parse_type, **kwargs)
        self.saver = new_saver(save_type, **kwargs)

    def do(self, data, filename):
        obj = self.parser.parse(data)
        self.saver.save(obj, filename)

handler = FileHandler('json', 'file', save_dir='save')
with open('hello.json') as fp:
    data = fp.read()
handler.do(data, 'data.json')
```

> 工厂方法和抽象工厂的选择： 先使用工厂方法，当发现需要使用一系列的工厂方法来创建多个对象时，可以考虑把这些创建对象的过程合并到一个抽象工厂。

**单例模式**

单例模式（Singleton Pattern）是一种常用的软件设计模式，该模式的主要目的是确保某一个类只有一个实例存在。当你希望在整个系统中，某个类只能出现一个实例时，单例对象就能派上用场。(但在个人Python使用场景中，单例模式事实上用处不大，因为往往只要使用一个全局变量就可以更加简单高效的实现该目的)我在另一篇文章中也列举了python中实现单例模式的实现方式[Python单例模式实现](https://blog.shaobo.fun/posts/2019/20191205-python-singleton-pattern/)
### 对象协议与鸭子类型

python作为一门动态语言，Duck Type概念遍布其中，所以其中的概念并不是以类型约束为载体，而是使用称作为协议的概念

python对象协议: 在python需要调用对象的某个方法，而该对象也正好有这个方法，就就是协议。比如在加法运算中，当出现`+`时，按照数值类型相关的协议，python会去调用相应对象的`__add()__`方法。
鸭子类型(Duck Type): 是动态语言的一种设计风格，在这种风格中，一个对象的有效的语意并不是由继承特定的类或实现特定的接口决定，而是由当前方法和属性的集合决定。简而言之就是不关心对象的类型，只关心其拥有的行为。

在James Whitcomb Riley提出的鸭子测试中这样表述: “当看到一只鸟走起来像鸭子、游泳起来像鸭子、叫起来也像鸭子就可以被称为鸭子”，在鸭子类型中关注的不是对象的类型本身，而是它如何使用，如:
```python
class Duck():
    def quack(self):
        print("嘎嘎嘎")

class Person():
    def quack(self):
        print("hello world")

def in_the_forest(duck):
    duck.quack()

if __name__ == '__main__':
    duck = Duck()
    bob = Person()
    in_the_forest(duck)
    in_the_forest(bob)
```

> 从上面可以看到，in_the_forest()函数并不关心其参数类型，只关心这个参数是否拥有quack()方法，只要有quack()方法的对象都可以被in_the_forest()函数调用。这就是鸭子类型，与C++、Java等静态语言的却别基本是完全不一样的。



### 技能图谱
<img src="/assets/2020/1222/python-hapmap.jpg" style="border: 1px solid #e0e0e0"/>