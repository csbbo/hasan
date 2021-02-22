---
title: "Django 数据库查询优化"
date: 2021-02-22T10:35:32+08:00
categories: ["Python"]
tags: ["Django","python"]
toc: true
---

对于网站和Web APP来说最影响网站性能的就是数据库查询了，因为反复从数据库读写数据很耗时间和计算资源，而查询返回的数据集非常大时还会占据很多内存。这里从django orm的角度来探索数据库查询的优化。

<!--more-->

### QuerySet与查询

#### 什么是QuerySet

QuerySet是Django提供的强大的数据库接口(API)。正是因为通过它，我们可以使用filter, exclude, get等方法进行数据库查询，而不需要使用原始的SQL语言与数据库进行交互。从数据库中查询出来的结果一般是一个集合，这个集合叫就做 queryset。

#### QuerySet是惰性的

当我们使用如`filter`语句获得queryset，Django的数据接口QuerySet并没有对数据库进行查询，只有在做进一步运算时(如打印查询结果、判断是否存在、计算结果长度)才会执行真正的数据库查询,这个过程就是queryset的执行(evaluation)。这样做的目的是减少对数据库无效的操作。

#### QeurySet自带缓存

当queryset被执行后，其查询结果会载入内存并保存在queryset内置的cache中。再次使用就不需要重新去查询了

#### 判断查询结果是否存在

`if`与`exists()`都可以判断查询结果是否存在，但两者使用却又很大的不相同。`if`会触发整个queryset的缓存，而`exists()`只会返回`True`或`False`检视查询结果是否存在而不会缓存查询结果。选用哪个办法来判断需要根据实际使用需求来看。

#### 统计查询结果数量

`len()`与`count()`方法均能统计查询结果数量，这里也不说哪个更好。`count()`是从数据库层面直接获取查询结果数量而不需要返回整个queryset数据集一般来说会更快。`len()`会导致queryset的执行，需要先将整个数据集载入内存方可计算，但如果queryset数据集已经缓存在内存当中了`len()`则会更快


#### 按需获取数据

当查询到的queryset非常大时，会占用大量的内存，使用`values`和`values_list`按需提取数据
> 注意: values和values_list返回的是字典形式字符串数据，而不是对象集合

#### 使用update更新数据

相比于使用`save()`方法，`update()`不需要先缓存整个queryset


#### 使用explain方法分析耗时，优化查询

```python
Blog.objects.filter(title='My Blog').explain(verbose=True)

# output
Seq Scan on public.blog  (cost=0.00..35.50 rows=10 width=12) (actual time=0.004..0.004 rows=10 loops=1)
  Output: id, title
  Filter: (blog.title = 'My Blog'::bpchar)
Planning time: 0.064 ms
Execution time: 0.058 ms
```

