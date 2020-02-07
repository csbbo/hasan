---
title: "下载Bing每日壁纸"
date: 2019-12-21T20:23:46+08:00
categories: ["Python"]
tags: ["thread"]
---

Bing首页每日更新的壁纸非常漂亮，爬下来当做桌面壁纸很nice。
Bing中提供了8天内的壁纸，而又以每天为参考一次最多可获取最近8天的壁纸。

<!--more-->

下载[https://cn.bing.com/](https://cn.bing.com/)每日更新的壁纸代码如下：

```python
#! /usr/bin python3
# -*- coding:utf-8 -*-

import sys
import os
import getopt
import requests
import re
from concurrent.futures import ThreadPoolExecutor

def get_and_save_picture(image, save_path='./'):
    if os.path.exists(save_path) is False:
        os.mkdir(save_path)
    url = 'https://cn.bing.com/'+image['url']
    name = re.findall(r'/th\?id=OHR\.(.+)_ZH-CN.*', image['url'])[0]
    filename = save_path+'/'+image['enddate']+'-'+name+'.jpg'
    with requests.get(url, stream=True) as r:
        with open(filename, 'wb') as f:
            for d in r.iter_content(128):
                f.write(d)

def get_picture_info(url, enddate: int = 0, nums: int = 1):
    url = url+'?format=js&idx='+str(enddate)+'&n='+str(nums)
    r = requests.get(url)
    images = r.json().get('images')
    return images

if __name__ == '__main__':
    options, _ = getopt.getopt(sys.argv[1:], 'd:n:D:h', ['date=', 'nums=', 'dir=', 'help'])
    options = dict(options)

    if options.get('-h') is not None or options.get('--help') is not None:
        print("-d   [--date]\n\t 一个数字表示，表示倒数第几天的图片，0表示今天,最大为7即能下载到倒数第八天的的图片")
        print("-n   [--nums]\n\t 一个数字表示，表示要下载的图片数量，最多8张")
        print("-D   [--dir]\n\t 图片存放路径，指定的路径不存在则创建，默认将图片下载到当前目录")
        print("-h   [--help]\n\t 使用说明")
        sys.exit()

    enddate = options.get('-d') if options.get('-d') else 0
    enddate = options.get('--date') if options.get('--date') else enddate
    nums = options.get('-n') if options.get('-n') else 8
    nums = options.get('--nums') if options.get('--nums') else nums
    path = options.get('-D') if options.get('-D') else './'
    path = options.get('--dir') if options.get('--dir') else path

    url = 'https://cn.bing.com/HPImageArchive.aspx'
    images = get_picture_info(url, enddate=enddate, nums=nums)

    pool = ThreadPoolExecutor(max_workers=8)
    for image in images:
        print(image['copyright'])
        pool.submit(get_and_save_picture, image, path)

    print("wait...")
```

环境:

`python3`,`requests`库

将代码保存为`.py`文件后直接运行默认下载最新8天的必应壁纸，使用`-h`或`--help`参数查看更多用法