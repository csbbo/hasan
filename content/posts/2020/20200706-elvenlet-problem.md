---
title: "记一次Evenlet事件"
date: 2020-07-06T14:46:00+08:00
categories: ["Python"]
tags: ["socket.io", "evenlet"]
toc: true
draft: false
---

事情的起因是要再项目中使用双向通信，首选是websocket但是由于ie10的不支持选用的socketio(sockerio会根据实际情自动切换websocket和轮询两种办法),而socketio的使用又需要引入evenlet,就这样一步一步的往里陷了。

<!--more-->

### Django中使用socketio

```python
import logging
import socketio
import eventlet

logger = logging.getLogger(__name__)

eventlet.monkey_patch(thread=False, socket=True, select=True)
sio = socketio.Server(async_mode=None, cors_allowed_origins='*')
SID = None


@sio.event
def connect(sid, environ):
    global SID
    SID = sid
    logger.error(f'Client {sid} connected')


@sio.event
def disconnect_request(sid):
    sio.disconnect(sid)


@sio.event
def disconnect(sid):
    logger.error(f'Client {sid} disconnected')


def open_virtual_machines(configs, user_id):
    from virtual_machine.tasks import open_vm_task
    global SID
    result_list = []
    for config in configs:
        message = open_vm_task.send(str(config.id), config.name, user_id)
        result_list.append(message)
    messages = []
    for msg in result_list:
        message = msg.get_result(block=True, timeout=300000)
        messages.append(message)
    sio.emit('vm_response', {'data': messages})
    sio.disconnect(SID)
```

第8行创建一个socketio服务，async_mode=None由sockerio内部算法决定使用异步模式，cors_allowed_origins='*'是因为sockio服务也有同源策略这里允许所有来源的地址请求，这也是我碰到的第一个问题导致nginx流量过来时候都没有正常返回，nginx配置如下：

```shell
location /socket.io/ {
    proxy_pass http://server:8000;
    proxy_redirect off;
    proxy_buffering off;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "Upgrade";
}
```

> 这里没什么问题，同官网例子

### Evenlet带来的问题

socketio提供多种async_mode但要样socketio去使用websocket最好就是采用evenlet模式,这里列举一下使用evenlet所需要的环境:

```shell
apt install netbase
pip3 install python-socketio4.5.1
pip3 install eventlet0.20.1
```

> 单元测试并不使用项目打包后的环境所以缺少新安装的netbase库导致ci一直过不了

运行evenlet WSGI服务器

因为一些原因使用的是gunicorn的方式来启动evenlet，而gunicorn的配置如下:

```python
bind = "0.0.0.0:8000"
workers = 1
errorlog = '/web/log/error.log'
accesslog = '/web/log/access.log'
loglevel = 'warning'
proc_name = 'gunicorn_project'
timeout = 300
worker_class = 'eventlet'
```

这里有两个需要关注的问题，一个是`worker_class = 'eventlet'`工作task改为evenlet,另一个是workers = 1,这里是应为gunicorn负载均衡算法的限制使用了evenlet后只能有一个工作进程，一个eventlet worker可以处理大量并发客户机，每个客户机都由一个greenlet处理(greenlet是一个协程)。

在使用中希望执行一个后台任务
```python
sio.start_background_task(open_virtual_machines, configs, user.id)
```

但是阻塞了，因为整个django项目还是以多线程方式执行，官方提供一个猴子补丁函数monkey_patch() 它将标准库中的所有阻塞函数替换为等效的异步版本，也就是Django中使用socketio例子中的第7行代码。至此就解决了目前这一路碰到的问题了。

[参考]

[python-socketio文档](https://python-socketio.readthedocs.io/en/latest/server.html?highlight=monkey_patch)  
