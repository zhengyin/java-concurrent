package com.izhengyin.demo.concurrent.part6;

import com.izhengyin.demo.concurrent.SleepUtils;
import org.springframework.util.Assert;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-18 08:17
 */
public class ReadWriteLockTest {
    public static void main(String[] args){
    //    readLock();
       writeLock();

    }

    /**
     * 读锁可以被多个线程获取,但当写锁被占有时，读锁的获取都将被阻塞。
     */
    private static void readLock(){
        ReentrantReadWriteLock wrl = new ReentrantReadWriteLock();
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        IntStream.rangeClosed(1,10)
                .forEach(i -> executorService.execute(() -> {
                    wrl.readLock().lock();
                    try {
                        System.out.println(System.currentTimeMillis()+" "+Thread.currentThread().getName()+" Get Read Lock");
                        //mock delay
                        SleepUtils.sleep(100);
                    }finally {
                        wrl.readLock().unlock();

                        if(i  == 5){
                            wrl.writeLock().lock();
                            try {
                                System.out.println(System.currentTimeMillis()+" "+Thread.currentThread().getName()+" Get Write Lock");
                                SleepUtils.sleep(3000);
                            }finally {
                                wrl.writeLock().unlock();
                            }
                        }

                    }
                }));
        executorService.shutdown();
    }

    /**
     * 写锁的获取是独占的,可重入的。除此之外，如果当前线程已获得读锁，写锁的获取同样也会被阻塞。
     */
    private static void writeLock(){
        ReentrantReadWriteLock wrl = new ReentrantReadWriteLock();
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        IntStream.rangeClosed(1,10)
                .forEach(i -> executorService.execute(() -> {
                    wrl.writeLock().lock();
                    try {
                        System.out.println(System.currentTimeMillis()+" "+Thread.currentThread().getName()+" Get Write Lock");
                        //mock delay
                        SleepUtils.sleep(100);
                    }finally {
                        wrl.writeLock().unlock();
                    }
                }));
        executorService.shutdown();

        SleepUtils.sleep(1000);


        wrl.readLock().lock();
        try {
            System.out.println("Main Get Read Lock");
        }finally {
            //注释以后主线程将被阻塞，因为无法获取写锁
            wrl.readLock().unlock();
        }

        wrl.writeLock().lock();
        try {
            System.out.println("Main Get Write Lock");
        }finally {
            wrl.writeLock().unlock();
        }

    }


    /**
     * cache
     * 通过对读写使用不同的所策略，兼顾读的并发性，也兼顾写操作对应读操作的可见性。
     */
    private final static class ConcurrentCache {
        private final Map<String,Object> map = new HashMap<>();
        private final ReentrantReadWriteLock wrl = new ReentrantReadWriteLock();
        private final ReentrantReadWriteLock.ReadLock readLock;
        private final ReentrantReadWriteLock.WriteLock writeLock;

        public ConcurrentCache(){
            this.readLock = wrl.readLock();
            this.writeLock = wrl.writeLock();
        }

        /**
         * 读的时候加读锁，多线程读不阻塞
         * @param key
         * @return
         */
        public Object get(String key){
            readLock.lock();
            try {
                return map.get(key);
            }finally {
                readLock.unlock();
            }
        }

        /**
         * 写的时候加写锁，写锁同时阻塞读锁，这样保证写完后的最新数据会第一时间被读到
         * @param key
         * @param supplier
         * @return
         */
        public Object set(String key , Supplier<Object> supplier){
            writeLock.lock();
            try {
                return map.put(key, supplier.get());
            }finally {
                writeLock.unlock();
            }
        }

        /**
         * 同 set
         */
        public void  clear(){
            writeLock.lock();
            try {
                map.clear();
            }finally {
                writeLock.unlock();
            }

        }
    }



}