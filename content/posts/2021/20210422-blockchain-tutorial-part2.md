---
title: "用少于200行的代码编写一个自己的区块链(二)"
date: 2021-04-22T14:20:51+08:00
categories: ["用少于200行的代码编写一个自己的区块链"]
tags: ["Go", "blockchain"]
toc: true
---

翻译文章

用少于200行的Go代码编写一个自己的区块链

<!--more-->

![](https://miro.medium.com/max/1388/1*NfKseUObx0HJnuios5Rs5g.png)

你已经看过了本系列的第一部分了吗？如果没有，请先回去看完再来。不用担心我们会在这等你的...

欢迎回来！

我们震惊于在《用少于200行的Go代码编写一个自己的区块链》中收到的回复，这本是一个区块链入门开发者的一个小教程，但他却活了下来。我们收到请求希望添加网络部分教程。

上一篇文章向你展示了如何编写自己的区块链，并完成哈希和校验每一个新区块。但是它们都是运行在单个终端(节点)的。我们要怎样才能让其他节点连接到我们的主实例并且接收它们贡献的新区块，还有如何去广播更新后的区块链到所有节点？

接下来我们将告诉你

**工作流程**

![](https://miro.medium.com/max/700/1*POJgtLy6gcAznRYjKRer4Q.png)

- 第一个终端创建创世区块和一个可以被新节点连接的TCP服务

步骤一

- 打开其它终端并创建与第一个终端的连接
- 新终端向第一个终端写入一个区块

步骤二

- 第一个终端校验接收到的区块
- 第一个终端广播新的区块链给其它节点

步骤三

- 所有终端完成同步新的区块链

在学完本教程后，请自己尝试：让每个新终端也充当具有不同TCP端口“第一”终端，并让其他终端连接到改终端，以此建立一个真正的网络！

**你将能够做什么**

- 运行一个提供创世区块的终端
- 触发任意你想的数量终端，并让它们向第一个终端写入区块
- 让第一个终端向其他终端广播新的区块链

**你将无法做到什么**

正如上一篇教程那般，我们的目的是使节点的基本网络可以正常工作，以便您决定是否开启区块链之旅。您将无法让其它网络的计算机节点写入您的第一个终端，但你可以通过将二进制文件放到公有云来轻松实现。同样的，区块链广播也会被模拟发送到每个节点。不用担心，我们将会解释这些。

### 让我们开始吧！

这里有部分内容是跟上篇一样的，我们将使用许多相同的区块的生成，散列和验证功能。但是我们将不会使用HTTP，因为我们将在控制台中查看结果并使用TCP来进行网络连接

*TCP与HTTP有什么不同呢？*

这里不做详细介绍，您只需知道TCP是传输数据的基本协议即可，而HTTP是建立在TCP之上的，利用TCP的数据传输在Web和浏览器上使用。当你浏览一个网站，你用的是HTTP，而支持其数据传输的协议就是TCP

在本教程，我们将使用TCP，因为我们不需要在浏览器中浏览任何东西。Go中有一个非常优秀的`net`包提供了所有我们需要的TCP连接函数。


**配置，导入和回顾**

其中一部分将会是对第一部分的回顾，在我们快速回顾时，请耐心等待。不要担心，我们很快就会推出新内容！

**配置**

在根目录创建`.env`文件，并添加下面内容

```
ADDR=9000
```

存储我们将要使用的端口号（这里是9000）到名称为`ADDR`的环境变量

如果你还没有做好准备工作，拉取下面的包：

`go get github.com/davecgh/go-spew/spew`将区块链打印到终端

`go get github.com/joho/godotenv`加载`.env`中的环境变量

创建空的`main.go`文件，我们所有的代码将会写在这里

**导入**

```go
package main

import (
	"bufio"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"io"
	"log"
	"net"
	"os"
	"strconv"
	"time"

	"github.com/davecgh/go-spew/spew"
	"github.com/joho/godotenv"
)
```

**回顾**

下面的代码片段在第一篇教程有更详细的说明，如果你需要复习可以参考它。我这里将快速通过。

创建`Block`结构体，并声明一个`Block`切片类型的变量`Blockchain`
```go
// Block represents each 'item' in the blockchain
type Block struct {
	Index     int
	Timestamp string
	BPM       int
	Hash      string
	PrevHash  string
}

// Blockchain is a series of validated Blocks
var Blockchain []Block
```

现在创建哈希函数，在创建新的区块时将会用到
```go
// SHA256 hashing
func calculateHash(block Block) string {
	record := string(block.Index) + block.Timestamp + string(block.BPM) + block.PrevHash
	h := sha256.New()
	h.Write([]byte(record))
	hashed := h.Sum(nil)
	return hex.EncodeToString(hashed)
}
```

区块生成函数
```go

// create a new block using previous block's hash
func generateBlock(oldBlock Block, BPM int) (Block, error) {

	var newBlock Block

	t := time.Now()

	newBlock.Index = oldBlock.Index + 1
	newBlock.Timestamp = t.String()
	newBlock.BPM = BPM
	newBlock.PrevHash = oldBlock.Hash
	newBlock.Hash = calculateHash(newBlock)

	return newBlock, nil
}
```

通过对比新区块的`PreHash`与前一个区块的`Hash`是否相同来校验新区块的正确性

```go
// make sure block is valid by checking index, and comparing the hash of the previous block
func isBlockValid(newBlock, oldBlock Block) bool {
	if oldBlock.Index+1 != newBlock.Index {
		return false
	}

	if oldBlock.Hash != newBlock.PrevHash {
		return false
	}

	if calculateHash(newBlock) != newBlock.Hash {
		return false
	}

	return true
}
```

确保我们使用的是最长的链作为对的区块链

```go
// make sure the chain we're checking is longer than the current blockchain
func replaceChain(newBlocks []Block) {
	if len(newBlocks) > len(Blockchain) {
		Blockchain = newBlocks
	}
}
```

非常好！我们基本上获取我们需要的所有区块链相关函数并且去掉HTTP相关的部分。现在我们可以继续网络部分了

**网络**

最后！让我们建立一个可以绕过新区块的网络，并将其继承到我们的区块链中，然后将新的区块链广播会网络

让我们从主函数开始，这是一个很好地抽象有助于我们理解整体流程

在此之前，我们先在其它结构体声明之下声明一个通道变量`bcServer`(blockchain server简称)用来接收传入区块

```go
// bcServer handles incoming concurrent Blocks
var bcServer chan []Block
```

现在让我们声明`main`函数并从位于根目录的`.env`文件加载环境变量。记住，我们将要使用的环境变量`ADDR`是我们的TCP端口`9000`。还有让我们在`main`函数中编写`bcServer`例子

```go
func main() {
	err := godotenv.Load()
	if err != nil {
		log.Fatal(err)
	}

	bcServer = make(chan []Block)

	// create genesis block
	t := time.Now()
	genesisBlock := Block{0, t.String(), 0, "", ""}
	spew.Dump(genesisBlock)
	Blockchain = append(Blockchain, genesisBlock)
}
```

现在我们需要编写我们的TCP服务。记住，你可以认为TCP跟HTTP差不多只是没有浏览器部分而已。所有的数据传输将会通过终端的控制台来完成。我们将会控制很多的连接到我们的TCP端口。下面的代码编写接着main函数里的最后一行

```go
	// start TCP and serve TCP server
	server, err := net.Listen("tcp", ":"+os.Getenv("ADDR"))
	if err != nil {
		log.Fatal(err)
	}
	defer server.Close()
```

上面代码将在9000端口启动一个TCP服务。`efer server.Close()`是非常重要的，它能在我们不再使用服务时清理干净连接。


当我们接收到一个新的请求，我们需要创建一个新的连接来服务它
```go
for {
		conn, err := server.Accept()
		if err != nil {
			log.Fatal(err)
		}
		go handleConn(conn)
	}
```
我们创建了一个无限循环来接收新的连接请求。我们通过分离的go协程`go handleConn(conn)`来并发处理每个连接，因此我们无需阻塞`for`循环。这就是为什么我们可以同时服务大量的请求的原因。

在这里有精明的读者就会跳出来说：”等等， 我们还没有编写handleConn函数“。没错，但是让我们喘口气先，非常好，我们先完整的编写main函数，如下

```go
func main() {
	err := godotenv.Load()
	if err != nil {
		log.Fatal(err)
	}

	bcServer = make(chan []Block)

	// create genesis block
	t := time.Now()
	genesisBlock := Block{0, t.String(), 0, "", ""}
	spew.Dump(genesisBlock)
	Blockchain = append(Blockchain, genesisBlock)

	// start TCP and serve TCP server
	server, err := net.Listen("tcp", ":"+os.Getenv("ADDR"))
	if err != nil {
		log.Fatal(err)
	}
	defer server.Close()

	for {
		conn, err := server.Accept()
		if err != nil {
			log.Fatal(err)
		}
		go handleConn(conn)
	}

}
```

现在我们来编写handleConn函数，它只需要一个参数，即封装良好的`net.Conn`接口。Go的接口非常棒，我们认为这是与其它类C语言不同的地方。虽然并发和协程得到了更多的关注，但是接口和其隐式满足才是该语言最强大的地方。如果你还没有在Go中使用过接口，赶快去熟悉它。接口是你成为10x Go开发者路途的下一步。

放入函数的骨架，用`defer`关键字在函数结束时关闭每一个连接作为handleConn函数的开始

```go
func handleConn(conn net.Conn) {
	defer conn.Close()
}
```

接下来我们需要允许客户端添加新的区块继承到区块链中，我们将使用和第一篇教程一样的心率。花超过一分钟的时间将这个数字记住，这就是我们BMP的值

为了完成上述内容，我们需要：
- 提示客户端输入他们的BPM值
- 从`stdin`扫描客户端的输入
- 通过输入数据，使用我们之前创建的函数generateBlock, isBlockValid, replaceChain创建新区块
- 将新的区块链放入我们创建的通道并广播到网络中
- 允许客户端输入新的BPM值

这里是按照上面描述的正确顺序完成上述功能的代码
```go
    io.WriteString(conn, "Enter a new BPM:")

	scanner := bufio.NewScanner(conn)

	// take in BPM from stdin and add it to blockchain after conducting necessary validation
	go func() {
		for scanner.Scan() {
			bpm, err := strconv.Atoi(scanner.Text())
			if err != nil {
				log.Printf("%v not a number: %v", scanner.Text(), err)
				continue
			}
			newBlock, err := generateBlock(Blockchain[len(Blockchain)-1], bpm)
			if err != nil {
				log.Println(err)
				continue
			}
			if isBlockValid(newBlock, Blockchain[len(Blockchain)-1]) {
				newBlockchain := append(Blockchain, newBlock)
				replaceChain(newBlockchain)
			}

			bcServer <- Blockchain
			io.WriteString(conn, "\nEnter a new BPM:")
		}
	}()
```

我们创建一个新的扫描器。`for scanner.Scan()`循环是在自己的协程中阻塞的，所以程序可以在其他的连接中的并发的执行。我们快速的将BPM进行字符串转换（需要检查BPM是一个整数）。我们进行标准块的生成和查验，并使用新块创建新的区块链。

`bcServer <- Blockchain`语法的意思是抛出新的区块链到我们创建的通道中。然后提示客户端输入新的BPM值来创建下一个区块

**广播**

我们需要广播新区块链到所有我们服务中的连接。由于我们是在一台电脑上编写的代码，我们需要模拟如何将数据传输到所有的客户端。在代码的最后一行，在相同的handleConn函数中我们需要：

- 将我们的新区块数据转为JSON数据以便我们可以容易的阅读
- 将新区块打印到我们每一个连接的控制台
- 设置一个定期执行此操作的计时器，这样我们就不会被区块链数据所淹没。这也是您在实时区块链网络中看到的情况，在该网络中，每隔X分钟广播一次新的区块链。我们将用30秒
- 漂亮地将主区块链打印到第一个终端，以便我们可以看到正在发生的事情，并确保将由不同节点添加的区块确实集成到主区块链中

这里是按照上面描述的正确顺序完成上述功能的代码
```go
// simulate receiving broadcast
	go func() {
		for {
			time.Sleep(30 * time.Second)
			output, err := json.Marshal(Blockchain)
			if err != nil {
				log.Fatal(err)
			}
			io.WriteString(conn, string(output))
		}
	}()

	for _ = range bcServer {
		spew.Dump(Blockchain)
	}
```

非常棒！我们的handleConn函数写完了，事实上，整个程序也写完了，我们将代码控制在200行之下，还不错吧？