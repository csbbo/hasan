---
title: "Numpy Pandas Matplotlib整理"
date: 2020-03-31T14:14:41+08:00
categories: ["Python"]
tags: ["Numpy", "Pandas", "Matplotlib"]
toc: true
---


<!--more-->

### Matplotlib

可能是 Python 2D-绘图领域使用最广泛的套件。它能让使用者很轻松地将数据图形化，并且提供多样化的输出格式。这里将会探索 matplotlib 的常见用法。

**绘制线条**

```python
import matplotlib.pyplot as plt
plt.plot([1,2,3,4])
plt.ylabel('some numbers')
plt.show()
```
> 如果向plot()命令提供单个列表或数组，则matplotlib假定它是一个y值序列，并自动为你生成x值。 由于 python 范围从 0 开始，默认x向量具有与y相同的长度，但从 0 开始。因此x数据是[0,1,2,3]

plot()是一个通用命令，并且可接受任意数量的参数。 例如，要绘制x和y，你可以执行命令：
```python
plt.plot([1, 2, 3, 4], [1, 4, 9, 16])
```
对于每个x,y参数对，有一个可选的第三个参数，它是指示图形颜色和线条类型的格式字符串。 格式字符串的字母和符号来自 MATLAB，并且将颜色字符串与线型字符串连接在一起。 默认格式字符串为"b-"，它是一条蓝色实线。 例如，要绘制上面的红色圆圈，你需要执行：
```python
import matplotlib.pyplot as plt
plt.plot([1,2,3,4], [1,4,9,16], 'ro')
plt.axis([0, 6, 0, 20])
plt.show()
```
> 有关线型和格式字符串的完整列表，请参见[plot()文档](https://matplotlib.org/api/pyplot_api.html#matplotlib.pyplot.plot)。 上例中的axis()命令接收[xmin，xmax，ymin，ymax]的列表，并指定轴域的可视区域。

如果matplotlib仅限于使用列表，它对于数字处理是相当无用的。 一般来说，你可以使用numpy数组。 事实上，所有序列都在内部转换为numpy数组。 下面的示例展示了使用数组和不同格式字符串，在一条命令中绘制多个线条。

```python
import numpy as np
import matplotlib.pyplot as plt

# evenly sampled time at 200ms intervals
t = np.arange(0., 5., 0.2)

# red dashes, blue squares and green triangles
plt.plot(t, t, 'r--', t, t**2, 'bs', t, t**3, 'g^')
plt.show()
```

**控制线条属性**

线条有许多你可以设置的属性：linewidth，dash style，antialiased等，请参见matplotlib.lines.Line2D。 有几种方法可以设置线属性：

- 使用关键字参数
```python
plt.plot(x, y, linewidth=2.0)
```
- 使用Line2D实例的setter方法
```python
line, = plt.plot(x, y, '-')
line.set_antialiased(False) # turn off antialising
```
- 使用setp()命令
```python
lines = plt.plot(x1, y1, x2, y2)
# 使用关键字参数
plt.setp(lines, color='r', linewidth=2.0)
# 或者 MATLAB 风格的字符串值对
plt.setp(lines, 'color', 'r', 'linewidth', 2.0)
```

**处理多个图形和轴域**

MATLAB 和 pyplot 具有当前图形和当前轴域的概念。 所有绘图命令适用于当前轴域。 函数gca()返回当前轴域（一个matplotlib.axes.Axes实例），gcf()返回当前图形（matplotlib.figure.Figure实例）。 通常，你不必担心这一点，因为它都是在幕后处理。 下面是一个创建两个子图的脚本。

```python
import numpy as np
import matplotlib.pyplot as plt

def f(t):
    return np.exp(-t) * np.cos(2*np.pi*t)

t1 = np.arange(0.0, 5.0, 0.1)
t2 = np.arange(0.0, 5.0, 0.02)

plt.figure(1)
plt.subplot(211)
plt.plot(t1, f(t1), 'bo', t2, f(t2), 'k')

plt.subplot(212)
plt.plot(t2, np.cos(2*np.pi*t2), 'r--')

plt.grid(True)
plt.show()
```
> 这里的figure()命令是可选的，因为默认情况下将创建figure(1)，如果不手动指定任何轴域，则默认创建subplot(111)。subplot()命令指定numrows，numcols，fignum，其中fignum的范围是从1到numrows * numcols。 如果numrows * numcols <10，则subplot命令中的逗号是可选的。 因此，子图subplot(211)与subplot(2, 1, 1)相同。 你可以创建任意数量的子图和轴域。 如果要手动放置轴域，即不在矩形网格上，请使用axes()命令，该命令允许你将axes([left, bottom, width, height])指定为位置，其中所有值都使用小数（0 到 1）坐标。 

**处理文本**

text()命令可用于在任意位置添加文本，xlabel()，ylabel()和title()用于在指定的位置添加文本

```python
import numpy as np
import matplotlib.pyplot as plt

mu, sigma = 100, 15
x = mu + sigma * np.random.randn(10000)

# 数据的直方图
n, bins, patches = plt.hist(x, 50, normed=1, facecolor='g', alpha=0.75)


plt.xlabel('Smarts')
plt.ylabel('Probability')
plt.title('Histogram of IQ')
plt.text(60, .025, r'$\mu=100,\ \sigma=15$')
plt.axis([40, 160, 0, 0.03])
plt.grid(True)
plt.show()
```

**对数和其它非线性轴**

matplotlib.pyplot不仅支持线性轴刻度，还支持对数和对数刻度。 如果数据跨越许多数量级，通常会使用它。 更改轴的刻度很容易：

```python
plt.xscale('log')
```

下面示例显示了四个图，具有相同数据和不同刻度的y轴

```python
import numpy as np
import matplotlib.pyplot as plt

# 生成一些区间 [0, 1] 内的数据
y = np.random.normal(loc=0.5, scale=0.4, size=1000)
y = y[(y > 0) & (y < 1)]
y.sort()
x = np.arange(len(y))

# 带有多个轴域刻度的 plot
plt.figure(1)

# 线性
plt.subplot(221)
plt.plot(x, y)
plt.yscale('linear')
plt.title('linear')
plt.grid(True)


# 对数
plt.subplot(222)
plt.plot(x, y)
plt.yscale('log')
plt.title('log')
plt.grid(True)


# 对称的对数
plt.subplot(223)
plt.plot(x, y - y.mean())
plt.yscale('symlog', linthreshy=0.05)
plt.title('symlog')
plt.grid(True)

# logit
plt.subplot(224)
plt.plot(x, y)
plt.yscale('logit')
plt.title('logit')
plt.grid(True)

plt.show()
```