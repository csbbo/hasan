#!/bin/bash

port="1313"
if [[ $# -eq 1 ]]
then
	if [[ $1 -eq "stop" ]]
	then
		pkill hugo
		exit
	fi
	port=$1
fi

(hugo server -D -p $port > /dev/null 2>&1 &)

url="http://localhost:"$port

# only deal for mac OS
open $url

echo "本地hugo服务已启动"
echo $url
echo "关闭服务:./run.sh stop"
