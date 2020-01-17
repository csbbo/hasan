---
title: "Django ORM annotate aggregate"
date: 2020-01-13T11:03:42+08:00
categories: ["Python"]
tags: ["Django"]
---

Django ORM中使用annotate aggregate进行复杂查询
<!--more-->

其实只是想简单弄两个例子出来，并没有长篇大论的计划

```python
MixtureProblem.objects.order_by('-last_modify_time') \
	.annotate(
		label=Coalesce(
		F('ctfproblem__label'),F('penetrationproblem__lable'), F('reinforceproblem__label'))
	).filter(label__contains='web安全')
```

> Coalesce返回第一个非空值，通过annotate以id分组每组通过重命名为label写入MixtureProblem中，实际上实现的是group by id的功能

```python
theory_list.filter(problem_type=TheoryProblemTypeEnum.single_choose).aggregate(Sum('score'))['score__sum']
```

> aggregate聚合函数对分数求和得到一个结果集{'score__sum':70}，并通过键'score__sum'取出具体的值，aggregate实际上做的事对某一列操作的结果集