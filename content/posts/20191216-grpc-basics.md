---
title: "gRPC基础"
date: 2019-12-16T15:08:50+08:00
categories: ["RPC"]
tags: ["grpc"]
toc: true
---

在 gRPC 里客户端应用可以像调用本地对象一样直接调用另一台不同的机器上服务端应用的方法，使得您能够更容易地创建分布式应用和服务。与许多 RPC 系统类似，gRPC 也是基于以下理念：定义一个服务，指定其能够被远程调用的方法（包含参数和返回类型）。在服务端实现这个接口，并运行一个 gRPC 服务器来处理客户端调用。在客户端拥有一个存根能够像服务端一样的方法。
<!--more-->

<img src="/assets/article/20191216/grpc_concept_diagram_00.png" style="width: 80%“/>


### 安装

Install gRPC
```
python -m pip install grpcio
```
Install gRPC tools

```
python -m pip install grpcio-tools
```
### 定义服务

使用 protocol buffers去定义 gRPC service 和方法 request、response 的类型。

要定义一个服务，你必须在你的 .proto 文件中指定 service：
```python
service RouteGuide {
   // (Method definitions not shown)
}
```

然后在你的服务中定义 rpc 方法，指定请求的和响应类型。gRPC 允许你定义4种类型的 service 方法

1.一个 简单 RPC ， 客户端使用存根发送请求到服务器并等待响应返回，就像平常的函数调用一样。  

```python
rpc GetFeature(Point) returns (Feature) {}
```

2.一个 应答流式 RPC ， 客户端发送请求到服务器，拿到一个流去读取返回的消息序列。 客户端读取返回的流，直到里面没有任何消息。从例子中可以看出，通过在 响应 类型前插入 stream 关键字，可以指定一个服务器端的流方法。

```python
rpc ListFeatures(Rectangle) returns (stream Feature) {}
```

3.一个 请求流式 RPC ， 客户端写入一个消息序列并将其发送到服务器，同样也是使用流。一旦客户端完成写入消息，它等待服务器完成读取返回它的响应。通过在 请求 类型前指定 stream 关键字来指定一个客户端的流方法。

```python
rpc RecordRoute(stream Point) returns (RouteSummary) {}
```

4.一个 双向流式 RPC 是双方使用读写流去发送一个消息序列。两个流独立操作，因此客户端和服务器可以以任意喜欢的顺序读写：比如， 服务器可以在写入响应前等待接收所有的客户端消息，或者可以交替的读取和写入消息，或者其他读写的组合。 每个流中的消息顺序被预留。你可以通过在请求和响应前加 stream 关键字去制定方法的类型。

```python
rpc RouteChat(stream RouteNote) returns (stream RouteNote) {}
```

### 样例

proto文件

```python
# hello.proto
syntax = "proto3";
package hello;

service Hello {
	rpc GetFeature(Point) returns (Feature) {}
	rpc ListFeatures(Rectangle) returns (stream Feature) {}
	rpc RecordRoute(stream Point) returns (RouteSummary) {}
	rpc RouteChat(stream RouteNote) returns (stream RouteNote) {}
}

message Point {
	float latitude = 1;
	float longitude = 2;
}

message Rectangle {
	Point lo = 1;
	Point hi = 2;
}

message Feature {
	string name = 1;
	Point location = 2;
}

message RouteNote {
	Point location = 1;
	string message = 2;
}

message RouteSummary {
	int32 point_count = 1;
	int32 feature_count = 2;
	int32 distance = 3;
	int32 elapsed_time = 4;
}
```

生成.proto定义的 gRPC 客户端和服务器端的接口
```
python -m grpc_tools.protoc -I . --python_out=. --grpc_python_out=. ./hello.proto
```

服务端代码
```python
# hello_server.py
import grpc
from concurrent import futures
import hello_pb2_grpc
from hello_pb2 import Point, Rectangle, Feature, RouteNote, RouteSummary

class HelloServicer(hello_pb2_grpc.HelloServicer):
    def __init__(self):
        self.name = "北京"

    def GetFeature(self, request, context):
        latitude = request.latitude
        longitude = request.longitude
        location = Point(latitude=latitude, longitude=longitude)
        return Feature(name=self.name, location=location)

    def ListFeatures(self, request, context):
        location = Point(latitude=request.lo.latitude, longitude=request.lo.longitude)
        for i in range(10):
            yield Feature(name="Beijing-Shanghai", location=location)

    def RecordRoute(self, request, context):
        for point in request:
            return RouteSummary(point_count=1, feature_count=2, distance=3, elapsed_time=4)

    def RouteChat(self, request, context):
        for note in request:
            yield note

def main():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    hello_pb2_grpc.add_HelloServicer_to_server(HelloServicer(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    server.wait_for_termination()

if __name__ == '__main__':
    main()
```

客户端代码

```python
# hello_client.py
import grpc

from hello_pb2 import Point, Rectangle, Feature, RouteNote, RouteSummary
from hello_pb2_grpc import HelloStub

def stream_point():
    for i in range(10):
        yield Point(latitude=1, longitude=2)

def stream_route_note():
    for i in range(10):
        yield RouteNote(location=Point(latitude=1, longitude=2), message='good job!')

def get_feature(latitude, longitude):
    with grpc.insecure_channel('127.0.0.1:50051') as channel:
        stub = HelloStub(channel)
        response = stub.GetFeature(Point(latitude=latitude, longitude=longitude))
        print(response)
        return response

def get_list_features():
    with grpc.insecure_channel('127.0.0.1:50051') as channel:
        stub = HelloStub(channel)
        lo = Point(latitude=1, longitude=2)
        hi = Point(latitude=3, longitude=4)
        response = stub.ListFeatures(Rectangle(lo=lo, hi=hi))
        for r in response:
            print(r)

def get_record_route():
    with grpc.insecure_channel('127.0.0.1:50051') as channel:
        stub = HelloStub(channel)
        data = stream_point()
        response = stub.RecordRoute(data)
        print(response)
        return response

def get_route_chat():
    with grpc.insecure_channel('127.0.0.1:50051') as channel:
        stub = HelloStub(channel)
        data = stream_route_note()
        response = stub.RouteChat(data)
        for r in response:
            print(r)

def main():
    get_feature(39.92, 116.46)
    get_list_features()
    get_record_route()
    get_route_chat()


if __name__ == '__main__':
    main()
```




[参考]

[gRPC官方文档](https://doc.oschina.net/grpc?t=58008)  
[gRPC Documentation](https://grpc.io/docs/quickstart/python/)  
[Protocol Buffers](https://developers.google.com/protocol-buffers/docs/proto3)