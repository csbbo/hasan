#! /usr/bin/bash

article_path=$(pwd)"/content/posts/20191231-books.md"
(
cat << 'EOF'
---
title: "书籍"
date: 2019-12-31T17:57:29+08:00
toc: true
draft: true
---
图书一角，收藏一些觉得不错的书籍，方便平时在线阅读。
<!--more-->
EOF
) > $article_path

books_path=$(pwd)"/content/assets/books/"
books=$(ls $books_path)
space="  "

for files in $books
do
    category=$(ls $books_path$files)
    echo -e "\n### "$files >> $article_path
    for book in $category
    do
        url="<a href=\"https://hasan.shaobo.fun/assets/books/"$files"/"$book"\" target=\"_blank\">"${book%.pdf}"</a>$space\n"
        echo -e $url >> $article_path
    done
done