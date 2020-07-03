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

> 此外还有日期/时间类型、布尔类型、网络地址类型、UUID类型

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


## 添加新用户和新数据库

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
`psql --help`

psql命令存在简写形式。如果当前Linux系统用户，同时也是PostgreSQL用户，则可以省略用户名（-U参数的部分）。举例来说，我的Linux系统用户名为ruanyf，且PostgreSQL数据库存在同名用户，则我以ruanyf身份登录Linux系统后，可以直接使用下面的命令登录数据库，且不需要密码。
```sql
psql exampledb
```
此时，如果PostgreSQL内部还存在与当前系统用户同名的数据库，则连数据库名都可以省略。比如，假定存在一个叫做ruanyf的数据库，则直接键入psql就可以登录该数据库。
```sql
psql
```

## 控制台常用命令

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
\i：执行sql脚本插入数据
```

> 删除PostgreSQL中的所有表,如果所有表都在单个模式中，则此方法可以工作`DROP SCHEMA public CASCADE;
CREATE SCHEMA public`

### postgresql数据导入导出
`pg_dump --help`

导出数据到data.sql
```sql
pg_dump -U trainingplatform -d trainingplatform -a --column-inserts -f data.sql
```

## 数据库操作

基本的数据库操作，就是使用一般的SQL语言。

### 创建数据库
1. 标准SQL语句
```sql
CREATE DATABASE dbname;
```
2. createdb命令
```shell
createdb -h localhost -p 5432 -U postgres testdb
```
### 连接数据库
```shell
psql -h localhost -p 5432 -U postgress testdb
```
### 删除数据库
1. 标准SQL语句
```sql
DROP DATABASE [ IF EXISTS ] testdb
```
2. dropdb命令
```shell
dropdb -h localhost -p 5432 -U postgres testdb
```

## 一些SQL
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

psql的连接建立于Unix Socket上默认使用peer authentication，所以必须要用和数据库用户相同的系统用户进行登录。
还有一种方法，将peer authentiction改为md5，并给数据库设置密码。修改配置文件
**/etc/postgresql/9.5/main/pg_hba.conf**
```sql
local   all             all                                     peer
修改为
local   all             all                                     md5
```
[参考]

[PostgreSQL新手入门](http://www.ruanyifeng.com/blog/2013/12/getting_started_with_postgresql.html)  
[PostgreSQL 数据类型](https://www.runoob.com/postgresql/postgresql-data-type.html)