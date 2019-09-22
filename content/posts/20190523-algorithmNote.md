---
title: "算法笔记"
date: 2019-05-23T20:04:46+08:00
tags: ["algorithm"]
categories: ["算法"]
toc : true
---

记录一下几个比较经典的算法。
<!--more-->
### HuffmanTree
> [哈弗曼编码](https://zh.wikipedia.org/wiki/%E9%9C%8D%E5%A4%AB%E6%9B%BC%E7%BC%96%E7%A0%81):使用变长编码表对应原符号进行编码，其中变长编码表是通过一种评估原符号出现几率的方法得到的，出现几率高的使用较短的编码，反之出现几率低的则使用较长的编码，这便使编码后的字符串的平均长度、期望值降低，从而达到无损压缩数据的目的。

使用01进行哈弗曼编码时任何一个编码都不能是另一个编码的前缀，满足这样性质的编码称为前缀码(Preix Code)  
最优编码问题，给出n个字符的频率，每个字符赋予一个01编码串，使得任意一个字符的编码不是另一个字符的前缀，而且编码后的总长度尽量小

Huffman算法:把每个字符看做单节点子树放在一个树的集合中，每棵子树的权值等于相应字符的频率。每次取出权值最小的两棵子树合并成一棵新树，并重新放到集合中。新树的权值等于两棵子树的权值之和。

下面证明算法的正确性:

结论 1: 设x和y是频率最小的两个字符,则存在前缀码使得x和y具有相同的码长，且仅有最后一位编码不同，也就是说第一步贪心算法选择保留最优解。
证明: 假设深度最大节点为a，则a一定有一个兄弟节点b。不妨设f(x)<=f(y),f(a)<=f(b),则f(x)<=f(a),f(y)<=f(b)。如果x不是a,则交换x和a;如果y不是b则交换y和b,这样得到新编码的树不会比。
结论 2: 设T是加权字符集C的最优编码树，x和y是树T中两个叶子节点，且互为兄弟节点，z是他们的父节点。若把z看成是具有频率f(z)=f(x)+f(y)的字符，则树T\`=T-{x,y}是字符集C\`=C-{x,y}U{z}的一棵最优编码树。换句话说，原问题最优解包含子问题最优解。
证明: 设T\`的编码长度为L，其中字符{x,}的深度为h，则把字符{x,y}拆成两个后,长度变为L-(f(x)+f(y))*h+(f(x)+f(y))*(h+1)=L+f(x)+f(y)。因此T\`必须是C\`的最优编码树,T才是C的最优编码树。

```c
#include<cstdio>
#include<deque>
#include<algorithm>

struct Node{
    unsigned int weight;
    Node *left;
    Node *right;
    Node(int x):weight(x),left(NULL),right(NULL){}
};



bool isFirst = true;   //输出时判断是否第一个

bool compare(Node* a,Node* b);  //定义排序规则
void printHuffmanLeaf(Node* root);  //输出编码后的叶子,从左至右
Node* encodingHuffman(int* arr,int len);    //构造哈夫曼树

int main(){
    int arr[8]={5,29,7,8,14,23,3,11};  //测试数据,根据权值进行哈夫曼编码
    const int len = sizeof(arr)/sizeof(unsigned int);

    Node* ptr = encodingHuffman(arr,len);
    printf("[");
    printHuffmanLeaf(ptr);
    printf("]\n");
    return 0;
}

bool compare(Node* a,Node* b){
    return a->weight < b->weight;   //按从小到大顺序来排
}

void printHuffmanLeaf(Node* root){
    if(root->left==NULL && root->right==NULL){
        if(isFirst)
            isFirst=false;
        else
            printf(",");
        printf("%d",root->weight);
        return ;
    }
    printHuffmanLeaf(root->left);   //递归输出左边叶子节点
    printHuffmanLeaf(root->right);  //递归输出右边叶子节点
}

Node* encodingHuffman(int* arr,int len){
    std::deque<Node*> huffmanTree;   //用于构造哈夫曼树的队列
    for(int i=0;i<len;++i){     //初始化队列
        Node* ptr = new Node(arr[i]);
        huffmanTree.push_back(ptr);
    }
    for(int i=0;i<len-1;i++){
        std::sort(huffmanTree.begin(),huffmanTree.end(),compare);    //每次构造完新的哈夫曼树后从新排序
        Node* ptr = new Node(huffmanTree[0]->weight + huffmanTree[1]->weight);  //新哈夫曼树的权值等于当前最小两棵哈夫曼树权值和(每个节点看做一棵小的哈夫曼树)
        ptr->left = huffmanTree[0];     //新节点的左子树等于最小哈夫曼树
        ptr->right = huffmanTree[1];    //新节点的右子树等于次小哈夫曼树
        huffmanTree.pop_front();        //最小两棵哈夫曼树出队
        huffmanTree.pop_front();
        huffmanTree.push_back(ptr);     //新哈夫曼树入队
    }
    return huffmanTree.front();         //返回第一棵哈夫曼树,此时也仅剩一棵了
}
```

时间复杂度分析:可以看出来本例算法时间主要用在哈弗曼编码函数encodingHuffuman()第二个for循环中,循环每次只需要将队列的前两个合成一棵哈夫曼树所以其时间复杂度为O(n),每次插入新的哈夫曼树后需要重新排序，其时间复杂度是logn,总的时间复杂度为O(nlogn)

---

### 欧几里得算法

> 欧几里得算法其实就是[辗转相除法](https://zh.wikipedia.org/wiki/%E8%BC%BE%E8%BD%89%E7%9B%B8%E9%99%A4%E6%B3%95),是求最大公约数的算法。  
辗转相除法基于如下原理：两个整数的最大公约数等于其中较小的数和两数的差的最大公约数。(a>b当a>2b需要重复多次a-b操作,所以欧几里得算法也是，两个整数的最大公约数等于其中较小的数和两数的余数的最大公约数)

```c
gcd(a,b){
    if(a<b)
        swap(a,b);
    return b==0 ? a:gcd(b,a%b);
}
```

辗转相除法的计算过程可以用图形演示。[24]假设我们要在a×b的矩形地面上铺正方形瓷砖，并且正好铺满，其中a大于b。我们先尝试用b×b的瓷砖，但是留下了r0×b的部分，其中r0<b。我们接着尝试用r0×r0的正方形瓷砖铺，又留下了r1×r0的部分，然后再使用r1×r1的正方形铺……直到全部铺满为止，即到某步时正方形刚好覆盖剩余的面积为止。此时用到的最小的正方形的边长就是原来矩形的两条边长的最大公约数。在图中，最小的正方形面积是21×21（红色），而原先的矩形（绿色）边长是1071×462，所以21是1071和462的最大公约数。

![](https://upload.wikimedia.org/wikipedia/commons/thumb/1/1c/Euclidean_algorithm_1071_462.gif/51px-Euclidean_algorithm_1071_462.gif)

---

### 杨辉三角

> [杨辉三角](https://zh.wikipedia.org/wiki/%E6%9D%A8%E8%BE%89%E4%B8%89%E8%A7%92%E5%BD%A2)，是二项式系数在三角形中的一种几何排列。

```c
#include
              １
　　　　　　　１　１
　　　　　　１　２　１
　　　　　１　３　３　１
　　　　１　４　６　４　１
　　　１　５　10　10　５　１
　　１　６　15　20　15　６　１
　１　７　21　35　35　21　７　１
１　８　28　56　70　56　28　８　１

```

(a+b)<sup>n</sup> 展开，将得到一个关于x的多项式:  
(a+b)<sup>0</sup> = 1  
(a+b)<sup>1</sup> = a+b  
(a+b)<sup>2</sup> = a<sup>2</sup> + 2ab + b<sup>2</sup>  
(a+b)<sup>3</sup> = a<sup>3</sup> + 3a<sup>2</sup>b + 3ab<sup>2</sup>  + b<sup>3</sup>  
(a+b)<sup>3</sup> = a<sup>4</sup> + 4a<sup>3</sup>b + 6a<sup>2</sup>b<sup>2</sup> + 4ab<sup>3</sup> + b<sup>4</sup>  

系数正好和杨辉三角一致,而(a+b)<sup>n</sup> =  &sum;<sup>n</sup><sub>k=0</sub> C<sup>k</sup><sub>n</sub>a<sup>n-k</sup>b<sup>k</sup>  
组合数公式: `C = n!/k!(n-k)!`  
(<i>[特殊符号](http://www.w3school.com.cn/tags/html_ref_symbols.html)</i>)

```c
１
１　１
１　２　１
１　３　３　１
１　４　６　４　１
１　５　10　10　５　１
１　６　15　20　15　６　１
１　７　21　35　35　21　７　１
１　８　28　56　70　56　28　８　１

```
把杨辉三角左对齐一下不难发现，从第二行开始元素x[i][j]的值等于x[i-1][j-1]+x[i-1][j],构造一个n次方的杨辉三角
```c
#include<cstdio>
#include<cstring>
using namespace std;

int main(){
    int n;
    scanf("%d",&n);
    int triangle[n+1][n+1];
    memset(triangle,0,sizeof(triangle));

    for(int i=0;i<=n;i++){  //构造杨辉三角
        triangle[i][0] = 1;
        for(int j=1;j<=i;j++){
            triangle[i][j] = triangle[i-1][j-1] + triangle[i-1][j];
        }
    }

    for(int i=0;i<=n;i++){  //输出
        for(int j=0;j<=i;j++){
            printf("%3d ",triangle[i][j]);
        }
        printf("\n");
    }
    return 0;
}
```


---

### 迪杰斯特拉算法(Dijkstra)

> [迪杰斯特拉算法](https://zh.wikipedia.org/wiki/%E6%88%B4%E5%85%8B%E6%96%AF%E7%89%B9%E6%8B%89%E7%AE%97%E6%B3%95)使用了广度优先搜索解决赋权有向图的单源最短路径问题。该算法存在很多变体；戴克斯特拉的原始版本找到两个顶点之间的最短路径，但是更常见的变体固定了一个顶点作为源节点然后找到该顶点到图中所有其它节点的最短路径，产生一个最短路径树。该算法常用于路由算法或者作为其他图算法的一个子模块。

最初的戴克斯特拉算法不采用最小优先级队列，时间复杂度是O(|V|<sup>2</sup>)(其中|V|为图的顶点个数)通过斐波那契堆实现的戴克斯特拉算法时间复杂度是O(|E|+|V|log|V|) (其中|E|是边数)。对于不含负权的有向图，这是当前已知的最快的单源最短路径算法。

```c
void Dijkstra(Vertex s)
{
    while(1){
        V = 未收录顶点中dist最小者
        if(这样的顶点不存在)
            break;
        collected[V] = true;
        for(V的每个邻接点W){
            if(collected[W] == false){
                if(dist[V]+E<v,w> < dist[W]){
                    dist[W] = dist[V] + E<v,w>;
                    path[W] = V;
                }
            }
        }
    }
}
```

---

### 普里姆算法(Prim)

[普里姆算法](https://zh.wikipedia.org/wiki/%E6%99%AE%E6%9E%97%E5%A7%86%E7%AE%97%E6%B3%95)图论中的一种算法，可在加权连通图里搜索最小生成树。意即由此算法搜索到的边子集所构成的树中，不但包括了连通图里的所有顶点，且其所有边的权值之和亦为最小。

```c
void Prim()
{
    MST = {s};
    while(1){
        V = 未收录顶点中dist最小值
        if(这样的V不存在){
            break;
        }
        将V收录进MST: dist[V] = 0;
        for(V的每个邻接点W){
            if(dist[W] != 0){
                if(E(v,w) < dist[W]){
                    parent[W] = V;
                }
            }
        }
    }
    if(MST中的顶点不到|V|个)
        Error("生成树不存在")
}
```
最小边、权的数据结构 | 时间复杂度（总计）
---|---
邻接矩阵、搜索                             |   O(\|V\|<sup>2</sup>)
二叉堆、邻接表   |  O((\|V\|+\|E\|)log\|V\|)=O(\|E\|log \|V\|)
斐波那契堆、邻接表                          |    O(\|E\|+\|V\|log\|V\|)

通过邻接矩阵图表示的简易实现中，找到所有最小权边共需O(\|V\|<sup>2</sup>)的运行时间。使用简单的二叉堆与邻接表来表示的话，普里姆算法的运行时间则可缩减为O(\|E\|log \|V\|)，其中|E|为连通图的边集大小，|V|为点集大小。如果使用较为复杂的斐波那契堆，则可将运行时间进一步缩短为 O(\|E\|+\|V\|log\|V\|)，这在连通图足够密集时（当|E|满足&Omega;(|V|log|V|)}条件时），可较显著地提高运行速度。

---

### 克鲁斯克尔算法(Kruskal)

> [Kruskal算法](https://zh.wikipedia.org/wiki/%E5%85%8B%E9%B2%81%E6%96%AF%E5%85%8B%E5%B0%94%E6%BC%94%E7%AE%97%E6%B3%95)是一种用来查找最小生成树的算法，由Joseph Kruskal在1956年发表。用来解决同样问题的还有Prim算法和Boruvka算法等。三种算法都是贪心算法的应用。和Boruvka算法不同的地方是，Kruskal算法在图中存在相同权值的边时也有效。

```c
void Kruskal(Graph G)
{
    MST = {};
    while(MST中不到|V|-1条边 && E中还有边){
        从E中取一条权重最小的边E(v,w);
        将E(v,w)从E中删除;
        if(E(v,w)不在MST中构成回路)
            将E(v,w)加入MST
        else
            彻底无视
    }
    if(MST中不到|V|-1条边)
        Error("生成树不存在")
}
```
相比于Prim，Kruskal更适合稀疏图