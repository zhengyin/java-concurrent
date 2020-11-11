package com.izhengyin.demo.concurrent.basic.thread;
import com.izhengyin.demo.concurrent.Sleep;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-11-08 15:22
 */
public class WaitNotify {
    public static void main(String[] args) {
        new Thread(new Wait(), "waitThread1").start();
        new Thread(new Wait(), "waitThread2").start();
        new Thread(new Wait(), "waitThread3").start();
        new Thread(new Wait(), "waitThread4").start();
        new Thread(new Wait(), "waitThread5").start();
        //唤醒一个
        Sleep.second(3);
        synchronized (Wait.class){
            Wait.class.notify();
        }
        //唤醒一个
        Sleep.second(3);
        synchronized (Wait.class){
            Wait.class.notify();
        }
        Sleep.second(3);
        //唤所有
        synchronized (Wait.class){
            Wait.class.notifyAll();
        }
    }

    static class Wait implements Runnable{
        @Override
        public void run() {
            System.out.println(System.currentTimeMillis()+" , run "+Thread.currentThread().getName());
            synchronized (Wait.class){
                try {
                    Wait.class.wait();
                    System.out.println(System.currentTimeMillis()+" , notified "+Thread.currentThread().getName());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
