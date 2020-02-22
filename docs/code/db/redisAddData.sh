#! /bin/bash

# 往Redis注入2000万条String数据

cat /dev/null > redisdata.txt 
for((i=1;i<=20000000;i++));	
do
	echo "set k$i v$i" >> redisdata.txt
done

cat redisdata.txt | redis-cli > /dev/null

rm redisdata.txt
