---
title: "C++ STL容器"
date: 2019-09-17T22:17:59+08:00
toc: true
tags: ["c++","Vector"]
---

## 序列容器

### Vector

Vector是动态数组，动态数组并不是真正意义上的动态的内存，而是一块连续的内存，当添加新的元素时，容量已经等于当前的大小的时候，重新开辟一块大小为当前容量两倍的数组，把原数据拷贝过去，释放掉旧的数组

**基本用法:**

+ `front()`返回头部元素的引用，可以当左值
+ `back()`返回尾部元素的引用，可以当左值
+ `push_back()`添加元素，只能尾部添加
+ `pop_back()`移除元素，只能在尾部移除
<!--more-->
+ `size()`获取容器长度
+ `empty()`判断容器是否为空
+ `clear()`清空容器
+ `begin()`返回容器第一个元素的迭代器。
+ `end()`返回容器最后一个元素之后的迭代器。

> 序列容器均具备以上能力，Array因为长度固定所以不支持push_back()之类修改长度操作

**Vector初始化:**

+ 直接构造函数初始化,`vector<int> v1;` `v1.push_back(1);`
+ 拷贝构造函数初始化`vector<int> v2 = v1;`
+ 使用部分元素来构造`vector<int> v3(v1.begin(), v1.begin() + 1);` `vector<int> v4(v1.begin(), v1.end());`
+ 存放3个元素，每个元素都是9`vector<int> v5(3,9);`

**Vector的遍历:**

+ `v[n]`方式，如果越界或出现其他错误，不会抛出异常，可能会崩溃，可能数据随机出现
+ `v.at(n)`方式，如果越界或出现其他错误，会抛出异常，需要捕获异常并处理
+ 迭代器提供了逆向遍历，可以通过迭代器来实现逆向遍历，当然上面两种方式也可以

**迭代器遍历**
```cpp
#include<iterator>

for(vector<int>::iterator it = v1.begin(); it != v1.end(); it++) {
    cout << *it << " ";
}
for(vector<int>::reverse_iterator it = v1.rbegin(); it != v1.rend(); it++){
    cout << *it << " ";
}
```

**插入删除:**

+ `erase(iterator)`函数，删除后会返回当前迭代器的下一个位置。例:删除前3个元素`v1.erase(v1.begin(),v1.begin()+3);`删除指定位置的元素`v1.erase(v1.begin() +3);`
+ Vector提供了`insert`函数，结合迭代器位置插入指定的元素。在指定的位置插入元素10的拷贝`v1.insert(v1.begin() + 3,10);`在指定的位置插入3个元素11的拷贝`v1.insert(v1.begin(),3,11);`

**例子**
```cpp
#include<vector> 
vector<int> coll; 
coll.push_back(i);  //所有序列容器都提供该函数
for(int i = 0;i<coll.size();++i){   //所有容器都提供该函数
    cout<<coll[i]<<endl;
} 
```

### Deque

deque是一个双端数组容器:可以在头部和尾部操作元素。

**基本用法:**

+ `push_back()`从尾部插入元素
+ `push_front()`从头部插入元素
+ `pop_back()`从尾部删除元素
+ `pop_front()`从头部删除元素

> `distance`函数可以求出当前的迭代器指针`it`距离头部的位置，也就是容器的指针。用法:`distance(v1.begin(),it)`

### Array

数组容器，可以使用迭代器操作

```cpp
#include<array>
#include<string>
array<string,5> coll = {"hello","world"};
coll.size()
``` 

### List

List可以在头部和尾部插入和删除元素，不能随机访问元素，也就是迭代器只能++,不能一次性跳转

**list的删除**

list提供了两个函数用来删除元素,分别是`erase`和`remove`:

+ `erase(iterater)`是通过位置或者区间来删除,主要结合迭代器指针来操作
+ `remove(value)`是通过值来删除

删除例子
```cpp
//删除某个元素
l.erase(l.begin());
//删除某个区间
list<int>::iterator it = l.begin();
it++;
it++;
it++;
l.erase(l.begin(), it);
//移除值为100的所有元素
l.remove(100);
```

**List的插入**

+ `l.insert(iterater_pos,num)`在pos位置插入元素num
+ `l.insert(iterater_pos,n,num)`在pos位置插入n个元素num
+ `l.insert(iterater_pos,beg,end)`在pos位置插入区间为[beg,end)的元素

例子
```cpp
#include<list>
lsit<char> coll;
coll.push_back();
for(auto& elem : coll){
    cout<<elem<<' ';
}
while(!coll.empty()){
    cout<<coll.front()<<' ';//coll.front()返回第一个元素
    coll.pop_front();
}
```
### Forward List 
Forward List是一个受限的list,不支持任何后退移动或效率低下的操作, 
因此它不提供push_back()和size()

```cpp
#include<forward_list>
forward_list<long> coll = {2,3,5,7,11,13,17};
coll.resize(9);
coll.resize(10,99);
for(auto elem : coll){
}
```

## 堆栈容器

### Stack 
在<stack>头文件中,class stack定义如下
```cpp
namespace std{
    template <typename T,typename Container = deque<T> >
    class stack;
}
```
Stack的实现只是单纯的把各项操作转化为内部容器的对应调用,所以可以使用sequence容器支持stack;只要他们提供`back()`,`push_back()`,`pop_back()`,例如:`std::stack<int,std::vector<int>> st;`

基本操作:

+ `push()`入栈
+ `top`取栈顶元素
+ `pop`出栈

> stack中没有元素会导致`top()`和`pop`的不明确行为可以用`size()`,`empty()`检验容器是否为空

```cpp
#include<stack>
stack<int> st;
st.push(1)
st.top()
st.pop()
``` 

### Queue

队列是一种先进先出数据结构

queue同样是把各项操作转化为对应容器的调用,只要他们支持`front()`,`back()`,`push_back()`,`pop_front()` 

基本操作

+ push()从队尾入队
+ pop()从队首出队
+ front()取队首元素
+ back()取队尾元素

例子
```cpp
#include<queue>
queue<int> q;
q.push(1)
q.front()
q.back()
q.pop()
```

### Priority Queue

优先级队列分为：最小值优先队列和最大值优先队列。
同理,它需要容器支持`random-access` `iterator`,`front()`,`push_back()`,`pop_back()`

核心接口:`push()`,`top()`返回`priority`中下一个元素,`pop()`用`size()`,`empty()`检验是否为空

基本操作:

+ `push()`从队尾入栈
+ `top()`取队首元素
+ `pop()`从队首出栈

定义优先级的方法：

+ `priority_queue<int>`默认定义`int`类型的最大值队列
+ `priority_queue<int,vector<int>,less<int>>`定义`int`型的最大值优先队列
+ `priority_queue<int,vector<int>,greater<int>>`定义`int`型的最小值队列

例子
```cpp
#include<queue>
priority_queue<int> p1;

//给默认的最大优先级队列入栈
p1.push(33);
p1.push(11);
p1.push(55);
p1.push(22);
//最大优先级的队首元素
p1.top()
//队首出栈
p1.pop()
```
其内部排序值大的先入队


## 关联容器

### Set和Multiset

```cpp
#include<set>
multiset<string> cities{//multiset允许重复
    "beijing","shanghai","tianjing","shenzheng"
};
for(const auto& elem : cities){}
cities.insert({"hangzou","wuhan","chongqing"});
```

### Map和Multimap

```cpp
#include<map>
multimap<int,string> coll;
coll = {
{5,"tagge"},
{2,"a"},
{4,"of"}
};
for(auto elem : coll){
    cout<<elem.second;
    cout<<elem.first;
}
```

### 迭代器

```cpp
list<char>::const_iterator pos;//只读模式,去掉const读写模式
for(pos = coll.begin();pos != coll.end();++pos){//这里前置++比后置++效率高
    cout<<*pos<<' ';
}
cbegin() 和 cend()返回类型cont::const_iterator对象
```

