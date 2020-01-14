#! /bin/bash

exec_path=`pwd`
echo $exec_path

for file in $*
do
    command="docker cp $file tp_server:/web/$file"
    docker cp $file tp_server:/web/$file
    echo $command
done

docker restart tp_server
echo "restart tp_server success!"