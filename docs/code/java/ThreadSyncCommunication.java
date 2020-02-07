/*
线程之间通信：生产者消费者模型
wait(),notify()和notifyAll()
在每个Java对象中都有两个池：锁池，等待池。锁池存放因竞争同步对象的锁而等待的线程。
等待池存放因wait()方法而释放掉对象锁的线程。当该对象被其他线程调用执行notifyAll()
，等待池中的所有线程就会进入锁池中，准备争夺锁的拥有权。如果执行的是notify()方法那么仅有一个线程会进入锁池。
 */

import java.util.Vector;

class SyncStack{    //同步堆栈类
    private Vector<Character> vector = new Vector<>();  //存放共享数据

    public synchronized void push(char c){      //入栈
        vector.addElement(c);   //数据进栈
        this.notify();          //唤醒等待进程
    }
    public synchronized char pop(){     //出栈
        while(vector.isEmpty()){    //堆栈无数据
            try {
                this.wait();        //线程等待
            }catch (InterruptedException e){};
        }
        char c = vector.remove(vector.size()-1);    //数据出栈
        return c;
    }
}

class Producer extends Thread{     //生产者类
    SyncStack theStack;
    public Producer(SyncStack s){
        theStack = s;
    }
    public void run(){
        char c;
        for(int i=0;i<20;i++){
            c = (char)(Math.random()*26+'A');   //随机产生20个字符
            theStack.push(c);                   //把字符入栈
            System.out.println(Thread.currentThread().getName()+":"+c);
            try{
                Thread.sleep(1000);       //线程睡眠1秒
            }catch (InterruptedException e){};
        }
    }
}

class Consumer extends Thread{     //消费者类
    SyncStack theStack;
    public Consumer(SyncStack s){
        theStack = s;
    }
    public void run(){
        char c;
        for(int i=0;i<20;i++){
            c = theStack.pop();         //从堆栈中读取字符
            System.out.println(Thread.currentThread().getName()+":"+c);
        }
        try {
            Thread.sleep(1000);     //线程睡眠1秒
        }catch (InterruptedException e){};
    }
}
public class ThreadSyncCommunication {
    public static void main(String[] argv){
        SyncStack stack = new SyncStack();
        Producer p = new Producer(stack);
        Consumer c = new Consumer(stack);
        p.setName("Thread Producer");
        c.setName("Thread Consumer");
        p.start();
        c.start();
    }
}
