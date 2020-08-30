package com.izhengyin.demo.concurrent.part2;

import com.izhengyin.demo.concurrent.SleepUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @author zhengyin
 */
public class FixMultiThreadIssue {
    private static volatile int COUNTER = 0;
    private static final int MAX_VALUE = 10;
    public static void main(String[] args){
    //    changeAndWatch();
        changeAndWatch2();
    //    reorder();
    }

    /**
     * 将变量设置为 volatile 能够正常检查到值的变化
     */
    private static void changeAndWatch(){
        CountDownLatch countDownLatch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(3);
        // change
        executor.execute(() -> {
            IntStream.rangeClosed(0,MAX_VALUE)
                    .forEach(i -> {
                        COUNTER = i;
                        SleepUtils.sleep(100);
                    });
            countDownLatch.countDown();
        });

        // watch 1
        executor.execute(() -> {
            int threadValue = COUNTER;
            while (COUNTER < MAX_VALUE){
                if(threadValue != COUNTER){
                    System.out.println(Thread.currentThread().getName()+" counter change old "+threadValue+" , new "+COUNTER);
                    threadValue = COUNTER;
                }
            }
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await(5000, TimeUnit.MILLISECONDS);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        executor.shutdown();
    }

    /**
     * 通过 synchronized watch1 也可以正常检查到值的变化
     */
    private static void changeAndWatch2(){
        CountDownLatch countDownLatch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        // change
        executor.execute(() -> {
            IntStream.rangeClosed(0,MAX_VALUE)
                    .forEach(i -> {
                            COUNTER = i;
                        SleepUtils.sleep(100);
                    });
            countDownLatch.countDown();
        });

        // watch 1
        executor.execute(() -> {
            int threadValue = COUNTER;
            while (COUNTER < MAX_VALUE){
                synchronized (FixMultiThreadIssue.class){
                    if(threadValue != COUNTER){
                        System.out.println(Thread.currentThread().getName()+" counter change old "+threadValue+" , new "+COUNTER);
                        threadValue = COUNTER;
                    }
                }
            }
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await(5000, TimeUnit.MILLISECONDS);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        executor.shutdown();
    }

    /**
     * 通过修改 flag 为 volatile 程序不会在输出 get wrong value 的错误
     */
    private static void reorder(){
        ExecutorService executor = Executors.newFixedThreadPool(2);
        // 未加锁
        IntStream.rangeClosed(0,1000000)
                .forEach(i -> {
                    ReorderExample example = new ReorderExample();
                    executor.execute(example::write);
                    executor.execute(example::read);
                });
        executor.shutdown();
    }

    /**
     * 从排序测试
     */
    private static class ReorderExample {
        private int a = 0;
        private volatile boolean flag = false;
        public void write(){
            a = 1;
            flag = true;
        }
        public void read(){
            if(flag){
                if(a != 1){
                    System.out.println("get wrong value , a != 1 ");
                }
            }
        }
    }
}