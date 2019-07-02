---
title: "Linux串口通信"
date: 2019-06-30T20:45:31+08:00
draft: true
tags: ["Linux"]
categories: ["嵌入式Linux操作系统笔记"]
---

在Linux中,串口是一个字符设备,访问具体的串行端口的编程与读/写文件的操作类似,只需打开相应的设备文件操作即可.串口编程的特殊在于串口通信时相关参数的设置.若在根文件没有串口设备文件可以用mknod命令创建.

串口作为终端I/O,它的参数设置需要使用`struct termios`结构体,这个结构体在termio.h文件中定义.
```c
typedef unsigned char cc_t;
typedef unsigned int speed_t;
typedef unsigned int tcflag_t;
struct termios{
    tcflag_t c_iflag;   //输入模式标志
    tcflag_t c_oflag;   //输出模式标志
    tcflag_t c_cflag;   //控制模式标志
    tcflag_t c_lflag;   //本地模式标志
    tcflag_t c_line;    //行规程类型,一般应用程序不使用
    cc_t c_cc[NCC]; //控制字符
    speed_t c_ispeed;   //输入数据波特率
    speed_t c_ospeed;   //输出数据波特率
}
```

<!--more-->
串口的设置主要就是设置这个结构体的各成员值,然后利用该结构体将参数传给硬件驱动程序.tcgetattr()/tcsetattr()函数获取/设置串口参数.
```c
int tcgetattr(int fd,struct termios *termios_p);
int tcsetattr(int fd,int optional_actions,struct termios *termios_p);
```
optional_action参数用于指定新设定参数生效时间,其设定值可以为:

TCSANOW|改变立即生效
---|---
TCSADRAIN|所有输出都被传输后改变生效
TCSAFLUSH|所有输出都被传输后改变生效,丢弃所有未读入的输入

1.设置波特率
使用函数设置波特率,使用波特率常数设置波特率,其定义为字母"B+波特率",如B19200就是波特率为19200 bps.
```c
int cfsetispeed(struct termios *termios_p,speed_t speed);
int cfsetospeed(struct termios *termios_p,speed_t speed);
```
例:
```c
int cfsetispeed(ttys0_opt,B115200);
int cfsetospeed(ttys0_opt,B115200);
```

2.设置控制模式标志
控制模式标志c_cflag主要用于设置串口对DCD信号状态检测,硬件流控制,字符位宽,停止位和奇偶检验位等,常用标志如下:

CLOCAL | 忽略DCD信号,若不使用MODEM或没有串口/CD脚,则设置此标志
---|---
CREAD | 启用接收装置,可以接收字符
CRTSCTS | 启用硬件流控制,对于许多三线制的串不应使用,需要设置~CRTCTS
CSIZE | 字符数位掩码,常用CS8
CSTOPB | 使用两个停止位,若用一位应设置~CSTOPB
PARENB | 启用奇偶检验

例如:
```c
struct temios ttys0
ttys0.c_cflag |= CLOCAL | CREAD;
ttys0.c_cflag &= ~CRTSCTS;
ttys0.c_cflag &= ~CSIZE;
ttys0.c_cflag |= CS8;
ttys0.c_cflag &= ~(PARENB|CSTOPB);
```

3.设置本地模式标志

本地模式设置标志c_lflag主要用于设置终端与用户的交互方式,常见的设置标志位有ICAN-ON,ECHO和ECHOE等.

4.设置输入模式标志

输入模式标志c_iflag主要用于控制串口的输入特性,常用的设置有IXOFF和IXON,分别用于软件流控制.

起用软件流控制:
```c
ttys0_opt.c_iflag |= IXOFF|IXON
```

5.设置输出模式标志

输出模式标志c_oflag主要用于处理串口在规范模式时输出的特殊字符,而对非规范模式无效.

6.设置控制字符

在非规范模式中,控制字符数组c_cc[]中的变量c_cc[VMIN]和c_cc[VTIME]用于设置read()返回前读到的最少字节数和读超时时间.

7.清空发送/接收缓冲区

为保证读/写操作不被串口缓冲区中原有的数据干扰,可以在读/写数据前用tc_flush()函数清空发送/接收缓冲区.

串口配置函数实例:
```c
int set_com_config(int fd,int baud_rate,int data_bits,char parity,int stop_bits)
{
	struct termios new_cfg,old_cfg;
	int speed;
	/* 保存测试现有串口参数设置 */
	if(tcgetattr(fd,&old_cfg) != 0){
		perror("tcgetattr");
		return -1;
	}
	/* 设置字符大小 */
	new_cfg = old_cfg;
	cfmakeraw(&new_cfg);
	new_cfg.c_flag &= ~CSIZE;
	/* 设置串口波特率 */
	switch(baud_rate){
		case 2400:
			speed = B2400;
			break;
		case 4800:
			speed = B4800;
			break;
		case 9600:
			speed = B9600;
			break;
		case 19200:
			speed = B19200;
			break;
		case 38400:
			speed = B38400;
			break;
		default:
		case 115200:
			speed = B115200;
			break;
	}
	cfsetispeed(&new_cfg,speed);
	cfsetospeed(&new_cfg,speed);
	/* 设置停止位 */
	switch(data_bits){
		case 7:
			new_cfg.c_cflag |= CS7;
			break;
		default:
		case 8:
			new_cfg.c_cflag |= CS8;
			break;
	}
	/* 设置奇偶检验位 */
	switch(parity){
		default:
		case 'n':
		case 'N':
			new_cfg.c_flag &= ~PARENB;
			new_cfg.ciflag |= INPCK;
			break;
		case 'o':
		case 'O':
			new_cfg.c_cflag |= (PARODD | PARENB);
			new_cfg.c_iflag |= INPCK;
			break;
		case 'e':
		case 'E':
			new_cfg.c_cflag |= PARENB;
			new_cfg.c_cflag &= ~PARODD;
			new_cfg.c_cflag |= INPCK;
			break;
		case 's':
		case 'S':
			new_cfg.c_cflag &= ~PARENB;
			new_cfg.c_cflag &= ~CSTOPB;
			break;
	}
	/* 设置停止位 */
	switch(stop_bits){
		default:
		case 1:
			new_cfg.c_cflag &= ~CSTOPB;
			break;
		case 2:
			new_cfg.c_cflag |= ~CSTOPB;
	}
	/* 设置等待时间和最小接收字符 */
	new_cfg.c_cc[VTIME] = 0;
	new_cfg.c_cc[VMIE] = 1;
	/* 处理接收字符 */
	tcflush(fd,TCIFLUSH);
	/* 激活新配置 */
	if((tcsetattr(fd,TCSANOW,&new_cfg)) != 0){
		perror("tcsetattr");
		return -1;
	}
	return 0;
}
```