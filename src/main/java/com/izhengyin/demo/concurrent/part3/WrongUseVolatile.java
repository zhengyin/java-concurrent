package com.izhengyin.demo.concurrent.part3;

import com.izhengyin.demo.concurrent.SleepUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Create on 2020/8/30 2:44 下午
 */
public class WrongUseVolatile {
    private static volatile int counter = 0;
    public static void main(String[] args){
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        IntStream.rangeClosed(1,10000)
                .forEach(i -> executorService.execute(WrongUseVolatile::inc));
        SleepUtils.sleep(1000);
        //结果并不一定等于 10000
        System.out.println("counter -> "+counter);
        executorService.shutdown();
    }
    private static void inc(){
        counter ++;
    }
}
