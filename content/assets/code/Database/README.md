# warehouse

导出数据库（表结构和数据）：
```
mysqldump -u用户名 -p密码 数据库名 > 数据库名.sql

```
导出数据库（仅表结构）：
```
mysqldump -u用户名 -p密码 -d 数据库名 > 数据库名.sql

```
导入数据库：
```
mysql -u用户名 -p密码 数据库名 < 数据库名.sql

```
在Windows首次需要修改`root`密码：
```
alter user 'root'@'localhost' identified by 'chenshaobo';

```

查询表中某列出现次数大于8的前10条数据按从大到小顺序列出：
```
select sno,count(sno) as count from score group by sno having count > 1 order by count desc limit 0,3;

```
