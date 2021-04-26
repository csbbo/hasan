---
title: "用少于200行的代码编写一个自己的区块链(一)"
date: 2021-04-22T14:20:47+08:00
categories: ["用少于200行的代码编写一个自己的区块链"]
tags: ["Go", "blockchain"]
toc: true
---

Code your own blockchain in less than 200 lines of Go!
<!--more-->

![](https://miro.medium.com/max/1170/1*Elzguv8ycXYcphhD7M95hQ.jpeg)

本教程是改编于一篇基于javascript搭建一个基础的区块链。我们将其改编成用Go语言实现并添加一些额外的功能，如在浏览器上查看区块链。

教程的样例数据使用的是你平时的心率（毕竟我们是一家关注健康的公司）。记录你一分钟的脉搏数，并留存至本教程结束。

尽管每个开发者都听说过区块链，但是大多数人都不知道他的原理。他们知道区块链大多因为比特币和智能合约。本文将带你揭开区块链的神秘面纱，并教你如何用不超过200行的Go代码编写一个简单的区块链。本文结束后，你将可以在本机运行起一个区块链并可以在浏览器中查看。

还有什么比自己创建一个区块链更好的学习方法？

**你将能够做什么**

- 创建一个你自己的区块链
- 哈希函数如何保持区块链的正确性
- 区块链如何添加一个新的区块进来
- 决策器如何选择多个节点生成的区块
- 在浏览器中查看你的区块链
- 创建一个新的区块
- 对区块链有一个基础的了解，以便帮助你决定是否要开始你的区块链之旅

**你将不能做什么**

为了保证本篇文章的精简，我们不会使用共识算法，如工作量证明，权益证明。我们会模拟一个网络交互，因此你可以在浏览器中看到区块链和区块的添加，但是网络广播将会保留到后面的文章。

### 让我们开始吧！

**配置**
我们将会使用Go来编写代码，在此之前我们认为你已经对Go有一定的经验了。在安装和配置完Go之后，我们还需要拉取下面的包：

```
go get github.com/davecgh/go-spew/spew
```
`Spew`可以帮助我们在控制台查看完全格式化的结构体和切片。值得拥有！

```
go get github.com/gorilla/mux
```
`Gorilla/mux`是一个流行的Web控制器，我们将会需要它。

```
go get github.com/joho/godotenv
```
`Godotenv`帮助我们从项目的根目录读取.env文件，这样我们就不需要像HTTP端口那样硬编码。我们也需要它。

在项目的根目录创建.env文件，并定义HTTP服务的端口。像下面一样：
```
PORT=8080
```

创建一个`main.go`文件，现在开始所有的代码都会写在这里，并且确保代码总数少于200行，那么开始吧！

**导入**

导入需要的包，从包声明开始，让我们开始编写代码吧

```go
package main

import (
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"io"
	"log"
	"net/http"
	"os"
	"time"

	"github.com/davecgh/go-spew/spew"
	"github.com/gorilla/mux"
	"github.com/joho/godotenv"
)
```

**数据模型**

创建构成区块链的每个区块的结构体，不要担心，我们接下来将会解释每个字段的意思

```
type Block struct {
	Index     int
	Timestamp string
	BPM       int
	Hash      string
	PrevHash  string
}
```

每个Block包含的数据将会被写入到区块链，这数据代表的是你每次脉搏跳动的次数。

- Index 是数据记录位于区块链的位置
- Timestamp 是自动创建的，表示数据被写入的时间
- BPM 你的心率，即每分钟脉搏跳动次数
- Hash 是一个SHA256的识别码，代表数据记录
- PreHash SHA256识别码，代表前一个记录

对区块链本身建模，就是一个Block类型的slice：

```go
var Blockchain []Block
```

那么如何用哈希适合区块和区块链，我们使用散列确认区块和保证区块链顺序的正确。让每个区块的PreHash与前一个区块的Hash完全相同来保证区块链顺序的正确

![](https://miro.medium.com/max/1400/1*VwT5d8NPjUpI7HiwPa--cQ.png)

**散列和生成新的区块**

那么为什么我们需要散列？我们有两个重要的原因需要哈希编码数据：

- 节省空间，哈希来源于该区块的所有数据。在我们的例子中，我们仅有很少的数据点，但是想象一下当我们有成百上千，甚至上万的数据块时。相对于我们一遍遍的复制前面的数据，对数据进行哈希是非常高效的。
- 保持区块链的完整性,像上面图表那样我们存储前面的散列值能够确保区块链顺序的正确。如果有恶意分子进入并尝试操纵数据（例如修改心率去获得保险理赔）散列值会迅速改变，区块链也会断裂，这样大家都知道不该信任该恶意链条。

让我们创建一个函数，使用我们的Block数据生成散列值。
```go
func calculateHash(block Block) string {
	record := string(block.Index) + block.Timestamp + string(block.BPM) + block.PrevHash
	h := sha256.New()
	h.Write([]byte(record))
	hashed := h.Sum(nil)
	return hex.EncodeToString(hashed)
}
```

calculateHash函数使用我们传入的Block参数的Index,Timestamp,BPM,PrevHash生成Hash并以字符串形式返回。现在我们可以通过generateBlock函数利用我们所需元素生成新的区块了。我们需要提供前一个区块数据和心率。这里我们先不管BPM参数，这会在稍后解决。

```go
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

可以看到当前时间被自动写入Block，calculateHash被调用，PreHash复制于前一个Block的Hash，Index基于前一个Block的Index递增。

**块校验**

我们需要写一个函数确保区块没有别篡改。我们校验Index确保是符合期望的递增。校验PreHash与前一个区块的Hash相同。最后我们希望再次使用calculateHash函数去校验当前区块。isBlockValid函数，它仅返回bool类型值。

```go
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
假如我们碰到这样一个问题，区块链系统中同时有两个节点向它们的链添加了区块，那么我们应该选着哪个呢？我们应该选着最长的那条链。这是一个经典的区块链问题，对于恶意的操作者将不会执行任何的操作。

对于两个只是长度不同的良好节点，自然而然最长的链是最新的，拥有最新的区块。所以，我们需要确定新链要比拥有的当前链长。这样之后，我们可以用拥有最新区块的链去覆盖当前链。

![](https://miro.medium.com/max/1400/1*H1fCp0NLun0Kn0wIy0dyEA.png)

我们可以通过简单的对比两个链条切片的长度来实现上述功能：

```go
func replaceChain(newBlocks []Block) {
	if len(newBlocks) > len(Blockchain) {
		Blockchain = newBlocks
	}
}
```

如果到目前为止你已经完成了，拍拍你的背，我们基本上已经完成了构建区块链所需的所有函数。

Web服务器

我们假设你已经熟悉Web服务如何工作了，并且有丰富的经验编写Go Web服务的经验。接下来，我们将引导你走完整个流程。

我们在run函数里创建server，稍后将会用到。

```go
func run() error {
	mux := makeMuxRouter()
	httpAddr := os.Getenv("ADDR")
	log.Println("Listening on ", os.Getenv("ADDR"))
	s := &http.Server{
		Addr:           ":" + httpAddr,
		Handler:        mux,
		ReadTimeout:    10 * time.Second,
		WriteTimeout:   10 * time.Second,
		MaxHeaderBytes: 1 << 20,
	}

	if err := s.ListenAndServe(); err != nil {
		return err
	}

	return nil
}
```

端口是之前创建的`.env`文件中定义的。我们通过`log.Prinln`快速的打印控制台消息，帮助我们了解到服务已经启动。我们对服务进行配置后然后启动`ListenAndServe`。这是标准的Go操作。

接下来我们需要编写`makeMuxRouter`函数，这将会定义我们的控制器。为了能够在浏览器上查看区块链，我们仅需要两个路由。如果我们发送`GET`请求到`localhost`我们将会查看到我们的区块链。如果我们发送`POST`请求，我们将会往区块链写入一个区块。

```go
func makeMuxRouter() http.Handler {
	muxRouter := mux.NewRouter()
	muxRouter.HandleFunc("/", handleGetBlockchain).Methods("GET")
	muxRouter.HandleFunc("/", handleWriteBlock).Methods("POST")
	return muxRouter
}
```

`GET`句柄：

```go
func handleGetBlockchain(w http.ResponseWriter, r *http.Request) {
	bytes, err := json.MarshalIndent(Blockchain, "", "  ")
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	io.WriteString(w, string(bytes))
}
```

我们简单的用JSON格式数据回写整个区块链，这样我们就可以在访问`localhost:8080`端口时候查看到区块链的数据了。PORT变量在`.env`文件中被设置为`8080`,如果你修改了它，请访问你正确的端口。

我们的`POST`请求有一点点复杂，但是也还好。首先我们需要一个`Message` `struct`。我们很快就会讲解为什么我们需要它。

```go
type Message struct {
	BPM int
}
```

下面是创建一个新的区块的代码，我们将在你读完后引导你理解它。

```go
func handleWriteBlock(w http.ResponseWriter, r *http.Request) {
	var m Message

	decoder := json.NewDecoder(r.Body)
	if err := decoder.Decode(&m); err != nil {
		respondWithJSON(w, r, http.StatusBadRequest, r.Body)
		return
	}
	defer r.Body.Close()

	newBlock, err := generateBlock(Blockchain[len(Blockchain)-1], m.BPM)
	if err != nil {
		respondWithJSON(w, r, http.StatusInternalServerError, m)
		return
	}
	if isBlockValid(newBlock, Blockchain[len(Blockchain)-1]) {
		newBlockchain := append(Blockchain, newBlock)
		replaceChain(newBlockchain)
		spew.Dump(Blockchain)
	}

	respondWithJSON(w, r, http.StatusCreated, newBlock)

}
```

我们之所以使用一个分离的Message结构体是因为我们需要接收将要用于创建新区块的JSON格式的POST数据。这允许简单的发送下面内容，我们的控制器将会为我们填充剩余的区块。

```go
{"BPM":50}
```

50是一个心率的样例数据，修改这个整数为你自己的心率。


在我们完成对请求体解码到`var m Message`后，我们可以将之前的区块和心率数据输入`generateBlock`函数去创建一个新的区块。这是创建一个新的区块所需的所有函数了。我们使用`isbBlockValid`
函数做一个快速的校验我们的新区块是否合法。

两个注意事项

- `spew.Dump`是一个方便的函数，帮助我们在终端打印结构体数据，这对调试非常有利。
- 对于测试POST请求，我们喜欢使用Postman。但是如果你离不开终端，curl也非常不错。

当我们发送POST请求成功或者失败，我们希望得到相应的警告。我们使用一个小的包装函数来使我们知道发生了什么。记住在Go中不要忽略错误，优雅的处理他们。

```go
func respondWithJSON(w http.ResponseWriter, r *http.Request, code int, payload interface{}) {
	response, err := json.MarshalIndent(payload, "", "  ")
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte("HTTP 500: Internal Server Error"))
		return
	}
	w.WriteHeader(code)
	w.Write(response)
}
```

几乎要完成了

让我们将所有这些不同的区块链函数，Web处理器和Web服务连接到一个简短简洁的main函数中。

```go
func main() {
	err := godotenv.Load()
	if err != nil {
		log.Fatal(err)
	}

	go func() {
		t := time.Now()
		genesisBlock := Block{0, t.String(), 0, "", ""}
		spew.Dump(genesisBlock)
		Blockchain = append(Blockchain, genesisBlock)
	}()
	log.Fatal(run())

}
```

那么这是怎么回事？

- `godotenv.Load()`允许我们从.env文件读入变量例如我们的端口，因此我们无需硬编码在应用程序里。
- `genesisBlock` 是main函数里面最重要的部分，我们需要为区块链提供一个初始区块，否者新区块将无法通过对比前一个区块的哈希去创建，因为前一个区块不存在。
- 我们隔离创始区块在一个自己的协程中，这样我们就可以将区块链的关注点与Web服务逻辑分离。没有协程也可以工作但这样会更干净。


终于完成了！

现在来看看有趣的东西，让我们尝试一下

在终端启动你的应用`go run main.go`

在终端，我们可以看到Web服务已经启动并打印出创世区块。

![](https://miro.medium.com/max/1400/1*sAkFOcjHxX9WnjGPud84rQ.png)

现在访问localhsot后接你的端口，我们这里是8080。正如期望那样，我们可以看到相同的创世区块。

![](https://miro.medium.com/max/1244/1*4HRKAkMy1smgB9xpGLj6RA.png)

现在让我们发送一些POST请求去创建区块。使用Postman，我们传入BPM参数创建一些区块。

![](https://miro.medium.com/max/1400/1*eYfFp1lqJUiAS1S6K8ZHbQ.png)

让我们刷新浏览器，瞧瞧，所有的区块的PreHash字段跟前一个区块的Hash字段一样，这正如我们所期望的那样。

![](https://miro.medium.com/max/1400/1*Qo4eZ0hQ1gMdXrsvBGSnxg.png)

下一步

恭喜！！你已经使用正确的哈希和区块验证创建了你自己的区块链，你现在可以开始你的区块链之旅了并且探索更多复杂的主题，例如工作量证明，权益证明，智能合约，Dapps，从链等

本篇教程没解决的是如何用工作量证明来挖矿。这将是一个独立的教程，但也有很多的区块链是没有工作量证明机制的。此外，目前网络广播是通过web服务的编写和查看区块链来模拟的。这里也没有P2P部分。

