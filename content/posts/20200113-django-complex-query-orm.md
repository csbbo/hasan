---
title: "Django ORM annotate aggregate"
date: 2020-01-13T11:03:42+08:00
categories: ["Python"]
tags: ["Django"]
---

annotate aggregate
<!--more-->

```python
MixtureProblem.objects.order_by('-last_modify_time') \
	.annotate(
		label=Coalesce(
		F('ctfproblem__label'),F('penetrationproblem__lable'), F('reinforceproblem__label'))
	).filter(label__contains='web安全')
```

```python
theory_list.filter(problem_type=TheoryProblemTypeEnum.single_choose).aggregate(Sum('score'))['score__sum']
```