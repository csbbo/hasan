---
title: "进程控制"
date: 2019-06-30T21:52:25+08:00
tags: ["Linux"]
categories: ["嵌入式Linux操作系统笔记"]
---

“进程是可并发执行的程序在一个数据集合上的运行过程”进程是一个程序的一次执行的过程。它是程序执行和资源管理的最小单位Linux 是一个多任务的操作系统,也就是说,在同一个时间内,可以有多个进程同时执行。但是单CPU计算机实际上在一个时间片断内只能执行一条指令,Linux 使用了一种称为“进程调度(process scheduling)”的手段分配时间片实现多进程同时执行。

<!--more-->

每个进程在创建时都会被分配一个数据结构,称为进程控制(Process Control Block,简称 PCB)。进程控制块包含了进程的描述信息、控制信息以及资源信息。进程ID也被称作进程标识符,是一个非负的整数,在 Linux操作系统中唯一地标志一个进程,在我们最常使用I386架构(即PC使用的架构)上,其变化范围是 0-32767

一个或多个进程可以合起来构成一个进程组(process group),一个或多个进程组可以合起来构成一个会话(session)。这样我们就有了对进程进行批量操作的能力,比如通过向某个进程组发送信号来实现向该组中的每个进程发送信号。

进程的状态和转换  
在 Linux 以及其他大部分操作系统中,进程根据它的生命周期可以划分成3种状态。
1. 执行状态:该进程正在执行,即进程正在占用 CPU。
2. 就绪状态:进程已经具备执行的一切条件,正在等待分配CPU的处理时间片。
3. 等待状态:进程不能使用 CPU,若等待事件发生则可将其唤醒。

![processtransition](/assets/article/20190630/process_state_transition.png)

### Linux进程编程

#### 进程创建
fork 函数用于从已存在进程中创建一个新进程。其原型如下:

```c
#include<sys/types.h> /* 提供类型 pid_t 的定义 */
#include<unistd.h> /* 提供函数的定义 */c
pid_t fork(void);
```

新进程称为子进程,而原进程称为父进程。这两个分别带回它们各自的返回值,其中父进程的返回值是子进程的进程号,是一个大于 0 的整数,而子进程则返回 0。因此,可以通过返回值来判定该进程是父进程还是子进程。如果出错则返回-1。

需要注意的是使用 fork 函数得到的子进程是父进程的一个复制品,它从父进程处继承了整个进程的地址空间,包括进程上下文、进程堆栈、内存信息、打开的文件描述符、信号控制设定、进程优先级、进程组号、当前工作目录、根目录、资源限制、控制终端等,而子进程所独有的只有它的进程号、资源使用和计时器等。因此可以看出,使用 fork 函数的代价是很大的,它复制了父进程中的代码段、数据段和堆栈段里的大部分内容,使得 fork 函数的执行速度并不很快。在 Linux 中,创造新进程的方法只有一个,就是我们正在介绍的 fork。其他一些库函数,如 system(),看起来似乎它们也能创建新的进程,如果能看一下它们的源码就会明白,它们实际上也在内部调用了 fork。包括我们在命令行下运行应用程序,新的进程也是由 shell 调用 fork 制造出来的。fork 有一些很有意思的特征,下面就让我们通过一个小程序来对它有更多的了解。

```c
/****fork_test.c *****/
#include<stdio.h>
#include<sys/types.h>
#include<unistd.h>
main()
{
	pid_t pid;
	/*此时仅有一个进程*/
	int n=4;
	pid=fork();
	/*此时已经有两个进程在同时运行*/
	if(pid<0)
		printf("error in fork!\n");
	else if(pid==0) /*返回 0 表示子进程*/
	{
		n++;
		printf("I am the child process, my process ID is %d,n=%d\n",getpid(),n);
	}
	else
	/*返回大于 0 表示父进程*/
	{
		n--;
		printf("I am the parent process, my process ID is %d,n=%d\n",getpid(),n);
	}
}
```

在语句 pid=fork()之前,只有一个进程在执行这段代码,但在这条语句之后,就变成两个进程在执行了,这两个进程的代码部分完全相同,其流程如图所示:

![fatherandsonprocess](/assets/article/20190630/father_and_son_process.png)

fork调用的一个奇妙之处就是它仅仅被调用一次,却能够返回两次,它可能有三种不同的返回值:
- 在父进程中,fork 返回新创建子进程的进程 ID;
- 在子进程中,fork 返回 0;
- 如果出现错误,fork 返回一个负值;

#### exec函数族

fork 函数是用于创建一个子进程,该子进程几乎拷贝了父进程的全部内容,但是,这个新创建的进程如何执行呢?exec 函数族就提供了一个在进程中启动另一个程序执行的方法。用 fork 创建子进程后执行的是和父进程相同的程序(但有可能执行不同的代码分支),子进程往往要调用一种 exec 函数以执行另一个程序。当进程调用一种 exec 函数时,该进程的用户空间代码和数据完全被新程序替换,从新程序的启动例程开始执行。调用 exec 并不创建新进程,所以调用 exec 前后该进程的 id 并未改变。其实有六种以 exec 开头的函数,统称 exec 函数,exec 函数原型如下:

```c
#include <unistd.h>
int execl(const char *path, const char *arg, ...)
int execv(const char *path, char *const argv[])
int execle(const char *path, const char *arg, ..., char *const envp[])
int execve(const char *path, char *const argv[], char *const envp[])
int execlp(const char *file, const char *arg, ...)
int execvp(const char *file, char *const argv[])
```

事实上,只有 execve 是真正的系统调用,其它五个函数最终都调用 execve,这些函数之间的关系如图所示

![execfamily](/assets/article/20190630/exec_family.png)

exec调用举例如下:

```c
/***exec.c***/
#include <unistd.h>
#include <stdlib.h>
int main(void)
{
    execlp("ps", "ps", "-ef", NULL);
    perror("exec ps");
    exit(1);
}
```

#### 进程退出

一个进程终止则存在异
常终止和正常终止两种情况。进程异常终止的两种方式是:当进程接收到某些信号时;或是调用abort()函数,它产生SIGABRT信号,这是前一种的特例一个进程正常终止有三种方式:
- 由 main()函数返回
- 调用 exit()函数
- 调用_exit()或_Exit()函数


由 main()函数返回的程序,一般应在函数的结尾处通过return 语句指明函数的返回值,如果不指定这个返回值,main()通常会返回0。但这种特性与编译器有关,因此为了程序的通用性,应该养成主动使用 return语句的习惯。

exit()的作用是来终止进程的。当程序执行到 exit 时,进程会无条件地停止剩下的所有操作,清除包括 PCB 在内的各种数据结构,并终止本进程的运行。exit()函数的原型为:

```c
#include <stdlib.h>
#include <unistd.h>
exit:void exit(int status)
_exit:void _exit(int status)
```

_exit()函数的作用是:直接使进程停止运行,清除其使用的内存空间,并清除其在内核中的各种数据结构;而 exit()函数则在执行退出之前加了若干道工序,它要检查文件的打开情况,把文件缓冲区中的内容写回文件,即“清理 I/O 缓冲”。其区别如图所示。

![exitand_exit](/assets/article/20190630/exit_and__exit.png)

#### wait和waitpid

如果一个父进程终止,而它的子进程还存在(这些子进程或者仍在运行,或者已经是僵尸进程了),则这些子进程的父进程改为 init 进程。init 是系统中的一个特殊进程,通常程序文件是/sbin/init,进程 id 是 1,在系统启动时负责启动各种系统服务,之后就负责清理子进程,只要有子进程终止,init 就会调用 wait 函数清理它。 wait 和 waitpid 函数的原型如下:

```c
#include <sys/types.h>
#include <sys/wait.h>
pid_t wait(int *status)
pid_t waitpid(pid_t pid, int *status, int options)
```

### Zombie进程

一个进程在终止时会关闭所有文件描述符,释放在用户空间分配的内存,但它的 PCB还保留着,内核在其中保存了一些信息:如果是正常终止则保存着退出状态,如果是异常终止则保存着导致该进程终止的信号是哪个。这个进程的父进程可以调用wait或waitpid获取这些信息,然后彻底清除掉这个进程。我们知道一个进程的退出状态可以在Shell中用特殊变量$?查看,因为Shell是它的父进程,当它终止时Shell调用wait或waitpid得到它的退出状态同时彻底清除掉这个进程。
如果一个进程已经终止,但是它的父进程尚未调用wait或waitpid 对它进行清理,这时的进程状态称为僵尸(Zombie)进程。任何进程在刚终止时都是僵尸进程,正常情况下,僵尸进程都立刻被父进程清理了,为了观察到僵尸进程,我们自己写一个不正常的程序,父进程fork出子进程,子进程终止,而父进程既不终止也不调用wait清理子进程:

```c
/*****zombie.c*****/
#include <unistd.h>
#include <stdlib.h>
int main(void)
{
    pid_t pid=fork();
    if(pid<0)
    {
        perror("fork");
        exit(1);
    }
    if(pid>0)
    { /* parent */
        while(1);
    }
    /* child */
    return 0;
}
```

### 守护进程

守护进程（Daemon）是一种运行在后台的特殊进程，它独立于控制终端并且周期性的执行某种任务或等待处理某些发生的事件。由于在linux中，每个系统与用户进行交流的界面称为终端，每一个从此终端开始运行的进程都会依附于这个终端，这个终端被称为这些进程的控制终端，当控制终端被关闭的时候，相应的进程都会自动关闭。但是守护进程却能突破这种限制，它脱离于终端并且在后台运行，并且它脱离终端的目的是为了避免进程在运行的过程中的信息在任何终端中显示并且进程也不会被任何终端所产生的终端信息所打断。它从被执行的时候开始运转，直到整个系统关闭才退出。