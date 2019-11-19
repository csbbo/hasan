
if [[ $# -eq 1 ]]
then
	article=$1
	date=$(date "+%Y%m%d")

	title=$date"-"$article".md"

	args="posts/"$title

	hugo new $args
	echo "文件"$title"创建成功"
else
	echo "参数错误！！！"
fi

