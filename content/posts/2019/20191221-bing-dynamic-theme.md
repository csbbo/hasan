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
from concurrent.futures import ThreadPoolExecutor

def get_and_save_picture(url, filename):
    with requests.get(url, stream=True) as r:
        with open(filename, 'wb') as f:
            for d in r.iter_content(128):
                f.write(d)

def get_picture_info(url, date: int = 0, nums: int = 1):
    HEADERS = {
        'accept-language': 'zh-CN,zh;q=0.9,en;q=0.8',
    }
    url = url+'?format=js&idx='+str(date)+'&n='+str(nums)+'&nc=1596768309402&pid=hp&uhd=1&uhdwidth=3840&uhdheight=2160'
    r = requests.get(url, headers=HEADERS)
    print(r.headers)
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
        # title = image['copyright'].split(' ')[0]
        # filename = image['enddate'] + '-' + title + '.jpg'
        filename = image['enddate'] + '.jpg'

        print(image['copyright'], 'Save As '+filename)

        save_name = args.file_path+'/'+filename
        url = 'https://cn.bing.com/'+image['url']
        pool.submit(get_and_save_picture, url, save_name)

    print("please wait...")
```

golang(新增)
```go
package main

import (
	"bytes"
	"encoding/json"
	"flag"
	"fmt"
	"io"
	"io/ioutil"
	"net/http"
	"os"
	"strconv"
	"sync"
)

const (
	BaseUrl = "https://cn.bing.com/"
	Url     = "https://cn.bing.com/HPImageArchive.aspx"
)

var wg sync.WaitGroup

var (
	help bool
	path string
	date int
	num  int
)

type Image struct {
	Startdate     string `json:"startdate"`
	Fullstartdate string `json:"fullstartdate"`
	Enddate       string `json:"enddate"`
	Url           string `json:"url"`
	Urlbase       string `json:"urlbase"`
	Copyright     string `json:"copyright"`
	Copyrightlink string `json:"copyrightlink"`
	Title         string `json:"title"`
	Caption       string `json:"caption"`
	Copyrightonly string `json:"copyrightonly"`
	Desc          string `json:"desc"`
	Date          string `json:"date"`
	Quiz          string `json:"quiz"`
	Wp            bool   `json:"wp"`
	Hsh           string `json:"hsh"`
	Drk           int    `json:"drk"`
	Top           int    `json:"top"`
	Bot           int    `json:"bot"`
}

type RespPictrueInfo struct {
	Images   []Image                `json:"images"`
	Tooltips map[string]interface{} `json:"tooltips`
}

func init() {
	flag.BoolVar(&help, "help", false, "this help")
	flag.StringVar(&path, "path", "./", "表示要下载的图片数量，最多8张")
	flag.IntVar(&date, "d", 0, "表示倒数第几天的图片，0表示今天,最大为7即能下载到倒数第八天的的图片")
	flag.IntVar(&num, "n", 8, "表示要下载的图片数量，最多8张")
}

func main() {
	flag.Parse()

	_, err := os.Stat(path)
	if err != nil || os.IsNotExist(err) {
		// fmt.Println("路径不存在")
		// os.Exit(0)
		err = os.Mkdir(path, os.ModePerm)
		if err != nil {
			fmt.Println("文件夹创建失败")
		}
	}

	images, err := getPictureInfo(Url, date, num)
	if err != nil {
		fmt.Println("图片信息获取失败")
	}

	for _, image := range images {
		filename := image.Enddate + ".jpg"
		savename := path + "/" + filename
		url := BaseUrl + image.Url
		wg.Add(1)
		go getAndSave(url, savename)
	}
	wg.Wait()
	fmt.Println("all finish!")
}

func getPictureInfo(url string, date int, num int) ([]Image, error) {
	url = url + "?format=js&idx=" + strconv.Itoa(date) + "&n=" + strconv.Itoa(num) + "&nc=1596768309402&pid=hp&uhd=1&uhdwidth=3840&uhdheight=2160"
	resp, err := http.Get(url)
	if err != nil {
		fmt.Println("网络错误")
	}

	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		fmt.Println("数据流读取失败")
	}

	var jsonResp RespPictrueInfo
	err = json.Unmarshal(body, &jsonResp)
	if err != nil {
		fmt.Println("Unmarshal失败")
	}
	return jsonResp.Images, nil
}

func getAndSave(url string, savename string) {
	defer wg.Done()
	resp, err := http.Get(url)
	if err != nil {
		fmt.Println("网络错误")
	}

	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		fmt.Println("数据流读取失败")
	}

	out, _ := os.Create(savename)
	io.Copy(out, bytes.NewReader(body))
	fmt.Println(savename + " save done")
}
```

环境:

`python3`,`requests`库

将代码保存为`.py`文件后直接运行默认下载最新8天的必应壁纸，使用`-h`或`--help`参数查看更多用法