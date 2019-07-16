---
title: "工欲善其事,必先利其器--vim"
date: 2019-06-23T11:37:52+08:00
tags: ["Vim"]
categories: ["Tool"]
toc : true
---

作为Linux下老牌编辑器vim发展至今已有30个年头了,期间各种编辑器兴衰更替,唯独vim做到了长盛不衰.在服务器上vim绝对是一个必备的技能.vim支持纯的键盘操作,能够极大地提高编辑的效率.但是vim对待新手并不友好,操作命令多,学习曲线陡,将很多非服务器端开发者拒之门外.
<!--more-->
![vimemacs](/assets/article/20190623/vimemacs.png)

### 模式
- normal模式是vim强大的编辑能力来源,在上面可以通过hjkl进行光标移动,normal模式命令往往需要一个操作符结尾,例如"dd"删除当前行,第二个"d"也可以替换成移动命令j表示删除当前行和下一行.
- 插入模式,在这个模式中大多数的按键都会向文本缓冲区插入文本,ESC键可以回到普通模式.
- 视图模式,移动命令可以扩大高亮文本区域,命令会被执行到这片高亮文本区域上,vim的"text object"也能像移动命令一样用在这个模式上

### 插入模式小技巧
在vim中可以通过a/A/i/I/o/O进入插入模式,gi能够快速的跳转到上一次编辑的地方并进入插入模式

快速纠错  
- ctrl+h删除上一个字符
- ctrl+w删除上一个单词
- ctrl+u删除当前行

### 快速移动
单词间移动  
- w/W移动到下一个单词的开头
- e/E移动到下一个单词的末尾
- b/B移动到上一个单词的开头
> 小写字母指的是以非空白符分割的单词,大写字母指的是以空白符分割的单词
行中移动
- f{char}移动到char字符上
- t{char}移动到char前一个字符上
- F{char}反过来搜索前面的字符
> 如果第一次没搜到可以用";"继续搜索下一个","继续搜索上一个
0移动到行首,$移动到行尾

### 增删改查
快速删除  
- daw删除一个单词
- dt{char}删除到char
- d$删除到行未,删除到行首
快速修改  
> 常用的命令有r(replace),c(change),s(substitue)
- r替换一个字符,s删除一个字符并进入插入模式
- c配合文本对象,快速修改
快速查询  
- "/"或"?"进行前向搜索或反向搜索
- "n"或"N"跳转到上一个或下一个匹配

### 搜索替换
- "% s/foo/far/g"全局范围将foo替换成far
- "1,6 s/foo/far/g"将1-6行foo替换成far
- "% s/foo//n"查询foo匹配次数

### 多文件模式
Buffer间切换
- ls列举当前缓冲区,b n跳转到第n个缓冲区
- bpre bnext bfirst blast
- b buffer_name加上tab补全跳转
窗口
- sp水平分割,vs垂直分割
- ctrl+w+[w|h|j|k|l]进行循环,上下左右的窗口切换
- e在当前窗口打开一个新的Buffer

### text object
```
[number] <comman> [textobject]
```
> number表示次数 comman是命令例如d,c,y textobject表示文本对象例如w,s,p
- iw表示inner word,aw表示a word选中包含单词后面的空格
- i(选中括号内内容,a(选中内容包括括号本身,同样的其他括号和引号也一样

### 复制粘贴与寄存器
- Insert模式粘贴,:set paste可以解决代码缩进错乱问题
- 默认的删除粘贴会将内容放到"无名寄存器"
- 通过"{register}前缀可以指定寄存器,a-z每一个都可以作为一个寄存器,0是复制专用寄存器
- "+前缀可以使用系统剪贴板,如果vim支持系统剪贴板即:echo  has('clipboard')输出为1

### 宏操作
- 宏的使用分为录制和回放,q录制q结束录制
- q{register}选择要保存的寄存器,把录制命令保存其中
- @{register}回放寄存器中保存的一系列命令
- visual模式选择多行,通过命令模式:'<,'>normal @a对多行进行回放操作

### 补全
- `<ctrl+n>,<ctrl+p>`补全单词
- `<ctrl+x><ctrl+f>`补全文件名
- `<ctrl+x><ctrl+o>`补全代码需要开启文件类型检查,安装插件

### 映射
vim映射就是吧一个操作映射到另一个操作
- nmap/vmap/imap分别在normal/visual/insert模式下分别有效
- nnoremap/vnoremap/inoremap分别表示normal/visual/insert模式下的非递归映射

### 插件

###### 插件管理器
安装[vim-plug](https://github.com/junegunn/vim-plug)插件管理器,在.vimrc文件的`call plug#begin('~/.vim/plugged')`与`call plug#end()`之间写入想要安装的插件脚本`Plug 'the plugin you want to install'`保存和重载.vimrc后在命令行模式下执行:PlugInstall安装

###### 界面美化
好看的界面赏心悦目,保持愉快的心情
- 启动界面插件[vim-startify](https://github.com/mhinz/vim-startify)
- 状态栏插件[vim-airline](https://github.com/vim-airline/vim-airline)
- 缩进线插件[indentLine](https://github.com/Yggdroot/indentLine)
- 主题插件[vim-hybrid](https://github.com/w0ng/vim-hybrid)

###### 效率工具
- 显示目录结构[nerdtree](https://github.com/scrooloose/nerdtree)
- 文件模糊搜索[ctrlp.vim](https://github.com/kien/ctrlp.vim),[fzf.vim](https://github.com/junegunn/fzf.vim)
- 光标快速定位[vim-easymotion](https://github.com/easymotion/vim-easymotion)
- 括号引号的自动配对[auto-pairs](https://github.com/jiangmiao/auto-pairs)
- 成对编辑[vim-surround](https://github.com/tpope/vim-surround)
- 搜索替换[far.vim](https://github.com/brooth/far.vim)
- 快速注释[vim-commentary](https://github.com/tpope/vim-commentary)
- 自动补全[coc.nvim](https://github.com/neoclide/coc.nvim)
- 单词高亮[vim-interestingwords](https://github.com/lfv89/vim-interestingwords)

###### Vim IDE
- [python-mode](https://github.com/python-mode/python-mode)
- [vim-go](https://github.com/fatih/vim-go)

###### 插件集中营

[VimAwesome](https://vimawesome.com/)是源自GitHub，Vim.org和用户提交的Vim插件的目录。插件使用数据是从GitHub上的dotfiles repos中提取的。

###### [我的vimrc](https://github.com/csbbo/vimrc)

![myvimrc](/assets/article/20190623/myvimrc.png)