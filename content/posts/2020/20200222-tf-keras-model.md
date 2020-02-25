---
title: "TensorFlow搭建神经网络"
date: 2020-02-22T18:11:11+08:00
categories: ["Tensorflow笔记"]
tags: ["Tensorflow", "python"]
toc: true
---

keras是基于python的高级神经网络API，由Francois Chollet编写，支持以Tensorflow、CNTK、Theano为后端运行。
而Tensorflow-keras是对keras API规范的实现，实现在`tf.keras`空间下与Tensorflow结合也更紧密并且还添加了一些keras没有的特性

<!--more-->

Tf-keras和keras区别:

- Tf-keras全面支持eager model
    - 只是使用keras.Sequential和keras.Model时没影响
    - 自定义Model内部运算逻辑会有影响
        - Tf低层API可以使用keras的model.fit等抽象
        - 适用与研究人员
- Tf-keras支持基于tf.data的模型训练
- Tf-keras支持TPU训练
- Tf-keras支持tf.distribution中分布式策略
- 其它特性
    - Tf-keras可以与Tensorflow中的estimator集成
    - Tf-keras可以保存为SavedModel

### 分类问题和回归问题

分类问题预测的是类别，输出的是概率分布，如三分类问题输出例子: [0.2,0.7,0.1]

回归问题预测的是值，模型输出是一个实数值

### 目标函数

模型的参数是逐步调整的，而目标函数可以帮助衡量模型的好坏。模型训练其实就是调整参数，使得目标函数逐渐变小的过程。

就分类问题来说我们需要衡量当前预测与目标类别的差距，如
预测输出: [0.2,0.7,0.1]
真实类别: 2 -> one_hot -> [0,0,1]

计算目标函数方法
- 平方差损失 <sup>1</sup>&frasl;<sud>n</sud>&sum;<sup>1</sup>&frasl;<sud>2</sud>(y-Model(x))<sup>2</sup>
- 交叉熵损失 <sup>1</sup>&frasl;<sud>n</sud>&sum;yln(Model(x))

回归问题中目标函数即计算预测值与真实值的差距， 如

- 平方差损失
- 绝对值损失

### 分类模型
tf.keras搭建分类模型，数据归一化，深度神经网络与批归一化，激活函数、回调函数使用，dropout防止过拟合

```python
# tf_keras_classification_model.py

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

# ----------------1.搭建模型-----------------
model = keras.models.Sequential()
model.add(keras.layers.Flatten(input_shape=(28, 28)))
model.add(keras.layers.Dense(300, activation="relu"))
model.add(keras.layers.Dense(100, activation="relu"))
model.add(keras.layers.Dense(10, activation="softmax"))
# 这里用到两个激活函数(增加模型表达力)
# relu: y = max(0,x)
# softmax: 将向量变成概率分布.  x = [x1, x2, x3],
#          y = [e^x1/sum, e^x2/sum, e^x3/sum] sum=e^x1/sum+e^x2/sum+e^x3/sum
# 参数的计算 
# [None, 784] * (W+b) -> [None, 300] ,W.shape=[784, 300] b=300, param=784*300+300
# -------2.搭建深度神经网络模型与批归一化--------
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

### 回归模型

回归问题模型搭建，函数式API实现wide&deep模型、子类API实现wide&deep模型、多输入与多输出模型的实现

```python
# tf_keras_regression_wide_deep.py

import matplotlib as mpl
import matplotlib.pyplot as plt
import numpy as np
import sklearn
import pandas as pd
import os
import sys
import time
import tensorflow as tf
from tensorflow import keras
from sklearn.datasets import fetch_california_housing
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler

housing = fetch_california_housing()

# 加利福尼亚房价预测数据集
x_train_all, x_test, y_train_all, y_test = train_test_split(
    housing.data, housing.target, random_state = 7)
x_train, x_valid, y_train, y_valid = train_test_split(
    x_train_all, y_train_all, random_state = 11)
# print(x_train.shape, y_train.shape)
# print(x_valid.shape, y_valid.shape)
# print(x_test.shape, y_test.shape)

# ----------------归一化-----------------
scaler = StandardScaler()
x_train_scaled = scaler.fit_transform(x_train)
x_valid_scaled = scaler.transform(x_valid)
x_test_scaled = scaler.transform(x_test)

# ---1.函数式API 功能API实现wide&deep模型----
# input = keras.layers.Input(shape=x_train.shape[1:])
# hidden1 = keras.layers.Dense(30, activation='relu')(input)
# hidden2 = keras.layers.Dense(30, activation='relu')(hidden1)

# concat = keras.layers.concatenate([input, hidden2])
# output = keras.layers.Dense(1)(concat)

# model = keras.models.Model(inputs = [input],
#                            outputs = [output])

# ------2.子类API实现实现wide&deep模型-----
# class WideDeepModel(keras.models.Model):
#     def __init__(self):
#         super(WideDeepModel, self).__init__()
#         """定义模型层次"""
#         self.hidden1_layer = keras.layers.Dense(30, activation='relu')
#         self.hidden2_layer = keras.layers.Dense(30, activation='relu')
#         self.output_layer = keras.layers.Dense(1)
#     def call(self, input):
#         """完成模型的正向计算"""
#         hidden1 = self.hidden1_layer(input)
#         hidden2 = self.hidden2_layer(hidden1)
#         concat = keras.layers.concatenate([input, hidden2])
#         output = self.output_layer(concat)
#         return output
# model = WideDeepModel()
# model.build(input_shape=(None, 8))

# --------------3.普通模型-----------------
model = keras.models.Sequential([
    keras.layers.Dense(30, activation='relu',
                       input_shape=x_train.shape[1:]),
    keras.layers.Dense(1),
])

# ---------------4.多输入------------------
# input_wide = keras.layers.Input(shape=[5])
# input_deep = keras.layers.Input(shape=[6])
# hidden1 = keras.layers.Dense(30, activation='relu')(input_deep)
# hidden2 = keras.layers.Dense(30, activation='relu')(hidden1)
# concat = keras.layers.concatenate([input_wide, hidden2])
# output = keras.layers.Dense(1)(concat)
# output2 = keras.layers.Dense(1)(hidden2) #多输出第二个输出
# model = keras.models.Model(inputs = [input_wide, input_deep],
#                             outputs = [output, output2])

# model.summary()
# ----------------构建图-----------------
model.compile(loss="mean_squared_error", optimizer="sgd")

# --------------使用回调函数--------------
callbacks = [
    keras.callbacks.EarlyStopping(patience=5, min_delta=1e-2)
]

# ----------------开始训练-----------------
history = model.fit(x_train_scaled, y_train,
                    validation_data = (x_valid_scaled, y_valid),
                    epochs = 100,
                    callbacks = callbacks)

# ---------开始训练(多输入多输出模型启用)------
# x_train_scaled_wide = x_train_scaled[:, :5]
# x_train_scaled_deep = x_train_scaled[:, 2:]
# x_valid_scaled_wide = x_valid_scaled[:, :5]
# x_valid_scaled_deep = x_valid_scaled[:, 2:]
# x_test_scaled_wide = x_test_scaled[:, :5]
# x_test_scaled_deep = x_test_scaled[:, 2:]
# history = model.fit([x_train_scaled_wide, x_train_scaled_deep],
#                     [y_train, y_train],
#                     validation_data = ([x_valid_scaled_wide, x_valid_scaled_deep],
#                     [y_valid, y_valid]),
#                     epochs = 100,
#                     callbacks = callbacks)

# 打印学习曲线图
def plot_learning_curves(history):
    pd.DataFrame(history.history).plot(figsize=(8, 5))
    plt.grid(True)
    plt.gca().set_ylim(0, 1)
    plt.show()
plot_learning_curves(history)

# 在测试集上进行指标评估
model.evaluate(x_test_scaled, y_test)

# # 在测试集上进行指标评估(多输入多输出模型启用)
# model.evaluate([x_test_scaled_wide, x_test_scaled_deep], [y_test, y_test])
```

### 超参数搜索

超参数就是在神经网络训练中不变的参数，如

- 网络结构参数: 神经网络的层数、每层宽度、每层激活函数
- 训练参数: batch_size、学习率和衰减算法

而手工去寻找超参数是很耗时的，我们需要一些搜索策略

- 网格搜索
- 随机搜索
- 遗传算法
- 启发式搜索(AutoML研究热点，循环神经网络生成)

```python
# tf_keras_regression_hp_search_sklearn.py

import matplotlib as mpl
import matplotlib.pyplot as plt
import numpy as np
import sklearn
import pandas as pd
import os
import sys
import time
import tensorflow as tf
from tensorflow import keras
from sklearn.datasets import fetch_california_housing
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler

housing = fetch_california_housing()

# 加利福尼亚房价预测数据集
x_train_all, x_test, y_train_all, y_test = train_test_split(
    housing.data, housing.target, random_state = 7)
x_train, x_valid, y_train, y_valid = train_test_split(
    x_train_all, y_train_all, random_state = 11)

# ----------------归一化-----------------
scaler = StandardScaler()
x_train_scaled = scaler.fit_transform(x_train)
x_valid_scaled = scaler.transform(x_valid)
x_test_scaled = scaler.transform(x_test)

# 1.tf.keras model -> sklear model
# 2.定义参数集合
# 3.使用RandomizedSearchCV搜索参数

def build_model(hidden_layers=1, layer_size=30, learning_rate = 3e-3):
    model = keras.models.Sequential()
    model.add(keras.layers.Dense(layer_size, activation='relu',
                                    input_shape=x_train.shape[1:]))
    for _ in range(hidden_layers-1):
        model.add(keras.layers.Dense(layer_size, activation='relu'))
    model.add(keras.layers.Dense(1))
    optimizer = keras.optimizers.SGD(learning_rate)
    model.compile(loss='mse', optimizer=optimizer)
    return model

sklearn_model = keras.wrappers.scikit_learn.KerasRegressor(build_model)

callbacks = [
    keras.callbacks.EarlyStopping(patience=5, min_delta=1e-2)
]
history = sklearn_model.fit(x_train_scaled, y_train,
                    validation_data = (x_valid_scaled, y_valid),
                    epochs = 100,
                    callbacks = callbacks)

# 打印学习曲线图
def plot_learning_curves(history):
    pd.DataFrame(history.history).plot(figsize=(8, 5))
    plt.grid(True)
    plt.gca().set_ylim(0, 1)
    plt.show()
plot_learning_curves(history)

# 在测试集上进行指标评估(sklearn没有evaluate函数)
# model.evaluate(x_test_scaled, y_test)

from scipy.stats import reciprocal
# f(x) = 1/(x*log(b/a)) a<=x<=b

param_distribution = {
    'hidden_layers': [1,2,3,4],
    'layer_size': np.arange(1,100),
    'learning_rate': reciprocal(1e-4, 1e-2),
}

from sklearn.model_selection import RandomizedSearchCV

random_search_cv = RandomizedSearchCV(sklearn_model,
                                      param_distribution,
                                      n_iter = 10,
                                      cv = 3,
                                      n_jobs = 1)
random_search_cv.fit(x_train_scaled, y_train, epochs=100,
                        validation_data = (x_valid_scaled, y_valid),
                        callbacks = callbacks)

print(random_search_cv.best_params_)
print(random_search_cv.best_score_)
print(random_search_cv.best_estimator_) # 最好的model

model = random_search_cv.best_estimator_.model
model.evaluate(x_test_scaled, y_test)
```

### 基础API

```python
# 常量

t = tf.constant([[1., 2., 3.], [4., 5., 6.]])
print(t)
# 像numpy一样取值
print(t[:, 1:])
print(t[..., 1])

# 运算
print(t+10)
print(tf.square(t))
print(t @ tf.transpose(t))

# 与numpy互转
print(t.numpy())
print(np.square(t))
np_t = np.array([[1., 2., 3.], [4., 5., 6.]])
print(tf.constant(np_t))

# 0维常量
t = tf.constant(2.718)
print(t.numpy())
print(t.shape)

# 字符串
t = tf.constant('cafe')
print(tf.strings.length(t))
print(tf.strings.length(t, unit='UTF8_CHAR'))
print(tf.strings.unicode_decode(t, 'UTF8'))

# 字符串数组

t = tf.constant(['cafe', 'coffee', '咖啡'])
print(tf.strings.length(t, unit='UTF8_CHAR'))
r = tf.strings.unicode_decode(t, 'UTF8')
print(r)

# ragged tensor
r = tf.ragged.constant([[11,12], [21,22,23],[],[41]])
print(r)
print(r[1])
print(r[1:2])

r2 = tf.ragged.constant([[51,52], [], [71]])
print(tf.concat([r,r2], axis=0))
print(r2.to_tensor())

# sparse tensor

s = tf.SparseTensor(indices = [[0,1],[1,0],[2,3]],
                    values = [1., 2., 3.],
                    dense_shape = [3, 4])
print(s)
print(tf.sparse.to_dense(s))

s2 = s * 2.0
print(s2)

try:
    s3 = s + 1
except TypeError as ex:
    print(ex)

s4 = tf.constant([[10., 20.],
                   [30., 40.],
                   [50., 60.],
                   [70., 80.]])
print(tf.sparse.sparse_dense_matmul(s, s4))


# 变量

v = tf.Variable([[1., 2., 3.], [4., 5., 6.]])
print(v)
print(v.value())
print(v.numpy())

# 赋值

v.assign(2*v)
print(v.numpy())
v[0,1].assign(42)
print(v.numpy())
v[1].assign([7., 8., 9.])
print(v.numpy())


# 自定义损失函数与DenseLayer

def customized_mse(y_true, y_pred):
    return tf.reduce_mean(tf.square(y_true - y_pred))

class CostomizedDenseLayer(keras.layers.Layer):
    def __init__(self, units, activation=None, **kw):
        self.units = units
        self.activation = keras.layers.Activation(activation)
        super(CostomizedDenseLayer, self).__init__(**kw)

    def build(self, input_shape):
        """构建所需要的参数"""
        # x*w+b 
        self.kernel = self.add_weight(name='kernel',
                                        shape = (input_shape[1], self.units),
                                        initializer = 'uniform',
                                        trainable = True)
        self.bias = self.add_weight(name='bias',
                                    shape=(self.units),
                                    initializer = 'zeros',
                                    trainable = True)
        super(CostomizedDenseLayer, self).build(input_shape)
    
    def call(self, x):
        """完成正向计算"""
        return self.activation(x @ self.kernel + self.bias)
# use
model = keras.models.Sequentail([
    CostomizedDenseLayer(30, activation='relu',
                            input_shape = x_train_shape[1:]),
    CostomizedDenseLayer(1)
])

# tf.function python函数转图

def scaled_elu(z, scale=1.0, alpha=1.0):
    is_position = tf.greater_equal(z, 0.0)
    return scale * tf.where(is_position, z, alpha * tf.nn.elu(z))

scaled_elu_tf = tf.function(scaled_elu)
print(scaled_elu_tf.python_function is scaled_elu)

# 函数签名
@tf.function(input_signature=[tf.TensorSpec([None], tf.int32, name='x')])
def cube(z):
    return tf.pow(z, 3)

try:
    print(cube(tf.constant([1., 2., 3.])))
except ValueError as ex:
    print(ex)

print(cube(tf.constant([1,2,3])))

# 图结构
# ...
# 近似求导

def f(x):
    return 3. * x**2 + 2. * x - 1

def aproximate_derivative(f, x, eps=1e-3):
    return (f(x+eps) - f(x-eps)) / (2. * eps)

print(aproximate_derivative(f, 1.))

def g(x1, x2):
    return (x1 + 5) * (x2 ** 2)

def aproximate_gradient(g, x1, x2, eps=1e-3):
    dg_x1 = aproximate_derivative(lambda x: g(x,x2), x1, eps)
    dg_x2 = aproximate_derivative(lambda x: g(x1, x), x2, eps)
    return dg_x1, dg_x2

print(aproximate_gradient(g, 2, 3))

# tensorflow求导
x1 = tf.Variable(2.)
x2 = tf.Variable(3.)

with tf.GradientTape(persistent=True) as tape:
    z = g(x1, x2)

dz_x1 = tape.gradient(z, x1)
dz_x2 = tape.gradient(z, x2)
dz_x1_x2 = tape.gradient(z, [x1, x2])
print(dz_x1,dz_x2, dz_x1_x2)
del tape

# 与optimizer结合
learning_rate = 0.1
x = tf.Variable(0.0)

optimizer = keras.optimizer.SGD(lr = learning_rate)
for _ in range(100):
    with tf.GradientTape() as tape:
        z = f(x)
    dz_dx = tape.gradient(z, x)
    x.assign_sub(learning_rate * dz_dx)
    optimizer.apply_gradients([dz_dx, x])

print(x)

# 与tf.keras结合使用
# ...
```
Tensorflow学习笔记系列均使用该环境
python --version
3.6.10
[requirements.txt](/accessory/requirements20200222.txt)