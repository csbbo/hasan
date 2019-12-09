---
title: "Openssl生成SSL证书"
date: 2019-12-06T15:45:55+08:00
categories: ["Tool"]
tags: ["Openssl", "Nginx"]
toc: true
---

OpenSSL是一个强大的安全套接字层密码库，囊括主要的密码算法、常用的密钥和证书封装管理功能及SSL协议，并提供丰富的应用程序供测试或其它目的使用

<!--more-->

虽说我只是单纯的想生成HTTPS证书，但既然入了OpenSSL的坑就多了解一点了

### 对称加密算法

OpenSSL一共提供了8种对称加密算法，其中7种是分组加密算法，仅有的一种流加密算法是RC4。这7种分组加密算法分别是AES、DES、Blowfish、CAST、IDEA、RC2、RC5，都支持电子密码本模式（ECB）、加密分组链接模式（CBC）、加密反馈模式（CFB）和输出反馈模式（OFB）四种常用的分组密码加密模式。其中，AES使用的加密反馈模式（CFB）和输出反馈模式（OFB）分组长度是128位，其它算法使用的则是64位。事实上，DES算法里面不仅仅是常用的DES算法，还支持三个密钥和两个密钥3DES算法。 

### 非对称加密算法

OpenSSL一共实现了4种非对称加密算法，包括DH算法、RSA算法、DSA算法和椭圆曲线算法（EC）。DH算法一般用户密钥交换。RSA算法既可以用于密钥交换，也可以用于数字签名，当然，如果你能够忍受其缓慢的速度，那么也可以用于数据加密。DSA算法则一般只用于数字签名。

### 信息摘要算法

OpenSSL实现了5种信息摘要算法，分别是MD2、MD5、MDC2、SHA和RIPEMD。SHA算法事实上包括了SHA和SHA1两种信息摘要算法，此外，OpenSSL还实现了DSS标准中规定的两种信息摘要算法DSS和DSS1。

### 密钥和证书管理

OpenSSL实现了ASN.1的证书和密钥相关标准，提供了对证书、公钥、私钥、证书请求以及CRL等数据对象的DER、PEM和BASE64的编解码功能。OpenSSL提供了产生各种公开密钥对和对称密钥的方法、函数和应用程序，同时提供了对公钥和私钥的DER编解码功能。并实现了私钥的PKCS#12和PKCS#8的编解码功能。OpenSSL在标准中提供了对私钥的加密保护功能，使得密钥可以安全地进行存储和分发。 

OpenSSL提供的CA应用程序就是一个小型的证书管理中心（CA），实现了证书签发的整个流程和证书管理的大部分机制。

### 使用 OpenSSL 生产自签名 SSL 证书

<img src="/assets/article/20191206/gen-certificate.png" style="width:70%">

在当前路径生成文件夹和文件

```shell
mkdir -p demoCA/newcerts && touch demoCA/index.txt && echo 01 > demoCA/serial
```

制作CA证书

1. 生成ca.key CA私钥
```cpp
openssl genrsa -des3 -out ca.key 2048
//输入密码A
```

2. 生成ca.crt CA根证书(公钥)
```cpp
openssl req -new -x509 -days 7305 -key ca.key -out ca.crt
//输入密码A
//Common Name必填
```

制作网站证书并用CA签名认证

1. 生成server.key 网站CA私钥
```cpp
openssl genrsa -des3 -out server.key 2048
//输入密码B
```

2. 生成网站签名请求server.csr
```cpp
openssl req -new -key server.key -out server.csr
//输入密码B
//Common Name必填
```

3. 使用CA进行签名
```cpp
openssl ca -cert ca.crt -keyfile ca.key -policy policy_anything -days 365 -in server.csr -out server.crt
//输入密码A
```

> 至此自签名证书生成完成，最终需要：server.key 和 server.crt

或则可以直接利用server.key生成证书
```cpp
openssl genrsa -out server.key 1024
openssl req -new -key server.key -out server.csr
openssl x509 -req -in server.csr -out server.crt -signkey server.key -days 3650
```

### 配置Nginx使用自签名证书
```cpp
server {
        listen  80;
        server_name     domain;
        return  301     https://$host$request_uri;
}
server {
        listen  443 ssl;
        ssl_certificate       ssl/nginx.crt; # 前面生成的 crt 证书文件
        ssl_certificate_key   ssl/nginx.key; # 前面生成的证书私钥
        server_name     domain;
        location / {
            root /var/www-html;
            index  index.html;
        }
}
```