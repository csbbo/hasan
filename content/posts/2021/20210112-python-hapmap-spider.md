---
title: "爬虫"
date: 2021-01-12T14:18:16+08:00
categories: ["Python技能图谱"]
tags: ["Python"]
toc: true
---

爬虫与反爬虫

<!--more-->

### Scrapy

[Scrapy入门教程](https://scrapy-chs.readthedocs.io/zh_CN/0.24/intro/tutorial.html)


### 反爬虫

+ cookie验证， 目前server对客户端的标识多采用cookie

+ HTTP头部伪造，发送请求时可以伪造头部字段，但源IP无法伪造，因为错误的IP地址将导致数据无法传回

+ WebDriver，Selenium WebDriver 是一个支持浏览器自动化的工具。它包括一组为不同语言提供的类库和“驱动”（drivers）可以使浏览器上的动作自动化

+ 验证码识别，通过验证码识别能够有效拦截机器请求

+ 随机代理IP，网站通常会根据某个IP的请求频率来分析是否是机器行为，通过随机代理IP的方法可以减少被封禁的概率

+ 分布式爬虫，单机Scrapy虽然通过异步加多线程来并发获取数据，但还是局限于一台机器，效率有限。Scrapy通过维护共享爬取队列，多台主机同时执行爬取任务，爬取效率会成倍提高

### 技能图谱
<img src="/assets/2020/1222/python-hapmap.jpg" style="border: 1px solid #e0e0e0"/>