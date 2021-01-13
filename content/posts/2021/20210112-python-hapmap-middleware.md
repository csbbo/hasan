---
title: "中间件"
date: 2021-01-12T16:09:12+08:00
categories: ["Python技能图谱"]
tags: ["Python"]
toc: true
---

消息队列、分布式缓存、RPC

<!--more-->

### 消息队列 (RabbitMQ)

macOS install RabbitMQ
```shell
brew install rabbitmq
brew services start rabbitmq
```

send.py
```python
#!/usr/bin/env python
import pika

connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))
channel = connection.channel()

channel.queue_declare(queue='hello')

channel.basic_publish(exchange='', routing_key='hello', body='Hello World!')
print(" [x] Sent 'Hello World!'")
connection.close()
```

receive.py
```python
#!/usr/bin/env python
import pika, sys, os

def main():
    connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))
    channel = connection.channel()

    channel.queue_declare(queue='hello')

    def callback(ch, method, properties, body):
        print(" [x] Received %r" % body.decode())

    channel.basic_consume(queue='hello', on_message_callback=callback, auto_ack=True)

    print(' [*] Waiting for messages. To exit press CTRL+C')
    channel.start_consuming()

if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        print('Interrupted')
        try:
            sys.exit(0)
        except SystemExit:
            os._exit(0)
```

### 分布式缓存 (Redis)

[redis初探](https://blog.shaobo.fun/posts/2019/20190914-redis-exploration/)

### RPC (GRPC)

[grpc基础](https://blog.shaobo.fun/posts/2019/20191216-grpc-basics/)