---
title: "Golang 反射"
date: 2021-03-26T10:13:05+08:00
categories: ["Golang"]
tags: ["reflect"]
toc: true
---

Go语言的反射可以为我们提供运行时操作任意类型对象的能力

<!--more-->

先准备一个结构体和它的几个方法
```go
package main

import "fmt"
import "reflect"

type User struct {
	UserName string
	Age	int
}

func (u User) GetName() string {
	return u.UserName
}

func (u User) GetAge() int {
	return u.Age
}

func (u User) PrintIn(in string) string {
	return in
}

func main() {
	user := User{"张三", 20}
}
```

### TypeOf和ValueOf

在Go反射的定义中，任何接口都会由接口的具体类型和类型对应的值两部分组成。因为interface{}可以表示任意类型，所以把变量转成interface{}这个变量在Go反射中就表示为`<Type,Value>`，标准库提供两种类型`reflect.Type`和`reflect.Value`来分别表示它们，并提供两个函数`TypeOf()`和`ValueOf()`来获取它们

```go
// 获取对象的Value和Type, reflect.ValueOf、reflect.TypeOf函数返回的是一份值的拷贝
t := reflect.TypeOf(user)
v := reflect.ValueOf(user)
fmt.Println(t)
fmt.Println(v)
```

### reflect.Value转原始类型
在Go反射中`reflect.Value`又同时持有`reflect.Value`和`reflect.Type`两部分，所以我们可以通过`reflect.Value`的`Interface`方法还原

```go
fmt.Println(v.Type())
u1 := v.Interface().(User)
```

### 获取类型的底层类型

其实对应的主要是基础类型，接口、结构体、指针这些，因为我们可以通过type关键字声明很多新的类型，比如上面的例子，对象u的实际类型是User，但是对应的底层类型是struct这个结构体类型，我们来验证下

```go
// 获取类型的底层类型，一下两种操作等价
fmt.Println(t.Kind())
fmt.Println(v.Kind())
```

### 遍历字段和方法

通过反射，我们可以获取一个结构体类型的字段,也可以获取一个类型的导出方法，这样我们就可以在运行时了解一个类型的结构。通过`reflect.ValueOf()`得到`reflect.Value`后调用`Elem()`方法得到指向的值，就可以修改对象了。`MethodByName`方法可以让我们根据一个方法名获取一个方法对象，然后我们构建好该方法需要的参数，最后调用`Call`就达到了动态调用方法的目的。


```go
// 遍历结构体类型的字段和方法
for i:=0; i<t.NumField(); i++ {
    fmt.Println(t.Field(i).Name)
}
for i:=0; i<t.NumMethod(); i++ {
    fmt.Println(t.Method(i).Name)
}

// 动态修改值
v = reflect.ValueOf(&user)
e := v.Elem()
e.FieldByName("UserName").Set(reflect.ValueOf("bob"))
e.FieldByName("Age").Set(reflect.ValueOf(100))
fmt.Println(user)

// 动态调用方法
v = reflect.ValueOf(user)
method := v.MethodByName("PrintIn")
if method.IsValid() {
    args := []reflect.Value{reflect.ValueOf("你好")}
    fmt.Println(method.Call(args))
} else {
    fmt.Println("method not Found")
}
```

### New方法

`New`方法可以通过`reflect.Type`创建一个空对象
```go
	u := User{"Nick", 18}
	vv := reflect.New(reflect.TypeOf(u)).Elem().Interface()
	fmt.Println(vv)

    // 少了Elem()函数，得到的是一个指针值
	vp := reflect.New(reflect.TypeOf(u)).Interface()
	fmt.Println(vp)
```
