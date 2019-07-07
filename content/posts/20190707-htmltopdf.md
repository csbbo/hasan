---
title: "HtmlToPdf"
date: 2019-07-07T22:40:51+08:00
draft: true
---

最近用html写简历，打印的时候需要将html转成pdf,网上也有网站提供转换功能，但还是自己写一个转换程序更为方便些

#### 准备工作

安装pdfkit
```
pip install pdfkit
```
自己的操作系统上还需要装上wkhtmltopdf

我的系统是archlinux
```
sudo pacman -S wkhtmltopdf
```
然后其他的操作系统也类似

**Ubuntu**
```
sudo apt-get install wkhtmltopdf
```
**CentOS**
```
sudo yum intsall wkhtmltopdf
```
**Mac**
```
brew install Caskroom/cask/wkhtmltopdf
```
**Windows**
```
下载地址：https://wkhtmltopdf.org/downloads.html
并配置系统环境变量
```

最是炒鸡简单Python程序代码
```python3
#! /usr/bin/python3
# -*- coding:utf-8 -*-

import pdfkit

options={
   'page-size':'A4',#Letter
    'margin-top':'0.4in',
    'margin-right':'0.6in',
    'margin-bottom':'0.4in',
    'margin-left':'0.6in',
    'encoding':"UTF-8",
    'no-outline':None
}  

pdfkit.from_file('./in.html','out.pdf',options=options)
```