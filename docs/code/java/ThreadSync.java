/* 线程同步
    在Java中临界区使用关键字synchronized修饰的一个语句块或一个方法
    临界区的控制通过对象锁实现，Java中每个对象都内置一个对象锁，对象锁是一种独占的互斥锁，也叫同步锁
 */
class Tickets{
    private int saledticket = 0;
    private int totalticket = 10;
    public synchronized boolean sale(){  //临界区
        if(saledticket < totalticket){
            saledticket += 1;              //更改票数
            System.out.println(Thread.currentThread().getName()+":卖出第"+saledticket+"张票");
            return true;
        }else
            return false;
    }
}

class SaleTicket extends Thread{  //售票线程
    private Tickets ticket;         //同步对象，即共享对象
    public SaleTicket(String name, Tickets ticket){
        super(name);    //调用父类方法给线程命名
        this.ticket = ticket;
    }
    public void run(){      //线程体
        while (ticket.sale()){
            try {
                Thread.sleep(1000); //线程休眠1秒
            }catch (InterruptedException e){};
        }
    }
}
public class ThreadSync {
    public static void main(String[] args){
        Tickets t = new Tickets();  //创建同步对象
        // 三个线程访问同一对象
        SaleTicket a1 = new SaleTicket("窗口1",t);
        SaleTicket a2 = new SaleTicket("窗口2",t);
        SaleTicket a3 = new SaleTicket("窗口3",t);
        a1.start();
        a2.start();
        a3.start();
        System.out.println("Main thread is over!");
    }
}
