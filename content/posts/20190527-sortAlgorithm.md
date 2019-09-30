---
title: "常用排序算法"
date: 2019-05-27T20:02:58+08:00
tags: ["sort","algorithm"]
categories: ["算法"]
toc : true
---

排序算法有很多，但就其全面性而言，很难提出一种被认为是最好的方法，每一种方法都有各自的优缺点，适合在不同的环境使用。排序过程是一个逐步扩大记录的有序序列长度的过程，排序过程中可以将记录分为有序序列区和无序序列区。
<!--more-->
以下算法都是经过运行成功的C++代码:

### 插入排序
> 插入排序是一种简单直观的排序算法。它的工作原理是通过构建有序序列，对于未排序数据，在已排序序列中从后向前扫描，找到相应位置并插入。插入排序在实现上，通常采用in-place排序（即只需用到O(1)的额外空间的排序），因而在从后向前扫描过程中，需要反复把已排序元素逐步向后挪位，为最新元素提供插入空间。

```cpp
void InsertSort(int arr[],int len){
    for(int i=1;i<len;i++){
        int key=arr[i];     //当前要插入值
        int j=i-1;
        while(key<arr[j] && j>=0){      //从后往前比对，比当前值大都往后挪
            arr[j+1]=arr[j];            //后一个等于前一个
            j--;
        }
        arr[j+1]=key;                   //找到合适位置后插入
    }
}
```

时间复杂度分析:

最好情况就是，序列已经是升序排列了，在这种情况下，需要进行的比较操作需n-1次即可。最坏情况就是，序列是降序排列，那么此时需要进行的比较共有1/2n(n-1)次。插入排序的赋值操作是比较操作的次数减去n-1次，（因为n-1次循环中，每一次循环的比较都比赋值多一个，多在最后那一次比较并不带来赋值）。平均来说插入排序算法复杂度为(O(n<sup>2</sup>))。

空间复杂度分析:

直接排序只需要一个记录的辅助空间r[0]，所以空间复杂度为O(1)

算法特点:

1. 稳定排序
2. 算法简便，容易实现
3. 适用链式存储结构
4. 更适合数据基本有序，数据量较小的排序

### 折半插入排序
> 折半插入排序是对插入排序的一种改进，由于插入排序过程中就是不断依次将元素插入前面已经排好的序列中。而前半部分已经是排好序的数列，所以在查找的过程中采用这半查找来加快查找插入点的速度

```cpp
void BinaryInsertSort(int arr[],int len){
    for(int i=1;i<len;i++){
        int key=arr[i];     //当前要插入值
        int low=0,high=i-1; //查找区间0~i-1
        while(low<=high){
            int m = (low+high)/2;
            if(key<arr[m])
                high = m-1; //插入点在前半段
            else
                low = m+1;  //插入点在后半段
        }
        for(int j=i-1;j>=high+1;j--)
            arr[j+1] = arr[j];
        arr[high+1]=key;                   //找到合适位置后插入
    }
}
```

复杂度分析:

在平均的情况下，折半插入排序只是减少了关键字的比较次数，而记录的移动次数不变因此时间复杂度仍然为O(n<sup>2</sup>),空间复杂度同插入排序

算法特点:

1. 因为要进行折半所以不适合链式存储结构
2. 适合初始记录无序,n较大的情况

### 希尔排序

> 希尔排序又称缩小增量排序，是插入排序中的一种。希尔排序实际是分组插入排序，将整个待排序列每次按照不同的增量分组。当经过几次分组后整个序列基本有序时，再对全体记录进行一次插入排序。

```cpp
void ShellSort(int arr[],int len){
    for(int d=len/2;d>0;d = d/2){

        /*
            按照增量进行插入排序
            插入排序中每次相差1的比较和赋值改为d
        */
        for(int i=d;i<len;i++){
        int key=arr[i];    
        int j=i-d;
        while(key<arr[j] && j>=0){   
            arr[j+d]=arr[j];     
            j = j-d;
        }
        arr[j+d]=key;    
    }
    }
}
```

复杂度分析:

当n &rarr; &infin;时，时间复杂度可减少到O(n(log<sub>2</sub>n)<sup>2</sup>)。空间复杂度仍然为O(1)

算法特点:

1. 跳跃式异动导致排序方法不稳定
2. 无法用于链式存储结构
3. 增量序列可以有多种取法，但是但应该使增量值序列中没有除1以外的公因子，且最后一个增量必须是1
4. 记录总的比较次数和移动次数都比插入排序要少，n越大越好，适用于初始记录无序n较大的情况

> 小结: 以上三种排序都是插入类排序，将无序序列中一个或多个序列插入到有序序列中

### 冒泡排序

> 冒泡排序是一种简单的交换排序方法，他通过两两比较相邻记录的关键字，如果发生逆序，则进行交换，从而使关键字小的记录如气泡一般逐渐往上漂浮(左移)，或者使关键字大的记录如石块一样向下坠落(右移)

```cpp
void BubbleSort(int arr[],int len)
{
    int m = len-1;
    bool flag = true;   //flag用来判断一趟排序是否交换
    while((m>0) && flag){ //若本趟排序没有发生交换则表明数据已经有序不再往后执行排序
        flag = false;
        for(int j=0;j<m;j++){
            if(arr[j] > arr[j+1]){
                flag=true;
                swap(arr[j],arr[j+1]);
            }
        }
        m--;
    }
}
```

复杂度分析:

在平均情况下，冒泡排序关键字的比较次数和移动次数分别约为n<sup>2</sup>/4和3n<sup>2</sup>/4,时间复杂度为O(n<sup>2</sup>),只有在两个元素交换时候需要一个辅助空间来暂存记录，所以空间复杂度为O(1)

算法特点:

1. 稳定排序，可用于链式存储结构
2. 移动记录次数较多，算法平均性能比直接插入排序差。不适用于初始记录无序，n较大的时候

### 快速排序

> 快速排序又称划分交换排序由冒泡排序改进而得，在冒泡排序中只对相邻的两个记录进行比较，每次只能消除一个逆序。快速排序方法中一次交换可能消除多个逆序。

算法步骤: 在待排序的n个记录中取任一记录(这里选最后一个)作为枢纽(pivot)，经过一趟排序后把所有关键字小于key的记录交换到前面，大于key的交换到后面形成两个子表，key放到分界处，然后分别对左右子表重复上诉操作，直至每一个字表只有一个记录时，排序完成。

**递归法**
```cpp
void QSort(int arr[],int start,int end)
{
    if (start >= end)
        return;
    int key = arr[end];
    int left = start, right = end - 1;
    while (left < right) { //在整个范围内搜寻比枢纽元值小或大的元素，然后将左侧元素与右侧元素交换
        while (arr[left] < key && left < right) //试图在左侧找到一个比枢纽元更大的元素
            left++;
        while (arr[right] >= key && left < right) //试图在右侧找到一个比枢纽元更小的元素
            right--;
        swap(arr[left], arr[right]); //交换元素
    }   // 每次执行完后left == right
    if (arr[left] >= arr[end])
        swap(arr[left], arr[end]);
    else
        left++;     //++前left可能是指向第一个记录
    QSort(arr, start, left - 1);
    QSort(arr, left + 1, end);
}
```
**迭代法**
```cpp
struct Range {
    int start, end;
    Range(int s = 0, int e = 0) {
        start = s, end = e;
    }
};
void quick_sort(int arr[], const int len) {
    if (len <= 0)
        return; // 避免len等于负值时越界
    // r[]模拟堆栈,p为数量,r[p++]为push,r[--p]为pop且取得元素
    Range r[len];
    int p = 0;
    r[p++] = Range(0, len - 1);
    while (p) {
        Range range = r[--p];
        if (range.start >= range.end)
            continue;
        int mid = arr[range.end];
        int left = range.start, right = range.end - 1;
        while (left < right) {
            while (arr[left] < mid && left < right) left++;
            while (arr[right] >= mid && left < right) right--;
            swap(arr[left], arr[right]);
        }
        if (arr[left] >= arr[range.end])
            swap(arr[left], arr[range.end]);
        else
            left++;
        r[p++] = Range(range.start, left - 1);
        r[p++] = Range(left + 1, range.end);
    }
}
```

时间复杂度:

在平均情况下，排序n个记录需要O(nlogn)次比较，在最坏的情况下则需要次比较O(n<sup>2</sup>)(这种情况很少)。其时间复杂度为O(nlog<sub>2</sub>n)。显然快速排序明显比其他算法要快，因为它的内部循环可以在大部分的架构上也很有效率地达成。

空间复杂度:

快速排序是递归的，执行时需要一个栈来存放相应的数据。最大的递归调用次数与递归树的深度一致，所以最好情况下空间复杂度为O(log<sub>2</sub>n)，最坏情况下为O(n)

算法特点:

1. 记录非顺次移动导致排序方法是不稳定的
2. 排序过程中需要定位标的上下界，所以很难用于链式结构
3. 当n较大时，在平均情况下快速排序是所有内部排序方法中速度最快的一种，所以其适合初始记录无序，n较大的情况。

> 小结: 冒泡排序和快速排序算法都属于交换类排序，通过交换无序序列中的记录从而得到其中关键字最小或最大的记录，并将其加入到有序的子序列当中，以此方法增加记录的有序子序列长度。

### 选择排序

> 选择排序是一种简单直观的排序算法。它的工作原理如下。首先在未排序序列中找到最小（大）suanfa元素，存放到排序序列的起始位置，然后，再从剩余未排序元素中继续寻找最小（大）元素，然后放到已排序序列的末尾。以此类推，直到所有元素均排序完毕。

```cpp
void SelectSort(int arr[],int len)
{
    for(int i=0;i<len;i++){
        int min = i;
        for(int j=i+1;j<len;j++){
            if(arr[j]<arr[min]){
                min = j;
            }
        }
        swap(arr[i],arr[min]);
    }
}
```

复杂度分析:

简单选择排序的时间复杂度是O(n<sup>2</sup>),空间复杂度为O(1)

算法特点:

1. 选择排序本身是一个稳定的排序算法，但其实现的交换策略导致算法的不稳定。
2. 可用于链式存出结构

### 堆排序

> 堆排序是指利用堆这种数据结构所设计的一种排序算法。堆是一个近似完全二叉树的结构，并同时满足堆积的性质：即子节点的键值或索引总是小于（或者大于）它的父节点。

```cpp
void MaxHeap(int arr[],int start,int end){
    int dad = start;
    int son = dad*2 + 1;
    while(son <= end){
        if(son+1<=end && arr[son] < arr[son+1]) //选择子节点中较大的那个
            son++;
        if(arr[dad] > arr[son]) //如果父节点已经大于最大子节点说明dad为父节点的堆有序
            return ;        
        else{
            swap(arr[dad],arr[son]);    //否则交换父子节点，并递归操作子节点
            dad = son;
            son = dad*2+1;
        }
    }
}
void HeadSort(int arr[],int len){
    for(int i=len/2-1;i>=0;i--){    //建初堆，len/2-1最后一个非叶子节点     
   MaxHeap(arr,i,len-1);
    }
    for(int i=len-1;i>0;i--){   //将最大节点移除到当前最后节点后调整堆
        swap(arr[0],arr[i]);
        MaxHeap(arr,0,i-1);
    }
}
```

通常堆是通过一维数组来实现的。在数组起始位置为0的情形中:

+ 父节点i的左子节点在位置 (2i+1);
+ 父节点i的右子节点在位置 (2i+2);
+ 子节点i的父节点在位置 floor((i-1)/2);

相应的数组起始位置为1的情形中:

+ 父节点i的左子节点在位置 (2i);
+ 父节点i的右子节点在位置 (2i+1);
+ 子节点i的父节点在位置 floor(i/2);


算法特点:

1. 是不稳定排序
2. 只能用于顺序结构,不能用于链式结构
3. 初始建堆所需比较次数较多，因此记录数比较少时不宜采用，堆排序最坏的情况下时间复杂度为O(nlog<sub>2</sub>n),相对于快速排序最坏情况下O(n<sup>2</sup>)是一个优点，当记录较多时较为高效
基数排序是通过对待排记录进行若干趟“分配”与“收集”来实现排序的，是一种借助于多关键字排序思想对关键字排序的方法。

> 小结: 选择排序是从无序子序列当中选择最大或最小的记录，并将其加入到有序序列当中

### 归并排序

> 归并排序，是创建在归并操作上的一种有效的排序算法,归并排序算法思想是假设初始序列含有n个记录，则可以看成n个有序的子序列，每个子序列的长度为１，然后两两归并，不断将归并后数据继续归并直到得到一个长度为n的有序序列为止

```cpp
void MergeSortRecursive(int arr[],int reg[],int start,int end){
    if(start >= end)
        return ;
    int mid = (start+end)/2;
    int start1 = start;
    int start2 = mid+1;
    //递归分解
    MergeSortRecursive(arr,reg,start1,mid);
    MergeSortRecursive(arr,reg,start2,end);

    //合并
    int k = start;
    while(start1<=mid && (start2)<=end) //将两个序列中最小值依次存入临时数组
        reg[k++] = (arr[start1] < arr[start2])?arr[start1++]:arr[start2++];

    while(start1<=mid)  //将第一个数组剩余数据依次存入临时数组
        reg[k++] = arr[start1++];
    while(start2<=end)  //同上
        reg[k++] = arr[start2++];
    for(int i=start;i<=end;i++) //将临时数组数据存回排序数组
        arr[i] = reg[i];
}
void MergeSort(int arr[],int len){
    int reg[len];
    MergeSortRecursive(arr,reg,0,len-1);
}
```

复杂度分析:

当有n个记录时，需进行log<sub>2</sub>n趟归并，每一趟归并比较次数不超过n，元素移动次数都是n，因此，归并排序的时间复杂度为O(nlog<sub>2</sub>n)。用顺序表实现归并排序需要和待排序记录个数相等的存储空间，所以空间复杂度为O(n)

算法特点:

1. 稳定排序
2. 可用于链式结构，但递归实现时需开辟相应的递归工作栈

### 基数排序

基数排序是通过对待排记录进行若干趟“分配”与“收集”来实现排序的，是一种借助于多关键字排序思想对关键字排序的方法。
```cpp
int maxbit(int data[], int n) //辅助函数，求数据的最大位数
{
    int maxData = data[0];		///< 最大数
    // 先求出最大数，再求其位数，这样有原先依次每个数判断其位数，稍微优化点。
    for (int i = 1; i < n; ++i)
    {
        if (maxData < data[i])
            maxData = data[i];
    }
    int d = 1;
    int p = 10;
    while (maxData >= p)
    {选择排序特点:

1. 选择排序本身是一个稳定的排序算法，但其实现的交换策略导致算法的不稳定。
2. 可用于链式存出结构
        maxData /= 10;
        ++d;
    }
    return d;
}
void radixsort(int data[], int n) //基数排序
{
    int d = maxbit(data, n);
    int *tmp = new int[n];
    int *count = new int[10]; //计数器,数值范围0-9
    int i, j, k;
    int radix = 1;
    for(i = 1; i <= d; i++) //进行d次排序
    {
        for(j = 0; j < 10; j++)
            count[j] = 0; //每次分配前清空计数器
        for(j = 0; j < n; j++)
        {
            k = (data[j] / radix) % 10; //统计每个桶中的记录数
            count[k]++;
        }
        for(j = 1; j < 10; j++)
            count[j] = count[j - 1] + count[j]; //将tmp中的位置依次分配给每个桶,通过count将tmp划分为10个桶
        for(j = n - 1; j >= 0; j--) //将所有桶中记录依次收集到tmp中
        {
            k = (data[j] / radix) % 10;
            tmp[count[k] - 1] = data[j];
            count[k]--;
        }
        for(j = 0; j < n; j++) //将临时数组的内容复制到data中
            data[j] = tmp[j];
        radix = radix * 10;
    }
    delete []tmp;
    delete []count;
}
```

以下是对于使用链式数据结构进行基数排序的算法分析(上面排序则使用数组)

时间复杂度分析:  
对于n个记录，含d个关键字，每个关键字取值范围是rd个值。进行链式基数排序时，每一趟分配的时间复杂度为O(n)，每一趟收集的时间复杂度为O(rd),整个排序需要d次分配和收集，所以时间复杂度为O(d(n+rd))。

空间复杂度分析:  
所需辅助空间为2rd个队列指针，另外由于需要用链表做存出结构，则相对于其他顺序结构存储记录的排序方法而言，还增加了n个指针域的空间，所以空间复杂度为O(n+rd)。

算法特点:

1. 是稳定排序
2. 可用于链式结构，也可用于顺序结构
3. 时间复杂度可以突破基于关键字比较一类方法的下界O(nlogn),达到O(n)
4. 基数排序使用条件有严格要求：需要知道各级关键字的主次关系和各级关键字的取值范围


[参考] 严蔚敏《数据结构》、《维基百科》