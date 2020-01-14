---
title: "PostgreSQL新手入门"
date: 2019-11-09T16:55:31+08:00
categories: ["数据库", "新手入门"]
tags: ["PostgreSQL"]
toc: true
---

PostgreSQL被誉为“世界上功能最强大的开源数据库”，是以加州大学伯克利分校计算机系开发的POSTGRES 4.2为基础的对象关系型数据库管理系统。

<!--more-->

PostgreSQL支持大部分 SQL标准并且提供了许多其他现代特性：复杂查询、外键、触发器、视图、事务完整性、MVCC。同样，PostgreSQL 可以用许多方法扩展，比如，通过增加新的数据类型、函数、操作符、聚集函数、索引。

初次安装postgresql后，默认生成一个名为postgres的数据库和一个名为postgres的数据库用户。这里需要注意的是，同时还生成了一个名为postgres的Linux系统用户。

## 添加新用户和新数据库

### 第一种方法，使用PostgreSQL控制台。
首先，新建一个Linux新用户，可以取你想要的名字，这里为dbuser。

```sql
sudo adduser dbuser
```

然后，切换到postgres用户。
```sql
sudo su - postgres
```

下一步，使用psql命令登录PostgreSQL控制台。
```sql
psql
```

这时相当于系统用户postgres以同名数据库用户的身份，登录数据库，这是不用输入密码的。如果一切正常，系统提示符会变为"postgres=#"，表示这时已经进入了数据库控制台。以下的命令都在控制台内完成。

第一件事是使用\password命令，为postgres用户设置一个密码。
```sql
\password postgres
```

第二件事是创建数据库用户dbuser（刚才创建的是Linux系统用户），并设置密码。
```sql
CREATE USER dbuser WITH PASSWORD 'password';
```
第三件事是创建用户数据库，这里为exampledb，并指定所有者为dbuser。
```sql
CREATE DATABASE exampledb OWNER dbuser;
```
第四件事是将exampledb数据库的所有权限都赋予dbuser，否则dbuser只能登录控制台，没有任何数据库操作权限。
```sql
GRANT ALL PRIVILEGES ON DATABASE exampledb to dbuser;
```

最后，使用\q命令退出控制台（也可以直接按ctrl+D）。
```sql
\q
```

### 第二种方法，使用shell命令行。

添加新用户和新数据库，除了在PostgreSQL控制台内，还可以在shell命令行下完成。这是因为PostgreSQL提供了命令行程序createuser和createdb。还是以新建用户dbuser和数据库exampledb为例。

首先，创建数据库用户dbuser，并指定其为超级用户。
```sql
sudo -u postgres createuser --superuser dbuser
```
然后，登录数据库控制台，设置dbuser用户的密码，完成后退出控制台。
```sql
sudo -u postgres psql

\password dbuser

\q
```
接着，在shell命令行下，创建数据库exampledb，并指定所有者为dbuser。
```sql
sudo -u postgres createdb -O dbuser exampledb
```

## 登录数据库
```sql
psql -U dbuser -d exampledb -h 127.0.0.1 -p 5432
```
上面命令的参数含义如下：-U指定用户，-d指定数据库，-h指定服务器，-p指定端口。

输入上面命令以后，系统会提示输入dbuser用户的密码。输入正确，就可以登录控制台了。

psql命令存在简写形式。如果当前Linux系统用户，同时也是PostgreSQL用户，则可以省略用户名（-U参数的部分）。举例来说，我的Linux系统用户名为ruanyf，且PostgreSQL数据库存在同名用户，则我以ruanyf身份登录Linux系统后，可以直接使用下面的命令登录数据库，且不需要密码。
```sql
psql exampledb
```
此时，如果PostgreSQL内部还存在与当前系统用户同名的数据库，则连数据库名都可以省略。比如，假定存在一个叫做ruanyf的数据库，则直接键入psql就可以登录该数据库。
```sql
psql
```
另外，如果要恢复外部数据，可以使用下面的命令。
```sql
psql exampledb < exampledb.sql
```

## 控制台命令

除了前面已经用到的\password命令（设置密码）和\q命令（退出）以外，控制台还提供一系列其他命令。

```sql
\h：查看SQL命令的解释，比如\h select。
\?：查看psql命令列表。
\l：列出所有数据库。
\c [database_name]：连接其他数据库。
\d：列出当前数据库的所有表格。
\d [table_name]：列出某一张表格的结构。
\du：列出所有用户。
\e：打开文本编辑器。
\conninfo：列出当前数据库和连接的信息。
```
### postgresql数据导入导出

导出数据库
```sql
pg_dump -U user database > db.sql
```
导出具体表
```sql
pg_dump -U user database -t table > table.sql
```
导入数据库
```sql
psql -d database -f db.sql user
```
导入具体表
```sql
psql -d database -f table.sql user
```

## 角色属性（Role Attributes）

一个数据库角色可以有一系列属性，这些属性定义了他的权限。
```sql
ALTER ROLE <rolename> <attributes>;
```

属性|说明
---|---
login | 只有具有 LOGIN 属性的角色可以用做数据库连接的初始角色名。
superuser | 数据库超级用户
createdb | 创建数据库权限
createrole | 允许其创建或删除其他普通的用户角色(超级用户除外)
replication | 做流复制的时候用到的一个用户属性，一般单独设定。
password | 在登录时要求指定密码时才会起作用，比如md5或者password模式，跟客户端的连接认证方式有关
inherit | 用户组对组员的一个继承标志，成员可以继承用户组的权限特性
... | ...

## PostgreSQL 数据类型

PostgreSQL提 供了丰富的数据类型。用户可以使用 CREATE TYPE 命令在数据库中创建新的数据类型。下面列出的也只是部分数据类型。

### 数值类型

数值类型由 2 字节、4 字节或 8 字节的整数以及 4 字节或 8 字节的浮点数和可选精度的十进制数组成。

名字|	存储长度|	描述|	范围
---|---|---|---
smallint|	2 字节|	小范围整数|	-32768 到 +32767
integer|	4 字节|	常用的整数|	-2147483648 到 +2147483647
bigint|	8 字节|	大范围整数|	-9223372036854775808 到 +9223372036854775807
decimal|	可变长|	用户指定的精度，精确|	小数点前 131072 位；小数点后 16383 位
numeric|	可变长|	用户指定的精度，精确|	小数点前 131072 位；小数点后 16383 位
real|	4 字节|	可变精度，不精确|	6 位十进制数字精度
double precision|	8 字节|	可变精度，不精确|	15 位十进制数字精度
smallserial|	2 字节|	自增的小范围整数|	1 到 32767
serial|	4 字节|	自增整数|	1 到 2147483647
bigserial|	8 字节|	自增的大范围整数|	1 到 9223372036854775807

### 字符类型

下表列出了 PostgreSQL 所支持的字符类型：

序号|	名字 & 描述
---|---
1	| character varying(n), varchar(n) 变长，有长度限制
2	| character(n), char(n) f定长,不足补空白
3	| text 变长，无长度限制

### 日期/时间类型

下表列出了 PostgreSQL 支持的日期和时间类型。

名字	|存储空间	|描述	|最低值	|最高值	|分辨率
---|---|---|---|---|---
timestamp [ (p) ] [ without time zone ]	|8 字节	|日期和时间(无时区)	|4713 BC	|294276 AD	|1 毫秒 / 14 位
timestamp [ (p) ] with time zone	|8 字节	|日期和时间，有时区	|4713 BC	|294276 AD	|1 毫秒 / 14 位
date	|4 字节	|只用于日期	|4713 BC	|5874897 AD	|1 天
time [ (p) ] [ without time zone ]	|8 字节	|只用于一日内时间	|00:00:00	|24:00:00	|1 毫秒 / 14 位
time [ (p) ] with time zone	|12 字节	|只用于一日内时间，带时区	|00:00:00+1459	|24:00:00-1459	|1 毫秒 / 14 位
interval [ fields ] [ (p) ]	|12 字节	|时间间隔	|-178000000 年	|178000000 年	|1 毫秒 / 14 位

### 布尔类型

PostgreSQL 支持标准的 boolean 数据类型。

boolean 有"true"(真)或"false"(假)两个状态， 第三种"unknown"(未知)状态，用 NULL 表示。

名称	|存储格式	|描述
---|---|---
boolean	|1 字节	|true/false

### 网络地址类型

PostgreSQL 提供用于存储 IPv4 、IPv6 、MAC 地址的数据类型。

用这些数据类型存储网络地址比用纯文本类型好， 因为这些类型提供输入错误检查和特殊的操作和功能。

名字	|存储空间	|描述
---|---|---
cidr	|7 或 19 字节	|IPv4 或 IPv6 网络
inet	|7 或 19 字节	|IPv4 或 IPv6 主机和网络
macaddr	|6 字节	|MAC 地址

### UUID 类型

uuid 数据类型用来存储 RFC 4122，ISO/IEF 9834-8:2005 以及相关标准定义的通用唯一标识符（UUID）。 （一些系统认为这个数据类型为全球唯一标识符，或GUID。） 这个标识符是一个由算法产生的 128 位标识符，使它不可能在已知使用相同算法的模块中和其他方式产生的标识符相同。 因此，对分布式系统而言，这种标识符比序列能更好的提供唯一性保证，因为序列只能在单一数据库中保证唯一。

UUID 被写成一个小写十六进制数字的序列，由分字符分成几组， 特别是一组8位数字+3组4位数字+一组12位数字，总共 32 个数字代表 128 位， 一个这种标准的 UUID 例子如下：
```sql
a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11
```

## 数据库操作

基本的数据库操作，就是使用一般的SQL语言。

```sql
# 创建新表
CREATE TABLE user_tbl(name VARCHAR(20), signup_date DATE);

# 插入数据
INSERT INTO user_tbl(name, signup_date) VALUES('张三', '2013-12-22');

# 选择记录
SELECT * FROM user_tbl;

# 更新数据
UPDATE user_tbl set name = '李四' WHERE name = '张三';

# 删除记录
DELETE FROM user_tbl WHERE name = '李四' ;

# 添加栏位
ALTER TABLE user_tbl ADD email VARCHAR(40);

# 更新结构
ALTER TABLE user_tbl ALTER COLUMN signup_date SET NOT NULL;

# 更名栏位
ALTER TABLE user_tbl RENAME COLUMN signup_date TO signup;

# 删除栏位
ALTER TABLE user_tbl DROP COLUMN email;

# 表格更名
ALTER TABLE user_tbl RENAME TO backup_tbl;

# 删除表格
DROP TABLE IF EXISTS backup_tbl;
```

### FAQ

+ Peer authentication failed for user "postgres" 的解决办法

psql的连接建立于Unix Socket上默认使用peer authentication，所以必须要用和 数据库用户 相同的 系统用户 进行登录。
还有一种方法，将peer authentiction 改为 md5，并给数据库设置密码。修改配置文件
**/etc/postgresql/9.5/main/pg_hba.conf**
```sql
local   all             all                                     peer
修改为
local   all             all                                     md5
```

[参考]

[PostgreSQL新手入门](http://www.ruanyifeng.com/blog/2013/12/getting_started_with_postgresql.html)  
[PostgreSQL 数据类型](https://www.runoob.com/postgresql/postgresql-data-type.html)