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
import argparse
import requests
import re
from concurrent.futures import ThreadPoolExecutor

def get_and_save_picture(url, filename):
    with requests.get(url, stream=True) as r:
        with open(filename, 'wb') as f:
            for d in r.iter_content(128):
                f.write(d)

def get_picture_info(url, date: int = 0, nums: int = 1):
    url = url+'?format=js&idx='+str(date)+'&n='+str(nums)
    r = requests.get(url)
    images = r.json().get('images')
    return images

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Bing Wallpaper Updates Daily')
    parser.add_argument('file_path', type=str, help='图片存放路径，默认将图片下载到当前目录')
    parser.add_argument('--date', type=int, default=0, help='表示倒数第几天的图片，0表示今天,最大为7即能下载到倒数第八天的的图片')
    parser.add_argument('--nums', type=int, default=8, help='表示要下载的图片数量，最多8张')

    args = parser.parse_args()

    if not os.path.exists(args.file_path):
        os.mkdir(args.file_path)

    url = 'https://cn.bing.com/HPImageArchive.aspx'
    images = get_picture_info(url, date=args.date, nums=args.nums)

    pool = ThreadPoolExecutor(max_workers=8)
    for image in images:
        name = re.findall(r'/th\?id=OHR\.(.+)_ZH-CN.*', image['url'])[0]
        filename = image['enddate']+'-'+name+'.jpg'

        print(image['copyright'], 'Save As '+filename)

        save_name = args.file_path+'/'+filename
        url = 'https://cn.bing.com/'+image['url']
        pool.submit(get_and_save_picture, url, save_name)

    print("please wait...")
```

环境:

`python3`,`requests`库

将代码保存为`.py`文件后直接运行默认下载最新8天的必应壁纸，使用`-h`或`--help`参数查看更多用法