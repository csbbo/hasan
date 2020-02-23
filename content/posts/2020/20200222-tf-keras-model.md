---
title: "TensorFlow搭建神经网络"
date: 2020-02-22T18:11:11+08:00
categories: ["Tensorflow"]
tags: ["Tensorflow", "python"]
toc: true
---
使用tf.keras搭建神经网络模型

<!--more-->
```python
#! /usr/bin/python3
# -*- encode:utf-8 -*-

import matplotlib as mpl
import matplotlib.pyplot as plt
import numpy as np
import sklearn
from sklearn.preprocessing import StandardScaler
import pandas as pd
import os
import sys
import time
import tensorflow as tf
from tensorflow import keras

# 加载分类数据集fashion_mnist
fashion_mnist = keras.datasets.fashion_mnist
(x_train_all, y_train_all), (x_test, y_test) = fashion_mnist.load_data()
x_valid, x_train = x_train_all[:5000], x_train_all[5000:]
y_valid, y_train = y_train_all[:5000], y_train_all[5000:]
# 打印数据集信息
# print(x_valid.shape, y_valid.shape)
# print(x_train.shape, y_train.shape)
# print(x_test.shape, y_test.shape)

# 打印训练集最大最小值
# print(np.max(x_train), np.min(x_train))

# 显示一张图片
# def show_single_image(img_arr):
#     plt.imshow(img_arr, cmap="binary")
#     plt.show()
# show_single_image(x_train[0])

# 显示多张图片
# def show_imgs(n_rows, n_cols, x_data, y_data, class_name):
#     assert len(x_data) == len(y_data)
#     assert n_rows * n_cols <= len(x_data)
#     plt.figure(figsize = (n_cols * 1.4, n_rows * 1.6))
#     for row in range(n_rows):
#         for col in range(n_cols):
#             index = n_cols * row + col
#             plt.subplot(n_rows, n_cols, index+1)
#             plt.imshow(x_data[index], cmap="binary", interpolation = 'nearest')
#             plt.axis('off')
#             plt.title(class_name[y_data[index]])
#     plt.show()
# class_name = ['T-shirt', 'Trouser', 'Pullover', 'Dress', 'Coat', 
#             'Sandal', 'Shirt', 'Sneaker', 'Bag', 'Ankle Bot']
# show_imgs(3, 5, x_train, y_train, class_name)

# ----------------归一化-----------------
# 归一化处理 (x - u) / std
# 其中u(均值) std(方差) 处理后得到均值是0方差是1标准正态分布
scaler = StandardScaler()
x_train_scaled = scaler.fit_transform(
    x_train.astype(np.float32).reshape(-1,1)).reshape(-1,28,28)

x_valid_scaled = scaler.transform(
    x_valid.astype(np.float32).reshape(-1,1)).reshape(-1,28,28)

x_test_scaled = scaler.transform(
    x_test.astype(np.float32).reshape(-1,1)).reshape(-1,28,28)
# fit_transform将数据处理为归一化后的数据，fit还有记录均值和方差的功能，验证集和测试集使用的也是训练集的均值和方差。
# 因为要做除法要将数据集中的数据转为np.float32的类型，而fit_transform处理的是二维的数据，所以要先将该数据reshape为二维的，归一化后在reshape回来

# 打印归一化后训练集的最大最小值
# print(np.max(x_train_scaled), np.min(x_train_scaled))

# ----------------搭建模型-----------------
model = keras.models.Sequential()
model.add(keras.layers.Flatten(input_shape=(28, 28)))
model.add(keras.layers.Dense(300, activation="relu"))
model.add(keras.layers.Dense(100, activation="relu"))
model.add(keras.layers.Dense(10, activation="softmax"))
# 这里用到两个激活函数
# relu: y = max(0,x)
# softmax: 将向量变成概率分布.  x = [x1, x2, x3],
#          y = [e^x1/sum, e^x2/sum, e^x3/sum] sum=e^x1/sum+e^x2/sum+e^x3/sum
# 参数的计算 
# [None, 784] * (W+b) -> [None, 300] ,W.shape=[784, 300] b=300, param=784*300+300
# 2-------搭建深度神经网络模型与批归一化--------
# model = keras.models.Sequential()
# model.add(keras.layers.Flatten(input_shape=(28, 28)))
# for _ in range(20):
#     model.add(keras.layers.Dense(100, activation="relu"))
#     model.add(keras.layers.BatchNormalization())
#     # 更改激活函数，selu自带归一化等同于上面两句
#     # model.add(keras.layers.Dense(100, activation="selu"))
#     """
#     # 把批归一化放在激活函数之前
#     model.add(keras.layers.Dense(100))
#     model.add(keras.layers.BatchNormalization())
#     model.add(keras.layers.Activation('relu))
#     """
# # dropout防止过拟合一般在最后几层添加，这里每添加一层dropout就是对它上面一层进行dropout
# model.add(keras.layers.AlphaDropout(rate=0.5)) # rate丢掉单元数目比例一般0.5
# # AlphaDropout: 1.均值和方差不变 2.归一化性质也不变
# model.add(keras.layers.Dense(10, activation="softmax"))


# ----------------构建图-----------------
model.compile(loss="sparse_categorical_crossentropy", optimizer="sgd", metrics=["accuracy"])
# sparse作用 index -> one_hot
# crossentropy 交叉熵损失函数

# 一些其他函数
# model.layers # 查看模型层数
# model.summary() # 查看模型概况

# --------------使用回调函数--------------
# EarlyStopping 在模型训练过程中loss不在下降可以提前将它给停下来
# ModelCheckpoint 模型训练过程中的中间状态，可以每个一段时间将其保存下来
# TensorBoard 可以查看模型训练过程的参数变化,启用方式"tensorboard --logdir=callbacks"
logdir = './callbacks'
if not os.path.exists(logdir):
    os.mkdir(logdir)
output_model_file = os.path.join(logdir, 'fashion_mnist_model.h5')

callbacks = [
    keras.callbacks.TensorBoard(logdir),
    keras.callbacks.ModelCheckpoint(output_model_file, save_best_only=True),
    keras.callbacks.EarlyStopping(patience=5, min_delta=1e-3)
]


# ----------------开始训练-----------------
history = model.fit(x_train_scaled, y_train, epochs=10,
                    validation_data=(x_valid_scaled, y_valid),
                    callbacks=callbacks)
# epochs遍历训练集次数
# validation_data每隔一段时间就对验证集做一个验证
# history返回数据集中间运行结果

# 用图打印出history.history运行过程中间一些指标的值
# def plot_learning_curves(history):
#     pd.DataFrame(history.history).plot(figsize=(8,5))
#     plt.grid(True)
#     plt.gca().set_ylim(0,1)
#     plt.show()
# plot_learning_curves(history)

# 在测试集上进行指标评估
# model.evaluate(x_test_scaled, y_test)
```

[requirements.txt](/accessory/requirements20200222.txt)