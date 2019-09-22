---
title: "Session Cookie Token JWT"
date: 2019-08-26T09:22:05+08:00
tags: ["Session","Cookie","Token","JWT"]
---

HTTP是无状态协议,而现实的业务场景中又往往需要保持用户的状态,Session和Cookie就是为解决这个问题而出现的。
<!--more-->
### 什么是Cookie

Cookie是客户端保存用户信息的一种机制，用来记录用户的一些信息，实际上Cookie是服务器在本地机器上存储的一小段文本，并随着每次请求发送到服务器。

Cookie技术通过请求和响应报文中写入Cookie信息来控制客户端的状态。Cookie会根据响应报文里的一个叫Set-Cookie的首部字段信息，通知客户端保存Cookie。当下客户端再向服务端发起请求时，客户端会自动在请求报文中加入Cookie值之后发送出去.之后服务端发现客户端发送过来的Cookie后，会检查是那个客户端发送过来的请求，然后对服务器上的记录，最后得到了之前的状态信息。

### 什么是Session

本来Session 是一个抽象概念，开发者为了实现中断和继续等操作，将user agent和server之间一对一的交互，抽象为"会话"，进而衍生出"会话状态"，也就是Session的概念。

Session的实现一般通过后端存储加Cookie来实现,常说的Session要比Cookie安全是因为敏感信息都存储在后端只将对应的Session ID通过Cookie交给前端存储。

> 目前常用的控制客户端状态的方案是Session+Cookie,仅靠Cookie本身也能通过存储用户信息维持交互状态,只是所有信息都存储在客户端一旦被劫持全部信息都会泄露,并且导致大量数据在网络上传输影响性能。Session的实现不一定只能依靠Cookie,比若说Session ID可以保存在url

### 什么是Token
token也称作令牌，由uid+time+sign[+固定参数]

token的认证方式类似于临时的证书签名, 并且是一种服务端无状态的认证方式, 非常适合于 REST API 的场景. 所谓无状态就是服务端并不会保存身份认证相关的数据。

+ uid: 用户唯一身份标识
+ time: 当前时间的时间戳
+ sign: 签名, 使用 hash/encrypt 压缩成定长的十六进制字符串，以防止第三方恶意拼接
+ 固定参数(可选): 将一些常用的固定参数加入到 token 中是为了避免重复查库

token在客户端一般存放于localStorage，cookie，或sessionStorage中。在服务器一般存于数据库中

token认证流程:

+ 用户登录，成功后服务器返回Token给客户端。
+ 客户端收到数据后保存在客户端
+ 客户端再次访问服务器，将token放入headers中
+ 服务器端采用filter过滤器校验。校验成功则返回请求数据，校验失败则返回错误码

> token可以抵抗csrf，cookie+session不行。假如用户正在登陆银行网页，同时登陆了攻击者的网页，并且银行网页未对csrf攻击进行防护。攻击者就可以在网页放一个表单，该表单提交src为http\://www.bank.com/api/transfer，body为count=1000&to=Tom。倘若是session+cookie，用户打开网页的时候就已经转给Tom1000元了.因为form 发起的POST 请求并不受到浏览器同源策略的限制，因此可以任意地使用其他域的 Cookie 向其他域发送 POST 请求，形成CSRF攻击。在post请求的瞬间，cookie会被浏览器自动添加到请求头中。但token不同，token是开发者为了防范csrf而特别设计的令牌，浏览器不会自动添加到headers里，攻击者也无法访问用户的token，所以提交的表单无法通过服务器过滤，也就无法形成攻击。

> session的状态，一般存于服务器内存或硬盘中，当服务器采用分布式或集群时，session就会面对负载均衡问题。

### 再来说说JWT

JSON Web Token（缩写 JWT）是目前最流行的跨域认证解决方案。

#### JWT原理
JWT 的原理是，服务器认证以后，生成一个 JSON 对象，发回给用户，就像下面这样。
```
{
  "姓名": "张三",
  "角色": "管理员",
  "到期时间": "2018年7月1日0点0分"
}
```
以后，用户与服务端通信的时候，都要发回这个 JSON 对象。服务器完全只靠这个对象认定用户身份。为了防止用户篡改数据，服务器在生成这个对象的时候，会加上签名（详见后文）。

服务器就不保存任何 session 数据了，也就是说，服务器变成无状态了，从而比较容易实现扩展。

#### JWT的数据结构

JWT 的三个部分依次如下。

+ Header（头部）
+ Payload（负载）
+ Signature（签名）

写成一行，就是下面的样子。
```
Header.Payload.Signature
```

**Header**

Header 部分是一个 JSON 对象，描述 JWT 的元数据，通常是下面的样子。
```
{
  "alg": "HS256",
  "typ": "JWT"
}
```
上面代码中，alg属性表示签名的算法（algorithm），默认是 HMAC SHA256（写成 HS256）；typ属性表示这个令牌（token）的类型（type），JWT 令牌统一写为JWT。

最后，将上面的 JSON 对象使用 Base64URL 算法（详见后文）转成字符串。

**Payload**

Payload 部分也是一个 JSON 对象，用来存放实际需要传递的数据。JWT 规定了7个官方字段，供选用。

+ iss (issuer)：签发人
+ exp (expiration time)：过期时间
+ sub (subject)：主题
+ aud (audience)：受众
+ nbf (Not Before)：生效时间
+ iat (Issued At)：签发时间
+ jti (JWT ID)：编号

除了官方字段，你还可以在这个部分定义私有字段，下面就是一个例子。
```
{
  "sub": "1234567890",
  "name": "John Doe",
  "admin": true
}
```
注意，JWT 默认是不加密的，任何人都可以读到，所以不要把秘密信息放在这个部分。

这个 JSON 对象也要使用 Base64URL 算法转成字符串。

**Signature**

Signature 部分是对前两部分的签名，防止数据篡改。

首先，需要指定一个密钥（secret）。这个密钥只有服务器才知道，不能泄露给用户。然后，使用 Header 里面指定的签名算法（默认是 HMAC SHA256），按照下面的公式产生签名。
```
HMACSHA256(
  base64UrlEncode(header) + "." +
  base64UrlEncode(payload),
  secret)
```
算出签名以后，把 Header、Payload、Signature 三个部分拼成一个字符串，每个部分之间用"点"（.）分隔，就可以返回给用户。

**Base64URL**

前面提到，Header 和 Payload 串型化的算法是 Base64URL。这个算法跟 Base64 算法基本类似，但有一些小的不同。

JWT 作为一个令牌（token），有些场合可能会放到 URL（比如 api.example.com/?token=xxx）。Base64 有三个字符+、/和=，在 URL 里面有特殊含义，所以要被替换掉：=被省略、+替换成-，/替换成_ 。这就是 Base64URL 算法。

#### JWT 的使用方式
客户端收到服务器返回的 JWT，可以储存在 Cookie 里面，也可以储存在 localStorage。

此后，客户端每次与服务器通信，都要带上这个 JWT。你可以把它放在 Cookie 里面自动发送，但是这样不能跨域，所以更好的做法是放在 HTTP 请求的头信息Authorization字段里面。
```
Authorization: Bearer <token>
```
另一种做法是，跨域的时候，JWT 就放在 POST 请求的数据体里面。

#### JWT 的几个特点
1. JWT 默认是不加密，但也是可以加密的。生成原始 Token 以后，可以用密钥再加密一次。
2. JWT 不加密的情况下，不能将秘密数据写入 JWT。
3. JWT 不仅可以用于认证，也可以用于交换信息。有效使用 JWT，可以降低服务器查询数据库的次数。
4. JWT 的最大缺点是，由于服务器不保存 session 状态，因此无法在使用过程中废止某个 token，或者更改 token 的权限。也就是说，一旦 JWT 签发了，在到期之前就会始终有效，除非服务器部署额外的逻辑。
5. JWT 本身包含了认证信息，一旦泄露，任何人都可以获得该令牌的所有权限。为了减少盗用，JWT 的有效期应该设置得比较短。对于一些比较重要的权限，使用时应该再次对用户进行认证。
6. 为了减少盗用，JWT 不应该使用 HTTP 协议明码传输，要使用 HTTPS 协议传输。