---
title: "多线程编程"
date: 2019-07-02T16:28:40+08:00
tags: ["Linux","thread"]
categories: ["嵌入式Linux操作系统笔记"]
---

在Linux 系统下,启动一个新的进程必须分配给它独立的地址空间,建立众多的数据表来维护它的代码段、堆栈段和数据段,这是一种"昂贵"的多任务工作方式。而运行于一个进程中的多个线程,它们彼此之间使用相同的地址空间,共享大部分数据,启动一个线程所花费的空间远远小于启动一个进程所花费的空间,而且,线程间彼此切换所需的时间也远远小于进程间切换所需要的时间。
<!--more-->
![thread_and_process](/assets/article/20190702/thread_and_process.png)


### 线程的实现

Linux系统下的多线程遵循POSIX线程接口,称为pthread。编写 Linux 下的多线程程序,需要使用头文件pthread.h,连接时需要使用库libpthread.a。
创建线程实际上就是确定调用该线程函数的入口点,这里通常使用的函数是pthread_create。在线程创建以后,就开始运行相关的线程函数。pthread_create 函数的原型如下:

```c
#include<pthread.h>
int pthread_create ((pthread_t *thread, pthread_attr_t *attr,void *(*start_routine) (void *),void arg));
```
函数 pthread_create 用来创建一个线程,第一个参数为指向线程标识符的指针,第二个参数用来设置线程属性,第三个参数是线程运行函数的起始地址,最后一个参数是运行函数的参数。当创建线程成功时,函数返回 0,若不为 0 则说明创建线程失败,常见的错误返回代码为EAGAIN 和 EINVAL。前者表示系统限制创建新的线程,例如线程数目过多了;后者表示第二个参数代表的线程属性值非法。下面我们展示一个最简单的多线程程序

```c
/*pthread.c*/
#include <stdio.h>
#include <pthread.h>
#include <unistd.h>
#include <stdlib.h>
void thread(void)
{
    int i;
    for(i=0;i<3;i++)
        printf("This is a pthread.\n");
}
int main()
{
    pthread_t id;
    int i,ret;
    ret=pthread_create(&id,NULL,(void *) thread,NULL);
    if(ret!=0){
        printf ("Create pthread error!\n");
        exit (1);
    }
    for(i=0;i<3;i++)
        printf("This is the main process.\n");
    pthread_join(id,NULL);
    return 0;
}
```

> pthread不是Linux系统默认库,编译时需要使用静态库libpthread.a,archlinux上安装`sudo pacman -S libpthread-stubs`.所以在使用pthread_create创建线程时在编译中加上-lpthread参数,即`gcc pthread.c -o pthread -lpthread`. pthread_join可以用于将当前线程挂起,等待线程的结束。这个函数是一个线程阻塞的函数,调用它的函数将一直等待到被等待的线程结束为止,当函数返回时,被等待线程的资源就被收回。函数pthread_join 用来等待一个线程的结束。

### 修改线程属性

pthread_create 函数创建了一个线程,在这个线程中,我们使用了默认参数,即将该函数的第二个参数设为 NULL。的确,对大多数程序来说,使用默认属性就够了,但我们还是有必要来了解一下线程的有关属性。

属性结构为pthread_attr_t,它同样在头文件/usr/include/pthread.h 中定义。属性值不能直接设置,须使用相关函数进行操作,初始化的函数为pthread_attr_init,这个函数必须在 pthread_create函数之前调用。属性对象主要包括是否绑定、是否分离、堆栈地址、堆栈大小、优先级。默认的属性为非绑定、非分离、缺省 1MB 的堆栈、与父进程同样级别的优先级。

1. 绑定属性

关于线程的绑定,牵涉到另外一个概念:轻进程(LWP:Light Weight Process)。轻进
程可以理解为内核线程,它位于用户层和系统层之间。系统对线程资源的分配、对线程的控
制是通过轻进程来实现的,一个轻进程可以控制一个或多个线程。默认状况下,启动多少轻
进程、哪些轻进程来控制哪些线程是由系统来控制的,这种状况即称为非绑定的。绑定状况
下,则顾名思义,即某个线程固定的"绑"在一个轻进程之上。被绑定的线程具有较高的响应
速度,这是因为 CPU 时间片的调度是面向轻进程的,绑定的线程可以保证在需要的时候它总
有一个轻进程可用。通过设置被绑定的轻进程的优先级和调度级可以使得绑定的线程满足诸
如实时反应之类的要求。下面的代码即创建了一个绑定的线程。

```c
#include <pthread.h>
pthread_attr_t attr;
pthread_t tid;
pthread_attr_init(&attr); /*初始化属性值,均设为默认值*/
pthread_attr_setscope(&attr, PTHREAD_SCOPE_SYSTEM); /* 绑定的 */
pthread_create(&tid, &attr, (void *) my_function, NULL);
```

2. 分离属性

线程的分离状态决定一个线程以什么样的方式来终止自己。在上面的例子中,我们采用了线程的默认属性,即为非分离状态,这种情况下,原有的线程等待创建的线程结束。只有当pthread_join()函数返回时,创建的线程才算终止,才能释放自己占用的系统资源。而分离线程不是这样子的,它没有被其他的线程所等待,自己运行结束了,线程也就终止了,马上释放系统资源。程序员应该根据自己的需要,选择适当的分离状态。设置线程分离状态的函数为 pthread_attr_setdetachstate(pthread_attr_t *attr, int detachstate)。第二个参数可选为PTHREAD_CREATE_DETACHED(分离线程)和PTHREAD_CREATE_JOINABLE(非分离线程)
。这里要注意的一点是,如果设置一个线程为分离线程,而这个线程运行又非常快,它很可能在pthread_create函数返回之前就终止了,它终止以后就可能将线程号和系统资源移交给其他的线程使用,这样调用 pthread_create的线程就得到了错误的线程号。要避免这种情况可以采取一定的同步措施,最简单的方法之一是可以在被创建的线程里调用pthread_cond_timewait函 数,让这个线程等待一会儿,留出足 够的时间让函数pthread_create返回。设置一段等待时间,是在多线程编程里常用的方法。但是注意不要使用诸如wait()之类的函数,它们是使整个进程睡眠,并不能解决线程同步的问题。

3. 优先级

另外一个可能常用的属性是线程的优先级,它存放在结构sched_param 中。用函数pthread_attr_getschedparam和函数pthread_attr_setschedparam进行存放,一般说来,我们总是先取优先级,对取得的值修改后再存放回去。下面即是一段简单的例子。

```c
#include <pthread.h>
#include <sched.h>

pthread_attr_t attr;
pthread_t tid;
sched_param param;
int newprio=20;
pthread_attr_init(&attr);
pthread_attr_getschedparam(&attr, &param);
param.sched_priority=newprio;
pthread_attr_setschedparam(&attr, &param);
pthread_create(&tid, &attr, (void *)myfunction,myarg);
```

### 多线程访问控制

由于多线程共享进程的资源和地址空间,因此对这些资源进行操作时,必须考虑到线程间资源访问的唯一性问题。线程同步可以使用互斥锁和信号量的方式来解决线程间数据的共享和通信问题,互斥锁一个明显的缺点是它只有两种状态:锁定和非锁定。而条件变量通过允许线程阻塞和等待另一个线程发送信号的方法弥补了互斥锁的不足,它常和互斥锁一起使用。使用时,条件变量被用来阻塞一个线程,当条件不满足时,线程往往解开相应的互斥锁并等待条件发生变化。一旦其它的某个线程改变了条件变量,它将通知相应的条件变量唤醒一个或多个正被此条件变量阻塞的线程。这些线程将重新锁定互斥锁并重新测试条件是否满足。一般说来,条件变量被用来进行线程间的同步。下面介绍这几个函数

1. pthread_cond_init()函数

该函数条件变量的结构为pthread_cond_t,函数 pthread_cond_init()被用来初始化一个条件变量。它的原型为:

```c
int pthread_cond_init (pthread_cond_t * cond, __const pthread_condattr_t
* cond_attr)
```

2. pthread_cond_wait()函数

使线程阻塞在一个条件变量上。它的函数原型为:

```c
extern int pthread_cond_wait (pthread_cond_t
pthread_mutex_t *__restrict __mutex)
```

3. pthread_cond_timedwait()函数

用来阻塞线程的另一个函数是 pthread_cond_timedwait(),它的原型为:

```c
extern int pthread_cond_timedwait __P ((pthread_cond_t
*__cond,pthread_mutex_t *__mutex, __const struct timespec *__abstime))
```

4.pthread_cond_signal()函数

它的函数原型为:extern int pthread_cond_signal (pthread_cond_t *__cond)它用来释放被阻塞在条件变量 cond 上的一个线程。多个线程阻塞在此条件变量上时,哪一个线程被唤醒是由线程的调度策略所决定的。要注意的是,必须用保护条件变量的互斥锁来保护这个函数,否则条件满足信号又可能在测试条件和调用 pthread_cond_wait 函数之间被发出,从而造成无限制的等待。
