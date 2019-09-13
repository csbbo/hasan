---
title: "进程通信"
date: 2019-07-01T22:34:15+08:00
tags: ["Linux","process"]
categories: ["嵌入式Linux操作系统笔记"]
---


每个进程各自有不同的用户地址空间,任何一个进程的全局变量在另一个进程中都看不到,所以进程之间要交换数据必须通过内核,在内核中开辟一块缓冲区,进程1把数据从用户空间拷到内核缓冲区,进程2再从内核缓冲区把数据读走,内核提供的这种机制称为进程间通信(IPC,InterProcess Communication)。如图所示。

![process_communication](/assets/article/20190701/process_communication.png)

<!--more-->

进程间通信主要包括有如下几种:

1. 管道及有名管道:管道可用于具有亲缘关系进程间的通信,有名管道:name_pipe, 除了管道的功能外,还可以在许多并不相关的进程之间进行通讯。
2. 信号(Signal):信号是比较复杂的通信方式,用于通知接收进程有某种事件发生,除了用于进程间通信外,进程还可以发送信号给进程本身;Linux 除了支持Unix早期信号语义函数sigal外,还支持语义符合Posix标准的信号函数sigaction。
3. 报文(Message)队列(消息队列):消息队列是消息的链接表,包括Posix消息队列systemV消息队列。有足够权限的进程可以向队列中添加消息,被赋予读权限的进程则可以读走队列中的消息。消息队列克服了信号承载信息量少,管道只能承载无格式字节流以及缓冲区大小受限等缺点。
4. 共享内存:使得多个进程可以访问同一块内存空间,是最快的可用IPC形式。是针对其他通信机制运行效率较低而设计的。往往与其它通信机制,如信号量结合使用,来达到进程间的同步及互斥。
5. 信号量:主要作为进程间以及同一进程不同线程之间的同步手段。
6. 套接口(Socket):更为一般的进程间通信机制,可用于不同机器之间的进程间通信。起初是由Unix系统的BSD分支开发出来的,但现在一般可以移植到 Linux 上。在接下来的小节中我们将重点介绍管道通信、共享内存通信以及信号通信这几种进程间通信的方式。

### 管道通信

简单地说,管道就是一种连接一个进程的标准输出到另一个进程的标准输入的方法。管
道是最古老的IPC工具,从UNIX系统一开始就存在。它提供了一种进程之间单向的通信方法。管道在系统中的应用很广泛,即使在shell环境中也要经常使用管道技术。管道通信分为管道和有名管道,管道可用于具有亲缘关系进程间的通信,有名管道, 除了管道的功能外,还可以在许多并不相关的进程之间进行通讯。

#### 管道

当进程创建一个管道时,系统内核设置了两个管道可以使用的文件描述符。一个用于向管道中输入信息 (write),另一个用于从管道中获取信息 (read)。管道有如下特点:

- 管道是半双工的,数据只能向一个方向流动;双方通信时,需要建立起两个管道;
- 只能用于父子进程或者兄弟进程之间(具有亲缘关系的进程)
- 单独构成一种独立的文件系统:管道对于管道两端的进程而言,就是一个文件,对于它的读写也可以使用普通的 read、write 等函数。但它不是普通的文件,它不属于某种文件系统,而是自立门户,单独构成一种文件系统,并且只存在于内存中。
- 数据的读出和写入:一个进程向管道中写的内容被管道另一端的进程读出。写入的内容每次都添加在管道缓冲区的末尾,并且每次都是从缓冲区的头部读出数据。

(1)管道的创建

管道是基于文件描述符的通信方式,当一个管道建立时,它会创建两个文件描述符fd[0]和fd[1],其中fd[0]固定用于读管道,而fd[1]固定用于写管道,无名管道的建立比较简单,可以使用 pipe()函数来实现。其函数原型如下:

```c
#include <unistd.h>
int pipe(int fd[2])
```
> 说明:参数 fd[2]表示管道的两个文件描述符,之后就可以直接操作这两个文件描述符;函数调用成功则返回 0,失败返回−1。

(2)管道的关闭

使用 pipe()函数创建了一个管道,那么就相当于给文件描述符fd[0]和fd[1]赋值,之后我们对管道的控制就像对文件的操作一样,那么我们就可以使用close()函数来关闭文件,关闭了fd[0]和fd[1]就关闭了管道。

(3)管道的读写操作

父子进程通过管道通信如图所示:

![pipe_communication](/assets/article/20190701/pipe_communication.png)

管道两端可分别用描述字fd[0]以及fd[1]来描述,需要注意的是,管道的两端是固定了任务的。即一端只能用于读,由描述字fd[0]表示,称其为管道读端;另一端则只能用于写,由描述字fd[1]来表示,称其为管道写端。如果试图从管道写端读取数据,或者向管道读端写入数据都将导致错误发生。要想对管道进行读写,可以使用文件的I/O函数,如read、write等等。下述例子实现了子进程向父进程写数据的过程。

```c
/*****pipe.c*******/
#include <unistd.h>
#include <sys/types.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
int main()
{
	int fd[2], nbytes;
	pid_t childpid;
	char string[] = "Hello, world!\n";
	char readbuffer[80];
	if(pipe(fd)<0) /*创建管道*/
	{
		printf("创建失败\n");
		return -1;
	}
	if((childpid = fork())== -1) /*创建一子进程*/
	{
		perror("fork");
		exit(1);
	}
	if(childpid == 0) /*子进程*/
	{
		close(fd[0]); /* 子进程关闭读取端 */
		sleep(3); /*暂停确保父进程已关闭相应的写描述符*/
		write(fd[1], string, strlen(string)); /* 通过写端发送字符串 */
		close(fd[1]); /*关闭子进程写描述符*/
		exit(0);
	}
	else
	{
		close(fd[1]); /* 父进程关闭写端*/
		nbytes = read(fd[0], readbuffer, sizeof(readbuffer)); /* 从管道中读取字符串 */
		printf("Received string: %s", readbuffer);
		close(fd[0]); /*关闭父进程读描述符*/
	}
	return(0);
}
```

#### 标准流管道

上面创建和使用管道的方法过于繁琐,可以使用下面的简单的方法:

库函数:popen();  
原型: FILE *popen ( char *command, char *type);
返回值:如果成功,返回一个新的文件流。如果无法创建进程或者管道,返回NULL。此标准的库函数通过在系统内部调用pipe()来创建一个半双工的管道,然后它创建一个子进程,启动shell,最后在shell上执行command参数中的命令。管道中数据流的方向是由第二个参数type控制的。此参数可以是r或者w,分别代表读或写。但不能同时为读和写。在 Linux 系统下,管道将会以参数type中第一个字符代表的方式打开。所以,如果你在参数type中写入rw,管道将会以读的方式打开。虽然此库函数的用法很简单,但也有一些不利的地方。例如它失去了使用系统调用 pipe()时可以有的对系统的控制。尽管这样,因为可以直接地使用shell命令,所以shell中的一些通配符和其他的一些扩展符号都可以在command参数中使用。使用popen()创建的管道必须使用pclose()关闭。其实,popen/pclose和标准文件输入/输出流中的 fopen()/fclose()十分相似。  
库函数: pclose();  
原型: int pclose( FILE *stream );  
返回值: 返回 popen 中执行命令的终止状态 。如果stream无效,或者系统调用失败,则返回-1。

```c
/*******popen.c*******/
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#define MAXSTRS 5
int main(void)
{
	int cntr;
	FILE *pipe_fp;
	char *strings[MAXSTRS] = { "echo", "bravo", "alpha","charlie","delta"};
	if (( pipe_fp = popen("sort", "w")) == NULL) /*调用 popen 创建管道 */
	{
		perror("popen");
		exit(1);
	}
	for(cntr=0; cntr<MAXSTRS; cntr++) /* 循环处理 */
	{
		fputs(strings[cntr], pipe_fp);
		fputc('\n', pipe_fp);
	}
	pclose(pipe_fp); /* 关闭管道 */
	return(0);
}
```
#### 有名管道

管道机制也存在着一些缺点和不足。由于管道是一种“无名”、“无形”的文件,它就只能通过fork()的过程创建于“近亲”的进程之间,而不可能成为可以在任意两个进程之间建立通信的机制,更不可能成为一种一般的、通用的进程间通信模型。同时,管道机制的这种缺点本身就强烈地暗示着人们,只要用“有名”、“有形”的文件来实现管道,就能克服这种缺点。这里所谓“有名”是指这样一个文件应该有个文件名,使得任何进程都可以通过文件名或路径名与这个文件挂上钩;所谓“有形”是指文件的inode应该存在于磁盘或其他文件系统介质上,使得任何进程在任何时间(而不仅仅是在fork()时)都可以建立(或断开)与这个文件之间的联系。所以,有了管道以后,“有名管道”的出现就是必然的了。与管道相比较,有名管道,即FIFO管道和一般的管道基本相同,但也有一些显著的不同:

- FIFO管道不是临时对象,而是在文件系统中作为一个特殊的设备文件而存在的实
体。并且可以通过mkfifo命令来创建。进程只要拥有适当的权限就可以自由的使
用FIFO管道。
- 不同祖先的进程之间可以通过有名管道共享数据。
- 当共享管道的进程执行完所有的I/O操作以后,有名管道将继续保存在文件系统中以便以后使用。

(1)FIFO 的创建
为了实现“有名管道”,在“普通文件”、“块设备文件”、“字符设备文件”之外,又设立了一种文件类型,称为FIFO文件。对这种文件的访问严格遵循“先进先出”的原则,不允许有在文件内移动读写指针位置的lseek()操作。这样一来,就可以像在磁盘上建立个文件一样地建立一个有名管道,有几种方法创建一个有名管道。

```sh
mknod MYFIFO p
mkfifo a=rw MYFIFO
```

> 上面的两个命令执行同样的操作,但其中有一点不同。命令 mkfifo 提供一个在创建之后直接改变 FIFO 文件存取权限的途径,而命令 mknod 需要调用命令chmod。一个物理文件系统可以通过 p 指示器十分容易地分辨出 FIFO 文件。请注意文件名后的管道符号“ |”。

mkfifo函数,该函数的作用是在文件系统中创建一个文件,该文件
用于提供FIFO功能,原型如下所示:

```c
#include <sys/types.h>
#include <sys/stat.h>
int mkfifo( const char *pathname, mode_t mode );
```

(2)使用实例

管道都是单向的,因此双方通信需要两个管道。该实例有两个程序,一个用于读管道,另一个用于写管道

```c
//*****fifowrite.c***
#include<sys/types.h>
#include<sys/stat.h>
#include<stdio.h>
#include<errno.h>
#include<fcntl.h>
#include<string.h>
#include<unistd.h>
#include<stdlib.h>
int main()
{
    char write_fifo_name[] = "lucy";
    char read_fifo_name[] = "peter";
    int write_fd, read_fd;
    char buf[256];
    int len;
    struct stat stat_buf;
    int ret = mkfifo(write_fifo_name, S_IRUSR | S_IWUSR);
    if( ret == -1)
    {
        printf("Fail to create FIFO %s: %s",write_fifo_name,strerror(errno));
        exit(-1);
    }
    write_fd = open(write_fifo_name, O_WRONLY);
    if(write_fd == -1)
    {
        printf("Fail to open FIFO %s: %s",write_fifo_name,strerror(errno));
        exit(-1);
    }
    while((read_fd = open(read_fifo_name,O_RDONLY)) == -1)
    {
        sleep(1);
    }
    while(1)
    {
        printf("Lucy: ");
        fgets(buf, 256, stdin);
        buf[strlen(buf)-1] = '\0';
        if(strncmp(buf,"quit", 4) == 0)
        {
            close(write_fd);
            unlink(write_fifo_name);
            close(read_fd);
            exit(0);
        }
        write(write_fd, buf, strlen(buf));

        len = read(read_fd, buf, 256);  /*这里read()函数会阻塞等待 */
        if( len > 0)
        {
            buf[len] = '\0';
            printf("Peter: %s\n", buf);
        }
    }
}
```
```c
//*****fiforead.c***
#include<sys/types.h>
#include<sys/stat.h>
#include<stdio.h>
#include<errno.h>
#include<fcntl.h>
#include<string.h>
#include<unistd.h>
#include<stdlib.h>
int main(void)
{
    char write_fifo_name[] = "peter";
    char read_fifo_name[] = "lucy";
    int write_fd, read_fd;
    char buf[256];
    int len;
    int ret = mkfifo(write_fifo_name, S_IRUSR | S_IWUSR);
    if( ret == -1)
    {
        printf("Fail to create FIFO %s: %s",write_fifo_name,strerror(errno));
        exit(-1);
    }
    while((read_fd = open(read_fifo_name, O_RDONLY)) == -1)
    {
        sleep(1);
    }
    write_fd = open(write_fifo_name, O_WRONLY);
    if(write_fd == -1)
    {
        printf("Fail to open FIFO %s: %s", write_fifo_name, strerror(errno));
        exit(-1);
    }
    while(1)
    {
        len = read(read_fd, buf, 256);  /*这里read()函数会阻塞等待 */
        if(len > 0)
        {
            buf[len] = '\0';
            printf("Lucy: %s\n",buf);
        }

        printf("Peter: ");
        fgets(buf, 256, stdin);
        buf[strlen(buf)-1] = '\0';
        if(strncmp(buf,"quit", 4) == 0)
        {
            close(write_fd);
            unlink(write_fifo_name);
            close(read_fd);
            exit(0);
        }
        write(write_fd, buf, strlen(buf));
    }
}
```

### 共享内存通信

共享内存可以说是最有用的进程间通信方式,也是最快的 IPC 形式。两个不同进程A、B 共享内存的意思是,同一块物理内存被映射到进程A、B各自的进程地址空间。进程A可以即时看到进程B对共享内存中数据的更新,反之亦然。由于多个进程共享同一块内存区域,必然需要某种同步机制,互斥锁和信号量都可以。

进程间需要共享的数据被放在一个叫做IPC共享内存区域的地方,所有需要访问该共享
区域的进程都要把该共享区域映射到本进程的地址空间中去。系统V共享内存通过shmget获得或创建一个IPC共享内存区域,并返回相应的标识符。对于系统V共享内存,主要有以下几个API:shmget()、shmat()、shmdt()及 shmctl()。

shmget()用来获得共享内存区域的ID,如果不存在指定的共享区域就创建相应的区
域。shmat()把共享内存区域映射到调用进程的地址空间中去,这样,进程就可以方便地对共享区域进行访问操作。shmdt()调用用来解除进程对共享内存区域的映射。Shmctl()实现对共享内存区域的控制操作。

使用实例  
创建了两个程序:sharewrite.c创建一个系统V共享内存区,并在其中写入格式化数据;另外一个程序 shareread.c访问同一个系统V共享内存区,读出其中的格式化数据。

```c
/***** sharewrite.c *******/
#include <sys/ipc.h>
#include <sys/shm.h>
#include <sys/types.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>

typedef struct{
    char name[4];
    int age;
} people;

int main(int argc, char** argv)
{
    int shm_id,i;
    key_t key;
    char temp;
    people *p_map;
    key = ftok(".",1);    /*得到一个指定ID值 */
    if(key==-1)
        perror("ftok error");
    
    shm_id=shmget(key,4096,IPC_CREAT);
    if(shm_id==-1)
    {
        perror("shmget error");
        return 0;
    }
    p_map=(people*)shmat(shm_id,NULL,0);    /*把共享内存区域映射到调用进程的地址空间中去 */

    temp='a';
    for(i = 0;i<10;i++)
    {
        temp += 1;
        memcpy((*(p_map+i)).name,&temp,1);
        (*(p_map+i)).age = 30+i;
    }
    if(shmdt(p_map)==-1)    /*解除进程对共享内存区域的映射 */
        perror(" detach error ");
    return 0;
}
```

```c
/********** shareread.c ************/
#include <sys/ipc.h>
#include <sys/shm.h>
#include <sys/types.h>
#include <unistd.h>
#include <stdio.h>

typedef struct{
    char name[4];
    int age;
} people;

int main(int argc, char** argv)
{
    int shm_id,i;
    key_t key;
    people *p_map;
    key = ftok(".",1);
    if(key == -1)
        perror("ftok error");
    shm_id = shmget(key,4096,IPC_CREAT);
    if(shm_id == -1)
    {
        perror("shmget error");
        return 0;
    }
    p_map = (people*)shmat(shm_id,NULL,0);
    for(i = 0;i<10;i++)
    {
        printf( "name:%s\n",(*(p_map+i)).name );
        printf( "age %d\n",(*(p_map+i)).age );
        if((i+1)%5==0)
            printf( "\n" );
    }
    if(shmdt(p_map) == -1)
        perror(" detach error ");
    return 0;
}
```

### 其他方式通信

其他的通信方式包括消息队列、信号量、信号以及套接字等进程间通信方式。消息队列就是一个消息的列表。用户可以从消息队列中添加消息、读取消息等。从这点上看,消息队列具有一定的 FIFO 的特性,但是它可以实现消息的随机查询,比 FIFO 具有更大的优势。同时,这些消息又是存在于内核中的,由“队列 ID”来标识;信号量不仅可以完成进程间通信,而且可以实现进程同步;套接字是应用非常广泛的进程间通信方式,它不仅能完成一般的进程间通信,更可用于不同机器之间的进程间通信。