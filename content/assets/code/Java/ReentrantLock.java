/*
    对象锁可重入性：一个线程获得某对象锁的情况下，可以再次请求并获得该对象的锁
 */
class Test{  //共享对象类
    public synchronized void a(){  //同步方法
        b();
        System.out.println("Here is method a");
    }
    public synchronized void b(){  //同步方法
        System.out.println("Here is method b");
    }
}

class MyThread implements Runnable{ //操作共享对象的线程
    private Test test;              //共享对象
    public MyThread(Test test){
        this.test = test;
    }
    public void run(){  //线程体
        test.a();       //执行同步方法
    }
}
public class ReentrantLock {
    public static void main(String[] args){
        Test t = new Test();
        MyThread myThread = new MyThread(t);
        Thread thread = new Thread(myThread);
        thread.start();
    }
}
