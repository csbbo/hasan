---
title: "Django ORM 查询"
date: 2020-01-13T11:03:42+08:00
categories: ["Python"]
tags: ["Django"]
---

django orm其实还是很简单的，学起来也容易，这篇文章更多也是辅助记忆，方便日后查找。

<!--more-->

### 查询集（QuertSet）

返回查询集的方法，称为过滤器

+ all()
+ filter()
+ exclude()
+ order_by()
+ values()：一个对象构成一个字典，然后构成一个列表返回

> 在管理器上调用过滤器方法会返回查询集。查询集经过过滤器筛选后返回新的查询集，因此可以写成链式过滤。查询集是惰性的，创建查询集不会带来任何数据库的访问，直到调用数据时，才会访问数据库。一般在进行迭代、序列化、使用if判断后才会对查询集求值，真正的去执行sql访问数据库。

返回单个值得方法

+ get()：返回单个满足条件的对象
	+ 如果未找到会引发"模型类.DoesNotExist"异常
	+ 如果多条被返回，会引发"模型类MultipleObjectsReturned"异常
+ first()：返回第一个对象
+ last()：返回最后一个对象
+ count()：返回当前查询的总条数
+ exists()：判断查询集中是否有数据，如果有则返回True

### 限制查询集

+ 查询集返回列表，可以使用下标的方式进行限制，等同于sql中的limit和offset子句
+ 注意：不支持负数索引不用能[0: -2]
+ 使用下标后返回一个新的查询集，不会立即执行查询
+ 如果获取一个对象，直接使用[0]，等同于[0:1].get()，但是如果没有数据，[0]引发IndexError异常，[0:1].get()引发DoesNotExist异常

### 比较运算符

+ __exact 精确等于, `like 'aaa'`
+ __contains 包含, `like '%aaa%'`
+ __startswith 以…开头, `like 'aaa%'`
+ __endswith 以…结尾, `like '%aaa'`

+ __in 在一个list范围内, `in ('aaa', 'bbb')`
+ __isnull (True, False)是否为NULL, `IS NULL`, `IS NOT NULL`

+ __gt 大于, `> 100`
+ __gte 大于等于, `>= 100`
+ __lt 小于, `< 100`
+ __lte 小于等于 `<= 100`
+ __range 在一个...范围内(list表示范围), `BETWEEN 30.0 AND 100.0`

+ __year (=2020)日期字段的年份, `BETWEEN 2020-01-01T00:00:00+00:00 AND 2020-12-31T23:59:59.999999+00:00`
+ __month (=10)日期字段的月份, `EXTRACT('month' FROM "vuldb_vulinfo"."publish_time" AT TIME ZONE 'UTC') = 10)`
+ __day (=1)日期字段的日, `EXTRACT('day' FROM "vuldb_vulinfo"."publish_time" AT TIME ZONE 'UTC') = 1)`
+ 快捷查询方式: pk表示primary key，默认的主键是id
> `like`匹配都可以在前面加`i`表示忽略大小写如`iexact`对应为`ilike`

### 高级用法

1. COALESCE()函数, 从左到右返回第一个非空表达式

2. F()允许Django在未实际链接数据的情况下具有对数据库字段的值的引用，不用获取对象放在内存中再对字段进行操作，直接执行原生产sql语句操作。

3. Q()对对象进行复杂查询，并支持&(and), |(or), ~(not)操作符。需要注意的是如果查询使用中带有关键字查询，Q对象一定要放在前面。

3. annotate相当于group by对数据进行分组,但使用的时候更像作多表连接时将子查询AS为当前对象一个字段。

4. 对查询集(queryset)的某些字段进行聚合操作时(比如Sum, Avg, Max, Count)使用

5. Subquery,OuterRef子查询

#### example
annotate
```python
MixtureProblem.objects.order_by('-last_modify_time').annotate(label=Coalesce(
	F('ctfproblem__label'),
	F('penetrationproblem__lable'),
	F('reinforceproblem__label'))).filter(label__contains='web安全')
```

aggregate
```python
TheoryProblem.filter(problem_type='理论题').aggregate(Sum('score'))['score__sum']
```

subquery
```python
CourseGroupBinding.objects.filter(user_group_id=group_id).order_by('-create_time').annotate(
	theory=Subquery(
		Course.objects.filter(id=OuterRef('course_id')).annotate(
			sum=Coalesce(Sum('theory_problem_list__score'), 0)).values('sum')),
	ctf=Subquery(
		Course.objects.filter(id=OuterRef('course_id')).annotate(
			sum=Coalesce(Sum('ctf_problem_list__score'), 0)).values('sum')),
	reinforce=Subquery(
		Course.objects.filter(id=OuterRef('course_id')).annotate(
			sum=Coalesce(Sum('reinforce_problem_list__score'), 0)).values('sum')),
	penetration=Subquery(
		Course.objects.filter(id=OuterRef('course_id')).annotate(
			sum=Coalesce(Sum('penetration_problem_list__penetrationproflag__score'), 0)).values('sum'))
).annotate(
	score=F('theory')+F('ctf')+F('reinforce')+F('penetration')).
	filter(reduce(or_, queries))
```
### 其他
[values与valus_list区别](https://www.jianshu.com/p/e92ab45075d5)


参考

[Django 查询集](https://my.oschina.net/esdn/blog/1502916)  
[Django基础(24): aggregate和annotate方法使用详解与示例](https://zhuanlan.zhihu.com/p/50974992)