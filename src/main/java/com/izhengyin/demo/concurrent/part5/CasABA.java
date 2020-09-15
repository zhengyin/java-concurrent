package com.izhengyin.demo.concurrent.part5;

import com.izhengyin.demo.concurrent.SleepUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-07 13:42
 */
public class CasABA {
    public static void main(String[] args) throws InterruptedException{

        System.out.println("abaProblem ");
        abaProblem();
        System.out.println("fixAbaProblem ");
        fixAbaProblem();
        System.out.println("fixAbaProblem2 ");
        fixAbaProblem2();

    }

    /**
     * 产生ABA问题的代码
     * @throws InterruptedException
     */
    private static void abaProblem() throws InterruptedException{
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final AtomicInteger atomicInteger = new AtomicInteger(1);
        executorService.execute(() -> {
            System.out.println(atomicInteger.compareAndSet(1,2)+" -> "+atomicInteger.get());
            System.out.println(atomicInteger.compareAndSet(2,1)+" -> "+atomicInteger.get());
            countDownLatch.countDown();
        });
        executorService.execute(() -> {
            SleepUtils.sleep(100);
            System.out.println(atomicInteger.compareAndSet(1,3)+" -> "+atomicInteger.get());
            countDownLatch.countDown();
        });
        countDownLatch.await();
        executorService.shutdown();
    }

    /**
     * 通过增加数据版本来避免ABA问题
     * @throws InterruptedException
     */
    private static void fixAbaProblem() throws InterruptedException{

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch countDownLatch = new CountDownLatch(2);
        final AtomicStampedReference<Integer> atomicInteger = new AtomicStampedReference<Integer>(1,0);
        final int initStamp = atomicInteger.getStamp();
        executorService.execute(() -> {
            int stamp = initStamp;
            System.out.println(atomicInteger.compareAndSet(1,2,stamp,++stamp) +" -> "+atomicInteger.getReference());
            System.out.println(atomicInteger.compareAndSet(2,1,stamp,++stamp) +" -> "+atomicInteger.getReference());
            countDownLatch.countDown();
        });
        executorService.execute(() -> {
            int stamp = initStamp;
            SleepUtils.sleep(100);
            System.out.println(atomicInteger.compareAndSet(1,3,stamp,++stamp) +" -> "+atomicInteger.getReference());
            countDownLatch.countDown();
        });
        countDownLatch.await();
        executorService.shutdown();
    }


    /**
     * 通过增加数据版本来避免ABA问题
     * @throws InterruptedException
     */
    private static void fixAbaProblem2() throws InterruptedException{

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch countDownLatch = new CountDownLatch(2);

        final AtomicMarkableReference<Integer> atomicInteger = new AtomicMarkableReference<Integer>(1,false);
        executorService.execute(() -> {
            System.out.println(atomicInteger.compareAndSet(1,2,false,true) +" -> "+atomicInteger.getReference());
            System.out.println(atomicInteger.compareAndSet(2,3,true,false) +" -> "+atomicInteger.getReference());
            System.out.println(atomicInteger.compareAndSet(3,1,false,true) +" -> "+atomicInteger.getReference());
            countDownLatch.countDown();
        });
        executorService.execute(() -> {
            SleepUtils.sleep(100);
            System.out.println(atomicInteger.compareAndSet(1,3,false,true) +" -> "+atomicInteger.getReference());
            countDownLatch.countDown();
        });
        countDownLatch.await();
        executorService.shutdown();
    }




}
