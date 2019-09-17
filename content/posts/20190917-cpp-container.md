---
title: "C++容器"
date: 2019-09-17T22:17:59+08:00
toc: true
tags: ["c++"]
---

## 序列容器

#### Vector 
默认被default构造函数初始化
```cpp
#include<vector> 
vector<int> coll; 
coll.push_back(i); //所有序列容器都提供该函数
for(int i = 0;i<coll.size();++i){//所有容器都提供该函数
} 
``` 

<!--more-->

#### Deque 
```cpp
#include<deque>
deque<float> coll;
coll.push_front(i*1.1);
coll.push_back;
```
#### Array
```cpp
#include<array>
#include<string>
array<string,5> coll = {"hello","world"};
coll.size()
``` 
上面可搭配[]被随机访问 
#### List 
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
#### Forward List 
forward list是一个受限的list,不支持任何 后退移动 或 效率低下 的操作, 
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

#### stack 
在<stack>头文件中,class stack定义如下
```cpp
namespace std{
    template <typename T,typename Container = deque<T> >
    class stack;
}
```
Stack的实现只是单纯的把各项操作转化为内部容器的对应调用,所以可以使用sequence容器支持stack；

只要他们提供back(),push_back(),pop_back(),例如: 

`std::stack<int,std::vector<int>> st;`
stack的核心接口:`push()`,`top()`,`pop()` 
stack中没有元素会导致`top()`和`pop`的不明确行为可以用`size()`,`empty()`检验容器是否为空
```cpp
#include<stack>
std::stack<int> st;
``` 
#### queue 
queue同样是把各项操作转化为对应容器的调用,只要他们支持front(),back(),push_back(),pop_front() 
核心接口:push(),pop(),front()返回queue内下一个元素,back()返回queue内最后一个元素 
```cpp
#include<queue>
queue<string> q;
```
#### priority queue 
同理,它需要容器支持random-access iterator,front(),push_back(),pop_back() 
核心借口:push(),top()返回priority中下一个元素,pop() 
用size(),empty()检验是否为空
```cpp
#include<queueu>
priority_queue<float> q;
```
其内部排序值大的先入队

## 关联容器
#### Set和Multiset
```cpp
#include<set>
multiset<string> cities{//multiset允许重复
"beijing","shanghai","tianjing","shenzheng"
};
for(const auto& elem : cities){}
cities.insert({"hangzou","wuhan","chongqing"});
```
#### Map和Multimap 
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
#### 迭代器
```cpp
list<char>::const_iterator pos;//只读模式,去掉const读写模式
for(pos = coll.begin();pos != coll.end();++pos){//这里前置++逼后置++效率高
cout<<*pos<<' ';
}
cbegin() 和 cend()返回类型cont::const_iterator对象
```

