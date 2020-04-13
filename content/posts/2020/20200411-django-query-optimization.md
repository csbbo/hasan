---
title: "Django查询优化"
date: 2020-04-11T23:23:53+08:00
categories: ["Python"]
tags: ["Django"]
toc: true
draft: true
---

django查询优化
<!--more-->

### 查看django orm的SQL执行情况
1. 通过QuerySet的query属性
```python
query_set = TheoryProblem.objects.all()
print(query_set.query)
```

2. 通过django connection会打印所有执行过的sql语句和时间消耗(更喜欢这个)
```python
from django.db import connection
print(connection.queries)
```

3. 使用原生explain分析sql
```python
vul = VulInfo.objects.filter(cve="cve")
print(vul.explain(verbose=True))
``` 

### 优化办法

1. 利用 [queryset lazy](https://docs.djangoproject.com/en/1.8/topics/performance/#understanding-laziness) 的特性 去优化代码，尽可能的减少连接数据库的次数
2. 如果查出的 queryset 只用一次，可以使用 iterator () 去来防止占用太多的内存
3. 尽可能把一些数据库层级的工作放到数据库，例如使用 filter/exclude, F, annotate, aggregate
4. 一次性拿出所有你要的数据，不去取那些你不需要的数据
5. 意思就是要巧用 select_related (), prefetch_related () 和 values_list (), values (), 例如如果只需要 id 字段的话，用 values_list ('id', flat=True) 也能节约很多资源。或者使用 defer() 和 only() 方法：不加载某个字段 (用到这个方法就要反思表设计的问题了) / 只加载某些字段.
6. 如果不用 select_related 的话，去取外键的属性就会连数据再去查找.
7. bulk (批量) 地去操作数据，比如 bulk_create
8. 查找一条数据时，尽量用有索引的字段去查询，O (1) 或 O (log n) 和 O (n) 差别还是很大的
9. 用 count() 代替 len(queryset), 用 exists() 代替 if queryset: