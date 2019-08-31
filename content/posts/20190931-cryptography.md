---
title: "Python字符串加密解密"
date: 2019-08-31T23:49:08+08:00
tags: ["crypto"]
---

之前接触比较多的加密还是非对称的,都不知道对称加密怎么玩了,莫名的想试下.感谢Python当工具用真的是无往不利,这里用到的库是[cryptography](https://github.com/pyca/cryptography),做的需求是给一篇文章加密,等到使用的时候再解密获取内容.

### 官方示例
```python
>>> from cryptography.fernet import Fernet
>>> # Put this somewhere safe!
>>> key = Fernet.generate_key()
>>> f = Fernet(key)
>>> token = f.encrypt(b"A really secret message. Not for prying eyes.")
>>> token
'...'
>>> f.decrypt(token)
'A really secret message. Not for prying eyes.'
```
<!--more-->
cryptography安装
```
pip install cryptography
```

1. 首先导入包Fernet
2. 实例化Fernet需要唯一的参数key,这个参数要求有点高，不是随便一个字节序列就行，要求32位 + url-safe + base64-encoded 的bytes类型。为了方便，Fernet类内置了生成key的类方法: generate_key()，作为加密解密的钥匙，生成的key要保存好，以供解密的时候使用
3. 实例化一个Fernet对象。
4. 接下来就是加密方法: fernet.encrypt(data) 接受一个bytes类型的数据，返回一个加密后的bytes类型数据(人类看不懂)，俗称 token-Fernet。
5. 解密fernet.decrypt(token)

# 使用实例

```python
import sys
from cryptography.fernet import Fernet

def read_article(path):
    with open(path, 'r') as f:
        article = f.read()
    return article

if __name__ == '__main__':
    params = len(sys.argv)
    if params == 1: # 生成秘钥
        key = Fernet.generate_key() # 生成加密解密秘钥
        print(key)
    elif params == 3: # 加密
        path = sys.argv[1]
        key = sys.argv[2]
        f = Fernet(key)
        content = read_article(path)
        token = f.encrypt(content.encode('utf-8'))
        content = str(token,encoding='utf-8')
        print(content)
    elif params == 4: # 解密
        path = sys.argv[1]
        key = sys.argv[2]
        decode = sys.argv[3]
        f = Fernet(key)
        if decode == '-d':
            content = read_article(path)
            token = bytes(content,encoding='utf-8')
            try:
                string = f.decrypt(token).decode('utf-8')
                print(string)
            except:
                print("密码错误!!!")
        else:
            print("参数错误!!!")
    else: # 参数错误
        print("生成秘钥: python cripto.py\n")
        print("加密: python cripto.py articlepath key")
        print("解密: python cripto.py articlepath key -d")

```

> 这就完成了对文档的加密解密了,但内容都在终端输出了可以用`>`重定向到文件