---
title: "Linux三剑客"
date: 2019-10-24T22:10:44+08:00
tags: ["Linux"]
categories: ["Tool"]
toc: true
---

在Linux系统当中，处理文本有三个常用的模式匹配命令 **awk** **sed** **grep**  ，这三个命令十分灵活，应该熟练掌握。

<!--more-->

## Awk

awk 是一种很棒的语言，它适合文本处理和报表生成，其语法较为常见，借鉴了某些语言的一些精华，如 C 语言等。在 linux 系统日常处理工作中，发挥很重要的作用。

### awk的原理

```awk
awk '{print $0}' /etc/passwd
echo hhh|awk '{print "hello,world"}'
awk '{ print "hiya" }' /etc/passwd 
```

通过上面的命令知道，awk可以从文件或输入流中获取数据，依次对每一行都执行print命令，最后将结果输出到stdout。

### awk脚本基本结构

```awk
awk 'BEGIN{ print "start" } pattern{ commands } END{ print "end" }' file
```

一个awk脚本通常由：BEGIN语句块、能够使用模式匹配的通用语句块、END语句块3部分组成，这三个部分是可选的。任意一个部分都可以不出现在脚本中，脚本通常是被单引号或双引号中，例如：

```awk
awk 'BEGIN{ i=0 } { i++ } END{ print i }' filename
awk "BEGIN{ i=0 } { i++ } END{ print i }" filename
```

+ BEGIN语句块在awk开始从输入流中读取行之前被执行，这是一个可选的语句块，比如变量初始化、打印输出表格的表头等语句通常可以写在BEGIN语句块中。

+ END语句块在awk从输入流中读取完所有的行之后即被执行，比如打印所有行的分析结果这类信息汇总都是在END语句块中完成，它也是一个可选语句块。

+ pattern语句块中的通用命令是最重要的部分，它也是可选的。awk读取的每一行都会执行该语句块。

### awk内置变量

变量名 | 属性
---|---
$0 | 当前记录
$1~$n | 当前记录的第 n 个字段
FS | 输入字段分隔符默认是空格
RS | 输入记录分割符默认为换行符
NF | 当前记录中的字段个数，就是有多少列
NR | 已经读出的记录数，就是行号，从 1 开始
OFS | 输出字段分隔符 默认也是空格
ORS | 输出的记录分隔符 默认为换行符

FS="[" ":]+" 以一个或多个空格或：分隔
```awc
awk -F [" ":]+ '{print $1,$2,$3}' hello.txt
```

字段数量 NF
```awc
 awk -F ":" 'NF==8{print $0}' hello.txt
```

记录数量 NR
```awk
 awk -F ":" 'NR>=2{print $0}' hello.txt 
```

### awk正则应用

`awk  '/REG/{action}' file`,/REG/为正则表达式，可以将$0 中，满足条件的记录送入到： action 进行处理 

```awk
awk '/root/{print $0}' /etc/passwd
awk  -F : '$1~/root/{print $0}' /etc/passwd
```

`awk  '布尔表达式{action}' file`仅当对前面的布尔表达式求值为真时，awk才执行代码块。

```awk
awk -F ":" '$1=="root"{print $0}' /etc/passwd 
```

### awk的if、循环和数组

```awk
{
    if ( $1== "foo" ) {
        if ( $2== "foo" ) {
            print "uno"
        } else {
            print "one"
        }
    } elseif ($1== "bar" ) {
        print "two"
    } else {
        print "three"
    }
}
```

```awk
for ( x=1;x<=4;x++ ) {
    print "iteration", x
} 
```

```awk
{
    cities[1]=”beijing”
    cities[2]=”shanghai”
    cities[“three”]=”guangzhou”
    for( c in cities) {
        print cities[c]
    }
    print cities[1]
    print cities[“1”]
    print cities[“three”]
} 
```

```
cat access.log| awk -F " " '{print $1}' | sort | uniq -c | sort -r -k 1 -n | head -10
```
## Sed

sed 是一种新型的，非交互式的编辑器。它能执行与编辑器 vi 和 ex 相同的编辑任务。 sed 编辑器没有提供交互式使用方式，使用者只能在命令行输入编辑命令、指定文件名，然 后在屏幕上查看输出。sed 编辑器没有破坏性，它不会修改文件，除非使用 shell 重定向来保 存输出结果。默认情况下，所有的输出行都被打印到屏幕上。

替换操作：s命令
```sed
sed 's/book/books/' file
```

-n选项和p命令一起使用表示只打印那些发生替换的行：
```sed
sed -n 's/test/TEST/p' file
```
直接编辑文件选项-i，会匹配file文件中每一行的第一个book替换为books：
```sed
sed -i 's/book/books/g' file
```

全面替换标记g
```sed
sed 's/book/books/g' file
```
当需要从第N处匹配开始替换时，可以使用 /Ng：
```sed
echo sksksksksksk | sed 's/sk/SK/2g'
skSKSKSKSKSK

echo sksksksksksk | sed 's/sk/SK/3g'
skskSKSKSKSK

echo sksksksksksk | sed 's/sk/SK/4g'
skskskSKSKSK
```

定界符
```sed
sed 's:test:TEXT:g'
sed 's|test|TEXT|g'
```
定界符出现在样式内部时，需要进行转义：
```sed
sed 's/\/bin/\/usr\/local\/bin/g'
```

删除操作：d命令

删除空白行：
```sed
sed '/^$/d' file
```
删除文件的第2行：
```sed
sed '2d' file
```
删除文件的第2行到末尾所有行：
```sed
sed '2,$d' file
```
删除文件最后一行：
```sed
sed '$d' file
```
删除文件中所有开头是test的行：
```sed
sed '/^test/'d file
```

追加（行下）：a\命令

将 this is a test line 追加到 以test 开头的行后面：
```sed
sed '/^test/a\this is a test line' file
```

在 test.conf 文件第2行之后插入 this is a test line：
```sed
sed -i '2a\this is a test line' test.conf
```
## Grep

grep 这个命令是一个全局查找正则表达式并且打印结果行的命令。它的输入是一个文 件或者是一个标准输入。

### grep 选项

```grep
-a 不要忽略二进制数据。
-A<显示列数> 除了显示符合范本样式的那一行之外，并显示该行之后的内容。
-b 在显示符合范本样式的那一行之外，并显示该行之前的内容。
-c 计算符合范本样式的列数。
-C<显示列数>或-<显示列数>  除了显示符合范本样式的那一列之外，并显示该列之前后的内容。
-d<进行动作> 当指定要查找的是目录而非文件时，必须使用这项参数，否则grep命令将回报信息并停止动作。
-e<范本样式> 指定字符串作为查找文件内容的范本样式。
-E 将范本样式为延伸的普通表示法来使用，意味着使用能使用扩展正则表达式。
-f<范本文件> 指定范本文件，其内容有一个或多个范本样式，让grep查找符合范本条件的文件内容，格式为每一列的范本样式。
-F 将范本样式视为固定字符串的列表。
-G 将范本样式视为普通的表示法来使用。
-h 在显示符合范本样式的那一列之前，不标示该列所属的文件名称。
-H 在显示符合范本样式的那一列之前，标示该列的文件名称。
-i 忽略字符大小写的差别。
-l 列出文件内容符合指定的范本样式的文件名称。
-L 列出文件内容不符合指定的范本样式的文件名称。
-n 在显示符合范本样式的那一列之前，标示出该列的编号。
-q 不显示任何信息。
-R/-r 此参数的效果和指定“-d recurse”参数相同。
-s 不显示错误信息。
-v 反转查找。
-w 只显示全字符合的列。
-x 只显示全列符合的列。
-y 此参数效果跟“-i”相同。
-o 只输出文件中匹配到的部分。
```

### 正则表达式元字符

元字符 |功能 |示例 | 示例的匹配对象
---|---|---|---|
^ |行首定位符 |/^love/ |匹配所有以 love 开头的行
$ |行尾定位符 |/love$/ |匹配所有以 love 结尾的行
. |匹配除换行外的单个字符 |/l..e/ |匹配包含字符 l、后跟两个任意 字符、再跟字母 e 的行
* |匹配零个或多个前导字符 |/*love/ |匹配在零个或多个空格紧跟着 |模式 love 的行 
[] |匹配指定字符组内任一字符 |/[Ll]ove/ |匹配包含 love 和 Love 的行 
[^] | 匹配不在指定字符组内任一字符 |/[^A-KM-Z]ove/ |匹配包含 ove，但 ove 之前的那 个字符不在 A 至 K 或 M 至 Z 间 的行
\(..\) | 保存已匹配的字符
& |保存查找串以便在替换串中引用 |s/love/**&**/ |符号&代表查找串。字符串 love 将替换前后各加了两个**的引 用，即 love 变成**love** 
\< |词首定位符 |/\<love/ |匹配包含以 love 开头的单词的行
\> |词尾定位符 |/love\>/ |匹配包含以 love 结尾的单词的 行 
x\{m\} |连续 m 个 x |/o\{5\}/ | 5 个字母 o、 至少 5 个连续的 o、或 5~10 个 连续的 o 的行 
x\{m,\} |至少 m 个 x |/o\{5,\}/ |同上
x\{m,n\} | m 个 x，但不 超过 n 个 x |/o\{5,10\}/ | 同上

egrep新增的元字符

egrep 在 grep 的基础上增加了更多的元字符。但是 egrep 不允许使用\(\),\{\}. 
元字符 | 功能 | 示例 | 示例的匹配对象 
---|---|---|---
+ | 匹配一个或多个 | 加号前面的字符 | '[a-z]+ove' 匹配一个或多个小写字母后跟ove的字符串。move love approve 
？ | 匹配0个或一个前导字符 | 'lo?ve' | 匹配l后跟一个或 0 个字母 o 以及 ve 的字符串。love lve
a\|b | 匹配 a或b | 'love \| hate' | 匹配 love 和 hate 这两个表达式之一 
() | 字符组 | 'love(able\|ly)(ov+)' | 匹配loveable或lovely匹配ov的一次或多次出现 

[参考]

[Linux命令大全](https://man.linuxde.net/sed)

《跟老男孩学Linux三剑客命令》