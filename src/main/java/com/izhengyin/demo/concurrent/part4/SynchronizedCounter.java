package com.izhengyin.demo.concurrent.part4;

import com.izhengyin.demo.concurrent.SleepUtils;
import com.izhengyin.demo.concurrent.part3.WrongUseVolatile;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Create on 2020/8/30 6:42 下午
 */
public class SynchronizedCounter {
    private static int counter = 0;
    public static void main(String[] args){
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        long sTime = System.currentTimeMillis();
        int loop = 1000000;
        IntStream.rangeClosed(1,loop)
                .forEach(i -> {
                    executorService.execute(() -> {
                        SynchronizedCounter.inc();
                        if(i == loop){
                            System.out.println("耗时 "+(System.currentTimeMillis()-sTime)+" ms");
                        }
                    });

                });
        SleepUtils.sleep(1000);
        //结果并不一定等于 10000
        System.out.println("counter -> "+counter);
        executorService.shutdown();
    }
    private synchronized static void inc(){
        counter ++;
    }
}
