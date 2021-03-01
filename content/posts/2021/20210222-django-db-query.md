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

### aggregate和annotate

Django的`aggregate`和`annotate`方法主要用于组合查询，当我们需要对查询集(queryset)的某些字段进行计算，或先分组在计算或排序就需要用到`aggregate`和`annotate`方法了


#### 准备工作

一个模型`Student`和`Hobbit`是多对多关系:
```python
class Student(models.Model):

    name = models.CharField(max_length=20)
    age = models.IntegerField()
    hobbies = models.ManyToManyField(Hobby)
    
class Hobby(models.Model):
    name = models.CharField(max_length=20)
```

#### aggregate
`aggregate`方法支持的聚合操作有`MIN`、`MAX`、`AVG`、`SUM`、`COUNT`，所以先提前import进来：
```python
from django.db.models import Max, Min, Avg, Sum, Count
```

一些例子:
```python
# 计算学生平均年龄, 返回字典。age和avg间是双下划线哦
Student.objects.all().aggregate(Avg('age'))
{ 'age__avg': 12 }

# 学生平均年龄，返回字典。all()不是必须的。
Student.objects.aggregate(Avg('age'))
{ 'age__avg: 12' }

# 计算学生总年龄, 返回字典。
Student.objects.aggregate(Sum('age'))
{ 'age__sum': 144 }

# 学生平均年龄, 设置字典的key
Student.objects.aggregate(average_age = Avg('age'))
{ 'average_age': 12 }

# 学生最大年龄，返回字典
Student.objects.aggregate(Max('age'))
{ 'age__max': 12 }

# 同时获取学生年龄均值, 最大值和最小值, 返回字典
Student.objects.aggregate(Avg('age‘), Max('age‘), Min('age‘))
{ 'age__avg': 12, 'age__max': 18, 'age__min': 6, }

# 根据Hobby反查学生最大年龄。查询字段student和age间有双下划线哦。
Hobby.objects.aggregate(Max('student__age'))
{ 'student__age__max': 12 }
```

#### annotate
`annotate`的中文意思是注释，但似乎有点词不达意，更好的理解应该是分组(group by)。如果需要对数据集先进行分组然后再进行某些聚合操作或排序时，就需要用`annotate`来实现。与`aggregate`不同的是`annotate`返回的是一个查询集，该查询集相当于是在原来的基础上多加了一个统计字段

一些例子:

```python
# 按学生分组，统计每个学生的爱好数量
Student.objects.annotate(Count('hobbies'))

# 按学生分组，统计每个学生爱好数量，并自定义字段名
Student.objects.annotate(hobby_count_by_student=Count('hobbies'))

# 按爱好分组，再统计每组学生数量。
Hobby.objects.annotate(Count('student'))

# 按爱好分组，再统计每组学生最大年龄。
Hobby.objects.annotate(Max('student__age'))
```

#### annotate与filter联用

有时我们需要对数据集先筛选再分组或先分组再筛选，就可以通过annotate与filter联用来实现

一些例子:
```python
# 先按爱好分组，再统计每组学生数量, 然后筛选出学生数量大于1的爱好。
Hobby.objects.annotate(student_num=Count('student')).filter(student_num__gt=1)

# 先按爱好分组，筛选出以'd'开头的爱好，再统计每组学生数量。
Hobby.objects.filter(name__startswith="d").annotate(student_num=Count('student‘))
```

#### annotate与order_by联用

一些例子:
```python
# 先按爱好分组，再统计每组学生数量, 然后按每组学生数量大小对爱好排序。
Hobby.objects.annotate(student_num=Count('student‘)).order_by('student_num')

# 统计最受学生欢迎的5个爱好。
Hobby.objects.annotate(student_num=Count('student‘)).order_by('-student_num')[:5]
```

#### annotate与values联用

在前面的例子中分组都是按照对象分组的，如按学生对象分组，同样的也可以通过`values`按如学生姓名name来分组，如果两个学生具有相同的名字他们的爱好将叠加

```python
# 按学生名字分组，统计每个学生的爱好数量。
Student.objects.values('name').annotate(Count('hobbies'))

你还可以使用values方法从annotate返回的数据集里提取你所需要的字段，如下所示:
# 按学生名字分组，统计每个学生的爱好数量。
Student.objects.annotate(hobby_count=Count('hobbies')).values('name', 'hobby_count')
```

### select_related和prefetch_related

#### 准备工作
文章(Article)与类别(Category)是一对多关系，文章(Article)与标签(Tag)是多对多关系
```python
class Article(models.Model):
    """文章模型"""
    title = models.CharField('标题', max_length=200, db_index=True)
    category = models.ForeignKey('Category', verbose_name='分类', on_delete=models.CASCADE, blank=False, null=False)
    tags = models.ManyToManyField('Tag', verbose_name='标签集合', blank=True)
```

#### 糟糕的用法
```python
articles = Article.objects.all()
for article in articles:
  print(article.title)
  print(article.category.name)
  for tag in article.tags.all():
    print(tag.name)
```

当使用`Article.objects.all()`查询得到的只是`Article`表的数据，并没有包含`Category`表和`Tag`表的数据。因此每一次打印`article.category.name`和`tag.name`都会重新去查询一遍`Category`表和`Tag`表，造成了很大不必要的浪费

#### select_related

`select_related`会根据外键关系(仅限一对一、一对多)，使用`inner join`来一次性获取主体对象和相关对象的信息，这样在打印`article.category.name`时就不用去重新查询数据库了

修改article查询语句:
```python
articles = Article.objects.all().select_related('category')
```

selected_related常用使用案例:
```python
# 获取id=13的文章对象同时，获取其相关category信息
Article.objects.select_related('category').get(id=13)

# 获取id=13的文章对象同时，获取其相关作者名字信息
Article.objects.select_related('author__name').get(id=13)

# 获取id=13的文章对象同时，获取其相关category和相关作者名字信息。下面方法等同
Article.objects.select_related('category', 'author__name').get(id=13)
Article.objects.select_related('category').select_related('author__name').get(id=13)

# 使用select_related()可返回所有相关主键信息,all()非必需
Article.objects.all().select_related()

# 获取Article信息同时获取blog信息,filter方法和selected_related方法顺序不重要
Article.objects.filter(pub_date__gt=timezone.now()).select_related('blog')
Article.objects.select_related('blog').filter(pub_date__gt=timezone.now())
```

#### prefetch_related

在多对多关系中不能再使用`selectd_related`，因为多对多`JOIN`操作后表会变得非常的大。而`prefetch_related`就是用来处理这个问题的,`prefect_related`可用于多对多关系字段，也可用于反向外键关系(related_name)

再次修改article的查询语句:
```python
articles = Article.objects.all().select_related('category').prefecth_related('tags')
```

prefetch_related常用使用案例:
```python
# 文章列表及每篇文章的tags对象名字信息
Article.objects.all().prefetch_related('tags__name')

# 获取id=13的文章对象同时，获取其相关tags信息
Article.objects.prefetch_related('tags').get(id=13)

用Prefetch方法可以给prefetch_related方法额外添加额外条件和属性
# 获取文章列表及每篇文章相关的名字以P开头的tags对象信息
Article.objects.all().prefetch_related(
    Prefetch('tags', queryset=Tag.objects.filter(name__startswith="P"))
)

# 文章列表及每篇文章的名字以P开头的tags对象信息, 放在article_p_tag列表
Article.objects.all().prefetch_related(
    Prefetch('tags', queryset=Tag.objects.filter(name__startswith="P")), to_attr='article_p_tag'
)
```

[参考]

[QuerySet特性及高级使用技巧](https://mp.weixin.qq.com/s?__biz=MjM5OTMyODA4Nw%3D%3D&chksm=a73c6215904beb033f3277e3d1a98aaeece313792649cf96eea77cb1ee3d06bb493d06bc0c99&idx=1&mid=2247483949&scene=21&sn=bc4c8929d5f8e99a769c63f2208ed6eb#wechat_redirect)  
[aggregate和annotate方法使用详解与示例](https://mp.weixin.qq.com/s?__biz=MjM5OTMyODA4Nw%3D%3D&chksm=a73c6325904bea33513a0426971e70c43779b9519b5f9eace0ef2d96d9b082a834c34d88c899&idx=1&mid=2247484189&scene=21&sn=f5402c86aeaacab1eeaa2a021bb60e79#wechat_redirect)  
[select_related和prefetch_related的用法与区别](https://blog.csdn.net/weixin_42134789/article/details/100571539)  
