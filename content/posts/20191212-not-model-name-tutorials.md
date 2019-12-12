---
title: "No module named 'tensorflow.examples.tutorials'解决办法"
date: 2019-12-12T12:22:15+08:00
categories: ["Tensorflow"]
tags: ["tensorflow"]
---

**Python 3.7.4**

**tensorflow==2.0.0**
<!--more-->

导入MNIST数据集
```python
from tensorflow.examples.tutorials.mnist import input_data
```

出现报错

```
ModuleNotFoundError: No module named 'tensorflow.examples.tutorials'
```

解决办法:

1. 检查目录中是否含有`tutorials`,`...\Python3\Lib\site-packages`,该目录下有文件夹`tensorflow`, `tensorflow_core`, `ensorflow_estimator`

2. 进入`tensorflow_core\examples`文件夹，如果文件夹下只有`saved_model`这个文件，则是没有`tutorials`


3. github的tensorflow主页下载缺失的文件 网址为：[https://github.com/tensorflow/tensorflow](https://github.com/tensorflow/tensorflow)


4. 在`tensorflow-master\tensorflow\examples\`这里找到了`tutorials`，把`tutorials`整个文件夹拷贝到上文中提到的`...\Python3\Lib\site-packages\tensorflow_core\examples\`

大功告成!!!