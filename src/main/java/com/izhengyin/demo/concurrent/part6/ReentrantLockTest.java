package com.izhengyin.demo.concurrent.part6;

import com.izhengyin.demo.concurrent.SleepUtils;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-14 17:15
 */
public class ReentrantLockTest {

    public static void main(String[] args){
        System.out.println(">>> >>> >>> >>> >>> >>> >>> >>> reentrant");
        reentrant();
        System.out.println(">>> >>> >>> >>> >>> >>> >>> >>> nonFair");
        nonFair();
        SleepUtils.sleep(1);
        System.out.println(">>> >>> >>> >>> >>> >>> >>> >>> fair");
        fair();
    }

    /**
     * 当占有锁的线程获取到的锁都释放以后，其它等待线程才可以获取锁。
     */
    public static void reentrant(){

        ReentrantLock lock = new ReentrantLock();
        IntStream.rangeClosed(0,10).forEach(i -> {
            lock.lock();
            System.out.println(System.currentTimeMillis()+" Get Lock "+Thread.currentThread().getName());
        });

        Thread thread = new Thread(() -> {
            lock.lock();
            try {
                System.out.println(System.currentTimeMillis()+" Get Lock "+Thread.currentThread().getName());
            }finally {
                lock.unlock();
                System.out.println(System.currentTimeMillis()+" unLock "+Thread.currentThread().getName());
            }
        });
        thread.start();



        IntStream.rangeClosed(0,10).forEach(i -> {
            SleepUtils.sleep(100);
            lock.unlock();
            System.out.println(System.currentTimeMillis()+" unLock "+Thread.currentThread().getName());
        });

        try {
            thread.join();
        }catch (InterruptedException e){

        }
    }



    /**
     * 乱序执行
     */
    private static void nonFair(){
        ReentrantLock nonFairLock = new ReentrantLock();
        IntStream.rangeClosed(0,10).forEach(i -> {
                    new Thread(() -> {
                        nonFairLock.lock();
                        try {
                            System.out.println(Thread.currentThread().getName()+" -> "+i);
                        }finally {
                            nonFairLock.unlock();
                        }
                    },"thread-"+i).start();
                });


    }

    /**
     * 有序执行
     */
    private static void fair(){
        ReentrantLock failLock = new ReentrantLock(true);
        IntStream.rangeClosed(0,10).forEach(i -> {
            new Thread(() -> {
                failLock.lock();
                try {
                    System.out.println(Thread.currentThread().getName()+" -> "+i);
                }finally {
                    failLock.unlock();
                }
            },"thread-"+i).start();
        });

    }

}
