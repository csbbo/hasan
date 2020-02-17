---
title: "分布式版本控制系统--Git"
date: 2019-09-15T21:13:13+08:00
tags: ["Git"]
toc: true
---

Linus花了两周时间自己用C写了一个分布式版本控制系统，这就是Git！  
Git是目前世界上最先进的分布式版本控制系统（没有之一）。  
Git有什么特点？简单来说就是：高端大气上档次！
<!--more-->
## 工作区暂存区版本库

工作区就是建立Git版本库的目录，Git版本库其实就是工作区中`.git`文件。Git的版本库里存了很多东西，其中最重要的就是称为stage的暂存区，还有Git为我们自动创建的第一个分支`master`，以及指向`master`的一个指针叫`HEAD`。

<img src="/assets/article/20190915/struct.png" style="width:50%"/>


把文件往Git版本库里添加的时候，是分两步执行的：

+ 用`git add`把文件添加进去，实际上就是把文件修改添加到暂存区；
+ 用`git commit`提交更改，实际上就是把暂存区的所有内容提交到当前分支。

> 因为我们创建Git版本库时，Git自动为我们创建了唯一一个`master`分支，所以，现在，`git commit`就是往`master`分支上提交更改。

## Git命令

常用的比如说有`git status`可以查看工作区状态,`git add <file>`可以将文件添加到暂存区，`git diff file|HEAD`查看修改内容,
`git commit -m "add file"`将文件提交到仓库，`git clone <remote_addr>`将远程仓库克隆到本地,接下来就来分类聊聊。

### 查看历史

HEAD指向的版本就是当前版本，因此，Git允许我们在版本的历史之间穿梭，使用命令`git reset --hard commit_id | HEAD^`。
穿梭前，用`git log`可以查看提交历史，以便确定要回退到哪个版本。要重返未来，用`git reflog`查看命令历史，以便确定要回到未来的哪个版本。

### 丢弃修改

+ 当你改乱了工作区某个文件的内容，想直接丢弃工作区的修改时，用命令`git restore <file>`。这里有两种情况：
    + 一种是`readme.txt`自修改后还没有被放到暂存区，现在，撤销修改就回到和版本库一模一样的状态；
    + 一种是`readme.txt`已经添加到暂存区后，又作了修改，现在，撤销修改就回到添加到暂存区后的状态。
+ 当你不但改乱了工作区某个文件的内容，还添加到了暂存区时，想丢弃修改，分两步，第一步用命令`git restore --staged <file>`，就回到了上面的场景，第二步按上面的场景操作。  
+ 已经提交了不合适的修改到版本库时，想要撤销本次提交，参考版本回退一节，不过前提是没有推送到远程库。
`git clean -fd`可以清空工作区修改。

### 删除文件

`rm <file>`后有连个选择  
`git rm <file>`从版本库中也删除该文件然后`commit`提交  
如果是误删`git checkout <file>`从版本库中恢复

### 远程仓库

由于你的本地Git仓库和GitHub仓库之间的传输是通过SSH加密的以需要一点设置生成ssh key`ssh-keygen -t rsa -c "your email"`

要关联一个远程库，使用命令`git remote add origin <git@server-name:path/repo-name.git>`

关联后，使用命令`git push -u origin master`第一次推送`master`分支的所有内容。

我们第一次推送master分支时，加上了-u参数，Git不但会把本地的master分支内容推送的远程新的master分支，还会把本地的master分支和远程的master分支关联起来，在以后的推送或者拉取时就可以使用`git push`简化命令。

### 分支管理
Git鼓励大量使用分支:

+ 查看分支：`git branch`  
+ 创建分支：`git branch <branch>`  
+ 切换分支：`git checkout <branch>`  
+ 创建+切换分支：`git checkout -b <branch>`  
+ 合并某分支到当前分支：`git merge <branch>`  
+ 删除分支：`git branch -d <branch>`
+ 重命名分支：`git branch -m old_name new_name`

> switch:我们注意到切换分支使用git checkout <branch>，而前面讲过的撤销修改则是git checkout -- <file>，同一个命令，有两种作用，确实有点令人迷惑。因此，最新版本的Git提供了新的git switch命令来切换分支,上面切换分支命令可以改为`git switch master`、`git switch -c dev`

解决冲突:

+ 解决冲突就是把Git合并失败的文件手动编辑为我们希望的内容，再add到`stage`后提交。

<img src="/assets/article/20190915/branch.png" style="width:50%"/>

+ 查看分支合并图:
`git log --graph --pretty=oneline --abbrev-commit`

+ 合并分支时，加上--no-ff参数就可以用普通模式合并，合并后的历史有分支，能看出来曾经做过合并，而`fast forward`合并就看不出来曾经做过合并。
`git merge --no-ff -m "no fast forward in master" dev`

分支策略:

首先，master分支应该是非常稳定的，也就是仅用来发布新版本，平时不能在上面干活；
那在哪干活呢？干活都在dev分支上，也就是说，dev分支是不稳定的，到某个时候，比如1.0版本发布时，再把dev分支合并到master上，在master分支发布1.0版本；
你和你的小伙伴们每个人都在dev分支上干活，每个人都有自己的分支，时不时地往dev分支上合并就可以了。
所以，团队合作的分支看起来就像这样

<img src="/assets/article/20190915/multilbranch.png" style="width:70%"/>

bug分支：

修复bug时，我们会通过创建新的bug分支进行修复，然后合并，最后删除；  当手头工作没有完成时，先把工作现场`git stash`一下，然后去修复bug，修复后，再`git stash apply [stash@{0}]`恢复工作现场`git stash list`可以查看多次`stash`指定恢复的`stash`，最后`git stash drop stash@{0}`删掉`stash`内容。

> `git stash`把当前工作区和暂存区内容存储起来(不包括`untrack`,即未被`git`管理的文件)

在master分支上修复了bug后，我们要想一想，dev分支是早期从master分支分出来的，所以，这个bug其实在当前dev分支上也存在。那怎么在dev分支上修复同样的bug？

为了方便操作，Git专门提供了一个cherry-pick命令，让我们能复制一个特定的提交到当前分支：`git cherry-pick commit_id`

Feature分支:

开发一个新`feature`，最好新建一个分支；  
如果要丢弃一个没有被合并过的分支，可以通过`git branch -D <name>`强行删除。

多人协作：
 
查看远程库信息，使用`git remote -v`；  
本地新建的分支如果不推送到远程，对其他人就是不可见的；

多人协作的工作模式通常是这样：

1. 首先，可以试图用`git push origin <branch-name>`推送自己的修改；
2. 如果推送失败，则因为远程分支比你的本地更新，需要先用`git pull`试图合并；
3. 如果合并有冲突，则解决冲突，并在本地提交；
4. 没有冲突或者解决掉冲突后，再用`git push origin <branch-name>`推送就能成功！

> 如果`git pull`提示`no tracking information`，则说明本地分支和远程分支的链接关系没有创建，用命令`git branch --set-upstream-to <branch-name> origin/<branch-name>`。

> 删除远程仓库分支：
`git branch -r -d origin/branch-name`删除本地分支与远程分支关系；
`git push origin :branch-name`将远程分支删除提交

在本地创建和远程分支对应的分支，使用`git checkout -b branch-name origin/branch-name`，本地和远程分支的名称最好一致；

### Rebase

对于克隆下来的远程分支

在`git pull`后输入`git rebase`把分叉的提交历史“整理”成一条直线,它把本地的提交都挪到了远程仓库的提交之后，也就是本地的修改变成了基于远程提交之后的了

优缺点:

+ rebase操作可以把本地未push的分叉提交历史整理成直线,缺点是本地的分叉提交已经被修改过了；
+ rebase的目的是使得我们在查看历史提交的变化时更容易，因为分叉的提交需要三方对比。

使用场景：

**Rebase合并多次提交纪录**

多次无用commit不利于代码review，造成分支污染，如果有一天线上出现了紧急问题，你需要回滚代码，却发现海量的 commit 需要一条条来看。

我们来合并最近的 4 次提交纪录，执行：

```git
git rebase -i HEAD~4
```

这时候，会自动进入 vi 编辑模式：

```git
pick cacc52da add: qrcode
pick f072ef48 update: indexeddb hack
pick 4e84901a feat: add indexedDB floder
pick 8f33126c feat: add test2.js

# Rebase 5f2452b2..8f33126c onto 5f2452b2 (4 commands)
#
# Commands:
# p, pick = use commit
# r, reword = use commit, but edit the commit message
# e, edit = use commit, but stop for amending
# s, squash = use commit, but meld into previous commit
# f, fixup = like "squash", but discard this commit's log message
# x, exec = run command (the rest of the line) using shell
# d, drop = remove commit
#
# These lines can be re-ordered; they are executed from top to bottom.
#
# If you remove a line here THAT COMMIT WILL BE LOST.
#
# However, if you remove everything, the rebase will be aborted.
#
```

按照如上命令来修改你的提交纪录：

```git
pick cacc52da add: qrcode
s f072ef48 update: indexeddb hack
s 4e84901a feat: add indexedDB floder
s 8f33126c feat: add test2.js
```

使用`wq`保存退出后，进入合并后commit信息编辑页确定合并后提交信息

**分支合并**

有这么一个使用场景:

1.我们先从 master 分支切出一个 dev 分支，进行开发。  
2.这时候，你的同事完成了一次 hotfix，并合并入了 master 分支，此时 master 已经领先于你的 feature1 分支了。  
3.恰巧，我们想要同步 master 分支的改动，首先想到了 merge执行`git merge master`  
4.就会在记录里发现一些 merge 的信息，但是我们觉得这样污染了commit记录，想要保持一份干净的 commit，怎么办呢？这时候，`git rebase` 就派上用场了。

让我们来试试 git rebase ，先回退到同事 hotfix 后合并 master 的步骤

使用 rebase 后来看看结果,执行`git rebase master`

> 这里补充一点：rebase 做了什么操作呢？首先，git 会把 feature1 分支里面的每个 commit 取消掉；
其次，把上面的操作临时保存成 patch 文件，存在 .git/rebase 目录下；
然后，把 feature1 分支更新到最新的 master 分支；
最后，把上面保存的 patch 文件应用到 feature1 分支上；
从 commit 记录我们可以看出来，feature1 分支是基于 hotfix 合并后的 master ，自然而然的成为了最领先的分支，而且没有 merge 的 commit 记录，是不是感觉很舒服了。


### 标签

标签不是按时间顺序列出，而是按字母排序的：

+ `git tag <tagname>`用于新建一个标签，默认为HEAD，也可以指定一个commit id;
+ `git tag -a <tagname> -m "blablabla" [commit_id]`可以指定标签信息；
+ `git tag`可以查看所有标签；
+ `git show <tagname>`可以用查看标签信息；
+ `git tag -d <tagname>`删除一个本地标签；
+ `git push origin <tagname>`可以推送一个本地标签；
+ `git push origin --tags`可以推送全部未推送过的本地标签；
+ `git push origin :refs/tags/<tagname>`删除一个远程标签。

### 多个远程仓库

+ `git remote rm origin`删除已有远程仓库
+ `git remote add github git@github.com:hasan/learngit.git`关联github仓库
+ `git remote add gitee git@gitee.com:hasan/learngit.git`关联码云仓库

### 忽略特殊文件

忽略某些文件时，需要编写`.gitignore`；  
`.gitignore`文件本身要放到版本库里，并且可以对`.gitignore`做版本管理！  
[GitHub](https://github.com)已经为我们准备了各种[配置文件](https://github.com/github/gitignore)，只需要组合一下就可以使用了。

### 追踪空目录

首先需要了解git是不能追踪空的目录的，如果有一个空的目录git会对它视而不见，但这个空目录正是我们想要的特性，这时候`.gitkeep`就派上用场了，只需在空目录下添加一个`.gitkeep`该目录就会被加入到git的版本管理系统中。

### 配置别名

```
git config --global alias.lg "log --color --graph --pretty=format:'%Cred%h%Creset -%C(yellow)%d%Creset %s %Cgreen(%cr) %C(bold blue)<%an>%Creset' --abbrev-commit"
```

## 搭建Git服务器

+ 安装git：`sudo apt-get install git`
+ 创建一个git用户，用来运行git服务：`sudo adduser git`
+ 创建证书登录：收集所有需要登录的用户的公钥，就是他们自己的id_rsa.pub文件，把所有公钥导入到/home/git/.ssh/authorized_keys文件里，一行一个。
+ 初始化Git仓库：先选定一个目录作为Git仓库，假定是/srv/sample.git，在/srv目录下输入命令`sudo git init --bare sample.git`
+ Git就会创建一个裸仓库，裸仓库没有工作区，因为服务器上的Git仓库纯粹是为了共享，所以不让用户直接登录到服务器上去改工作区，并且服务器上的Git仓库通常都以.git结尾。然后，把owner改为git `sudo chown -R git:git sample.git`
+ 出于安全考虑，第二步创建的git用户不允许登录shell，这可以通过编辑/etc/passwd文件完成。找到类似下面的一行：`git:x:1001:1001:,,,:/home/git:/bin/bash`改为`git:x:1001:1001:,,,:/home/git:/usr/bin/git-shell`这样，git用户可以正常通过ssh使用git，但无法登录shell，因为我们为git用户指定的git-shell每次一登录就自动退出
+ 现在，可以通过git clone命令克隆远程仓库了，在各自的电脑上运行

管理公钥

把每个人的公钥收集起来放到服务器的/home/git/.ssh/authorized_keys文件里就是可行的。如果团队有几百号人，就没法这么玩了，这时，可以用[Gitosis](https://github.com/res0nat0r/gitosis)来管理公钥。

管理权限

Git是为Linux源代码托管而开发的，所以Git也继承了开源社区的精神，不支持权限控制。不过，因为Git支持钩子（hook），所以，可以在服务器端编写一系列脚本来控制提交等操作，达到权限控制的目的。[Gitolite](https://github.com/sitaramc/gitolite)就是这个工具。

## Some problems

git默认大小写不敏感，当将一个文件以大写的形式存入版本库后无法重命名为其小写形式，如需要重命名可以先将其从版本库中删除后重新添加。

[参考]

[廖雪峰Git教程](https://www.liaoxuefeng.com/wiki/896043488029600)  
[彻底搞懂 Git-Rebase](http://jartto.wang/2018/12/11/git-rebase/)
